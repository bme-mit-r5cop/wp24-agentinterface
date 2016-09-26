package ros_display;

import org.ros.node.ConnectedNode;

import agent.AbstractAgent;
import demo.sales_agent.SalesAgent;

public class ROSDisplay extends AbstractAgent{
	public static ROSDisplay agent;
	public static ROSDisplayGUI gui;

	public static void main(String[] args) {
		ROSDisplay.objectName = "ROSDisplay";
		if (args.length != 1) {
			System.out.println("Missing ROS URL as command line argument.");
			System.exit(-1);
		}
		agent = new ROSDisplay();
		agent.setRosURL(args[0]);
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
