package demo.pickup_agent;

import java.awt.GridLayout;

import javax.swing.JFrame;

import demo.acl.Product;
import demo.common.ProductDB;
import demo.warehouse_display.WarehouseDisplay;
import demo.warehouse_display.WarehouseDisplayPanel;

public class PickupAgentDisplay extends JFrame{
	public ProductDB db;
	
	public PickupAgentDisplay() {
		setLayout(new GridLayout(3,3));
		db = new ProductDB();
		
		setTitle("PickupAgentDisplay");
		setBounds(10,10, 1800, 900);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		
		for (Product product : db.getDB().values()) {
			WarehouseDisplayPanel panel = new WarehouseDisplayPanel(product,false);
			add(panel);
	    }
	}
}
