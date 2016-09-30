package demo.sub_agents;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class InfoFrame extends JFrame {
	JLabel label;
	
	public InfoFrame(String title, String content) {
		super(title);
		setBounds(0,0,1920,1080);
		
		label = new JLabel(content,SwingConstants.CENTER);
		label.setBounds(0,0,1900,1000);
		label.setVerticalTextPosition(JLabel.CENTER);
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setFont(new Font("Arial",Font.BOLD,22));
				
		add(label);
	}
}
