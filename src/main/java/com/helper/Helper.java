package com.helper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Helper {
	
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

}
