package com.trackeirb.peer.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.trackeirb.peer.ConfigurationManager;
import com.trackeirb.peer.domain.BufferMap;
import com.trackeirb.peer.domain.Constants;
import com.trackeirb.peer.domain.FilePiece;
import com.trackeirb.peer.domain.PeerFile;
import com.trackeirb.peer.ui.TrackeirbUI;

/**
 * Manages seeded and leeched files
 * 
 * @author Elian ORIOU
 * 
 */

public class LocalStorage {

	private static final int _PIECE_SIZE = ConfigurationManager.getInstance()
			.getPropertyAsInt(Constants._PIECE_SIZE_CONF_KEY, 2048);

	private static final String _ROOT_FOLDER_PATH = ConfigurationManager
			.getInstance().getProperty(Constants._LOCAL_STORAGE_CONF_KEY,
					"downloads");

	private static LocalStorage _INSTANCE;
	private File rootFolder;
	private Map<String, PeerFile> files;

	private LocalStorage() {
		rootFolder = new File(_ROOT_FOLDER_PATH);
		if (rootFolder.isDirectory() == false) {
			throw new IllegalArgumentException();
		}
		// The synchronized (thread-safe) container
		files = Collections.synchronizedMap(new HashMap<String, PeerFile>());
		// Create the seeded files list (by browsing the local storage
		// directory (recursively ?))
		refreshSeededFilesList();
	}

	public static LocalStorage getInstance() {
		if (_INSTANCE == null) {
			_INSTANCE = new LocalStorage();
		}
		return _INSTANCE;
	}

	public void addLeechedFile(PeerFile file) {
		if (file == null) {
			return;
		}
		try {
			PeerFile newFile = file.clone();
			newFile.setBufferMap(new BufferMap(file.getBufferMap().realLength()));
			// Add the new file to the local storage
			synchronized (files) {
				files.put(newFile.getKey(), newFile);
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	public PeerFile getFile(String key) {
		return files.get(key);
	}

	public List<PeerFile> getFilesList() {
		return new ArrayList<PeerFile>(getFilesMap().values());
	}

	public Map<String, PeerFile> getFilesMap() {
		if (files.isEmpty()) {
			refreshSeededFilesList();
		}
		return files;
	}

	private File createLeechedFile(PeerFile file) {

		if (file.getName() == null || file.getName().isEmpty()) {
			throw new IllegalArgumentException();
		}

		// Creates the new file on the disk
		File newFile = new File(_ROOT_FOLDER_PATH + File.separator
				+ file.getName());
		try {
			newFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Associates the physical file to the peer file
		file.setPhysicalFile(newFile);

		return newFile;
	}

	public void refreshSeededFilesList() {

		for (File file : rootFolder.listFiles()) {

			if (!file.isFile()) {
				continue;
			}

			String key = FileTools.getInstance().getMD5FileDigest(file);
			PeerFile peerFile = new PeerFile(key, _PIECE_SIZE, null, file);
			// Load file content by block (piece)
			try {
				InputStream reader = new FileInputStream(file);
				int i = 0;
				int len = 0;
				byte[] buffer = new byte[_PIECE_SIZE];
				do {
					// Reads the file by block of _PIECE_SIZE
					len = reader.read(buffer, 0, _PIECE_SIZE);
					if (len > 0) {
						// Get only valid data (read data). It avoid invalid
						// zero byte list at the end of the stream for instance.
						ByteBuffer buff = ByteBuffer.allocate(len);
						buff.put(buffer, 0, len);

						// Simply create an ordered piece and add it into the
						// peer file
						peerFile.addPiece(new FilePiece(i++, len, buff.array()));
					}

					// Clear the buffer
					buffer = new byte[_PIECE_SIZE];

				} while (len != -1);

				// Close the stream
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Put the file into the map
			synchronized (files) {
				files.put(peerFile.getKey(), peerFile);
			}
		}
	}

	public void savePeerFileToDisk(PeerFile file) throws Exception {

		if (file == null) {
			return;
		}
		File physFile = file.getPhysicalFile();
		if (physFile == null) {
			physFile = createLeechedFile(file);
		}

		// 1. Sort pieces into the list
		List<FilePiece> pieces = file.getPieces();
		Collections.sort(pieces);

		// 2. Write data into the file
		try {
			FileOutputStream stream = new FileOutputStream(physFile);
			for (FilePiece piece : pieces) {
				if (piece == null || piece.getData() == null) {
					continue;
				}
				stream.write(piece.getData());
			}
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 3. Check if the file is not corrupted by re-computing its MD5 hash
		String newKey = FileTools.getInstance().getMD5FileDigest(physFile);
		if (newKey.equals(file.getKey())) {
			System.out.println("VALID FILE !");
		} else {
			int n = JOptionPane.showConfirmDialog(new JFrame(), "The file \""
					+ file.getName()
					+ "\" is corrupted ! Would you want to keep it ? ",
					"Warning !", JOptionPane.YES_NO_OPTION);
			// 3.1 If the file is corrupted, lets the user choose the
			// appropriate option (Keep file or delete it)
			if (JOptionPane.YES_OPTION != n) {
				synchronized (files) {
					files.remove(file.getKey());
				}
				physFile.delete();
				TrackeirbUI.getInstance().removeFileProgressFromView(file);
				return;
			}
		}
	}
}
