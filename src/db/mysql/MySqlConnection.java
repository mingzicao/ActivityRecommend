package db.mysql;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;


public class MySqlConnection implements DBConnection {
	
	private Connection conn;
	
	private PreparedStatement saveItemStmt = null;
	
	private PreparedStatement getSavedItem() {
		try {
			
			if (saveItemStmt == null) {
				if (conn ==null) {
					System.err.println("no database connection");
					return null;
					
				}
				saveItemStmt = conn.prepareStatement("INSERT IGNORE INTO items VALUES(?,?,?,?,?,?)");
			} 
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return saveItemStmt;
		// more to notice, when using sigleton pattern it is better to use the scync method;
		// cause two threads are possible to new two 
		
	}
	
	public MySqlConnection() {
		super();
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			/*so that the driver manager can know how to accesss sql driver*/
			 conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch(Exception e ) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() {
		// TODO Auto-generated method stub
		// close can also inherit from auto close AutoClosable;
		// when try with resource, we 
		if (conn != null) {
			try {
				conn.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			System.err.println("DB connection failed!");
			return;
		}
		try {
			String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			for (String itemId : itemIds) {
				stmt.setString(2, itemId);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			System.err.println("DB connection failed!");
			return;
		}
		
		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			for (String itemId : itemIds) {
				stmt.setString(2, itemId);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			return new HashSet<>();
		}
		
		Set<String> favoriteItemIds = new HashSet<>();
		
		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String itemId = rs.getString("item_id");
				favoriteItemIds.add(itemId);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return favoriteItemIds;

	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			System.err.println("DB Conn failed");
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);
		try {
			String sql = "SELECT * FROM items WHERE item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				stmt.setString(1, itemId);
			
				ResultSet rs = stmt.executeQuery();
				
				ItemBuilder builder = new ItemBuilder();
				while (rs.next()) { // this is similar to collection iterator
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories(itemId));
					builder.setRating(rs.getDouble("rating"));
				    favoriteItems.add(builder.build());
					
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed!");
			return null;
		}
		
		Set<String> categories = new HashSet<>();
		
		try {
			String sql = "SELECT category FROM categories WHERE item_id = ? ";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);
			
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				categories.add(rs.getString("category"));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return categories;
 	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		// TODO Auto-generated method stub
		// 改变search ， 从数据库发出请求 然后存进数据库
		
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
		for (Item item : items) {
			saveItem(item);
		}
		return items;

		
	}

	@Override
	public void saveItem(Item item) {
		if (conn == null) {
			System.err.println("DB connection failed!");
			return;
		}
		
		try {
			// SQL Injection
			// Example:
			// SELECT * FROM users WHERE username = '<username>' AND password = '<password>'
			// version 1
			// username: aoweifapweofj' OR 1=1 --
			// password: joaiefjajfaow
			// ->
			// SELECT * FROM users WHERE username = 'aoweifapweofj' OR 1=1 --' AND password = 'joaiefjajfaow'
			// version 2
			// username: oiaejofijaw
			// password: awjeofaiwjefowai' OR '1' = '1
			// ->
			// SELECT * FROM users WHERE username = 'oiaejofijaw' AND password = 'awjeofaiwjefowai' OR '1' = '1'
			
			String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, item.getItemId());
			stmt.setString(2, item.getName());
			stmt.setDouble(3, item.getRating());
			stmt.setString(4, item.getAddress());
			stmt.setString(5, item.getImageUrl());
			stmt.setString(6, item.getUrl());
			stmt.setDouble(7, item.getDistance());
			stmt.execute();
			
			sql = "INSERT IGNORE INTO categories VALUES (?, ?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, item.getItemId());
			for (String category : item.getCategories()) {
				stmt.setString(2, category);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		/*
		// < 1 >
		// before insert, we have to check if item_id has existed, cause primary key should onlu
		// exist once. so that Insert Ignore into ; can avoid the replica insert to the table;
		// < 2 > SQL injection: probably there will be same input as the sql command;
		// in this case, there will be some unintentional results;
	    // Select * from user where username = ‘111‘ OR 1 = 1
		// 会hack 到 all 用户信息 为了避免以上情况发生 不能用 %s string 避免拼接成sql 语句
		//String sql = String.format("INSERT IGNORE INTO items ('%s', '%s', '%s', '%s', %s, %s)", args).getItemNma
		/Solution: use ? instead of %s   ？ Is prepared statement;
		try {
			// here to insert into items table; 
			String sql = "INSERT IGNORE INTO items VALUES(?,?,?,?,?,?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, item.getItemId()); // 对参数进行类型检查看看有没一些奇怪的信息
			stmt.setString(2, item.getName());
			stmt.setDouble(3, item.getRating());
			stmt.setString(4, item.getAddress());
			stmt.setString(5, item.getImageUrl());
			stmt.setString(6, item.getUrl());
			stmt.setDouble(7, item.getDistance());
			stmt.execute();
			// prepare stmt 在复用的时候 效率会比每次都prepare 所有的参数 要快很多
			// here to insert into Catgory table ;
			sql = "INSERT IGNOEW INTO categories VALUES (?, ?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, item.getItemId());
			for (String c : item.getCategories()) {  // Catogories is a set of String
				stmt.setString(2, c);
				stmt.execute();
			}
			// more opt: probably the user open the browser doesnt want to conn with db. 
			// they just want to look at what they save
			// so two prepare stmt is waste; if we opt it to only when the first time that save item;
			// 只有第一次叫search item 的时候prepare 别的时候就不prepare
			// Singleton !! comes here;
			// 
			
		} catch (SQLException e) {
			e.printStackTrace();
			
		}
		*/
		
	}

	@Override
	public String getFullname(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		// TODO Auto-generated method stub
		return false;
	}

}
