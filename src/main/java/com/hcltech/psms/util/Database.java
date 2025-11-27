package com.hcltech.psms.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class Database {

	private static final Properties props = new Properties();

	static {
		try {
			// Ensure driver loads (Connector/J auto-registers, but explicit is fine)
			Class.forName("com.mysql.cj.jdbc.Driver"); // doc: com.mysql.cj.jdbc.Driver
			try (InputStream in = Database.class.getClassLoader().getResourceAsStream("db.properties")) {
				if (in == null)
					throw new RuntimeException("Missing db.properties in classpath");
				props.load(in);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to init DB: " + e.getMessage(), e);
		}
	}

	public static Connection getConnection() {
		try {
			return DriverManager.getConnection(
					props.getProperty("db.url"),
					props.getProperty("db.username"),
					props.getProperty("db.password"));
		} catch (Exception e) {
			throw new RuntimeException("DB connection failed: " + e.getMessage(), e);
		}
	}


}
