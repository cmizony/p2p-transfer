package com.trackeirb.peer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationManager {

	private static final String _CONFIGURATION_FILE_PATH = "peer.conf";

	private static ConfigurationManager _INSTANCE;
	private Properties properties;

	private ConfigurationManager() {
		properties = new Properties();
		loadConfiguration();
	}

	public static ConfigurationManager getInstance() {
		if (_INSTANCE == null) {
			_INSTANCE = new ConfigurationManager();
		}
		return _INSTANCE;
	}

	private void loadConfiguration() {
		try {
			properties.load(new FileInputStream(_CONFIGURATION_FILE_PATH));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Properties getProperties() {
		return properties;
	}

	public String getProperty(String key, String def) {
		return properties.getProperty(key, def);
	}

	public int getPropertyAsInt(String key, int def) {
		return Integer.parseInt(getProperty(key, Integer.toString(def)));
	}

	public boolean getPropertyAsBoolean(String key, boolean def) {
		return Boolean.parseBoolean(getProperty(key, Boolean.toString(def)));
	}
}
