package demo;

import java.util.Scanner;

import agent.AbstractAgent;
import agentinterface.AgentInterface;
import agentinterface.State;
import demo.common.ProductDB;
import demo.sub_agents.ItemCollectorAgent;
import demo.sub_agents.PickupAgent;
import demo.sub_agents.SalesAgent;
import demo.sub_agents.StopAgent;
import demo.sub_agents.WarehouseDisplay;
import ros_display.ROSDisplay;

public class MainAgent extends AbstractAgent {
	public static MainAgent agent;
	//public static WarehouseDisplay warehouseDisplay;
	public static ItemCollectorAgent itemCollectorAgent;
	public static PickupAgent pickupAgent;
	public static SalesAgent salesAgent;
	public static StopAgent stopAgent;
	
	public static void main(String[] args) {
		// Init demo main agent
		if (args.length != 1) {
			System.out.println("Missing ROS URL as command line argument.");
			System.exit(-1);
		}
		init(args[0]);
		
		// Create a warehouse display
		//warehouseDisplay = WarehouseDisplay.init();
		
		// Start ROSDisplay (debug)
		ROSDisplay.init(args[0]);
		safeSleep(1000);
		
		
		// Start an item collector agent
		itemCollectorAgent = ItemCollectorAgent.init(args[0]);
		
		// Start a pickup agent
		pickupAgent = PickupAgent.init(args[0]);
		
		// Start a sales agent
		salesAgent = SalesAgent.init(args[0]);
		
		// Robot stop agent
		stopAgent = StopAgent.init(args[0]);
		
		processConsoleInput();
	}
	
	public static void init(String rosURL) {
		ItemCollectorAgent.objectName = "DemoMainAgent";
		log("Initializing.");
		
		// Init ROS node and agent interface
		agent = new MainAgent();
		agent.setRosURL(rosURL);
		agent.setConfigFile("MainAgent.json");
		agent.execute();
	}
	
	
	public static void processConsoleInput() {
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
            		itemCollectorAgent.addItemToList("R5COP-"+itemID);
            	}
            } else if (command.startsWith("psay")) {
            	if (command.length()>4) {
	            	String text = command.substring(5, command.length());
	            	pickupAgent.getAgentInterface().processSpeechRecognitionMessage(text);
            	}
            } else if (command.startsWith("isay")) {
            	if (command.length()>4) {
	            	String text = command.substring(5, command.length());
	            	itemCollectorAgent.getAgentInterface().processSpeechRecognitionMessage(text);
            	}
            } else if (command.startsWith("ssay")) {
            	if (command.length()>4) {
	            	String text = command.substring(5, command.length());
	            	salesAgent.getAgentInterface().processSpeechRecognitionMessage(text);
	        	
            	}
            } else if (command.startsWith("msay")) {
            	if (command.length()>4) {
	            	String text = command.substring(5, command.length());
	            	agent.getAgentInterface().processSpeechRecognitionMessage(text);
            	}
        	 } else if (command.startsWith("tsay")) {
                 if (command.length()>4) {
     	            String text = command.substring(5, command.length());
 	            	stopAgent.getAgentInterface().processSpeechRecognitionMessage(text);
                 }
            	
             } else if (command.startsWith("pick")) {
            	if (command.length()>4) {
	            	String text = command.substring(5, command.length());
	            	pickupAgent.checkPickup("R5COP-"+text);
            	}
            } else if (command.startsWith("reset")) {
        		agent.getAgentInterface().sendManagementMessage("reset");
        		
            } else if (command.startsWith("terminate")) {
        		agent.getAgentInterface().sendManagementMessage("terminate");
        		
            }
		}
	}
	
	public State activateTrigger(AgentInterface ai, String code, String input) {
		if (code.equals("go_to_store")) {
			pickupAgent.goToStore();
		} else if (code.equals("go_to_warehouse")) {
			pickupAgent.goToWarehouse();
		}
		
		return null;
	}
	
	public static void safeSleep(int timeout) {
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
