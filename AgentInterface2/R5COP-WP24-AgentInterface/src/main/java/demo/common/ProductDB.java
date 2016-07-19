package demo.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProductDB {
	private HashMap<String,Product> database = new HashMap<String,Product>();
	
	public ProductDB() {
		this("ProductDB.json");
	}
	
	public ProductDB(String fileName) {
		try {
			String fileContent = FileReader.readFile(fileName, StandardCharsets.UTF_8);
			
			JSONObject json = new JSONObject(fileContent);
			JSONArray array = json.getJSONArray("products");
			
			for (int i=0; i<array.length();i++) {
				JSONObject product = array.getJSONObject(i);
				try {
					String id = product.getString("id");
					
					Product productObject = new Product(
								product.getString("name"),
								id,
								Float.valueOf(product.getString("price")),
								Integer.valueOf(product.getString("pos_x")),
								Integer.valueOf(product.getString("pos_y"))
							);
					database.put(id, productObject);
				} catch (Exception e) {
					System.err.println("Failed to load product from JSON object: "+product.toString());
				}
			}
			
		} catch (IOException e) {
			System.err.println("Failed to load ProductDB: ");
			e.printStackTrace();
		}
	}
	
	public  HashMap<String,Product> getDB() {
		 return database;
	}
}
