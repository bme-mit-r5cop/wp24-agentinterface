package demo.warehouse_display;

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
	}
	
	public static void init() {
		display = new WarehouseDisplay();
		display.setVisible(true);
	}
	
	public void showFrame() {
		setVisible(true);
	}
	
	public void hideFrame() {
		setVisible(false);
	}
	
	public WarehouseDisplay() {
		setLayout(new GridLayout(3,3));
		db = new ProductDB();

		setTitle("WarehouseDisplay");
		setBounds(10,10, 1800, 900);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		for (Product product : db.getDB().values()) {
			WarehouseDisplayPanel panel = new WarehouseDisplayPanel(product,true);
			add(panel);
	    }
	}
}
