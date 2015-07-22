package com.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.Servlet;
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

import com.dao.SSODao;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.AMIdentityRepository;
import com.sun.identity.idm.IdEventListener;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdType;

/**
 * Servlet implementation class Test
 */
public class Test extends HttpServlet implements IdEventListener {
	private static final long serialVersionUID = 1L;

	AMIdentityRepository idrepo;
	SSOToken token;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
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

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		System.out.println("starting init()...");

		try {

			String tokenId = retrieveToken();
			System.out.println("retrieved the tokenId : " + tokenId);

			SSOTokenManager manager = SSOTokenManager.getInstance();
			System.out.println("created SSOTokenManager...");

			token = manager.createSSOToken(tokenId);
			System.out.println("created token...");

			// setup
			idrepo = new AMIdentityRepository(token, "/");
			System.out.println("instantiated idrepo...");

			idrepo.addEventListener(this);
			System.out.println("added listener...");
			
			// don't know why this is required...
			deleteIdentity();
			System.out.println("finished setup??...");
			
			

		} catch (SSOException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();

		}
		
		System.out.println("finished setUp()...");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		System.out.println("doGet()...");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	public void allIdentitiesChanged() {
		// TODO Auto-generated method stub
		System.out.println("******************************************************************************allIdentitiesChanged()...");
	}

	public void identityChanged(String universalId) {
		System.out.println("****************************************************************************starting identityChanged()....");
		String userId = retrieveUserId(universalId);
		Set<AMIdentity> amIdentity = getAMIdentity(token, userId, IdType.USER, "/");

		if (!amIdentity.isEmpty()) {
			AMIdentity identity = amIdentity.iterator().next();

			String firstName = "";
			String lastName = "";
			String email = "";
			String userName = "";

			try {
				Map<String, Set<String>> attrMap = identity.getAttributes();

				for (Map.Entry<String, Set<String>> entry : attrMap.entrySet()) {

					System.out.println("key : " + entry.getKey());

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

				System.out.println("givenName : " + firstName);
				System.out.println("sn : " + lastName);
				System.out.println("mail : " + email);
				System.out.println("uid : " + userName);

				SSODao dao = new SSODao();

				System.out.println("Calling update()....");
				dao.update(firstName, lastName, email, userName);

				System.out.println("Called update()... Check values...");

			} catch (SSOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IdRepoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println("******************************************************************************identityChanged()...");
	}

	public void identityDeleted(String arg0) {
		// TODO Auto-generated method stub
		System.out.println("******************************************************************************identityDeleted()....");
	}

	public void identityRenamed(String arg0) {
		// TODO Auto-generated method stub
		System.out.println("******************************************************************************identityRenamed()...");
	}

	private String retrieveToken() throws Exception {

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("http://localhost:9080/openam/identity/authenticate");

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("username", "amadmin"));
		nameValuePairs.add(new BasicNameValuePair("password", "password"));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		HttpResponse response = client.execute(post);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		String line = "";

		String token = "";

		while ((line = rd.readLine()) != null) {
			System.out.println(line);
			token = line;
		}
		return token.replace("token.id=", "");

	}

	private String retrieveUserId(String universalId) {
		// String id = "id=utish,ou=user,dc=openam,dc=forgerock,dc=org";

		StringTokenizer str = new StringTokenizer(universalId, "=,");

		String userId = "";

		while (str.hasMoreTokens()) {
			String key = str.nextToken();
			String value = str.nextToken();
			// System.out.println("key : " + key + "\t" + "value : " + value);
			if (key.equalsIgnoreCase("id")) {
				userId = value;
			}
		}

		System.out.println("userId : " + userId);

		return userId;
	}

}
