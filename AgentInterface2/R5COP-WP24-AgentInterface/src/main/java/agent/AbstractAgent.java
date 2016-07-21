/*
 * R5COP-WP24-AgentInterface
 */

package agent;

import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.ros.node.ConnectedNode;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeMainExecutor;

import acl.GeneralMessage;
import agentinterface.AgentInterface;
import agentinterface.AgentLogicInterface;
import agentinterface.State;

/**
 * Example agent class using the AgentInterface interface
 * 
 * @author Peter Eredics
 *
 */
public class AbstractAgent implements AgentLogicInterface {
	// Configuration strings
	private String rosURL, configFile, agentID = null;
	
	// The Agent object identifier
	public static String objectName = "";
	
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
		ai = new AgentInterface(rosURL,configFile, this, agentID);
		
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
	public State activateTrigger(AgentInterface ai, String code, String input) {
		log("No handler implemented for custom triggers, thus no state change is triggered by code '"+code+"'.");
		return null;
	}
	
	
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
	public void processAllManagementMessages(GeneralMessage message) {
		if (message.getContent().equals("terminate")) {
			log("Terminating on ManagementMessage: "+message.getContent());
			ai.getConnectedNode().shutdown();
		} else if (message.getContent().equals("reset")) {
			log("Resetting agent on ManagementMessage: "+message.getContent());
			ai.resetState();
			ai.exportCurrentMasks();
			reset();
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
	public void processUnhandledManagementMessages(GeneralMessage message) {
		log("No custom handler implemented for unhandled management messages, ignoring message: '"+message.getContent()+"'");
	}
	
	
	
	/**
	 * Init the agent after connecting to ROS
	 */
	public void onStart(ConnectedNode connectedNode) {
		log("Node connected to ROS.");
	}
	
	
    /**
     * Setter for the agentID to be used when no config file is specified
     * 
     * @param agentID					The agent ID to use
     */
    public void setAgentID(String agentID) {
    	this.agentID = agentID+"_"+Math.round((Math.random()*100000+1));
    }
    
    
    /**
     * Terminate both the ROS node and the java application
     */
    public void terminate() {
    	ai.getConnectedNode().shutdown();
    	System.exit(0);
    
    }
    
    
    /**
     * Reset the agent to its initial state
     */
    public void reset() {
    	// Do nothing for the general agent
    }
    
    
    
    /**
     * Display log message on stdout
     * @param message
     */
    public static void log(String message) {
        String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
        System.out.println("["+timeStamp+"] "+message);
    }

}
