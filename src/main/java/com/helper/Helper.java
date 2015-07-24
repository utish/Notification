package com.helper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.util.text.StrongTextEncryptor;

public class Helper {
	
	private static final String PASSWORD = "P@ssword!23";

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
		textEncryptor.setPassword(PASSWORD);
		
		return textEncryptor.decrypt(key);
	}
}
