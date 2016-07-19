package demo_dummy;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

import agent.AbstractAgent;

public class ButtonAgentGUI extends JFrame{
	private AbstractAgent agent;
	private JButton terminateAll = new JButton("Terminate all");
	private JButton batteryLow = new JButton("Battery low");
	private JButton batteryFull = new JButton("Battery full");
	
	public ButtonAgentGUI(AbstractAgent agent) {
		this.agent = agent;
		
		setTitle("ButtonAgentGUI");
		setBounds(100,100, 600, 100);
		setLayout(new GridLayout(1,3));
		
		add(terminateAll);
		add(batteryLow);
		add(batteryFull);
		//terminateAll.setBounds(10, 100, 100, 40);
		
		 addWindowListener(new WindowAdapter()
	        {
	            @Override
	            public void windowClosing(WindowEvent e)
	            {
	                agent.terminate();
	            }
	        });
		 
		 terminateAll.addActionListener(new ActionListener()
		    {
		      public void actionPerformed(ActionEvent e)
		      {
		        agent.getAgentInterface().sendManagementMessage("terminate");
		        System.exit(0);
		      }
		    });
		 
		 batteryLow.addActionListener(new ActionListener()
		    {
		      public void actionPerformed(ActionEvent e)
		      {
		        agent.getAgentInterface().sendManagementMessage("battery_low");
		      }
		    });
		 
		 batteryFull.addActionListener(new ActionListener()
		    {
		      public void actionPerformed(ActionEvent e)
		      {
		        agent.getAgentInterface().sendManagementMessage("battery_charged");
		      }
		    });
	}
}
