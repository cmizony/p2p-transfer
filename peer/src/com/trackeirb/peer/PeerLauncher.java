package com.trackeirb.peer;

import com.trackeirb.peer.domain.Constants;
import com.trackeirb.peer.exceptions.InvalidResponseException;
import com.trackeirb.peer.files.LocalStorage;
import com.trackeirb.peer.network.PeerClientConnector;
import com.trackeirb.peer.network.PeerServerConnector;
import com.trackeirb.peer.protocol.ActionExecutor;
import com.trackeirb.peer.ui.TrackeirbUI;

public class PeerLauncher {

	// Local Storage
	private static final LocalStorage _STORAGE = LocalStorage.getInstance();

	// Configuration
	private static final ConfigurationManager _CONFIG_MANAGER = ConfigurationManager
			.getInstance();
	private static final String _TRACKER_HOST = _CONFIG_MANAGER.getProperty(
			Constants._TRACKER_HOST_CONF_KEY, "localhost");
	private static final int _TRACKER_PORT = _CONFIG_MANAGER.getPropertyAsInt(
			Constants._TRACKER_PORT_CONF_KEY, 60001);
	private static final String _PEER_HOST = _CONFIG_MANAGER.getProperty(
			Constants._PEER_HOST_CONF_KEY, "localhost");
	private static final int _PEER_PORT = _CONFIG_MANAGER.getPropertyAsInt(
			Constants._PEER_PORT_CONF_KEY, 60002);

	public static void main(String[] args) {
		// Initialize the P2P system
		initP2P();

		// Initialize the UI
		initUI();

		// Print available local files
		System.out.println(LocalStorage.getInstance().getFilesList());
	}

	private static void initP2P() {

		// 1. Launch server mode : The network component which can listen on
		// network for incoming message (connection from another peers)
		new PeerServerConnector(_PEER_HOST, _PEER_PORT).start();

		// 2. Launch Tracker notifier
		// new UpdateNotifier().start();

		// 3. Launch client mode : Declare the peer itself to the tracker
		PeerClientConnector trackerConnector = new PeerClientConnector(
				_TRACKER_HOST, _TRACKER_PORT);
		try {
			ActionExecutor.getInstance().announceToTracker(trackerConnector,
					_STORAGE.getFilesList(), null);
		} catch (InvalidResponseException e) {
			e.printStackTrace();
		}
	}

	private static void initUI() {
		// Launch UI
		TrackeirbUI.getInstance().setVisible(true);
	}
}
