package com.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SSODao {

	static final String JDBC_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
	static final String DB_URL = "jdbc:jtds:sqlserver://femaned-db.cse-rd.com/NEXS_QA_Instance";

	static final String USER = "sa";
	static final String PASS = "C$EC0RP";

	public void update(String firstName, String lastName, String email, String userName) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		PreparedStatement pStmt;

		try {
			Class.forName(JDBC_DRIVER);
			System.out.println("Created Class.forName(..)");

			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Created conn");

			stmt = conn.createStatement();
			System.out.println("Created stmt");

			/*
			 * String sql = "SELECT * FROM NEXS_SSO_Resource";
			 * 
			 * rs = stmt.executeQuery(sql);
			 * System.out.println("Created ResultSet...");
			 * 
			 * while(rs.next()) { String email = rs.getString("EMail");
			 * 
			 * System.out.println("Email : " + email); }
			 */
			String updateTableSql = "UPDATE NEXS_SSO_Resource SET FName = ?, LName = ?, EMail = ? WHERE UName = ?";

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
					System.out.println("Exception...");
				}
			}
		}
	}
}
