package app.dao;

import app.dao.utils.DatabaseUtils;
import app.model.Account;
import app.model.Show;
import org.eclipse.collections.impl.list.mutable.FastList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



public class ShowDAO {
    public static final String SALT = "$2a$10$h.dl5J86rGH7I8bD9bZeZe";


    /**
     * Method to fetch users from the database.
     * You should use this as an example for future queries, though the sql statement
     * will change -and you are supposed to write them.
     *
     * Current user: caramel 6, password (the password is "password" without quotes)
     * @param username what the user typed in the log in form.
     * @return Some of the user data to check on the password. Null if there
     *         no matching user.
     */
    public static Show getShowByID(String showid) {
        // Fish out the results
        List<Show> shows = new ArrayList<>();

        try {
            // Here you prepare your sql statement
//        	show_id, show_title, genre, length, movie, series, proco_id, year, synopsis
            String sql = "SELECT * FROM `show` WHERE show_id =" + showid;

            // Execute the query
            Connection connection = DatabaseUtils.connectToDatabase();
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql);

            // If you have multiple results, you do a while
            while(result.next()) {
                // 2) Add it to the list we have prepared
                shows.add(
                  // 1) Create a new account object                			
                  new Show(result.getInt("show_id"), result.getString("show_title"), result.getDouble("length"),
                		  result.getBoolean("movie"), result.getBoolean("series"), result.getString("genre"),
                		  result.getInt("year"), result.getString("synopsis"))
                );
            }

            // Close it
            DatabaseUtils.closeConnection(connection);
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        // If there is a result
        if(!shows.isEmpty()) return shows.get(0);
        // If we are here, something bad happened
        return null;
    }



}
