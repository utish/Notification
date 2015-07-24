package com.test;

import java.io.IOException;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.helper.Helper;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.AMIdentityRepository;
import com.sun.identity.idm.IdEventListener;
import com.sun.identity.idm.IdType;

public class SetupServlet extends HttpServlet implements IdEventListener {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger("SetupServlet");
	
	private AMIdentityRepository idrepo;
	private SSOToken token;
	private String realm = "/";

	
	/**
	 * Entry point of the application.
	 * It creates the AMConfig.properties from the template.
	 * It establishes connection with OpenAM
	 * 
	 */
	public void init(ServletConfig config) throws ServletException {
		try {
			Properties prop = Helper.retrieveProperties();
			realm = prop.getProperty("realm");
			
			// Create the file
			FileProcessing fp = new FileProcessing();
			fp.startFileProcessing();

			// Establish the connection
			this.setUpConnection();

		} catch (MissingResourceException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	
	private void setUpConnection() throws Exception {
		logger.info("Starting setUpConnection()");

		String tokenId = Helper.retrieveToken();
		SSOTokenManager manager = SSOTokenManager.getInstance();
		token = manager.createSSOToken(tokenId);
		idrepo = new AMIdentityRepository(token, realm);
		idrepo.addEventListener(this);

		deleteIdentity();

		logger.info("finished setUpConnection()");
	}
	
	private void deleteIdentity() throws Exception {
		idrepo.deleteIdentities(getAMIdentity(token, "xxx", IdType.USER, realm));
	}

	private Set<AMIdentity> getAMIdentity(SSOToken token, String name, IdType idType, String realm) {
		Set<AMIdentity> set = new HashSet<AMIdentity>();
		set.add(new AMIdentity(token, name, idType, realm, null));
		return set;
	}
	
	@Override
	public void allIdentitiesChanged() {
	
	}

	@Override
	public void identityChanged(String universalId) {
		logger.info("identityChanged() called with universalId :" + universalId);
		
		String userId = Helper.retrieveUserId(universalId);
		Set<AMIdentity> amIdentity = new HashSet<>();
		
		try {
			amIdentity = getAMIdentity(token, userId, IdType.USER, realm);
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}

		try {
			if (amIdentity == null || amIdentity.isEmpty()) {
				this.setUpConnection();
				amIdentity = getAMIdentity(token, userId, IdType.USER, realm);

			}

			Helper.updateDatabase(amIdentity.iterator().next());
			
		} catch (Exception e) {
			e.printStackTrace();

		}
		
		logger.info("Finished executing idenityChanged()...");
	}

	@Override
	public void identityDeleted(String arg0) {

	}

	@Override
	public void identityRenamed(String arg0) {

	}

	
}
