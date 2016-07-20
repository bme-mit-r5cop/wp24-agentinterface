package ros_display;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

import agent.AbstractAgent;
import demo.common.FileReader;

public class ROSDisplayGUI extends JFrame{
	private AbstractAgent agent;
	
	public ROSDisplayGUI(AbstractAgent agent) {
		this.agent = agent;
		
		setTitle("ROSDisplay");
		setBounds(1920,0, 1920, 1080);
		setLayout(new GridLayout(3,3));
		
		
		//terminateAll.setBounds(10, 100, 100, 40);
		
		 addWindowListener(new WindowAdapter()
	        {
	            @Override
	            public void windowClosing(WindowEvent e)
	            {
	                agent.terminate();
	            }
	        });
		
		 try {
			 String config = FileReader.readFile("rosdisplay.txt");
			 
			 String lines[] = config.split("[\\r\\n]+");
			 int i = 0;
			 for (i=0; ((i<lines.length)&&(i<9));i++) {
				 add(new ROSDisplayPanel(agent, lines[i], Color.white));
			 }
			 
			 while (i<9) {
				 add(new ROSDisplayPanel(agent, "", Color.white));
				 i++;
			 }
			 
			 
		 } catch (Exception e){
			 e.printStackTrace();
		 }
		
		
	}
}
