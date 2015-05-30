package com.trackeirb.peer.protocol;

import java.math.BigInteger;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.google.common.collect.Lists;
import com.trackeirb.peer.ConfigurationManager;
import com.trackeirb.peer.domain.Constants;
import com.trackeirb.peer.domain.FilePiece;
import com.trackeirb.peer.domain.Peer;
import com.trackeirb.peer.domain.PeerFile;
import com.trackeirb.peer.files.LocalStorage;
import com.trackeirb.peer.network.PeerServerConnector;

/**
 * Component which can handle input message and analyze them in order to extract
 * data
 * 
 * @author Elian ORIOU
 * 
 */

public class InputMessageHandler {

	// Singleton pattern
	private static InputMessageHandler _INSTANCE;

	private static final OutputMessagesFactory _FACTORY = OutputMessagesFactory
			.getInstance();

	public static InputMessageHandler getInstance() {
		if (_INSTANCE == null) {
			_INSTANCE = new InputMessageHandler();
		}
		return _INSTANCE;
	}

	// ////////////////////////////////////////////
	// //// SERVER MODE METHODS
	// ////////////////////////////////////////////

	/**
	 * [SERVER MODE]
	 * 
	 * @param sChannel
	 * @param inputMessage
	 */

	public void handleFileBufferMapRequest(SocketChannel sChannel,
			String inputMessage) {

		// Analyze message
		Matcher fileBufferMapMatcher = InputMessagesPatternsBundle._FILE_INTERESTED_PATTERN
				.matcher(inputMessage);

		if (sChannel == null || fileBufferMapMatcher.matches() == false
				|| fileBufferMapMatcher.groupCount() == 0) {
			return;
		}
		String key = fileBufferMapMatcher.group(1);

		// Retrieves the concerned file
		PeerFile concernedFile = LocalStorage.getInstance().getFile(key);

		// Answer to the partner
		PeerServerConnector.write(sChannel,
				_FACTORY.returnHaveListMessage(concernedFile));
	}

	/**
	 * [SERVER MODE]
	 * 
	 * @param sChannel
	 * @param inputMessage
	 * 
	 * @return -1 in error, the number of pieces sent otherwise
	 */

	public void handlePiecesDataRequest(SocketChannel sChannel,
			String inputMessage) {

		Matcher requestedPiecesMatcher = InputMessagesPatternsBundle._FILE_GET_PIECES_REQUEST_PATTERN
				.matcher(inputMessage);

		if (requestedPiecesMatcher.matches() == false
				|| requestedPiecesMatcher.groupCount() == 0) {
			return;
		}

		// Retrieves the concerned file
		PeerFile concernedFile = LocalStorage.getInstance().getFile(
				requestedPiecesMatcher.group(1));

		// Retrieves requested pieces
		String[] pieces = requestedPiecesMatcher.group(2).split("\\s");
		List<Integer> i_pieces = new ArrayList<Integer>(pieces.length);

		for (String piece : pieces) {
			if (piece.isEmpty()) {
				continue;
			}
			i_pieces.add(Integer.parseInt(piece));
		}

		int messageMaxSize = ConfigurationManager.getInstance()
				.getPropertyAsInt(Constants._MAXIMUM_MESSAGE_SIZE_CONF_KEY, 50);
		if (i_pieces.size() > messageMaxSize) {
			for (List<Integer> list : Lists.partition(i_pieces, messageMaxSize)) {
				// Answer to the requester
				PeerServerConnector.write(sChannel,
						_FACTORY.returnDataMessage(concernedFile, list));
			}
		} else {
			// Answer to the requester
			PeerServerConnector.write(sChannel,
					_FACTORY.returnDataMessage(concernedFile, i_pieces));
		}
	}

	// ////////////////////////////////////////////
	// //// CLIENT MODE METHODS
	// ////////////////////////////////////////////

	/**
	 * Returns a list of file received from the tracker
	 * 
	 * @param sChannel
	 * @param inputMessage
	 * @return
	 */

	public List<PeerFile> getRequestedTrackerFileList(String inputMessage) {

		List<PeerFile> files = new ArrayList<PeerFile>();

		Matcher fileListMatcher = InputMessagesPatternsBundle._FILE_LIST_PATTERN
				.matcher(inputMessage);
		// 1. Launch regular expression analysis on the input and check if the
		// content of the file list is present
		if (!fileListMatcher.matches() || fileListMatcher.groupCount() == 0) {
			return null;
		}
		// 2. Check if the content of the file list is well formed
		String[] listComponents = fileListMatcher.group(1).split("\\s");
		if (listComponents.length % Constants._FILE_ARGS_NUMBER != 0) {
			return null;
		}

		// 3. Check file descriptor(s) type(s) and pattern(s), then retrieves
		// content
		StringBuilder fileDescription;
		for (int i = 0; i < listComponents.length; i += Constants._FILE_ARGS_NUMBER) {

			fileDescription = new StringBuilder();
			fileDescription.append(listComponents[i] + " ");
			fileDescription.append(listComponents[i + 1] + " ");
			fileDescription.append(listComponents[i + 2] + " ");
			fileDescription.append(listComponents[i + 3]);

			// 3.1 Launch regular expression analysis on file descriptor(s)
			Matcher fileDecriptorMatcher = InputMessagesPatternsBundle._FILE_LIST_COMPONENT_PATTERN
					.matcher(fileDescription.toString());

			if (!fileDecriptorMatcher.matches()
					|| fileDecriptorMatcher.groupCount() != Constants._FILE_ARGS_NUMBER) {
				return null;
			}

			// 3.2 Retrieve file descriptor component
			String name = fileDecriptorMatcher.group(1);
			int size = Integer.parseInt(fileDecriptorMatcher.group(2));
			int fragmentSize = Integer.parseInt(fileDecriptorMatcher.group(3));
			String key = fileDecriptorMatcher.group(4);

			// 3.3 Add the file into the list
			files.add(new PeerFile(key, name, size, fragmentSize, null));
		}

		return files;
	}

	/**
	 * Retrieves a list of peers received from the tracker
	 * 
	 * @param sChannel
	 * @param inputMessage
	 * @return
	 */

	public List<Peer> getRequestedPeerList(String inputMessage) {

		List<Peer> peers = new ArrayList<Peer>();

		Matcher peersListMatcher = InputMessagesPatternsBundle._PEERS_LIST_PATTERN
				.matcher(inputMessage);

		// 1. Launch regular expression analysis on the input and check if the
		// content of the peer list is present
		if (peersListMatcher.matches() == false
				|| peersListMatcher.groupCount() == 0) {
			return null;
		}

		// 2. Get peer list
		String peersList = peersListMatcher.group(1);
		String[] ips = peersList.split("\\s");

		// 3. Add peers to the result list
		for (String ip : ips) {
			String address = ip.substring(0, ip.indexOf(":"));
			int port = Integer.parseInt(ip.substring(ip.indexOf(":") + 1));
			peers.add(new Peer(address, port));
		}

		return peers;
	}

	/**
	 * [CLIENT MODE]
	 * 
	 * @param inputMessage
	 * @return
	 */

	public PeerFile getRequestedFileBufferMap(String inputMessage) {

		Matcher fileBufferMapMatcher = InputMessagesPatternsBundle._FILE_BUFFERMAP_PATTERN
				.matcher(inputMessage);

		if (fileBufferMapMatcher.matches() == false
				|| fileBufferMapMatcher.groupCount() == 0) {
			return null;
		}
		String key = fileBufferMapMatcher.group(1);
		String bufferMap = fileBufferMapMatcher.group(2);

		PeerFile peer = new PeerFile(key, null, 0, 1, null, null);
		peer.getBufferMap().stringToBuffermap(bufferMap);

		return peer;
	}

	/**
	 * [CLIENT MODE]
	 * 
	 * @param inputMessage
	 */

	public PeerFile getRequestedPiecesData(String inputMessage) {

		Matcher requestedPiecesMatcher = InputMessagesPatternsBundle._FILE_GET_PIECES_ANSWER_PATTERN
				.matcher(inputMessage);

		if (requestedPiecesMatcher.matches() == false
				|| requestedPiecesMatcher.groupCount() == 0) {
			return null;
		}

		String key = requestedPiecesMatcher.group(1);
		String[] pieces = requestedPiecesMatcher.group(2).split("\\s");

		PeerFile resultedFile = new PeerFile(key, "", 0, 1, null, null);
		for (String piece : pieces) {
			String[] elements = piece.split(":");
			// The piece data
			BigInteger binary = new BigInteger(elements[1], 2);
			// Add the piece data
			FilePiece newPiece = new FilePiece(Integer.parseInt(elements[0]),
					piece.length(), binary.toByteArray());
			resultedFile.addPiece(newPiece);
		}

		return resultedFile;
	}
}
