/*
 * R5COP-WP24-AgentInterface
 */

package agent;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.ros.concurrent.CancellableLoop;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import acl.SpeechRecognitionMessage;
import acl.SubscribeMessage;
import agentinterface.AgentInterface;


/**
 * Console interface for AgentInterface: commands can be entered on the standard input, 
 * and SubscribeMessages are displayed on the stanard error.
 * 
 * @author Peter Eredics
 *
 */
public class ConsoleTalker extends AbstractNodeMain{
	// The ROS node configuration
	private NodeConfiguration nodeConfiguration;
	
	// The connected ROS node
	ConnectedNode connectedNode;
	
	// The publisher to send SpeechRecognitionMessages through
	Publisher<std_msgs.String> publisher;
	
	// The topic to send text input to
	private static final String topicName = "moving_test_agent_1_speech_recognition";
	
	
	/**
	 * Create the ConsoleTalker object and start the ROS node
	 * @param args
	 */
	public static void main(String[] args) {
		ConsoleTalker ct = new ConsoleTalker("http://10.5.0.1:11311");
		
		NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
		nodeMainExecutor.execute(ct, ct.nodeConfiguration);
	}
	
	
	/**
	 * Init the ROS node
	 * 
	 * @param rosURL					The ROS core URL
	 */
	public ConsoleTalker(String rosURL) {
		URI rosMaster = URI.create(rosURL);		
		 nodeConfiguration = null;
		try {
			java.net.Socket socket = new java.net.Socket(rosMaster.getHost(), rosMaster.getPort());
			java.net.InetAddress local_network_address = socket.getLocalAddress();
			nodeConfiguration = NodeConfiguration.newPublic(local_network_address.getHostAddress(), rosMaster);
		} catch (UnknownHostException e) { 
			e.printStackTrace();
		} catch (IOException e) {		
			e.printStackTrace();
		}
	}

	
	/**
	 * Generate random name for the ConsoleTalker
	 */
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("r5cop_wp24_agentinterface/console_talker/"+Math.round(Math.random()*1000));
	}

	
	/**
	 * Start the node
	 */
	@Override
	public void onStart(final ConnectedNode connectedNode) {
		// Init publisher
		publisher = connectedNode.newPublisher(topicName, std_msgs.String._TYPE);
    
		// Start main loop
		connectedNode.executeCancellableLoop(new CancellableLoop() {
			// The console scanner
			private Scanner scanner; 

			@Override
			protected void setup() {
				// Init the scanner with std in
				scanner = new Scanner(System.in);
			}
	
			@Override
			protected void loop() throws InterruptedException {
				// Endless loop reading commands
		        String command = "";
		        while (true) {
		            System.out.println("-------------------------------------------");
		            System.out.print("> ");
		            command = scanner.nextLine();
		            
		            // Construct a new SpeechREcognitionMessage and send it to the target topic
		            std_msgs.String str = publisher.newMessage();
		            SpeechRecognitionMessage srm = new SpeechRecognitionMessage(getDefaultNodeName().toString(), topicName, command);
					str.setData(srm.toJson());
					publisher.publish(str);
					
					// Terminate on "exit" command and shutdown ROS node properly
					if (command.equals("exit")) {
		            	connectedNode.shutdown();
		            	System.exit(0);
		            }
		        }
			}
		});
		
		// Whenever SpeechRecognitionRegister messages is received, display it on std err
		Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber("SpeechRecognitionRegister", std_msgs.String._TYPE);
			subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
				@Override
				public void onNewMessage(std_msgs.String message) {
					System.err.println("Subscribe message received: ");
					System.err.println("-------------------------------------------");
					System.err.println(message.getData());
					System.err.println("-------------------------------------------");
				}
		    });
	}
}
