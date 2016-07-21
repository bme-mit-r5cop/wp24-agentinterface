package demo.pickup_agent;

import java.util.Scanner;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import acl.GeneralMessage;
import agent.AbstractAgent;
import demo.acl.Product;
import demo.acl.ProductMessage;
import demo.common.ProductDB;

public class PickupAgent extends AbstractAgent {
	static PickupAgent agent = null;
	static ProductDB db = null;
	static boolean waiting = true;
	Product product = null;
	PickupAgentDisplay display;
	
	public static void main(String[] args) {
		System.out.println("PickupAgent standalone starting.");
		init();
		
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
	
	public static void init() {
		System.out.println("PickupAgent initializing.");
		
		// Init ROS node and agent interface
		agent = new PickupAgent();
		agent.setRosURL("http://10.5.0.1:11311/");
		agent.setConfigFile("PickupAgent.json");
		agent.execute();
		
		// Load product database
		db = new ProductDB();

		// Create agent display of boxes and QR codes
		agent.display = new PickupAgentDisplay();
		agent.display.setVisible(false);
	}
	
	public void onStart(ConnectedNode connectedNode) {
		System.out.println("PickupAgent connected to ROS.");
		
		// Subscribe to received arrival messages from ItemCollectorAgent
		Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber("PickupAgent_Arrival", std_msgs.String._TYPE);
	       subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
	         @Override
	         public void onNewMessage(std_msgs.String message) {
	       	     // Parse GeneralMessage
	             ProductMessage pm = new ProductMessage(message.getData());
	             System.out.println("PickupAgent_Arrival message received to item location :"+pm.getProduct().getId());
	             
	             // It the agent is not already collecting something start pickup
	             if (waiting) {
	            	 product = pm.getProduct();
	            	 startPickup();
	             } else {
	            	 System.out.println("PickupAgent is already in pickup process. Ignoring message.");
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
	            System.out.println("PickupAgent_PickUp message received with QR code: "+gm.getContent());
	              
	            // If we are waiting for pickup, let's process the box
	        	if (!waiting) {
	        		checkPickup(gm.getContent());
	        	} else {
	        		System.out.println("Not in pickup process. Ignoring message.");
	        	}
	        }
	      });
	}
	
	public void startPickup() {
		System.out.println("Starting pickup process.");
		
		// Now the agent is allowed to communicate with the user
		agent.getAgentInterface().changeState(agent.getAgentInterface().getStateByName("pickup_active"));
		
		// Notify ShoppingListAgent to activate the QR code reader
		ProductMessage shoppingListAgentNotifyMessage = new ProductMessage("inform","PickupAgent","ShoppingListAgent_Control",product,"at_product");
		agent.getAgentInterface().publishMessage("ShoppingListAgent_Control", shoppingListAgentNotifyMessage);

		// Instruct to user to look for the right box
		instructPickup();
		
		// Display the GUI showing the boxes with QR codes and wait for the ShoppingListAgent to sent the QR code of the correct box
		agent.display.setVisible(true);
	}
	
	public void instructPickup() {
		System.out.println("Instructing user to pick up the correct box.");
		agent.getAgentInterface().sendText2SpeechMessage("Please locate the box containing the "+product.getType()+" named "+product.getName()+", and read the QR code on the box! The correct product code is "+product.getId()+"!.");
	}
	
	public void checkPickup(String code) {
		if (code.equals(product.getId())) {
			// This is the product we are looking for
			System.out.println("Correct box is being picked up.");
			
			// Notify the ShoppingListAgent to display a tick in the list
			ProductMessage shoppingListAgentNotifyMessage = new ProductMessage("inform","PickupAgent","ShoppingListAgent_Control",product,"pickup_success");
			agent.getAgentInterface().publishMessage("ShoppingListAgent_Control", shoppingListAgentNotifyMessage);
			
			// Notify the user
			agent.getAgentInterface().sendText2SpeechMessage("This is the correct box, please put it on me!");
			
			// Hide GUI
			agent.display.setVisible(false);
			
			// Notify the ItemCollectorAgent about successfull pickup
			shoppingListAgentNotifyMessage.setTarget("PickupSuccess");
			agent.getAgentInterface().publishMessage("ItemCollectorAgent_PickupSuccess", shoppingListAgentNotifyMessage);
			
			// Disable user communication
			agent.getAgentInterface().changeState(agent.getAgentInterface().getStateByName("waiting_for_item_location"));
		} else {
			// This is not the correct product
			System.out.println("Incorrect box selected.");
			
			ProductMessage shoppingListAgentNotifyMessage = new ProductMessage("inform","PickupAgent","ShoppingListAgent_Control",product,"pickup_failure");
			agent.getAgentInterface().publishMessage("ShoppingListAgent_Control", shoppingListAgentNotifyMessage);
			
			// Notify the user about the failure and ask to try again
			agent.getAgentInterface().sendText2SpeechMessage("This is not the box you need.");
			instructPickup();
			
		}
	}
	
	public void reset() {
		waiting = true;
		product = null;
		agent.display.setVisible(false);
	}
}
