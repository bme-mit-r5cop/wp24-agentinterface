package demo.item_collector;

import java.util.ArrayList;
import java.util.Scanner;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import acl.GeneralMessage;
import acl.SpeechRecognitionMessage;
import agent.AbstractAgent;
import agentinterface.AgentInterface;
import agentinterface.State;
import demo.common.Product;
import demo.common.ProductDB;

public class ItemCollectorAgent extends AbstractAgent {
	static ItemCollectorAgent agent;
	static ProductDB db;
	static boolean waiting = true;
	static boolean isPickupActive = false;
	static ArrayList<Product> shoppingList = new ArrayList<Product>();
	static int shoppingListIndex = -1;
	
	public static void main(String[] args) {        
		init();
		
		Scanner scanner = new Scanner(System.in);
		String command;
		while (true) {
            System.out.println("-------------------------------------------");
            System.out.print("> ");
            command = scanner.nextLine();
            
            if (command.equals("exit")) {
            	agent.terminate();
            } else if (command.startsWith("item")) {
            	if (command.length()>4) {
            		String itemID = command.substring(5, command.length());
            		agent.addItemToList("R5COP-"+itemID);
            	}
            } else if (command.startsWith("said")) {
            	if (command.length()>4) {
	            	String text = command.substring(5, command.length());
	        		agent.getAgentInterface().processSpeechRecognitionMessage(text);
            	}
            }
		}
	}
	
	public static void init() {
		// Init ROS node and agent interface
		agent = new ItemCollectorAgent();
		agent.setRosURL("http://10.5.0.1:11311/");
		agent.setConfigFile("ItemCollectorAgent.json");
		agent.execute();
		
		// Load product database
		db = new ProductDB();
	}
	
	
	public void onStart(ConnectedNode connectedNode) {
		 Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber("ItemCollectorAgent_AddItem", std_msgs.String._TYPE);
	        subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
	          @Override
	          public void onNewMessage(std_msgs.String message) {
	        	  // Parse GeneralMessage
	              GeneralMessage gm = new GeneralMessage(message.getData());
	              
	              // Check item and add it to the list if exits
	        	  addItemToList(gm.getContent());
	          }
	        });
	}
	
	public State activateTrigger(AgentInterface ai, String code, String input) {
		if (code.equals("start_collecting")) {
			waiting = false;
			shoppingListUpdated();
			return ai.getStateByName("collecting");
		} else {
			return null;
		}
	}
	
	public void addItemToList(String itemID) {
		Product product = db.getDB().get(itemID);
  	  	if (product != null) {
  	  		// Valid item -> let's add to the list
  	  		if (shoppingList.indexOf(product) != -1) {
  	  			agent.getAgentInterface().sendText2SpeechMessage("Item '"+product.getName()+"' is already on the shopping list.");
  	  		} else {
  	  			agent.getAgentInterface().sendText2SpeechMessage("Adding item to shopping list: '"+product.getName()+"'");
  	  			shoppingList.add(product);
  	  			if (shoppingList.size() == 1) {
  	  				System.out.println("Shopping list not empty anymore.");
  	  				agent.getAgentInterface().changeState(agent.getAgentInterface().getStateByName("waiting_for_start_collecting"));
  	  			} else {
  	  				System.out.println("Shopping list item #"+shoppingList.size()+" added.");
  				}
  	  			shoppingListUpdated();
  	  		}
  	  	} else {
  	  		// Invalid item 
  	  		agent.getAgentInterface().sendText2SpeechMessage("Invali QR code: this item is not present in our catalog database. The scanned item code was '"+itemID+"'.");
  	  	}
	}

	
	public void shoppingListUpdated() {
		if (waiting) {
			// No items on the shopping list yet
			return;
		}

		if (!isPickupActive) {
			// The pickup agent is active -> let's wait for it to finish
			return;
		}
		
		// Go for the next item
		shoppingListIndex++;
		
		if (shoppingListIndex >= shoppingList.size()) {
			agent.getAgentInterface().sendText2SpeechMessage("All items have been collected. Please proceed to the cashiers to pay!");
		} else {
			Product actual = shoppingList.get(shoppingListIndex);
			agent.getAgentInterface().sendText2SpeechMessage("Please follow me to pick up the '"+actual.getName()+"' package.");
			
			// TODO
			// Trigger robot navigation to move to the location of the item:
			// actual.getPosX()
			// actual.getPosY()
			
			// Let's assume we have arrived
			robotArrived();
		}
	}
	
	public void robotArrived() {
		// Start pickup agent TODO
	}
}
