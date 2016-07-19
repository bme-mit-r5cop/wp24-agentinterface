package demo.common;

public class Product {
	private String name, id;
	float price;
	int posX, posY;
	
	
	public Product(String name, String id, float price, int posX, int posY) {
		this.name = name;
		this.id = id;
		this.price = price;
		this.posX = posX;
		this.posY = posY;
	}
	
	
	public int getPosX() {
		return posX;
	}


	public void setPosX(int posX) {
		this.posX = posX;
	}


	public int getPosY() {
		return posY;
	}


	public void setPosY(int posY) {
		this.posY = posY;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
