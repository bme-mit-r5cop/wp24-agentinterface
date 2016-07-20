package demo.warehouse_display;

import java.awt.Color;
import java.awt.Font;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import demo.acl.Product;

public class WarehouseDisplayPanel extends JPanel {
	public WarehouseDisplayPanel(Product product, boolean customImage) {
		setLayout(null);
		setBounds(0,0,600,300);
		if (customImage) {
			addImageLabel("src/images/"+product.getId()+".JPG",false,0,0,300,300);
		} else {
			addImageLabel("src/images/BOX.JPG",false,0,0,300,300);
		}
		addImageLabel("src/images/"+product.getId()+"-QR.JPG",false,300,50,200,200);
		
		JLabel title = new JLabel(product.getName()+" (€ "+product.getPrice()+")");
		title.setBounds(310,0,250,30);
		title.setFont(new Font("Arial", Font.BOLD, 22));
		add(title);
		
		setBackground(Color.white);
	}
	
	
	
	protected JLabel addImageLabel(String imageFileName, boolean inJarImage, int left, int top, int width, int height) {
		//System.out.println("Adding: "+imageFileName );
        JLabel imageLabel = new JLabel();
        ImageIcon ii;
        if (inJarImage) {
            URL imgURL = getClass().getResource("/"+imageFileName);
            ii = new ImageIcon(imgURL);
        } else {
            ii = new ImageIcon(imageFileName);
        }
        
        imageLabel.setIcon(ii);
        imageLabel.setBounds(left,top,width,height);
        add(imageLabel);
        return imageLabel;
    }
}
