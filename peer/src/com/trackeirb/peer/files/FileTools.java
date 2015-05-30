package com.trackeirb.peer.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileTools {

	private static FileTools _INSTANCE;
	private static MessageDigest _DIGESTER;

	private FileTools() {
		try {
			_DIGESTER = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static FileTools getInstance() {
		if (_INSTANCE == null) {
			_INSTANCE = new FileTools();
		}
		return _INSTANCE;
	}

	/**
	 * Generates the MD5 digest for a specific file
	 * 
	 * @param file
	 * @return
	 */

	public String getMD5FileDigest(File file) {

		byte[] b = new byte[(int) file.length()];
		try {
			InputStream stream = new FileInputStream(file);
			int bRead = stream.read(b);
			if (bRead > 0) {
				return new BigInteger(1, _DIGESTER.digest(b)).toString(16);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
