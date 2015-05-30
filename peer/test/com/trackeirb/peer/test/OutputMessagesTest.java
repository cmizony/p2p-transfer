package com.trackeirb.peer.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.trackeirb.peer.ConfigurationManager;
import com.trackeirb.peer.domain.PeerFile;
import com.trackeirb.peer.files.LocalStorage;
import com.trackeirb.peer.protocol.OutputMessagesFactory;

public class OutputMessagesTest {

	private static final ConfigurationManager _CONF_MANAGER = ConfigurationManager
			.getInstance();

	public static void main(String[] args) {

		LocalStorage storage = LocalStorage.getInstance();
		List<PeerFile> files = new ArrayList<PeerFile>(storage.getFilesMap()
				.values());

		OutputMessagesFactory factory = OutputMessagesFactory.getInstance();

		// 1. Announce itself to the tracker
		System.out.println(factory.getAnnouncementMessage(
				_CONF_MANAGER.getPropertyAsInt("peer.port", 60002), files,
				files));

		// 2. Get a list of files which can match with expressed criterion
		System.out.println(factory.getFileListMessage(
				"filename = \"toto.pdf\"", "size > \"33000\""));

		// 3. Get a list of peers which propose the file identified by the key
		System.out.println(factory.getPeerListMessage(files.get(0).getKey()));

		// 4. Notify interest to another peer concerning a specific file
		System.out.println(factory.getFileBufferMapMessage(files.get(0)
				.getKey()));

		// 5. Request data pieces for a specific file
		System.out.println(factory.getPiecesMessage(files.get(0).getKey(), 3,
				5, 7, 8));

		// 6. Return the buffer map for a specific file
		System.out.println(factory.returnHaveListMessage(files.get(0)));

		// 7. Returns the data for a specific file and for specific blocks
		System.out.println(factory.returnDataMessage(files.get(0),
				Arrays.asList(0, 1, 2, 3, 4)));
	}
}
