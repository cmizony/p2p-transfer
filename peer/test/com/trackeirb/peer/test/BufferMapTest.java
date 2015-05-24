package com.trackeirb.peer.test;

import com.trackeirb.peer.domain.BufferMap;
import com.trackeirb.peer.domain.PeerFile;
import com.trackeirb.peer.files.LocalStorage;

public class BufferMapTest {

	public static void main(String[] args) {

		LocalStorage storage = LocalStorage.getInstance();
		PeerFile file = storage.getFilesMap().values().iterator().next();

		System.out.println(file.getPhysicalFile().getName());
		System.out.println(file.getPhysicalFile().length());
		System.out.println(file.getPhysicalFile().length() / 1024);

		BufferMap map2 = new BufferMap();
		map2.set(0, 34);
		map2.set(12, 18, false);

		System.out.println(map2.getStringForm());
		System.out.println(map2.stringToBuffermap("010101010000")
				.getStringForm());
	}
}
