/*
 * R5COP-WP24-AgentInterface
 */

package agentinterface;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.topic.DefaultPublisherListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import org.ros.node.AbstractNodeMain;

import acl.ACLMessage;
import acl.AcceptedPattern;
import acl.GeneralMessage;
import acl.SubscribeMessage;
import acl.Text2SpeechMessage;
import agent.AbstractAgent;
import demo.MainAgent;
import demo.common.FileReader;


/**
 * The AgentInterface class acting as a ROS node based on a finit state machine loaded from the
 * JSON configuration file being able to change state when receiving SpeecRecognitionMessages,
 * and send output messages upon stat chage.
 *
 * @author Peter Eredics
 */
public class AgentInterface extends AbstractNodeMain{
	// Turn on logging for the init phase
    private static final boolean logging = true;
    
    // The current state of the agent
    private State currentState = null;
    
    // State register
    private HashMap<String,State> stateMap;
    
    // The connected ROS node
    private ConnectedNode connectedNode = null;
    
    // The ROS node configuration
    private NodeConfiguration nodeConfiguration = null;
    
    // The unique ID of the agent (overwritten by the config file)
    private String agentID = "0";
    
    // The topic to wait recognized sentences on
    private String speechRecognitionTopic = "";
    
    // The publisher used to register at the VoiceAgent
    Publisher<std_msgs.String> speechRecognitionRegisterPublisher;
    
    // The publisher used to send management messages
    Publisher<std_msgs.String> managemenetPublisher;
    
    // The SubscribeMessage sent every time the subscription has to sent
    SubscribeMessage subscribeMessage;
    
    // The register of the publishers used to send messages to different topics
    private HashMap<String,Publisher<std_msgs.String>> publishers = new HashMap<String,Publisher<std_msgs.String>>();
    
    // The agent using this interface and to trigger event on
    private AbstractAgent agent;
    
    // Name of the starting state of the robot
    private String startStateName = "";
    
    
    /**
     * Return the ROS node name of the agent
     */
    public GraphName getDefaultNodeName() {
      return GraphName.of("r5cop_wp24_agentinterface/"+agentID+"_"+Math.round(1+10000*Math.random()));
    }
    
    public void onShutdownComplete (Node node) {
    	agent.log("Terminating on ROS node shutdown.");
    	System.exit(0);
    }
    
    
    /**
     * Init the ROS node 
     */
    public void onStart(final ConnectedNode connectedNode) {
    	this.connectedNode = connectedNode;
    	    	
    	// Subscribe to agent's own speechRecognitionTopic
    	if (!speechRecognitionTopic.equals("")) {
    		agent.log("Subscribing to speech recognition topic: "+speechRecognitionTopic);
	        Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber(speechRecognitionTopic, std_msgs.String._TYPE);
	        subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
	          @Override
	          public void onNewMessage(std_msgs.String message) {
	        	  // Parse SRM
	        	  GeneralMessage srm = new GeneralMessage(message.getData());
	              
	              // Process SRM and change state based on content
	        	  processSpeechRecognitionMessage(srm);
	          }
	        });
    	} else {
    		agent.log("No speech recognition topic set to subscribe for.");
    	}
        
        Subscriber<std_msgs.String> managementSubscriber = connectedNode.newSubscriber("R5COP_Management", std_msgs.String._TYPE);
        managementSubscriber.addMessageListener(new MessageListener<std_msgs.String>() {
          @Override
          public void onNewMessage(std_msgs.String message) {
        	  // Parse management messages
        	  GeneralMessage mm = new GeneralMessage(message.getData());
              
              // Ask the agent to process the management message
              agent.processAllManagementMessages(mm);
          }
        });

        
        // Register as publisher to SpeechRecognitionRegister topic to export command patterns
    	speechRecognitionRegisterPublisher = connectedNode.newPublisher("SpeechRecognitionRegister", std_msgs.String._TYPE);
    	managemenetPublisher = connectedNode.newPublisher("R5COP_Management", std_msgs.String._TYPE);
    	// Wait for the publisher to get ready
        safeSleep(1000);

        // Export current masks whenever a new subscriber connects to the topic SpeechRecognitionRegister
        speechRecognitionRegisterPublisher.addListener(
        	new DefaultPublisherListener<std_msgs.String>() {
				@Override
				public void onNewSubscriber(Publisher<std_msgs.String> arg0, SubscriberIdentifier arg1) {
					MainAgent.safeSleep(1000);
					exportCurrentMasks();
				}
		});
        
        // Init the agent's custom start method
        agent.onStart(connectedNode);
        

        // Export accepted masks for the starting state	
        exportCurrentMasks();
    }

    
    
    /**
     * Default constructor 
     * 
     * @param rosURL				The ROS core URL
     * @param configFileName		The config file path
     * @param agent					The agent running the interface
     */
    public AgentInterface(String rosURL, String configFileName, AbstractAgent agent, String forceAgentID) {
    	// Save agent
    	this.agent = agent;
    	
    	// Init ROS
        URI rosMaster = URI.create(rosURL);		
		try {
			java.net.Socket socket = new java.net.Socket(rosMaster.getHost(), rosMaster.getPort());
			java.net.InetAddress local_network_address = socket.getLocalAddress();
			nodeConfiguration = NodeConfiguration.newPublic(local_network_address.getHostAddress(), rosMaster);
		} catch (UnknownHostException e) { 
			e.printStackTrace();
		} catch (IOException e) {		
			e.printStackTrace();
		}
    	
        // Loading JSON file
		if (configFileName != null) {
	        File configFile = new File(configFileName);
	        if (!configFile.exists()) {
	            agent.log("Invalid  configuration file: "+configFileName);
	            System.exit(-1);
	        } else {
	        	agent.log("Config file exists: "+configFileName);
	        }
	        
	        // Reading configuration file contents
	        String configContent = "";
	        try {
	            configContent = FileReader.readFile(configFileName);
	        } catch (Exception e) {
	            agent.log("Falied to read configuration file. Exiting. ");
	            System.exit(-1);
	        }
	        
	        // Prepare state map
	        stateMap = new HashMap<String,State>();
	        
	        // Parsing the JSON config
	        JSONObject json = new JSONObject(configContent);
	        
	        // Init agent id and related variables
	        agentID = json.getString("agent_id"); //+"___"+Math.round(Math.random()*100000+1);
	        speechRecognitionTopic = agentID+"_SpeechRecognition";
	        subscribeMessage = new SubscribeMessage(agentID, "SpeechRecognitionRegister", speechRecognitionTopic);
	        
	        // Loading empty states
	        agent.log ("Loading states...");
	        JSONArray stateArray = json.getJSONArray("states");
	        JSONObject state = null;
	        String stateName = "";
	        String initMessage = "";
	        for (int i = 0; i < stateArray.length(); i++)
	        {
	            // Read state
	            state = stateArray.getJSONObject(i);
	            stateName = state.getString("name");
	            
	            try {
	            	initMessage = state.getString("init_message");
	            } catch (Exception e) {
	            	initMessage = "";
	            }
	            
	            //Create state object
	            State currentState = new State(stateName, initMessage);
	            
	            // Store state into the state map
	            stateMap.put(stateName, currentState);
	            agent.log("State found in configuration: "+stateName);
	        }
	        
	        // Reading again states to load transitions
	        String mask = "";
	        String newState = "";
	        String trigger = "";
	        int priority;
	        for (int stateIndex = 0; stateIndex < stateArray.length(); stateIndex++) {
	            state = stateArray.getJSONObject(stateIndex);
	            stateName = state.getString("name");
	            agent.log("Processing state transitions for "+stateName);
	            
	            // Read transitions under selected state
	            try {
		            JSONArray transitionArray = state.getJSONArray("transitions");
		            for (int transitionIndex=0; transitionIndex<transitionArray.length(); transitionIndex++) {
		                JSONObject transition = transitionArray.getJSONObject(transitionIndex);
		
		                // Reading mandatory mask parameter
		                mask = transition.getString("mask");
		                    
		                // Reading optional new state parameter
		                try {
		                    // New state present in config
		                    newState = transition.getString("new_state");
		                } catch (Exception e) {
		                    // Keeping current state
		                	newState = state.getString("name");
		                }
		                
		                // Reading optional trigger
		                try {
		                    // Trigger present in config
		                    trigger = transition.getString("trigger");
		                } catch (Exception e) {
		                    // No trigger defined
		                	trigger = "";
		                }
		                
		                // Reading optional priority parameter
		                try {
		                    priority = transition.getInt("priority");
		                } catch (Exception e) {
		                    // Keeping current state
		                	priority = 0;
		                }
		                    
		                // Create new transition object
		                Transition transitionObject = null;
		                if (!trigger.equals("")) {
		                	transitionObject = new TriggerTransition(mask, priority, trigger);
		                	agent.log("  - adding trigger with mask '"+mask+"' and code '"+trigger+"'");
		                } else {
		                	transitionObject = new StateTransition(mask, priority, stateMap.get(newState));
		                	agent.log("  - adding transition with mask '"+mask+"' and newState '"+newState+"'");
		                } 
		               
		                    
		                // Read output messages specifications
		                try {
		                    JSONArray messageArray = transition.getJSONArray("output_messages");
		                    for (int messageIndex=0; messageIndex<messageArray.length(); messageIndex++) {
		                        JSONObject message = messageArray.getJSONObject(messageIndex);
		                        String targetName = message.getString("target");
		                        String messageText = message.getString("message");
		
		                        transitionObject.addOutputMessage(targetName, messageText);
		                        agent.log("    - adding output message to target '"+targetName+"' and content '"+messageText+"'");
		                    }
		                } catch (Exception e) {
		                	agent.log("    - no output messages declared");
		                }
		                
		                stateMap.get(state.getString("name")).addTransition(transitionObject);
		            }
	            } catch (Exception e) {
	            	agent.log(" - no transitions defined for this state");
	            }
	        }
	        
	        // Reading starting state
	        startStateName = json.getString("start_state");
	        agent.log ("Starting state set to: "+startStateName);
	        currentState = stateMap.get(startStateName);
	        
	        agent.log("Configuration processing ended.");
		} else {
			agent.log("No configuration file specified for the agent.");
		}
		
		if (forceAgentID != null) {
			agentID = forceAgentID;
			agent.log("AgentID forced to '"+agentID+"'.");
		}
    }
    
    
    /**
     * Getter for NodeConfiguration
     * 
     * @return						The NodeConfiguration					
     */
    public NodeConfiguration getNodeConfiguration() {
    	return nodeConfiguration;
    }

    
    
    /**
     * Send message through ROS
     * 
     * @param target				The target topic
     * @param message				The message to send
     */
    public void sendMessage(String target, String message) {
    	agent.log(" - - {"+target+"} - - > "+message);
    	Publisher<std_msgs.String> p = getPublisher(target);
    	std_msgs.String str = p.newMessage();
    	
    	if (target.equals("Text2Speech")) {
    		// Sending Text2Speech messages
    		Text2SpeechMessage ttsm = new Text2SpeechMessage(agentID, "Text2Speech", message);
			str.setData(ttsm.toJson());
    	} else {
    		// Sending standard string message
			str.setData(message);
    	}
    	p.publish(str);
    }
    
    
    /**
     * Send Text2Speech request to VoiceAgent
     * 
     * @param message				The message to say out loud
     */
    public void sendText2SpeechMessage(String message) {
    	sendMessage("Text2Speech",message);
    }

    
    
    /**
     * Return publisher based on target topic or create new if no publisher existed yet
     * 
     * @param target				The target topic the publisher is needed for
     * @return						The publisher for the target topic
     */
    public Publisher<std_msgs.String> getPublisher(String target) {
    	Publisher<std_msgs.String> p = publishers.get(target);
    	if (p == null) {
    		// No publisher present yet: creating new
    		p = connectedNode.newPublisher(target, std_msgs.String._TYPE);
    		publishers.put(target, p);
    		safeSleep(1000);
    	}
    	return p;
    }
    
    
    /**
     * Send the message content to the ROS topic as string
     * 
     * @param target				The ROS topic to send to	
     * @param content				The content of the message
     */
    public void publishMessage(String target, String content) {
    	Publisher<std_msgs.String> p = getPublisher(target);
    	std_msgs.String str = p.newMessage();
    	str.setData(content);
    	p.publish(str);
    }
    
    
    /**
     * Send ACL message to a ROS topic
     * 
     * @param target				The ROS topic to send to	
     * @param message				The ACL message to send
     */
    public void publishMessage(String target, ACLMessage message) {
    	publishMessage(target,message.toJson());
    }
    
    
    /**
     * Getter for connectedNode
     * 
     * @return						The connected node
     */
    public ConnectedNode getConnectedNode() {
    	return connectedNode;
    }
    
    
    /**
     * Process recognized speech message
     * 
     * @param message				The received message containing the sentence.
     */
    public void processSpeechRecognitionMessage(GeneralMessage message) {
    	agent.log("-------------------------------------------");
    	agent.log("Processing message: "+message.getContent());
    	agent.log("-------------------------------------------");
    	
    	// Terminate when "exit" command is received
    	if (message.getContent().equals("exit")) {
    		try {
    			connectedNode.shutdown();
    		} finally {
    			System.exit(0);    			
    		}
    	}
    	
    	processSpeechRecognitionMessage(message.getContent());
    }
    
    
    /**
     * Act on the contents of a speech recognition message
     * 
     * @param message				The string heard from the user
     */
    public void processSpeechRecognitionMessage(String message) {
    	// Change state if necessary
        State newState = currentState.getNewState(this, message);
        if (!newState.equals(currentState)) {
        	changeState(newState);
        }
    }
    
    
    /**
     * Update current state and export accepted message masks
     * @param newState
     */
    public void changeState(State newState) {
    	// Change state
    	currentState = newState;
    	
    	// Log
    	agent.log("-------------------------------------------");
    	agent.log("New current state: "+currentState.getName());
    	agent.log("-------------------------------------------");
    	
    	// This state has an init message to say out loud
        if (!newState.getInitMessage().equals("")) {
        	agent.log("Sending text to speech on new state init: "+newState.getName());
    		sendText2SpeechMessage(newState.getInitMessage());
    	} else {
    		agent.log("No init message set for new state: "+newState.getName());
    	}
    	
    	// Export masks
    	exportCurrentMasks();
    }
    
    
    /**
     * Export valid masks for the current state
     */
    public void exportCurrentMasks() {
    	if (subscribeMessage != null) {
	    	agent.log("Exporting masks: ");
	    	ArrayList<AcceptedPattern> patterns = currentState.getPatterns();
	    	for (int i=0; i<patterns.size(); i++) agent.log(" - "+patterns.get(i).getMask());
	    	
	    	// Clear and update reuseable subscribeMessage
	    	subscribeMessage.clearPatterns();
	    	subscribeMessage.updatePatternList(currentState.getPatterns());
	    	
	    	// Send the message
	    	std_msgs.String str = speechRecognitionRegisterPublisher.newMessage();
			str.setData(subscribeMessage.toJson());
			speechRecognitionRegisterPublisher.publish(str);
    	}
    }
    
    
    /**
     * Sleep for the given number of milliseconds
     * @param interval					Time to sleep
     */
    public static void safeSleep(int interval) {
		 try {
			 Thread.sleep(interval);
		 } catch (Exception e) {}
    }
    
    
    /**
     * Getter for the current state
     * 
     * @return							The current state
     */
    public State getCurrentState() {
    	return currentState;
    }
    
    
    /**
     * Return the agent using this interface
     *  
     * @return							The agent using this interface
     */
    public AbstractAgent getAgent() {
    	return agent;
    }
    
    
    
    /**
     * Return state based on state name specified
     * 
     * @param name						The state name
     * @return							The state object 
     */
    public State getStateByName(String name) {
    	return stateMap.get(name);
    }
    
    
    /**
     * Send managemenet message to all R5COP nodes in the system
     * 
     * @param content					The message content to send out
     */
    public void sendManagementMessage(String content) {
    	GeneralMessage mm = new GeneralMessage(agentID,"R5COP_Management",content);
    	std_msgs.String str = managemenetPublisher.newMessage();
    	str.setData(mm.toJson());
    	managemenetPublisher.publish(str);
    }
    
    
    /**
     * Returns the name of the starting state of the agent defined originally in the
     * agent configuration file
     * @return
     */
    public String getStartStateName() {
    	return startStateName;
    }
    
    
    /**
     * Reset the state to the initial one
     */
    public void resetState() {
    	if (!startStateName.equals("")) changeState(stateMap.get(startStateName));
    }
    
    
    /**
     * Returns the agent ID
     * @return							The agent ID
     */
    public String getAgentID() {
    	return agentID;
    }
}
