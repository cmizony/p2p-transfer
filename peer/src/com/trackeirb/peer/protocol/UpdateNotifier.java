package com.trackeirb.peer.protocol;

import com.trackeirb.peer.ConfigurationManager;
import com.trackeirb.peer.domain.Constants;
import com.trackeirb.peer.exceptions.InvalidResponseException;
import com.trackeirb.peer.files.LocalStorage;
import com.trackeirb.peer.network.PeerClientConnector;

public class UpdateNotifier extends Thread {

	private static final ConfigurationManager _CONFIGURATION = ConfigurationManager
			.getInstance();

	public UpdateNotifier() {
		super();
	}

	@Override
	public void run() {
		while (true) {
			try {
				sleep(ConfigurationManager.getInstance().getPropertyAsInt(
						Constants._UPDATE_FREQUENCY_CONF_KEY, 20) * 1000);
				PeerClientConnector connector = new PeerClientConnector(
						_CONFIGURATION.getProperty(
								Constants._TRACKER_HOST_CONF_KEY, "localhost"),
						_CONFIGURATION.getPropertyAsInt(
								Constants._TRACKER_PORT_CONF_KEY, 60001));
				ActionExecutor.getInstance().updateToTracker(connector,
						LocalStorage.getInstance().getFilesList(), null);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvalidResponseException e) {
				e.printStackTrace();
			}
		}
	}
}
