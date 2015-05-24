package com.trackeirb.peer.test;

import com.trackeirb.peer.files.LocalStorage;

public class LocalStorageTest {

	public static void main(String[] args) {
		System.out.println(LocalStorage.getInstance().getFilesMap());
	}

}
