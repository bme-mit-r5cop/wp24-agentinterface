/*
 * R5COP-WP24-AgentInterface
 */

package agent;

import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeMainExecutor;

import agentinterface.AgentInterface;

/**
 * Example agent class using the AgentInterface interface
 * 
 * @author Peter Eredics
 *
 */
public class Agent {
	// Configuration strings
	private String rosURL, configFile;

	/**
	 * Create a new agent and execute it
	 * 
	 * @param args
	 */
	public static void main(String[] args) {        
		Agent a = new Agent("http://10.5.0.1:11311/", "agentConfiguration.json");
		a.execute();
	}
	
	
	/**
	 * Default constructor
	 * 
	 * @param rosURL				The ROS core URL
	 * @param configFile			The configuration file path
	 */
	public Agent(String rosURL, String configFile) {
		this.rosURL = rosURL;
		this.configFile = configFile;
	}
	
	
	/**
	 * Execute the agent
	 */
	public void execute() {
		// Create new AgentInterface
		AgentInterface ai = new AgentInterface(rosURL,configFile);
		
		// Execute the ROS node of AgentInterface
		NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
		nodeMainExecutor.execute(ai, ai.getNodeConfiguration());
	}

}
