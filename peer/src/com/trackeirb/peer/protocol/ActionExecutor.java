package com.trackeirb.peer.protocol;

import java.util.List;

import com.trackeirb.peer.ConfigurationManager;
import com.trackeirb.peer.domain.Constants;
import com.trackeirb.peer.domain.Peer;
import com.trackeirb.peer.domain.PeerFile;
import com.trackeirb.peer.exceptions.InvalidResponseException;
import com.trackeirb.peer.files.LocalStorage;
import com.trackeirb.peer.network.PeerClientConnector;
import com.trackeirb.peer.network.PeerClientsBundle;
import com.trackeirb.peer.ui.TrackeirbUI;

/**
 * Executes actions and handle response in CLIENT mode ONLY. So, the execution
 * is like :
 * 
 * 1) User Action (from UI) => 2) ActionExecutor => 3) FACTORY to get
 * appropriate message => 4) send message to remote host => 5) receive response
 * from remote host => 6) HANDLER to handle received message => Some data
 * treatments => 8) Feedback to system or user (to UI)
 * 
 * @author Elian ORIOU
 * 
 */

public class ActionExecutor {

	// Output factory
	private static OutputMessagesFactory _FACTORY = OutputMessagesFactory
			.getInstance();

	// Input handler
	private static InputMessageHandler _HANDLER = InputMessageHandler
			.getInstance();

	// The local storage
	private static LocalStorage _STORAGE = LocalStorage.getInstance();

	// Singleton pattern
	private static ActionExecutor _INSTANCE;

	public static ActionExecutor getInstance() {
		if (_INSTANCE == null) {
			_INSTANCE = new ActionExecutor();
		}
		return _INSTANCE;
	}

	public void announceToTracker(PeerClientConnector connector,
			List<PeerFile> seededFiles, List<PeerFile> leechedFiles)
			throws InvalidResponseException {

		// Connect to the tracker
		connector.initConnection();

		// Send announcement
		int peerPort = ConfigurationManager.getInstance().getPropertyAsInt(
				Constants._PEER_PORT_CONF_KEY, 0);

		connector.write(_FACTORY.getAnnouncementMessage(peerPort, seededFiles,
				leechedFiles));

		// Read the response
		String response = connector.read();

		// Handle the response
		if (response == null
				|| response.startsWith(InputMessagesPatternsBundle._OK_CST) == false) {
			throw new InvalidResponseException(response);
		}

		// Close connection with the tracker
		connector.closeConnection();
	}

	public void updateToTracker(PeerClientConnector connector,
			List<PeerFile> seededFiles, List<PeerFile> leechedFiles)
			throws InvalidResponseException {

		// Connect to the tracker
		connector.initConnection();

		// Send announcement
		connector.write(_FACTORY.getUpdateMessage(seededFiles, leechedFiles));

		// Read the response
		String response = connector.read();

		// Handle the response
		if (response == null
				|| response.startsWith(InputMessagesPatternsBundle._OK_CST) == false) {
			throw new InvalidResponseException(response);
		}

		// Close connection with the tracker
		connector.closeConnection();
	}

	public List<PeerFile> getFileList(PeerClientConnector connector,
			String criterions) throws InvalidResponseException {

		// 0. Connect to the tracker
		connector.initConnection();

		// 1. Send file list request
		connector.write(_FACTORY.getFileListMessage(criterions));

		// 2. Read the response
		String response = connector.read();

		// 3. Handle the file list response
		if (response == null || response.startsWith("list") == false) {
			throw new InvalidResponseException(response);
		}
		List<PeerFile> files = _HANDLER.getRequestedTrackerFileList(response);

		// 4. Disconnect from the tracker
		connector.closeConnection();

		return files;
	}

	public void dispatchByPeer(PeerClientConnector connector,
			List<PeerFile> files) throws InvalidResponseException {

		if (files == null || files.isEmpty()) {
			return;
		}

		// 2. For each chose file retrieves peer list
		for (PeerFile file : files) {

			// 2.1 Reconnect to the tracker
			connector.initConnection();

			// 2.2 Send peer list request
			connector.write(_FACTORY.getPeerListMessage(file.getKey()));

			// 2.3 Read the response
			String response = connector.read();

			// 2.4 Close connection with the tracker
			connector.closeConnection();

			// 2.5 Handle the peer list response
			if (response == null || response.startsWith("peers") == false) {
				throw new InvalidResponseException(response);
			}

			// 2.6 Retrieves concerned peers
			List<Peer> peers = _HANDLER.getRequestedPeerList(response);

			// 2.7 Try to connect to each peers in order to obtain their file
			// buffer map.
			for (Peer peer : peers) {

				// Create a new client
				PeerClientConnector newClient = new PeerClientConnector(
						peer.getAddress(), peer.getPort());
				newClient.initConnection();
				PeerClientsBundle.getInstance().addPeerClient(newClient, peer);

				// Refresh peers list on UI
				TrackeirbUI.getInstance().refreshPeersList();

				// Add the file to the local storage
				if (_STORAGE.getFile(file.getKey()) == null) {
					_STORAGE.addLeechedFile(file);
				}
				// Start downloading pieces for the specific file
				newClient.start(file.getKey());
			}
		}
	}

	public PeerFile getFileBufferMap(PeerClientConnector connector,
			String fileKey) throws InvalidResponseException {

		// Send buffer map request
		String message = _FACTORY.getFileBufferMapMessage(fileKey);
		connector.write(message);

		// Read the response
		String response = connector.read();

		// Handle the response
		if (response == null || response.startsWith("have") == false) {
			throw new InvalidResponseException(response);
		}

		return _HANDLER.getRequestedFileBufferMap(response);
	}

	public PeerFile getFileData(PeerClientConnector connector, String fileKey,
			Integer... pieces) throws InvalidResponseException {

		// Send data request
		connector.write(_FACTORY.getPiecesMessage(fileKey, pieces));

		// Read the response
		String response = connector.read();

		// Handle the response
		if (response == null || response.startsWith("data") == false) {
			throw new InvalidResponseException(response);
		}

		// Build resulted peer file (read more than once if the message is
		// split)
		PeerFile receivedFile = _HANDLER.getRequestedPiecesData(response);
		while (receivedFile.getPieces().size() < pieces.length) {
			response = connector.read();
			receivedFile.getPieces().addAll(
					_HANDLER.getRequestedPiecesData(response).getPieces());
			TrackeirbUI.getInstance().refreshFileProgress(receivedFile,
					receivedFile.getPieces().size());
		}

		return receivedFile;
	}
}
