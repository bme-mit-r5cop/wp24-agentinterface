package demo.sub_agents;

import java.awt.GridLayout;

import javax.swing.JFrame;

import demo.acl.Product;
import demo.common.ProductDB;

public class PickupAgentDisplay extends JFrame{
	public ProductDB db;
	
	public PickupAgentDisplay() {
		setLayout(new GridLayout(3,3));
		db = new ProductDB();
		
		setTitle("Step 2: Please find the box you need and read its QR code!");
		setBounds(0,0, 1920, 1080);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		
		for (Product product : db.getDB().values()) {
			WarehouseDisplayPanel panel = new WarehouseDisplayPanel(product,false);
			add(panel);
	    }
	}
}
