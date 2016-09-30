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
import simalatorInterface.SimplePlanner;
import simalatorInterface.SimulatorInterface2;

public class ItemCollectorAgent extends AbstractAgent {
	static ItemCollectorAgent agent;
	static ProductDB db;
	static boolean waiting = true;
	static boolean waitingForRobotMoving = false;
	static boolean isListDone = true;
	static boolean isPickupActive = false;
	static ArrayList<Product> shoppingList = new ArrayList<Product>();
	static int shoppingListIndex = -1;
	static SimulatorInterface2 smi;
	
	public static void main(String[] args) {   
		log("ItemCollectorAgent starting as standalone application.");
		if (args.length != 1) {
			System.out.println("Missing ROS URL as command line argument.");
			System.exit(-1);
		}
		init(args[0]);
		
		Scanner scanner = new Scanner(System.in);
		String command;
		while (true) {
            log("-------------------------------------------");
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
	
	public static ItemCollectorAgent init(String rosURL) {
		ItemCollectorAgent.objectName = "ItemCollectorAgent";
		log("ItemCollectorAgent initializing.");
		
		// Init ROS node and agent interface
		agent = new ItemCollectorAgent();
		agent.setRosURL(rosURL);
		agent.setConfigFile("ItemCollectorAgent.json");
		agent.execute();
		
		// Load product database
		db = new ProductDB();
		
		// Init simulator interface
		smi = new SimulatorInterface2(rosURL);
		
		return agent;
	}
	
	
	public void onStart(ConnectedNode connectedNode) {
		log("Connected to ROS.");
		
		Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber("ItemCollectorAgent_AddItem", std_msgs.String._TYPE);
	        subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
	          @Override
	          public void onNewMessage(std_msgs.String message) {
	        	  log("Processing message received on topic 'ItemCollectorAgent_AddItem'.");
	        	  
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
	        	  log("Processing message received on topic 'ItemCollectorAgent_PickupSuccess'.");
	        	  
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
			log("Item '"+code+"' has been successfully picked up.");
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			isPickupActive = false;
			shoppingListIndex++;
			shoppingListUpdated(false);
        } else {
        	log("Invalid pickup notification. Agent is not waiting for item '"+code+"' to be picked up. Ignoring.");
        }
	}
	
	public State activateTrigger(AgentInterface ai, String code, String input) {
		if (code.equals("start_collecting")) {
			log("Start collecting trigger received.");
			shoppingList = SimplePlanner.makeGreedyPickupPlan(shoppingList, null);
			
			waiting = false;
			shoppingListIndex = 0;
			if (shoppingListUpdated(true)) {
				return ai.getStateByName("collecting");
			} else {
				return null;
			}
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
		log("Adding item to the list: "+itemID);
		Product product = db.getDB().get(itemID);		
		
  	  	if (product != null) {
  	  		// Valid item -> let's add to the list
  	  		log("Product identified: "+product.getName());
  	  		
  	  		if (shoppingList.indexOf(product) != -1) {
  	  			log("Product already on the list. Skipping.");
  	  			agent.getAgentInterface().sendText2SpeechMessage("Item '"+product.getName()+"' is already on the shopping list.");
  	  		} else {
  	  			log("Product not yet on the list. Adding.");
  	  			
  	  			// Notify user
  	  			agent.getAgentInterface().sendText2SpeechMessage("Adding "+product.getType()+" called "+product.getName()+" to shopping list.");
  	  			
  	  			// Notify ShoppingListAgent
  	  			ProductMessage pm = new ProductMessage("inform","ItemCollectorAgent","ShoppingListAgent_Control", product,"item_added");
  	  			agent.getAgentInterface().publishMessage("ShoppingListAgent_Control", pm);
  	  			
  	  			
  	  			boolean listFinishedBefore = shoppingList.size() <= shoppingListIndex;
  	  			shoppingList.add(product);
  	  			if (shoppingList.size() == 1) {
  	  				log("First item added, shopping list not empty anymore. The user can start collecting.");
  	  				agent.getAgentInterface().changeState(agent.getAgentInterface().getStateByName("waiting_for_start_collecting"));
  	  			} else {
  	  				log("Shopping list item #"+shoppingList.size()+" added.");
  				}
  	  			
  	  			if (listFinishedBefore) {
  	  				log("Collecting was finished before. Adding new item restarts collecting.");
  	  				shoppingListUpdated(true);
  	  			} else {
  	  				log("The list was not yet finished.");
  	  			}
  	  		}
  	  	} else {
  	  		// Invalid item 
  	  		log("Invalid QR code received. No item found in database for code: "+itemID);
  	  		agent.getAgentInterface().sendText2SpeechMessage("Invali QR code: this item is not present in our catalog database. The scanned item code was '"+itemID+"'.");
  	  	}
	}

	
	public boolean shoppingListUpdated(boolean forceLast) {
		log("Shopping list update running.");
		
		if (waiting) {
			// No items on the shopping list yet
			log("No items in the list yet. Doing nothing.");
			return false;
		}

		if (isPickupActive) {
			// The pickup agent is active -> let's wait for it to finish
			log("Pickup is active. Doing nothing.");
			return false;
		}
		
		
		if (shoppingListIndex >= shoppingList.size() && (!forceLast)) {
			log("All items have already been collected. Instructing the user to go to the cashiers.");
			isListDone = true;
			agent.getAgentInterface().sendText2SpeechMessage("All items have been collected. Please proceed to the cashiers to pay!");
			smi.gotoExit();
			agent.getAgentInterface().sendText2SpeechMessage("Thank you for shopping with us. Good bye!");
			return true;
		} else {
			isListDone = false;
			Product actual = shoppingList.get(shoppingListIndex);
			log("Missing items are on the shopping list. Asking to user to pick up "+actual.getName()+".");
			agent.getAgentInterface().sendText2SpeechMessage("Please follow me to pick up the '"+actual.getType()+"' called "+actual.getName()+".");
			waitingForRobotMoving = true;
			
			if (smi.moveRobot(actual.getPosX(), actual.getPosY())) {
				// we have arrived
				robotArrived();
				return true;
			} else {
				waitingForRobotMoving = false;
				agent.getAgentInterface().sendText2SpeechMessage("Robot navigation has been interrupted.");
				agent.ai.changeState(agent.ai.getStateByName("waiting_for_start_collecting"));
				return false;
			}
		}
	}
	
	public void robotArrived() {
		log("The robot has arrived to its destination. Notifying PickupAgent.");
		waitingForRobotMoving = false;
		
		// Start pickup agent 
		ProductMessage pm = new ProductMessage("inform","ItemCollectorAgent","PickupAgent_Arrival", shoppingList.get(shoppingListIndex),"arrival_at");
		agent.getAgentInterface().sendMessage("PickupAgent_Arrival", pm.toJson());
		
		isPickupActive = true;
	}
	
	
	public void reset() {
		log("Resetting ItemCollectorAgent.");
		waiting = true;
		waitingForRobotMoving = false;
		isListDone = true;
		isPickupActive = false;
		shoppingList = new ArrayList<Product>();
		shoppingListIndex = -1;		
	}
}
