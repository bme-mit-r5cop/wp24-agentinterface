package demo;

import java.util.Scanner;

import agent.AbstractAgent;
import agentinterface.AgentInterface;
import agentinterface.State;
import demo.common.ProductDB;
import demo.item_collector.ItemCollectorAgent;
import demo.pickup_agent.PickupAgent;
import demo.sales_agent.SalesAgent;
import demo.warehouse_display.WarehouseDisplay;
import ros_display.ROSDisplay;

public class MainAgent extends AbstractAgent {
	public static MainAgent agent;
	//public static WarehouseDisplay warehouseDisplay;
	public static ItemCollectorAgent itemCollectorAgent;
	public static PickupAgent pickupAgent;
	public static SalesAgent salesAgent;
	
	public static void main(String[] args) {
		// Init demo main agent
		init();
		
		// Create a warehouse display
		//warehouseDisplay = WarehouseDisplay.init();
		
		// Start ROSDisplay (debug)
		ROSDisplay.main(null);
		
		// Start an item collector agent
		itemCollectorAgent = ItemCollectorAgent.init();
		
		// Start a pickup agent
		pickupAgent = PickupAgent.init();
		
		// Start a sales agent
		salesAgent = SalesAgent.init();
		
		processConsoleInput();
	}
	
	public static void init() {
		ItemCollectorAgent.objectName = "DemoMainAgent";
		log("Initializing.");
		
		// Init ROS node and agent interface
		agent = new MainAgent();
		agent.setRosURL("http://10.5.0.1:11311/");
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

}
