package com.trackeirb.peer.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class WeirdFileContentTest {

	public static void main(String[] args) {

		File f = new File("downloads2/1a.csv");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String str = "";
			do {
				str = reader.readLine();
				System.out.println(str);
			} while (str != null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
