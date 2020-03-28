package app.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import app.dao.utils.DatabaseUtils;
import app.model.ProductionCompany;

public class ProCoDAO {
	
	public static ProductionCompany getProductionCompanyByID(int procoID) {

		List<ProductionCompany> productionCompanies = new ArrayList<>();

        try {
            String sql = "SELECT * FROM production_company WHERE proco_id =" + procoID;

            Connection connection = DatabaseUtils.connectToDatabase();
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql);

            while(result.next()) {
            	int procoID1 = result.getInt("proco_id");
        		String procoName = result.getString("proco_name");
        		
                productionCompanies.add(
                  new ProductionCompany(procoID1, procoName)
                );
            }

            DatabaseUtils.closeConnection(connection);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if(!productionCompanies.isEmpty()) return productionCompanies.get(0);
        return null;
    }

}
