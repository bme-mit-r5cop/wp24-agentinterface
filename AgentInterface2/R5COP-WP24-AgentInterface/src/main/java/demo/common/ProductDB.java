package demo.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import demo.acl.Product;

public class ProductDB {
	private HashMap<String,Product> database = new HashMap<String,Product>();
	
	public ProductDB() {
		this("ProductDB.json");
	}
	
	public ProductDB(boolean discountOnly) {
		this("ProductDB.json",discountOnly);
	}
	
	public ProductDB(String fileName) {
		this(fileName, false);
	}
	
	public ProductDB(String fileName, boolean discountOnly) {
		try {
			System.out.println("Loading product DB.");
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
								product.getString("type"),
								Float.valueOf(product.getString("price")),
								Integer.valueOf(product.getString("discount")),
								Integer.valueOf(product.getString("pos_x")),
								Integer.valueOf(product.getString("pos_y"))
							);
					if (discountOnly) {
						// Add discounted items only
						if (productObject.getDiscount()>0) {
							database.put(id, productObject);
						}
					} else {
						// Add all items
						database.put(id, productObject);
					}
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
