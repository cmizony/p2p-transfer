package com.trackeirb.peer.protocol;

import java.util.regex.Pattern;

public class InputMessagesPatternsBundle {

	// 0. Messages Constants
	public static final String _OK_CST = "ok";

	public static final String _KO_CST = "ko";

	// 1. Generic Regular Expressions
	private static final String _FILENAME_REGEXP = "\\p{Graph}+";

	private static final String _FILE_HASH_REGEXP = "\\p{Alnum}+";

	private static final String _FILE_BUFFERMAP_REGEXP = "[10]+";

	private static final String _IP_ADDRESS_REGEXP = "(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)"
			+ "(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}";

	private static final String _FILE_PIECE_ID_REGEX = "\\p{Digit}+";

	// /////////////////////////////////////////////////////
	// /////// MESSAGES RECEIVED FROM THE TRACKER
	// /////////////////////////////////////////////////////

	// 2. A list of files received from the tracker
	private static final String _FILE_LIST_REGEX = "^\\Qlist\\E\\s\\[(.*)\\]$";

	private static final String _FILE_LIST_COMPONENT_REGEX = "("
			+ _FILENAME_REGEXP + ")\\s(\\p{Digit}+)\\s(\\p{Digit}+)\\s("
			+ _FILE_HASH_REGEXP + ")";

	public static final Pattern _FILE_LIST_PATTERN = Pattern
			.compile(_FILE_LIST_REGEX);

	public static final Pattern _FILE_LIST_COMPONENT_PATTERN = Pattern
			.compile(_FILE_LIST_COMPONENT_REGEX);

	// 3. A list of peers (providing a specific file) received from the tracker
	private static final String _PEERS_LIST_COMPONENT_REGEX = _IP_ADDRESS_REGEXP
			+ "[:]\\p{Digit}+";

	public static final String _PEERS_LIST_REGEX = "^\\Qpeers\\E\\s"
			+ _FILE_HASH_REGEXP + "\\s\\[((" + _PEERS_LIST_COMPONENT_REGEX
			+ "\\s?)*)\\]$";

	public static final Pattern _PEERS_LIST_PATTERN = Pattern
			.compile(_PEERS_LIST_REGEX);

	// /////////////////////////////////////////////////////
	// /////// MESSAGES RECEIVED FROM ANOTHER PEER
	// /////////////////////////////////////////////////////

	// 1. A file information request received from another peer
	// interested > have
	private static final String _FILE_INTERESTED_REGEX = "^\\Qinterested\\E\\s("
			+ _FILE_HASH_REGEXP + ")$";

	public static final Pattern _FILE_INTERESTED_PATTERN = Pattern
			.compile(_FILE_INTERESTED_REGEX);

	private static final String _FILE_BUFFERMAP_REGEX = "^\\Qhave\\E\\s("
			+ _FILE_HASH_REGEXP + ")\\s(" + _FILE_BUFFERMAP_REGEXP + ")$";

	public static final Pattern _FILE_BUFFERMAP_PATTERN = Pattern
			.compile(_FILE_BUFFERMAP_REGEX);

	// 2. A file download request received from another peer
	// getpieces > data
	public static final String _FILE_GET_PIECES_REQUEST_REGEX = "^\\Qgetpieces\\E\\s("
			+ _FILE_HASH_REGEXP
			+ ")\\s\\[(("
			+ _FILE_PIECE_ID_REGEX
			+ "\\s?)*)\\]$";

	public static final Pattern _FILE_GET_PIECES_REQUEST_PATTERN = Pattern
			.compile(_FILE_GET_PIECES_REQUEST_REGEX);

	private static final String _FILE_GET_PIECES_ANSWER_REGEX = "^\\Qdata\\E\\s("
			+ _FILE_HASH_REGEXP + ")\\s\\[(.*)\\]$";

	public static final Pattern _FILE_GET_PIECES_ANSWER_PATTERN = Pattern
			.compile(_FILE_GET_PIECES_ANSWER_REGEX);
}
