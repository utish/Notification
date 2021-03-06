package com.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.helper.Helper;
import com.sun.identity.security.EncodeAction;

public class FileProcessing {

	private static final Logger logger = LogManager.getLogger("FileProcessing");

	private static final String FILE_AMCONFIG_PROPERTIES_TEMPLATE = "AMConfig.properties.template";
	private static final String FILE_AMCONFIG_PROPERTIES = "AMConfig.properties";
	private static final String TAG_DEBUG_DIR = "DEBUG_DIR";
	private static final String TAG_APPLICATION_PASSWD = "ENCODED_APPLICATION_PASSWORD";
	private static final String TAG_NAMING_URL = "NAMING_URL";
	private static final String TAG_SERVER_PROTOCOL = "SERVER_PROTOCOL";
	private static final String TAG_SERVER_HOST = "SERVER_HOST";
	private static final String TAG_SERVER_PORT = "SERVER_PORT";
	private static final String TAG_DEPLOY_URI = "DEPLOY_URI";
	private static final String TAG_CLIENT_ENC_KEY = "ENCRYPTION_KEY_LOCAL";
	private static final String TAG_SESSION_PROVIDER_CLASS = "SESSION_PROVIDER_CLASS";
	private static final String SESSION_PROVIDER_CLASS = "com.sun.identity.plugin.session.impl.FMSessionProvider";
	private static final String TAG_CONFIGURATION_PROVIDER_CLASS = "CONFIGURATION_PROVIDER_CLASS";
	private static final String CONFIGURATION_PROVIDER_CLASS = "com.sun.identity.plugin.configuration.impl.ConfigurationInstanceImpl";
	private static final String TAG_DATASTORE_PROVIDER_CLASS = "DATASTORE_PROVIDER_CLASS";
	private static final String DATASTORE_PROVIDER_CLASS = "com.sun.identity.plugin.datastore.impl.IdRepoDataStoreProvider";
	private static final String TRUST_ALL_CERTS = "com.iplanet.am.jssproxy.trustAllServerCerts=true\n";
	private static final String CLIENT_ENC_KEY = "com.sun.identity.client.encryptionKey";
	private static final String AM_ENC_KEY = "am.encryption.pwd";

	private Map properties = new HashMap();
	private Map labels = new HashMap();

	private String debugDirectory;
	private String password;
	private String hostname;
	private String port;
	private String deploymentURI;
	private String protocol;

	private static List questions = new ArrayList();
	private static List clientQuestions = new ArrayList();

	static {
		questions.add(TAG_DEBUG_DIR);
		questions.add(TAG_APPLICATION_PASSWD);
		questions.add(TAG_SERVER_PROTOCOL);
		questions.add(TAG_SERVER_HOST);
		questions.add(TAG_SERVER_PORT);
		questions.add(TAG_DEPLOY_URI);
		questions.add(TAG_NAMING_URL);
	}

	public void startFileProcessing() throws Exception {
		logger.info("Started processing...");

		getDefaultValues();
		retrieveValuesFromPropertiesFile();
		promptForServerAnswers();
		createPropertiesFile();

		logger.info("Completed processing...");

	}

	private void getDefaultValues() throws MissingResourceException {
		ResourceBundle rb = ResourceBundle.getBundle("clientDefault");
		for (Enumeration e = rb.getKeys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			String value = (String) rb.getString(key);

			if (value.startsWith("[")) {
				labels.put(key, value.substring(1, value.length() - 1));
			} else {
				properties.put(key, value);
			}
		}

		// add defaults to properties
		properties.put(TAG_DATASTORE_PROVIDER_CLASS, DATASTORE_PROVIDER_CLASS);
		properties.put(TAG_CONFIGURATION_PROVIDER_CLASS, CONFIGURATION_PROVIDER_CLASS);
		properties.put(TAG_SESSION_PROVIDER_CLASS, SESSION_PROVIDER_CLASS);

		System.setProperty(CLIENT_ENC_KEY, (String) properties.get(TAG_CLIENT_ENC_KEY));
		System.setProperty(AM_ENC_KEY, (String) properties.get(TAG_CLIENT_ENC_KEY));
	}

	private void promptForServerAnswers() throws IOException {
		for (Iterator i = questions.iterator(); i.hasNext();) {
			String q = (String) i.next();

			String value = "";
			while (value.length() == 0) {
				String defaultValue = null;
				if (q.equals(TAG_NAMING_URL)) {
					defaultValue = properties.get(TAG_SERVER_PROTOCOL) + "://" + properties.get(TAG_SERVER_HOST) + ":" + properties.get(TAG_SERVER_PORT) + "/" + properties.get(TAG_DEPLOY_URI)
							+ "/namingservice";
					value = defaultValue;
				}

				if (q.equals(TAG_DEBUG_DIR)) {
					value = debugDirectory;
				}

				if (q.equals(TAG_APPLICATION_PASSWD)) {
					value = password;
				}

				if (q.equals(TAG_SERVER_HOST)) {
					value = hostname;
				}

				if (q.equals(TAG_SERVER_PORT)) {
					value = port;
				}

				if (q.equals(TAG_DEPLOY_URI)) {
					value = deploymentURI;
				}

				if (q.equals(TAG_SERVER_PROTOCOL)) {
					value = protocol;
				}

				value = value.trim();

			}

			if (q.equals(TAG_APPLICATION_PASSWD)) {
				properties.put(q, (String) AccessController.doPrivileged(new EncodeAction(value)));
			} else {
				properties.put(q, value);
			}

		}
	}

	private void createPropertiesFile() throws Exception {
		URL path = Thread.currentThread().getContextClassLoader().getResource("com/test/");
		File file = new File(path.getPath() + "../../" + FILE_AMCONFIG_PROPERTIES_TEMPLATE);
		String content = getFileContent(file.getAbsolutePath());

		for (Iterator i = properties.keySet().iterator(); i.hasNext();) {
			String tag = (String) i.next();
			String value = (String) properties.get(tag);
			value = value.replaceAll("\\\\", "\\\\\\\\");
			content = content.replaceAll("@" + tag + "@", value);
		}

		String protocol = "http";
		if (protocol.equalsIgnoreCase("https")) {
			content += TRUST_ALL_CERTS;
		}

		File amConfigFile = new File(path.getPath() + "../../" + FILE_AMCONFIG_PROPERTIES);

		BufferedWriter out = new BufferedWriter(new FileWriter(amConfigFile));
		out.write(content);
		out.close();

	}

	private String getFileContent(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		StringBuffer buff = new StringBuffer();
		String line = reader.readLine();

		while (line != null) {
			buff.append(line).append("\n");
			line = reader.readLine();
		}
		reader.close();
		return buff.toString();
	}

	private void retrieveValuesFromPropertiesFile() {
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream("c:\\tmp\\openam.properties");
			prop.load(input);

			debugDirectory = prop.getProperty("debugdirectory");
			password = Helper.decodePassword(prop.getProperty("password"));
			hostname = prop.getProperty("hostname");
			port = prop.getProperty("port");
			deploymentURI = prop.getProperty("deploymentURI");
			protocol = prop.getProperty("protocol");

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
