package app.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import app.dao.utils.DatabaseUtils;
import app.model.ShowImage;

public class ImageDAO {
	
	public static boolean addNewShowImage(ShowImage imgToAdd) {
		boolean success = true;
		try {
			Connection connection = DatabaseUtils.connectToDatabase();
			Statement statement = connection.createStatement();
			
			String sql = String.format("INSERT INTO `show_image_show` (url, show_id) VALUES "
					+ "('%s',%d);", imgToAdd.getUrl(), imgToAdd.getShowID());
			
			statement.executeUpdate(sql);
			
			statement.close();
			DatabaseUtils.closeConnection(connection);
			
		} catch (Exception e) {
			success = false;
			e.printStackTrace();
		}
		
		return success;
		
	}

}