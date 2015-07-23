package com.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.helper.Helper;

public class SSODao {

	private static final String JDBC_JTDS_SQLSERVER = "jdbc:jtds:sqlserver://";
	private static final String DB_PASSWORD_KEY = "dbPassword";
	private static final String DB_USER_KEY = "dbUser";
	private static final String DATABASE_KEY = "database";
	private static final String JDBC_HOST_KEY = "jdbcHost";
	private static final String UPDATE_QUERY = "UPDATE NEXS_SSO_Resource SET FName = ?, LName = ?, EMail = ? WHERE UName = ?";
	private static final String JDBC_DRIVER = "net.sourceforge.jtds.jdbc.Driver";

	private final String DB_URL;
	private final String DB_USER;
	private final String DB_PASS;
	
	public SSODao() {
		Properties prop = Helper.retrieveProperties();
		String host = prop.getProperty(JDBC_HOST_KEY);
		String database = prop.getProperty(DATABASE_KEY);
		
		
		DB_URL = new StringBuilder().append(JDBC_JTDS_SQLSERVER).append(host).append("/").append(database).toString();
		DB_USER = prop.getProperty(DB_USER_KEY);
		DB_PASS = prop.getProperty(DB_PASSWORD_KEY);
		
	}

	public void update(String firstName, String lastName, String email, String userName) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		PreparedStatement pStmt;

		try {
			Class.forName(JDBC_DRIVER);
			System.out.println("Created Class.forName(..)");

			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			System.out.println("Created conn");

			stmt = conn.createStatement();
			System.out.println("Created stmt");

			String updateTableSql = UPDATE_QUERY;

			pStmt = conn.prepareStatement(updateTableSql);

			pStmt.setString(1, firstName);
			pStmt.setString(2, lastName);
			pStmt.setString(3, email);
			pStmt.setString(4, userName);

			pStmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();

		} finally {

			if (conn != null) {
				try {
					conn.close();

					if (stmt != null) {
						stmt.close();
					}

					if (rs != null) {
						rs.close();
					}

				} catch (SQLException e) {
					
					e.printStackTrace();
					
				}
			}
		}
	}
}
