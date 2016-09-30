package demo.sub_agents;

import java.awt.GridLayout;
import java.util.Iterator;

import javax.swing.JFrame;

import demo.acl.Product;
import demo.common.ProductDB;

public class WarehouseDisplay extends JFrame {
	private static WarehouseDisplay display;
	public ProductDB db;

	public static void main(String[] args) {
		init();
		display.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		display.setVisible(true);
	}
	
	public static WarehouseDisplay init() {
		display = new WarehouseDisplay();
		return display;
	}
	
	public WarehouseDisplay() {
		setLayout(new GridLayout(3,3));
		db = new ProductDB();

		setTitle("Step 1: Please read the QR codes of the items you want to buy!");
		setBounds(0,0, 1600, 900);
		
		for (Product product : db.getDB().values()) {
			WarehouseDisplayPanel panel = new WarehouseDisplayPanel(product,true);
			add(panel);
	    }
	}
}
