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

import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.AMIdentityRepository;
import com.sun.identity.idm.IdEventListener;
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
		// TODO Auto-generated constructor stub

		System.out.println("constructor ...");

		String tokenId = retrieveToken();

		SSOTokenManager manager = SSOTokenManager.getInstance();

		token = manager.createSSOToken(tokenId);

		System.out.println("token created...");

		// setup
		idrepo = new AMIdentityRepository(token, "/");

		idrepo.addEventListener(this);
		
		
	}

	public void createIdentity() throws Exception {
		String strID = "utish";
		IdType idType = IdType.USER;
		Map map = new HashMap();
		Set set = new HashSet();
		set.add(strID);

		map.put("sn", set);
		map.put("cn", set);

		set = new HashSet();
		set.add("active");

		map.put("inetuserstatus", set);

		set = new HashSet();
		set.add("password");

		map.put("userpassword", set);

		AMIdentity amid = idrepo.createIdentity(idType, strID, map);
		assert (amid.getName().equals(strID));
	}
	
	public void deleteIdentity() throws Exception {
		idrepo.deleteIdentities(getAMIdentity(token, "utish", IdType.USER, "/"));
		
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
		// TODO Auto-generated method stub
		System.out.println("init()...");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("doGet()...");
		
		//createIdentity();
		
				try {
					deleteIdentity();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("deleteIdentity() has thrown some exception....");
					e.printStackTrace();
				}
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

	public void identityChanged(String arg0) {
		// TODO Auto-generated method stub
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

}
