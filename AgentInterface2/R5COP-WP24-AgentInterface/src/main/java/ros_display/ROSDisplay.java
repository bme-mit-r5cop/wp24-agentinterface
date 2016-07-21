package ros_display;

import org.ros.node.ConnectedNode;

import agent.AbstractAgent;

public class ROSDisplay extends AbstractAgent{
	public static ROSDisplay agent;
	public static ROSDisplayGUI gui;

	public static void main(String[] args) {        
		agent = new ROSDisplay();
		agent.setRosURL("http://10.5.0.1:11311/");
		agent.setAgentID("ROSDisplay");
		agent.execute();
	}
	
	public void onStart(ConnectedNode connectedNode) {
		gui = new ROSDisplayGUI(agent);
		gui.setVisible(true);
	}
	
	public void reset() {
		gui.reset();
	}
}
