package com.trackeirb.peer.test.exchanges;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.trackeirb.peer.ConfigurationManager;
import com.trackeirb.peer.exceptions.InvalidResponseException;
import com.trackeirb.peer.files.LocalStorage;
import com.trackeirb.peer.network.PeerClientConnector;
import com.trackeirb.peer.network.PeerServerConnector;
import com.trackeirb.peer.protocol.ActionExecutor;
import com.trackeirb.peer.protocol.InputMessageHandler;

public class BasicExchangePeer1Test {

	private static final InputMessageHandler _HANDLER = InputMessageHandler
			.getInstance();

	private static final LocalStorage _STORAGE = LocalStorage.getInstance();

	public static void main(String[] args) {

		// Announce and get file (to the tracker)
		PeerClientConnector trackerConnector = new PeerClientConnector(
				ConfigurationManager.getInstance().getProperty("tracker.host",
						"localhost"), ConfigurationManager.getInstance()
						.getPropertyAsInt("tracker.port", 60001));
		try {
			ActionExecutor.getInstance().announceToTracker(trackerConnector,
					_STORAGE.getFilesList(), null);
		} catch (InvalidResponseException e) {
			e.printStackTrace();
		}

		// Start server mode
		PeerServerConnector peer1 = new PeerServerConnector(
				ConfigurationManager.getInstance().getProperty("peer.host",
						"localhost"), ConfigurationManager.getInstance()
						.getPropertyAsInt("peer.port", 60002)) {

			@Override
			public void handleMessage(SocketChannel sChannel, String message) {

				// Quit message
				if ("quit".equalsIgnoreCase(message)) {
					try {
						sChannel.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}

				// SERVER MODE : FROM ANOTHER PEER
				else if (message.startsWith("interested")) {
					// interested (> have)
					_HANDLER.handleFileBufferMapRequest(sChannel, message);
				} else if (message.startsWith("getpieces")) {
					// getpieces (> data)
					_HANDLER.handlePiecesDataRequest(sChannel, message);
				}
			}
		};
		peer1.start();

		// Print local storage
		System.out.println(LocalStorage.getInstance().getFilesList());
	}
}
