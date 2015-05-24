package com.trackeirb.peer.test;

import com.trackeirb.peer.protocol.InputMessageHandler;

public class PatternsTest {

	public static void main(String[] args) {

		InputMessageHandler handler = InputMessageHandler.getInstance();

		// 1. A list of file received from tracker
		String fileList = "list [toto_2.data 2099838 1024 ef5e8e2568qs5s8d4f44hj4k7lk4d7d4xs7ss "
				+ "toto_34.txt 100550 1024 dssdsd5sd8sd5sd8lk4d7d4xs7ss]";
		handler.getRequestedTrackerFileList(fileList);

		System.out.println("-------------------");

		// 2. A list of peers received from tracker
		String peersList = "peers 039zezkzlekz6273ksjs [192.168.0.12:34 192.168.0.23:22 192.168.0.56:45]";
		handler.getRequestedPeerList(peersList);

		System.out.println("-------------------");

		// 3. A file interest
		String interestedMessage = "interested ef5e8e2568qs5s8d4f44hj4k7lk4d7d4xs7ss";
		handler.handleFileBufferMapRequest(null, interestedMessage);

		System.out.println("-------------------");

		// 4. A file buffer map received from another peer
		String haveMessage = "have ef5e8e2568qs5s8d4f44hj4k7lk4d7d4xs7ss 11100001010101111";
		handler.getRequestedFileBufferMap(haveMessage);

		System.out.println("-------------------");

		// 5. A file pieces requested
		String requestedPieces = "getpieces ef5e8e2568qs5s8d4f44hj4k7lk4d7d4xs7ss [3 5 7 8]";
		handler.handlePiecesDataRequest(null, requestedPieces);

		System.out.println("-------------------");

		// 6. Pieces answer
		String filePieces = "data ef5e8e2568qs5s8d4f44hj4k7lk4d7d4xs7ss [3:10101010110 5:100111111 7:11110001111 8:11110001111]";
		handler.getRequestedPiecesData(filePieces);
	}
}
