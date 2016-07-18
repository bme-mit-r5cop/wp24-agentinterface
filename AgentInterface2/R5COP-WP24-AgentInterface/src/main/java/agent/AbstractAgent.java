/*
 * R5COP-WP24-AgentInterface
 */

package agent;

import org.ros.node.ConnectedNode;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeMainExecutor;

import acl.ManagementMessage;
import agentinterface.AgentInterface;
import agentinterface.AgentLogicInterface;
import agentinterface.State;

/**
 * Example agent class using the AgentInterface interface
 * 
 * @author Peter Eredics
 *
 */
public abstract class AbstractAgent implements AgentLogicInterface {
	// Configuration strings
	private String rosURL, configFile = "";
	
	// AgentInterface
	AgentInterface ai;
	
	
	/**
	 * Set the ROS core URL
	 * 
	 * @param rosURL				The ROS core URL
	 */
	public void setRosURL(String rosURL) {
		this.rosURL = rosURL;
	}
	
	
	/**
	 * Set the config file path
	 * 
	 * @param configFile			The configuration file path
	 */
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
	
	
	/**
	 * Execute the agent
	 */
	public void execute() {
		// Create new AgentInterface
		ai = new AgentInterface(rosURL,configFile, this);
		
		// Execute the ROS node of AgentInterface
		NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
		nodeMainExecutor.execute(ai, ai.getNodeConfiguration());
	}


	/**
	 * Implement the trigger logic to handle triggers defined in the AgentInterface
	 * configuration JSON file.
	 * 
	 * @param ai					The AgentInterface
	 * @param code					The trigger code
	 * @param input					The user input firing the trigger
	 * @return						The new state of the AgentInterface to move into
	 */
	public abstract State activateTrigger(AgentInterface ai, String code, String input);
	
	
	/**
	 * Returns the AgentInterface object of the agent
	 * 
	 * @return						The AgentInterface of the agent
	 */
	public AgentInterface getAgentInterface() {
		return ai;
	}
	
	
	
	/**
	 * General management message processing implemented in the AbstractAgent
	 * 
	 * @param message				The message to process
	 */
	public void processAllManagementMessages(ManagementMessage message) {
		if (message.getContent().equals("terminate")) {
			System.out.println("Terminating on ManagementMessage: "+message.getContent());
			ai.getConnectedNode().shutdown();
		} else {
			// Let the specific agent process this message
			processUnhandledManagementMessages(message);
		}
	}
	
	
	/**
	 * Agent specific management message handling for messages not handled by the
	 * AbstractAgent handleManagementMessages function
	 * @param message
	 */
	public abstract void processUnhandledManagementMessages(ManagementMessage message);
	
	
	
	/**
	 * Init the agent after connecting to ROS
	 */
	public void onStart(ConnectedNode connectedNode) {
		System.out.println("Node connected to ROS.");
	}

}
