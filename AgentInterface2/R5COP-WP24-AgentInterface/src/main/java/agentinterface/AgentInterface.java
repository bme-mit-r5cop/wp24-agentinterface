/*
 * R5COP-WP24-AgentInterface
 */

package agentinterface;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.node.topic.DefaultPublisherListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import org.ros.node.AbstractNodeMain;

import acl.SpeechRecognitionMessage;
import acl.SubscribeMessage;
import acl.Text2SpeechMessage;


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
    
    // The SubscribeMessage sent every time the subscription has to sent
    SubscribeMessage subscribeMessage;
    
    // The register of the publishers used to send messages to different topics
    private HashMap<String,Publisher<std_msgs.String>> publishers = new HashMap<String,Publisher<std_msgs.String>>();
    
    
    public GraphName getDefaultNodeName() {
      return GraphName.of("r5cop_wp24_agentinterface/"+agentID+"_"+Math.round(1+10000*Math.random()));
    }
    
    
    /**
     * Init the ROS node 
     */
    public void onStart(final ConnectedNode connectedNode) {
    	this.connectedNode = connectedNode;
    	    	
    	// Subscribe to agent's own speechRecognitionTopic
        Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber(speechRecognitionTopic, std_msgs.String._TYPE);
        subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
          @Override
          public void onNewMessage(std_msgs.String message) {
        	  // Parse SRM
              SpeechRecognitionMessage srm = new SpeechRecognitionMessage(message.getData());
              
              // Process SRM and change state based on content
        	  processSpeechRecognitionMessage(srm);
          }
        });

        
        // Register as publisher to SpeechRecognitionRegister topic to export command patterns
    	speechRecognitionRegisterPublisher = connectedNode.newPublisher("SpeechRecognitionRegister", std_msgs.String._TYPE);
    	// Wait for the publisher to get ready
        safeSleep(1000);

        // Export current masks whenever a new subscriber connects to the topic SpeechRecognitionRegister
        speechRecognitionRegisterPublisher.addListener(
        	new DefaultPublisherListener<std_msgs.String>() {
				@Override
				public void onNewSubscriber(Publisher<std_msgs.String> arg0, SubscriberIdentifier arg1) {
					exportCurrentMasks();
				}
		});
        

        // Export accepted masks for the starting state	
        exportCurrentMasks();
    }

    
    
    /**
     * Default constructor 
     * 
     * @param rosURL				The ROS core URL
     * @param configFileName		The config file path
     */
    public AgentInterface(String rosURL, String configFileName) {
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
        File configFile = new File(configFileName);
        if (!configFile.exists()) {
            System.out.println("Invalid  configuration file: "+configFileName);
            System.exit(-1);
        } else {
            log("Config file exists: "+configFileName);
        }
        
        // Reading configuration file contents
        String configContent = "";
        try {
            configContent = readFile(configFileName, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Falied to read configuration file. Exiting. ");
            System.exit(-1);
        }
        
        // Prepare state map
        stateMap = new HashMap<String,State>();
        
        // Parsing the JSON config
        JSONObject json = new JSONObject(configContent);
        
        // Init agent id and related variables
        agentID = json.getString("agent_id"); //+"___"+Math.round(Math.random()*100000+1);
        speechRecognitionTopic = agentID+"_speech_recognition";
        subscribeMessage = new SubscribeMessage(agentID, "SpeechRecognitionRegister", speechRecognitionTopic);
        
        // Loading empty states
        log ("Loading states...");
        JSONArray stateArray = json.getJSONArray("states");
        JSONObject state = null;
        String stateName = "";
        for (int i = 0; i < stateArray.length(); i++)
        {
            // Read state
            state = stateArray.getJSONObject(i);
            stateName = state.getString("name");
            
            //Create state object
            State currentState = new State(stateName);
            
            // Store state into the state map
            stateMap.put(stateName, currentState);
            log("State found in configuration: "+stateName);
        }
        
        // Reading again states to load transitions
        String mask = "";
        String newState = "";
        int priority;
        for (int stateIndex = 0; stateIndex < stateArray.length(); stateIndex++) {
            state = stateArray.getJSONObject(stateIndex);
            stateName = state.getString("name");
            log("Processing state transitions for "+stateName);
            
            // Read transitions under selected state
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
                
                // Reading optional priority parameter
                try {
                    priority = transition.getInt("priority");
                } catch (Exception e) {
                    // Keeping current state
                	priority = 0;
                }
                    
                // Create new transition object
                StateTransition transitionObject = new StateTransition(mask, stateMap.get(newState),priority);
                log("  - adding transition with mask '"+mask+"' and newState '"+newState+"'");
                    
                // Read output messages specifications
                try {
                    JSONArray messageArray = transition.getJSONArray("output_messages");
                    for (int messageIndex=0; messageIndex<messageArray.length(); messageIndex++) {
                        JSONObject message = messageArray.getJSONObject(messageIndex);
                        String targetName = message.getString("target");
                        String messageText = message.getString("message");

                        transitionObject.addOutputMessage(targetName, messageText);
                        log("    - adding output message to target '"+targetName+"' and content '"+messageText+"'");
                    }
                } catch (Exception e) {
                    log("    - no output messages declared");
                }
                
                stateMap.get(state.getString("name")).addTransition(transitionObject);
            }
        }
        
        // Reading starting state
        String startStateName = json.getString("start_state");
        log ("Starting state set to: "+startStateName);
        currentState = stateMap.get(startStateName);
        
        log("Configuration processing ended.");
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
     * Read file into String
     * 
     * @param path					The file to read
     * @param encoding				The encoding to use
     * @return						The file content as String
     * @throws IOException			Exception when something goes wrong
     */
    static String readFile(String path, Charset encoding) throws IOException {
      byte[] encoded = Files.readAllBytes(Paths.get(path));
      return new String(encoded, encoding);
    }
    
    
    /**
     * Log line into standard output
     * 
     * @param line					The line to print
     */
    public static void log(String line) {
        if (!logging) return;
        String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
        System.out.println("["+timeStamp+"] "+line);
    }
    
    
    /**
     * Send message through ROS
     * 
     * @param target				The target topic
     * @param message				The message to send
     */
    public void sendMessage(String target, String message) {
    	System.out.println(" - - {"+target+"} - - > "+message);
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
     * Process recognized speech message
     * 
     * @param message				The received message containing the sentence.
     */
    public void processSpeechRecognitionMessage(SpeechRecognitionMessage message) {
    	System.out.println("-------------------------------------------");
    	System.out.println("Processing message: "+message.getContent());
    	System.out.println("-------------------------------------------");
    	
    	// Terminate when "exit" command is received
    	if (message.getContent().equals("exit")) {
    		try {
    			connectedNode.shutdown();
    		} finally {
    			System.exit(0);    			
    		}
    	}
    	
    	// Change state if necessary
        State newState = currentState.getNewState(this, message.getContent());
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
    	System.out.println("-------------------------------------------");
    	System.out.println("New current state: "+currentState.getName());
    	System.out.println("-------------------------------------------");
    	
    	// Export masks
    	exportCurrentMasks();
    }
    
    
    /**
     * Export valid masks for the current state
     */
    public void exportCurrentMasks() {
    	System.out.println("Exporting masks.");
    	
    	// Clear and update reuseable subscribeMessage
    	subscribeMessage.clearPatterns();
    	subscribeMessage.updatePatternList(currentState.getPatterns());
    	
    	// Send the message
    	std_msgs.String str = speechRecognitionRegisterPublisher.newMessage();
		str.setData(subscribeMessage.toJson());
		speechRecognitionRegisterPublisher.publish(str);
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
}
