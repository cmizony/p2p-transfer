package com.trackeirb.peer.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.trackeirb.peer.ConfigurationManager;
import com.trackeirb.peer.domain.Constants;
import com.trackeirb.peer.protocol.InputMessageHandler;

/**
 * Represents a connector used to wait connections from another peer(s). This
 * connector is composed by non-blocking sockets so it's many-sync-client
 * compatible.
 * 
 * @author Elian ORIOU
 * 
 */

public class PeerServerConnector extends Thread {

	// The input message handler which can handle input message in server
	// mode
	private static final InputMessageHandler _HANDLER = InputMessageHandler
			.getInstance();

	private static final int _MAXIMUM_CONNECTED_PEERS = ConfigurationManager
			.getInstance().getPropertyAsInt(
					Constants._MAXIMUM_CONNECTED_PEERS_CONF_KEY, 5);

	private InetSocketAddress host;
	private Selector selector;

	/**
	 * The parameterized constructor
	 * 
	 * @param host
	 * @param port
	 */

	public PeerServerConnector(String host, int port) {
		this.host = new InetSocketAddress(host, port);
		initSelector();
	}

	/**
	 * THIS METHOD HANDLES INPUT MESSAGES IN SERVER MODE ONLY. OTHERWISE
	 * MESSAGES RECEIVED IN CLIENT MODE ARE HANDLES BY THE ACTION EXECUTOR.
	 * 
	 * @param sChannel
	 * @param message
	 */

	public void handleMessage(SocketChannel sChannel, String message) {

		System.out.println("Message received from client : " + message);

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

	/**
	 * The thread running method
	 */

	@Override
	public void run() {
		listen();
	}

	/**
	 * initiates the selector
	 */

	private void initSelector() {
		try {
			this.selector = Selector.open();
			ServerSocketChannel sChannel = createSocketChannel(host);
			// Register the channel with selector, listening for all events
			sChannel.register(selector, sChannel.validOps());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a non-blocking socket channel for the specified host name and
	 * port. connect() is called on the new channel before it is returned.
	 */

	private static ServerSocketChannel createSocketChannel(
			InetSocketAddress hostName) throws IOException {
		// Create a non-blocking socket channel
		ServerSocketChannel sChannel = ServerSocketChannel.open();
		sChannel.configureBlocking(false);

		// Send a connection request to the server; this method is non-blocking
		sChannel.socket().bind(hostName);
		return sChannel;
	}

	/**
	 * Listening to an incoming connection
	 */

	private void listen() {
		// Wait for events
		while (true) {
			try {
				// Wait for an event
				selector.select();
			} catch (IOException e) {
				return;
			}

			// Get list of selection keys with pending events
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();

			// Process each key at a time
			while (it.hasNext()) {
				// Get the selection key
				SelectionKey selKey = (SelectionKey) it.next();
				// Remove it from the list to indicate that it is being
				// processed
				it.remove();
				// Accept if the number of connected clients is into the
				// interval defined into the configuration file
				if (selKey.isAcceptable()
						&& selector.selectedKeys().size() <= _MAXIMUM_CONNECTED_PEERS) {
					this.accept(selKey);
				} else if (selKey.isReadable() && selKey.isWritable()) {
					this.read(selKey);
				}
			}
		}
	}

	/**
	 * Accepts an incoming connection
	 * 
	 * @param selKey
	 *            The selector channel where the connection is hosted
	 */

	private void accept(SelectionKey selKey) {
		// For an accept to be pending the channel must be a server socket
		// channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selKey
				.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel;
		try {
			socketChannel = serverSocketChannel.accept();
			socketChannel.configureBlocking(false);

			// Register the new SocketChannel with our Selector, indicating
			// we'd like to be notified when there's data waiting to be read
			socketChannel.register(this.selector, SelectionKey.OP_READ
					| SelectionKey.OP_WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Read data on the specific selector hosted on the socket
	 * 
	 * @param selKey
	 */

	private void read(SelectionKey selKey) {
		// Get channel with bytes to read
		SocketChannel sChannel = (SocketChannel) selKey.channel();
		ByteBuffer buf = ByteBuffer.allocateDirect(4096);
		String message = "";

		try {
			// Clear the buffer and read bytes from socket
			buf.clear();
			int numBytesRead = sChannel.read(buf);

			if (numBytesRead == -1) {
				// No more bytes can be read from the channel
				sChannel.close();
			} else {
				// To read the bytes, flip the buffer
				buf.flip();
				CharBuffer cBuff = Constants._NETWORK_STRING_DECODER
						.decode(buf);
				message += cBuff.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Clean and handles the incoming message
		message = cleanMessage(message);
		handleMessage(sChannel, message);
	}

	/**
	 * Write data on selector channel
	 * 
	 * @param sChannel
	 * @param message
	 */

	public static void write(SocketChannel sChannel, String message) {

		if (sChannel == null) {
			return;
		}

		String data = message + "\n";
		ByteBuffer buffer = ByteBuffer.allocate(data.length());
		buffer.clear();
		buffer.put(data.getBytes());
		buffer.flip();

		while (buffer.hasRemaining()) {
			try {
				sChannel.write(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Clean an incoming String message
	 * 
	 * @param message
	 * @return
	 */

	public String cleanMessage(String message) {
		return message.replaceAll("\\n", "").replaceAll("\\r", "");
	}
}
