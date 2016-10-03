package demo.sub_agents;

import java.util.Scanner;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import acl.GeneralMessage;
import agent.AbstractAgent;
import agentinterface.AgentInterface;
import agentinterface.State;
import demo.acl.Product;
import demo.acl.ProductMessage;
import demo.common.ProductDB;

public class PickupAgent extends AbstractAgent {
	static PickupAgent agent = null;
	static ProductDB db = null;
	static boolean waiting = true;
	Product product = null;
	//PickupAgentDisplay pickupDisplay;
	//WarehouseDisplay warehouseDisplay;
	
	public static void main(String[] args) {
		System.out.println("PickupAgent standalone starting.");
		if (args.length != 1) {
			System.out.println("Missing ROS URL as command line argument.");
			System.exit(-1);
		}
		init(args[0]);
		
		Scanner scanner = new Scanner(System.in);
		String command;
		while (true) {
			System.out.println("-------------------------------------------");
            System.out.print("> ");
            command = scanner.nextLine();
            
            if (command.equals("exit")) {
            	agent.terminate();
            } else if (command.startsWith("qrcd")) {
            	if (command.length()>4) {
	            	String text = command.substring(5, command.length());
	            	agent.checkPickup("R5COP-"+text);
            	}
            } else if (command.startsWith("said")) {
            	if (command.length()>4) {
	            	String text = command.substring(5, command.length());
	        		agent.getAgentInterface().processSpeechRecognitionMessage(text);
            	}
            } else if (command.startsWith("reset")) {
        		agent.getAgentInterface().sendManagementMessage("reset");
            } else if (command.startsWith("terminate")) {
        		agent.getAgentInterface().sendManagementMessage("terminate");
            }
		}
	}
	
	public static PickupAgent init(String rosURL) {
		PickupAgent.objectName = "PickupAgent";
		System.out.println("PickupAgent initializing.");
		
		// Init ROS node and agent interface
		agent = new PickupAgent();
		agent.setRosURL(rosURL);
		agent.setConfigFile("PickupAgent.json");
		agent.execute();
		
		// Load product database
		db = new ProductDB();

		/*
		// Create agent display of boxes and QR codes
		agent.pickupDisplay = new PickupAgentDisplay();
		agent.pickupDisplay.setVisible(false);
		
		agent.warehouseDisplay = new WarehouseDisplay();
		agent.warehouseDisplay.setVisible(true);
		*/
		
		return agent;
	}
	
	public void onStart(ConnectedNode connectedNode) {
		log("PickupAgent connected to ROS.");
		
		// Subscribe to received arrival messages from ItemCollectorAgent
		Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber("PickupAgent_Arrival", std_msgs.String._TYPE);
	       subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
	         @Override
	         public void onNewMessage(std_msgs.String message) {
	       	     // Parse GeneralMessage
	             ProductMessage pm = new ProductMessage(message.getData());
	             log("PickupAgent_Arrival message received to item location :"+pm.getProduct().getId());
	             
	             // It the agent is not already collecting something start pickup
	             if (waiting) {
	            	 product = pm.getProduct();
	            	 startPickup();
	             } else {
	            	 log("PickupAgent is already in pickup process. Ignoring message.");
	             }
	         }
	       });
	       
	   // Subscribe to receive QR codes from ShoppingListAgent when a QR code is scanned before putting the box on the robot
       Subscriber<std_msgs.String> subscriber2 = connectedNode.newSubscriber("PickupAgent_Pickup", std_msgs.String._TYPE);
	      subscriber2.addMessageListener(new MessageListener<std_msgs.String>() {
	        @Override
	        public void onNewMessage(std_msgs.String message) {
	        	// Parse GeneralMessage
	            GeneralMessage gm = new GeneralMessage(message.getData());
	            log("PickupAgent_PickUp message received with QR code: "+gm.getContent());
	              
	            // If we are waiting for pickup, let's process the box
	        	if (!waiting) {
	        		log("Waiting is false: accepting pickup QR code.");
	        	} else {
	        		log("Waiting is true: we should ignore the QR code, but we don't ignore it now.");
	        	}
	            
	        		checkPickup(gm.getContent());
	        	/*} else {
	        		log("Not in pickup process. Ignoring message.");
	        		
	        		// Notify ShoppingListAgent
	        		ProductMessage shoppingListAgentNotifyMessage = new ProductMessage("inform","PickupAgent","ShoppingListAgent_Control",product,"pickup_failure");
	    			agent.getAgentInterface().publishMessage("ShoppingListAgent_Control", shoppingListAgentNotifyMessage);
	        	}*/
	        }
	      });
	}
	
	public void startPickup() {
		log("Starting pickup process.");
		waiting = false;
		
		// Now the agent is allowed to communicate with the user
		agent.getAgentInterface().changeState(agent.getAgentInterface().getStateByName("pickup_active"));
		
		// Notify ShoppingListAgent to activate the QR code reader
		ProductMessage shoppingListAgentNotifyMessage = new ProductMessage("inform","PickupAgent","ShoppingListAgent_Control",product,"at_product");
		agent.getAgentInterface().publishMessage("ShoppingListAgent_Control", shoppingListAgentNotifyMessage);

		// Instruct to user to look for the right box
		instructPickup();
		
		/*
		// Display the GUI showing the boxes with QR codes and wait for the ShoppingListAgent to sent the QR code of the correct box
		agent.pickupDisplay.setVisible(true);
		agent.warehouseDisplay.setVisible(false);
		*/
	}
	
	public void instructPickup() {
		log("Instructing user to pick up the correct box.");
		agent.getAgentInterface().sendText2SpeechMessage("Please locate the box containing the "+product.getType()+" named "+product.getName()+", and read the QR code on the box!");
	}
	
	public void checkPickup(String code) {
		if (code.equals(product.getId())) {
			// This is the product we are looking for
			log("Correct box is being picked up.");
			
			// Notify the ShoppingListAgent to display a tick in the list
			ProductMessage shoppingListAgentNotifyMessage = new ProductMessage("inform","PickupAgent","ShoppingListAgent_Control",product,"pickup_success");
			agent.getAgentInterface().publishMessage("ShoppingListAgent_Control", shoppingListAgentNotifyMessage);
			
			// Notify the user
			agent.getAgentInterface().sendText2SpeechMessage("This is the correct box, please put it on me!");
			
			/*
			// Hide GUI
			agent.pickupDisplay.setVisible(false);
			agent.warehouseDisplay.setVisible(true);
			*/
			
			// Notify the ItemCollectorAgent about successfull pickup
			shoppingListAgentNotifyMessage.setTarget("PickupSuccess");
			agent.getAgentInterface().publishMessage("ItemCollectorAgent_PickupSuccess", shoppingListAgentNotifyMessage);
			
			// Disable user communication
			agent.getAgentInterface().changeState(agent.getAgentInterface().getStateByName("waiting_for_item_location"));
			
			// Resume into waiting for item location
			waiting = true;
		} else {
			// This is not the correct product
			log("Incorrect box selected.");		
			
			// Notify the user about the failure and ask to try again
			agent.getAgentInterface().sendText2SpeechMessage("This is not the box you need.");
			instructPickup();
			
		}
	}
	
	public void reset() {
		waiting = true;
		product = null;
		/*agent.pickupDisplay.setVisible(false);
		agent.warehouseDisplay.setVisible(true);*/
	}
	
	public void goToStore() {
		/*agent.pickupDisplay.setVisible(false);
		agent.warehouseDisplay.setVisible(true);*/
	}
	
	public void goToWarehouse() {
		/*agent.pickupDisplay.setVisible(true);
		agent.warehouseDisplay.setVisible(false);*/
	}
	
	public State activateTrigger(AgentInterface ai, String code, String input) {
		if (code.equals("what_are_you_doing")) {
			agent.getAgentInterface().sendText2SpeechMessage("I'm waiting for you to select the appropriate box.");
			instructPickup();
		}
		return null;
	}
}
