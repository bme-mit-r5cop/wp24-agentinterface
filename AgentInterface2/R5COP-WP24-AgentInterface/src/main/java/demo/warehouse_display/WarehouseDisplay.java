package demo.warehouse_display;

import java.awt.GridLayout;
import java.util.Iterator;

import javax.swing.JFrame;

import demo.common.Product;
import demo.common.ProductDB;

public class WarehouseDisplay extends JFrame {
	private static WarehouseDisplay display;

	public static void main(String[] args) {
		init();
	}
	
	public static void init() {
		display = new WarehouseDisplay();
		display.setVisible(true);
	}
	
	public static void showFrame() {
		display.setVisible(true);
	}
	
	public static void hideFrame() {
		display.setVisible(false);
	}
	
	public WarehouseDisplay() {
		setTitle("WarehouseDisplay");
		setBounds(10,10, 1800, 900);
		setLayout(new GridLayout(3,3));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		ProductDB db = new ProductDB();
		
		for (Product product : db.getDB().values()) {
			WarehouseDisplayPanel panel = new WarehouseDisplayPanel(product);
			add(panel);
	    }
	}
}
