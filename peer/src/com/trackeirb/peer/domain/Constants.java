package com.trackeirb.peer.domain;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class Constants {

	public static final Charset _DEFAULT_CHARSET = Charset.forName("UTF-8");

	public static final CharsetDecoder _NETWORK_STRING_DECODER = _DEFAULT_CHARSET
			.newDecoder();

	public static final int _FILE_ARGS_NUMBER = 4;

	public static final int _MIN_PORT_NUMBER = 60000;

	public static final int _MAX_PORT_NUMBER = 60025;

	/* Configuration file keys */

	public static final String _TRACKER_HOST_CONF_KEY = "tracker.host";

	public static final String _TRACKER_PORT_CONF_KEY = "tracker.port";

	public static final String _PEER_HOST_CONF_KEY = "peer.host";

	public static final String _PEER_PORT_CONF_KEY = "peer.port";

	public static final String _LOCAL_STORAGE_CONF_KEY = "local.storage";

	public static final String _PIECE_SIZE_CONF_KEY = "piece.size";

	public static final String _MAXIMUM_CONNECTED_PEERS_CONF_KEY = "maximum.connected.peers";

	public static final String _MAXIMUM_MESSAGE_SIZE_CONF_KEY = "maximum.message.size";

	public static final String _UPDATE_FREQUENCY_CONF_KEY = "update.frequency";

}
