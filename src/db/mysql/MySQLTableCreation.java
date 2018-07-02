package db.mysql;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;

public class MySQLTableCreation {
	// Run this as Java application to reset db schema.
		public static void main(String[] args) {
			try {
				// Step 1 Connect to MySQL.
				System.out.println("Connecting to " + MySQLDBUtil.URL);
				/**
				 * Class.forName  = reflection 机制
				 * runtime 的值来 initialize a new instance
				 * not compile time; jdbc does not care abt what your db is, 
				 * why we need this:  create a driver;
				 * the mysqlDriver register himself to driver manager
				 * jdbc only provide api to developer 
				 * but doesn's implement the connection to db
				 * db provides driver to jdbc
				 * */
				Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
				
				/*so that the driver manager can know how to accesss sql driver*/
				Connection conn = DriverManager.getConnection(MySQLDBUtil.URL);
				
				
				if (conn == null) {
					return;
				}
				// Step 2 Drop tables in case they exist.
				Statement stmt = conn.createStatement();
				String sql = "DROP TABLE IF EXISTS categories";
				stmt.executeUpdate(sql);
				
				sql = "DROP TABLE IF EXISTS history";
				stmt.executeUpdate(sql);
				
				sql = "DROP TABLE IF EXISTS items";
				stmt.executeUpdate(sql);
				
				sql = "DROP TABLE IF EXISTS users";
				stmt.executeUpdate(sql);

				// Step 3 Create new tables
				sql = "CREATE TABLE items ("
						+ "item_id VARCHAR(255) NOT NULL,"
						+ "name VARCHAR(255),"
						+ "rating FLOAT,"
						+ "address VARCHAR(255),"
						+ "image_url VARCHAR(255),"
						+ "url VARCHAR(255),"
						+ "distance FLOAT,"
						+ "PRIMARY KEY (item_id)"
						+ ")";
				stmt.executeUpdate(sql);
				
				sql = "CREATE TABLE categories ("
						+ "item_id VARCHAR(255) NOT NULL,"
						+ "category VARCHAR(255) NOT NULL,"
						+ "PRIMARY KEY (item_id, category),"
						+ "FOREIGN KEY (item_id) REFERENCES items(item_id)"
						+ ")";
				stmt.executeUpdate(sql);
				
				sql = "CREATE TABLE users ("
						+ "user_id VARCHAR(255) NOT NULL,"
						+ "password VARCHAR(255) NOT NULL,"
						+ "first_name VARCHAR(255),"
						+ "last_name VARCHAR(255),"
						+ "PRIMARY KEY (user_id)"
						+ ")";
				stmt.executeUpdate(sql);
				
				sql = "CREATE TABLE history ("
						+ "user_id VARCHAR(255) NOT NULL,"
						+ "item_id VARCHAR(255) NOT NULL,"
						+ "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
						+ "PRIMARY KEY (user_id, item_id),"
						+ "FOREIGN KEY (item_id) REFERENCES items(item_id),"
						+ "FOREIGN KEY (user_id) REFERENCES users(user_id)"
						+ ")";
				stmt.executeUpdate(sql);

				System.out.println("Import done successfully");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

}
