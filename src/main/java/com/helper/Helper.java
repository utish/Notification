package com.helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import com.dao.SSODao;
import com.sun.identity.idm.AMIdentity;

public class Helper {

	private static final String KEY_PASSWORD = "P@ssword!23";
	private static final String TOKEN_ID = "token.id=";
	private static final String ADMIN_PASSWORD = "adminPassword";
	private static final String ADMIN_USER = "adminUser";
	private static final String DEPLOYMENT_URI = "deploymentURI";
	private static final String PORT = "port";
	private static final String HOSTNAME = "hostname";
	private static final String PROTOCOL = "protocol";
	private static final String PASSWORD = "password";
	private static final String USERNAME = "username";

	public static Properties retrieveProperties() {
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream("c:\\tmp\\openam.properties");
			prop.load(input);

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

		return prop;
	}

	public static String decodePassword(String key) {

		StandardPBEStringEncryptor textEncryptor = new StandardPBEStringEncryptor();
		textEncryptor.setPassword(KEY_PASSWORD);

		return textEncryptor.decrypt(key);
	}

	public static String retrieveToken() throws Exception {

		HttpClient client = new DefaultHttpClient();

		Properties prop = Helper.retrieveProperties();

		String protocol = prop.getProperty(PROTOCOL);
		String host = prop.getProperty(HOSTNAME);
		String port = prop.getProperty(PORT);
		String deploymentURI = prop.getProperty(DEPLOYMENT_URI);
		String adminUser = prop.getProperty(ADMIN_USER);
		String adminPassword = Helper.decodePassword(prop.getProperty(ADMIN_PASSWORD));

		String url = new StringBuilder().append(protocol).append("://").append(host).append(":").append(port).append("/").append(deploymentURI).append("/identity/authenticate").toString();
		HttpPost post = new HttpPost(url);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair(USERNAME, adminUser));
		nameValuePairs.add(new BasicNameValuePair(PASSWORD, adminPassword));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		HttpResponse response = client.execute(post);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		String line = "";

		String token = "";

		while ((line = rd.readLine()) != null) {
			token = line;
		}
		return token.replace(TOKEN_ID, "");

	}

	public static String retrieveUserId(String universalId) {
		StringTokenizer str = new StringTokenizer(universalId, "=,");

		String userId = "";

		while (str.hasMoreTokens()) {
			String key = str.nextToken();
			String value = str.nextToken();
			if (key.equalsIgnoreCase("id")) {
				userId = value;
			}
		}

		return userId;
	}

	public static void updateDatabase(AMIdentity amIdentity) throws Exception {
		String firstName = "";
		String lastName = "";
		String email = "";
		String userName = "";

		Map<String, Set<String>> attrMap = amIdentity.getAttributes();

		for (Map.Entry<String, Set<String>> entry : attrMap.entrySet()) {

			Set<String> set = entry.getValue();

			if (entry.getKey().equalsIgnoreCase("givenName")) {
				firstName = !set.isEmpty() ? set.iterator().next() : "";
			}

			if (entry.getKey().equalsIgnoreCase("sn")) {
				lastName = !set.isEmpty() ? set.iterator().next() : "";
			}

			if (entry.getKey().equalsIgnoreCase("mail")) {
				email = !set.isEmpty() ? set.iterator().next() : "";
			}

			if (entry.getKey().equalsIgnoreCase("uid")) {
				userName = !set.isEmpty() ? set.iterator().next() : "";
			}

		}

		SSODao dao = new SSODao();

		dao.update(firstName, lastName, email, userName);
	}
}
