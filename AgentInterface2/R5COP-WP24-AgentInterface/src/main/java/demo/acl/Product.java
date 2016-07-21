package demo.acl;

/**
 * Object representing a product in the demo
 * 
 * @author Peter Eredics
  */
public class Product {
	// Product name, ID and type
	private String name, id, type;
	
	// Product price
	float price;
	
	// Product location on the map
	int posX, posY;
	
	// Discount percent
	int discount = 0;
	
	
	/**
	 * Constructor
	 * 
	 * @param name					Product name
	 * @param id					Product ID
	 * @param type					Product type
	 * @param price					Product price
	 * @param discount				Discount percent
	 * @param posX					Location x
	 * @param posY					Location y
	 */
	public Product(String name, String id, String type, float price, int discount, int posX, int posY) {
		this.name = name;
		this.id = id;
		this.type = type;
		this.discount = discount;
		this.price = price;
		this.posX = posX;
		this.posY = posY;
	}
	
	
	/**
	 * Create an empty product object
	 * 
	 * @param id					Product ID
	 */
	public Product(String id) {
		this.id = id;
	}
	
	
	/**
	 * Returns the X coordinate of the product location
	 * 	
	 * @return						The X coordinate of the product location
	 */
	public int getPosX() {
		return posX;
	}


	/**
	 * Sets the X coordinate of the product location
	 * 
	 * @param posX					The X coordinate of the product location
	 */
	public void setPosX(int posX) {
		this.posX = posX;
	}

	
	/**
	 * Returns the Y coordinate of the product location
	 * 	
	 * @return						The Y coordinate of the product location
	 */
	public int getPosY() {
		return posY;
	}


	/**
	 * Sets the Y coordinate of the product location
	 * 
	 * @param posY					The Y coordinate of the product location
	 */
	public void setPosY(int posY) {
		this.posY = posY;
	}


	/**
	 * Returns the product ID
	 * @return						The product ID
	 */
	public String getId() {
		return id;
	}

	
	/**
	 * Sets the product ID
	 * @param id					The product ID
	 */
	public void setId(String id) {
		this.id = id;
	}

	
	/**
	 * Returns the product price
	 * @return						The product price
	 */
	public float getPrice() {
		return price;
	}

	
	/**
	 * Sets the product price
	 * 	
	 * @param price					The product price
	 */
	public void setPrice(float price) {
		this.price = price;
	}

	
	/**
	 * Sets the product name
	 * 
	 * @param name					The product name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * Returns the product name
	 * 
	 * @return						The product name					
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Returns the product type
	 * 
	 * @return						The product type
	 */
	public String getType() {
		return type;
	}
	
	
	/**
	 * Sets the product type
	 * 
	 * @param type					The product type
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	
	/**
	 * Compares two products: products are considered to equal if their ID is the same
	 * 
	 * @param other					The other product to compare to
	 * @return						True if the two product ID equals
	 */
	public boolean compareTo(Product other) {
		return id.equals(other.getId());
	}
	
	
	/**
	 * Returns the discount value
	 * @return
	 */
	public int getDiscount() {
		return discount;
	}
	
	
	
	/**
	 * Set the discount value
	 * 
	 * @param discount				The discount value to set
	 */
	public void setDiscount(int discount) {
		this.discount = discount;
	}
}
