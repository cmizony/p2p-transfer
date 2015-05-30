package com.trackeirb.peer.network;

import java.util.HashMap;
import java.util.Map;

import com.trackeirb.peer.domain.Peer;

public class PeerClientsBundle {

	private static PeerClientsBundle _INSTANCE;

	private Map<PeerClientConnector, Peer> connectors;

	private PeerClientsBundle() {
		connectors = new HashMap<PeerClientConnector, Peer>();
	}

	public static PeerClientsBundle getInstance() {
		if (_INSTANCE == null) {
			_INSTANCE = new PeerClientsBundle();
		}
		return _INSTANCE;
	}

	public Map<PeerClientConnector, Peer> getConnectors() {
		return connectors;
	}

	public void addPeerClient(PeerClientConnector connector, Peer peer) {
		connectors.put(connector, peer);
	}
}
