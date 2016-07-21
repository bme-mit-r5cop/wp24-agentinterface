package demo.acl;

import org.json.JSONException;
import org.json.JSONObject;

import acl.ACLMessage;


/**
 * ACL message containing a product and a status
 * 
 * @author Peter Eredics
  */
public class ProductMessage extends ACLMessage {
	// The product object sent
	Product product;
	
	// The status of the product object, this attribute determines the "meaning" of the message
	// for the related product
	String status = "";

	
	/**
	 * Constructor from JSON string
	 * 
	 * @param jsonString					The JSON string
	 * @throws JSONException				Throws exception on malfored JSON input string
	 */
	public ProductMessage(String jsonString) throws JSONException {
		super(jsonString);
		
		// Load product status from the message
		status = json.getString("status");
				
		// Load the product object from the message
		JSONObject productObject = json.getJSONObject("product");
		
		// Load product object from the JSON
		product = new Product(
				productObject.getString("name"), 
				productObject.getString("id"),
				productObject.getString("type"), 
				(float)Float.valueOf(productObject.getString("price")),
				Integer.valueOf(productObject.getString("discount")),
				Integer.valueOf(productObject.getString("pos_x")), 
				Integer.valueOf(productObject.getString("pos_y"))
				);
	}
	
	
	/**
	 * Return the product object
	 * 
	 * @return								The product object contained in the message
	 */
	public Product getProduct() {
		return product;
	}

	
	/**
	 * Sets the product object
	 * 
	 * @param product						The product object contained in the message
	 */
	public void setProduct(Product product) {
		this.product = product;
	}

	
	/**
	 * Return the status attribute
	 * 
	 * @return								Value of the status attribute			
	 */
	public String getStatus() {
		return status;
	}

	
	/**
	 * Sets the status attribute
	 * 
	 * @param status						Value of the status attribue
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	
	/**
	 * Default constructor 
	 * 
	 * @param type							The message type
	 * @param sender						The sender
	 * @param target						The target ROS topic
	 * @param p								The Product object to attach
	 * @param status						The status of the product
	 */
	public ProductMessage(String type, String sender, String target, Product p, String status) {
		super(type,sender,target);
		this.product = p;
		this.status = status;
	}
	
	
	/**
	 * Converts the message into JSON string to be sent to a ROS topic
	 */
	public String toJson() {
		String content = 
				"  \"product\" : {\n"+
				"    \"name\" : \""+product.getName()+"\",\n" +
				"    \"id\" : \""+product.getId()+"\",\n" +
				"    \"type\" : \""+product.getType()+"\",\n" +
				"    \"price\" : \""+product.getPrice()+"\",\n" +
				"    \"discount\" : \""+product.getDiscount()+"\",\n" +
				"    \"pos_x\" : \""+product.getPosX()+"\",\n" + 
				"    \"pos_y\" : \""+product.getPosY()+"\"\n" + 
				"  },\n"+
				"  \"status\" : \""+status+"\"";
	
		return super.toJson(content);
	}
}
