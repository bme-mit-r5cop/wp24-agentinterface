package demo.item_collector;

import java.util.ArrayList;
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

public class ItemCollectorAgent extends AbstractAgent {
	static ItemCollectorAgent agent;
	static ProductDB db;
	static boolean waiting = true;
	static boolean waitingForRobotMoving = false;
	static boolean isListDone = true;
	static boolean isPickupActive = false;
	static ArrayList<Product> shoppingList = new ArrayList<Product>();
	static int shoppingListIndex = -1;
	
	public static void main(String[] args) {   
		System.out.println("ItemCollectorAgent starting as standalone application.");
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
            } else if (command.startsWith("pick")) {
            	if (command.length()>4) {
	            	String text = command.substring(5, command.length());
	            	agent.productPickupNotification(text);
            	}
            } else if (command.startsWith("reset")) {
        		agent.getAgentInterface().sendManagementMessage("reset");
            } else if (command.startsWith("terminate")) {
        		agent.getAgentInterface().sendManagementMessage("terminate");
            }
		}
	}
	
	public static void init() {
		System.out.println("ItemCollectorAgent initializing.");
		
		// Init ROS node and agent interface
		agent = new ItemCollectorAgent();
		agent.setRosURL("http://10.5.0.1:11311/");
		agent.setConfigFile("ItemCollectorAgent.json");
		agent.execute();
		
		// Load product database
		db = new ProductDB();
	}
	
	
	public void onStart(ConnectedNode connectedNode) {
		System.out.println("Connected to ROS.");
		
		Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber("ItemCollectorAgent_AddItem", std_msgs.String._TYPE);
	        subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
	          @Override
	          public void onNewMessage(std_msgs.String message) {
	        	  System.out.println("Processing message received on topic 'ItemCollectorAgent_AddItem'.");
	        	  
	        	  // Parse GeneralMessage
	              GeneralMessage gm = new GeneralMessage(message.getData());
	              
	              // Check item and add it to the list if exits
	        	  addItemToList(gm.getContent());
	          }
	        });
	
	        
	     Subscriber<std_msgs.String> subscriber2 = connectedNode.newSubscriber("ItemCollectorAgent_PickupSuccess", std_msgs.String._TYPE);
	        subscriber2.addMessageListener(new MessageListener<std_msgs.String>() {
	          @Override
	          public void onNewMessage(std_msgs.String message) {
	        	  System.out.println("Processing message received on topic 'ItemCollectorAgent_PickupSuccess'.");
	        	  
	        	  // Parse GeneralMessage
	              ProductMessage pm = new ProductMessage(message.getData());
	              
	              // Process notification
	              productPickupNotification(pm.getProduct().getId());
	          }
	        });
	}
	
	public void productPickupNotification(String code) {
		if (code.equals(shoppingList.get(shoppingListIndex).getId())) {
			// The actual item has been picked up
			System.out.println("Item '"+code+"' has been successfully picked up.");
			
			isPickupActive = false;
			shoppingListIndex++;
			shoppingListUpdated(false);
        } else {
        	System.out.println("Invalid pickup notification. Agent is not waiting for item '"+code+"' to be picked up. Ignoring.");
        }
	}
	
	public State activateTrigger(AgentInterface ai, String code, String input) {
		if (code.equals("start_collecting")) {
			System.out.println("Start collecting trigger received.");
			
			waiting = false;
			shoppingListIndex = 0;
			shoppingListUpdated(true);
			return ai.getStateByName("collecting");
		} else if (code.equals("what_are_you_doing")) {
			if (isPickupActive) {
				// Let the pickup agent handle the question
				return null;
			} else if (waitingForRobotMoving) {
				agent.getAgentInterface().sendText2SpeechMessage("I'm on my way to the next product location.");
				return null;
			} else if (isListDone) {
				agent.getAgentInterface().sendText2SpeechMessage("I have finished picking up all items from your shopping list. Now I'm waiting for you to pay at a cashier, or to select new items to your shopping list.");
				return null;
			}
			return null;
		} else {
			return null;
		}
	}
	
	public void addItemToList(String itemID) {
		System.out.println("Adding item to the list: "+itemID);
		Product product = db.getDB().get(itemID);		
		
  	  	if (product != null) {
  	  		// Valid item -> let's add to the list
  	  		System.out.println("Product identified: "+product.getName());
  	  		
  	  		if (shoppingList.indexOf(product) != -1) {
  	  			System.out.println("Product already on the list. Skipping.");
  	  			agent.getAgentInterface().sendText2SpeechMessage("Item '"+product.getName()+"' is already on the shopping list.");
  	  		} else {
  	  		System.out.println("Product not yet on teh list. Adding.");
  	  			agent.getAgentInterface().sendText2SpeechMessage("Adding "+product.getType()+" called "+product.getName()+" to shopping list.");
  	  			boolean listFinishedBefore = shoppingList.size() <= shoppingListIndex;
  	  			
  	  			shoppingList.add(product);
  	  			if (shoppingList.size() == 1) {
  	  				System.out.println("First item added, shopping list not empty anymore. The user can start collecting.");
  	  				agent.getAgentInterface().changeState(agent.getAgentInterface().getStateByName("waiting_for_start_collecting"));
  	  			} else {
  	  				System.out.println("Shopping list item #"+shoppingList.size()+" added.");
  				}
  	  			
  	  			if (listFinishedBefore) {
  	  				System.out.println("Collecting was finished before. Adding new item restarts collecting.");
  	  				shoppingListUpdated(true);
  	  			} else {
  	  				System.out.println("The list was not yet finished.");
  	  			}
  	  		}
  	  	} else {
  	  		// Invalid item 
  	  		System.out.println("Invalid QR code received. No item found in database for code: "+itemID);
  	  		agent.getAgentInterface().sendText2SpeechMessage("Invali QR code: this item is not present in our catalog database. The scanned item code was '"+itemID+"'.");
  	  	}
	}

	
	public void shoppingListUpdated(boolean forceLast) {
		System.out.println("Shopping list update running.");
		
		if (waiting) {
			// No items on the shopping list yet
			System.out.println("No items in the list yet. Doing nothing.");
			return;
		}

		if (isPickupActive) {
			// The pickup agent is active -> let's wait for it to finish
			System.out.println("Pickup is active. Doing nothing.");
			return;
		}
		
		
		if (shoppingListIndex >= shoppingList.size() && (!forceLast)) {
			System.out.println("All items have already been collected. Instructing the user to go to the cashiers.");
			isListDone = true;
			agent.getAgentInterface().sendText2SpeechMessage("All items have been collected. Please proceed to the cashiers to pay!");
		} else {
			isListDone = false;
			Product actual = shoppingList.get(shoppingListIndex);
			System.out.println("Missing items are on the shopping list. Asking to user to pick up "+actual.getName()+".");
			agent.getAgentInterface().sendText2SpeechMessage("Please follow me to pick up the '"+actual.getType()+"' called "+actual.getName()+".");
			waitingForRobotMoving = true;
			
			
			// TODO
			// Trigger robot navigation to move to the location of the item:
			// actual.getPosX()
			// actual.getPosY()
			AgentInterface.safeSleep(5000);
			
			// Let's assume we have arrived
			robotArrived();
		}
	}
	
	public void robotArrived() {
		System.out.println("The robot has arrived to its destination. Notifying PickupAgent.");
		waitingForRobotMoving = false;
		agent.getAgentInterface().sendText2SpeechMessage("We arrived to the item location.");
		
		// Start pickup agent 
		ProductMessage pm = new ProductMessage("inform","ItemCollectorAgent","PickupAgent_Arrival", shoppingList.get(shoppingListIndex),"arrival_at");
		agent.getAgentInterface().sendMessage("PickupAgent_Arrival", pm.toJson());
		
		isPickupActive = true;
	}
	
	
	public void reset() {
		System.out.println("Resetting ItemCollectorAgent.");
		waiting = true;
		waitingForRobotMoving = false;
		isListDone = true;
		isPickupActive = false;
		shoppingList = new ArrayList<Product>();
		shoppingListIndex = -1;		
	}
}
