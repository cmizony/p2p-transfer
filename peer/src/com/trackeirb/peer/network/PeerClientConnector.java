package com.trackeirb.peer.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.trackeirb.peer.domain.BufferMap;
import com.trackeirb.peer.domain.PeerFile;
import com.trackeirb.peer.files.LocalStorage;
import com.trackeirb.peer.protocol.ActionExecutor;
import com.trackeirb.peer.ui.TrackeirbUI;

/**
 * This class represents a connector (in client mode) used to connect to the
 * tracker, or to another peer
 * 
 * @author Elian ORIOU
 * 
 */

public class PeerClientConnector extends Thread {

	public enum RemoteType {
		PEER, TRACKER
	};

	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;

	private String host;
	private int port;

	private String currentFileKey;

	private static final LocalStorage _STORAGE = LocalStorage.getInstance();

	public PeerClientConnector(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void initConnection() {
		try {
			socket = new Socket(host, port);
			writer = new BufferedWriter(new PrintWriter(
					socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeConnection() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Thread method, you must call startToPeer() instead
	 */

	@Override
	public void run() {
		if (currentFileKey == null) {
			return;
		}
		try {
			// /////////////////////////////////////////////////////////////////
			// / CONNECTION TO ANOTHER PEER (FOR FILE DOWNLOAD) IN CLIENT MODE
			// / THE CONNECTION TO THE TRACKER IS NOT THREADED (except for
			// / update messages)
			// /////////////////////////////////////////////////////////////////

			System.out.println("Connected to : " + host + ":" + port);

			// Display file progress
			TrackeirbUI.getInstance().addFileProgressToView(
					_STORAGE.getFile(currentFileKey));

			// Initialize connection
			initConnection();

			PeerFile receivedBufferMapFile = ActionExecutor.getInstance()
					.getFileBufferMap(this, currentFileKey);
			// 1. If the remote file (in the seeder's local storage) doesn't
			// exists : Error !
			if (receivedBufferMapFile == null) {
				return;
			}
			PeerFile localFile = _STORAGE.getFile(receivedBufferMapFile
					.getKey());

			// 2. If the mirror file (in the leecher's local storage)
			// doesn't exists : Error !
			if (localFile == null) {
				return;
			}

			// 3. Local and remote buffer maps
			BufferMap receivedBufferMap = receivedBufferMapFile.getBufferMap();
			BufferMap localBufferMap = localFile.getBufferMap();

			// 4. Compare the received buffer map to the stored buffer map,
			// and if the remote peer had pieces that I don't have : request
			// the peer to download specific piece(s)
			List<Integer> requestedPieces = new ArrayList<Integer>();
			for (int i = 0; i < receivedBufferMap.length(); i++) {
				if (localBufferMap.get(i) == false
						&& receivedBufferMap.get(i) == true) {
					// Request piece (by its ID)
					requestedPieces.add(i);
				}
			}

			// 5. If all pieces are present on local storage : stop
			// connection
			if (requestedPieces.isEmpty()) {
				return;
			}

			// 6. Launch download
			Integer[] p = requestedPieces.toArray(new Integer[] {});
			PeerFile receivedData = ActionExecutor.getInstance().getFileData(
					this, currentFileKey, p);

			// 7. Add received pieces to the local file (thread-safe mode)
			synchronized (localFile.getPieces()) {
				localFile.getPieces().addAll(receivedData.getPieces());
			}

			// 8. Set the new file buffer map (it applies a OR logical
			// operation) on the local file buffer map
			localBufferMap.or(receivedBufferMap);

			// 9. Refresh view
			TrackeirbUI.getInstance().refreshFileProgress(localFile,
					localFile.getPieces().size());

			// 10. Save data on disk if the client has all pieces of the
			// download file
			if (localBufferMap.cardinality() == localBufferMap.length()) {
				_STORAGE.savePeerFileToDisk(localFile);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(new JFrame(),
					"An error occured during the download : " + e.getMessage(),
					"Error !", JOptionPane.ERROR_MESSAGE);
			TrackeirbUI.getInstance().restart();
		}

		// 10. Close connection to the peer
		closeConnection();

		// 11. Delete the connector and refresh view
		PeerClientsBundle.getInstance().getConnectors().remove(this);
		TrackeirbUI.getInstance().refreshPeersList();
	}

	public void start(String fileKey) {
		currentFileKey = fileKey;
		start();
	}

	public String read() {
		try {
			String response = reader.readLine();
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void write(String message) {
		try {
			writer.write(message);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
