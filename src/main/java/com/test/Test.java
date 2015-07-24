package com.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dao.SSODao;
import com.helper.Helper;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.AMIdentityRepository;
import com.sun.identity.idm.IdEventListener;
import com.sun.identity.idm.IdType;

public class Test extends HttpServlet implements IdEventListener {

	private static final Logger logger = LogManager.getLogger("Test");

	private static final String ADMIN_PASSWORD = "adminPassword";
	private static final String ADMIN_USER = "adminUser";
	private static final String DEPLOYMENT_URI = "deploymentURI";
	private static final String PORT = "port";
	private static final String HOSTNAME = "hostname";
	private static final String PROTOCOL = "protocol";
	private static final String TOKEN_ID = "token.id=";
	private static final String PASSWORD = "password";
	private static final String USERNAME = "username";
	private static final long serialVersionUID = 1L;

	AMIdentityRepository idrepo;
	SSOToken token;

	public Test() throws Exception {
		super();

	}

	public void deleteIdentity() throws Exception {
		idrepo.deleteIdentities(getAMIdentity(token, "xxx", IdType.USER, "/"));
	}

	private Set<AMIdentity> getAMIdentity(SSOToken token, String name, IdType idType, String realm) {
		Set<AMIdentity> set = new HashSet<AMIdentity>();
		set.add(new AMIdentity(token, name, idType, realm, null));
		return set;
	}

	public void init(ServletConfig config) throws ServletException {

		try {
			this.setUpConnection();

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	private void setUpConnection() throws Exception {
		logger.info("Starting setUpConnection()");

		String tokenId = retrieveToken();
		SSOTokenManager manager = SSOTokenManager.getInstance();
		token = manager.createSSOToken(tokenId);
		idrepo = new AMIdentityRepository(token, "/");
		idrepo.addEventListener(this);

		deleteIdentity();

		logger.info("finished setUpConnection()");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		logger.info("Called doGet()..");

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

	public void allIdentitiesChanged() {
	}

	public void identityChanged(String universalId) {
		logger.info("identityChanged() called with universalId :" + universalId);

		String userId = retrieveUserId(universalId);
		Set<AMIdentity> amIdentity = new HashSet<>();
		
		try {
			amIdentity = getAMIdentity(token, userId, IdType.USER, "/");
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}

		try {
			if (amIdentity == null || amIdentity.isEmpty()) {
				this.setUpConnection();
				amIdentity = getAMIdentity(token, userId, IdType.USER, "/");

			}

			this.updateDatabase(amIdentity.iterator().next());
		} catch (Exception e) {
			e.printStackTrace();

		}
		logger.info("Finished executing idenityChanged()...");
	}

	private void updateDatabase(AMIdentity amIdentity) throws Exception {
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

	public void identityDeleted(String arg0) {

	}

	public void identityRenamed(String arg0) {

	}

	private String retrieveToken() throws Exception {
		logger.info("Calling retrieveToken()...");

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

	private String retrieveUserId(String universalId) {
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

}
