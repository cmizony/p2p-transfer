package com.trackeirb.peer.test;

import com.trackeirb.peer.ConfigurationManager;

public class ConfigurationTest {

	public static void main(String[] args) {

		ConfigurationManager configManager = ConfigurationManager.getInstance();
		System.out.println(configManager.getProperties().entrySet());
	}

}
