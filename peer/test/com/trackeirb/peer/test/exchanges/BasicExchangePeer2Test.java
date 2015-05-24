package com.trackeirb.peer.test.exchanges;

import com.trackeirb.peer.ConfigurationManager;

public class BasicExchangePeer2Test {

	// private static final String _REQUESTED_KEY =
	// "5e7192cbe4f349c6bcc5b7fdcbb67eeb";

	// private static final String _REQUESTED_KEY =
	// "96d7f66dda86abc4d71707ccbeb70615";

	// private static final String _REQUESTED_KEY =
	// "955cba0f71f4c360f72accbebdd38a7f";

	public static void main(String[] args) {

		// Change download folder for the second peer
		ConfigurationManager.getInstance().getProperties()
				.put("local.storage", "downloads2");

		// The file is created

		// LocalStorage.getInstance().createLeechedFile(
		// new PeerFile(_REQUESTED_KEY, "1a.csv", 2048, 48175, null));

		// LocalStorage.getInstance().createLeechedFile(
		// new PeerFile(_REQUESTED_KEY, "1920x1080v1.jpg", 2048, 1338096,
		// null));
		//

		// LocalStorage.getInstance().createLeechedFile(
		// new PeerFile(_REQUESTED_KEY, "snow.jpg", 2048, 510084, null));

		// PeerClientConnector peer3 = new PeerClientConnector("localhost",
		// 10004);
		// peer3.startToPeer(_REQUESTED_KEY);
	}
}
