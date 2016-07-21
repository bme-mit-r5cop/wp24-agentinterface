package demo.sales_agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import acl.GeneralMessage;
import agent.AbstractAgent;
import agentinterface.AgentInterface;
import agentinterface.State;
import demo.acl.Product;
import demo.common.ProductDB;
import demo.pickup_agent.PickupAgent;

public class SalesAgent extends AbstractAgent {
	private static SalesAgent agent;
	private static ProductDB db;
	private Product suggestedProduct = null;
	
	public static void main(String[] args) {
		System.out.println("SalesAgent standalone starting.");
		init();
		
		Scanner scanner = new Scanner(System.in);
		String command;
		while (true) {
            System.out.println("-------------------------------------------");
            System.out.print("> ");
            command = scanner.nextLine();
            
            if (command.equals("exit")) {
            	agent.terminate(); 
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
	
	public static SalesAgent init() {
		SalesAgent.objectName = "SalesAgent";
		System.out.println("SalesAgent initializing.");
		
		// Init ROS node and agent interface
		agent = new SalesAgent();
		agent.setRosURL("http://10.5.0.1:11311/");
		agent.setConfigFile("SalesAgent.json");
		agent.execute();
		
		// Load products that are discounted
		db = new ProductDB(true);
		
		return agent;
	}
	
	public State activateTrigger(AgentInterface ai, String code, String input) {
		if (code.equals("what_is_on_sale")) {
			if (db.getDB().size() == 0) {
				agent.getAgentInterface().sendText2SpeechMessage("I'm sorry, currently no product is on sale.");
			} else {			
				List<Product> valuesList = new ArrayList<Product>(db.getDB().values());
				int randomIndex = new Random().nextInt(valuesList.size());
				suggestedProduct  = valuesList.get(randomIndex);
				
				agent.getAgentInterface().sendText2SpeechMessage("You can buy the "+suggestedProduct.getName()+" "+suggestedProduct.getType()+" with "+suggestedProduct.getDiscount()+" percent discount at the price of "+suggestedProduct.getPrice()+" euros. Do you want to buy one?");
			}
		} else if (code.equals("yes_to_buy")) {
			if (suggestedProduct != null) {
				System.out.println("The user wants to buy the item: "+suggestedProduct.getName());
				
				GeneralMessage gm = new GeneralMessage("sales_agent","ItemCollectorAgent_AddItem",suggestedProduct.getId());
				agent.getAgentInterface().publishMessage("ItemCollectorAgent_AddItem", gm);
				
				suggestedProduct = null;
			} else {
				System.out.println("The user said yes to buy an item, but no item was offered.");
			}
		} else if (code.equals("no_to_buy")) {
			System.out.println("The user said no to buy.");
			suggestedProduct = null;
		}
		
		// No state change possible for this agent
		return null;
	}
}
