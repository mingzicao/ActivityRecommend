package db;

import db.mysql.MySqlConnection;

public class DBConnectionFactor {
	// This should change based on the pipeline.
		private static final String DEFAULT_DB = "mysql";
		
		public static DBConnection getConnection(String db) {
			switch (db) {
			case "mysql":
				 return new MySqlConnection();
				
			case "mongodb":
				// return new MongoDBConnection();
				return null;
			default:
				throw new IllegalArgumentException("Invalid db: " + db);
			}
		}
		
		public static DBConnection getConnection() {
			return getConnection(DEFAULT_DB);
		}

}
