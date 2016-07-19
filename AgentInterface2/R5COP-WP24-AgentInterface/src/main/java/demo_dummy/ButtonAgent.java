package demo_dummy;

import agent.AbstractAgent;

public class ButtonAgent extends AbstractAgent {
	public static ButtonAgent agent;
	public static ButtonAgentGUI gui;

	public static void main(String[] args) {        
		agent = new ButtonAgent();
		agent.setRosURL("http://10.5.0.1:11311/");
		agent.setAgentID("button_agent");
		agent.execute();
		
		gui = new ButtonAgentGUI(agent);
		gui.setVisible(true);
	}
}
