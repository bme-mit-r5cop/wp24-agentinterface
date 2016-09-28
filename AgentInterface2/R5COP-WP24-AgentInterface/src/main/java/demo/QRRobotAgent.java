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

public class QRRobotAgent extends AbstractAgent {
	public static QRRobotAgent agent;
	
	public static void main(String[] args) {
		// Init demo main agent
		if (args.length != 1) {
			System.out.println("Missing ROS URL as command line argument.");
			System.exit(-1);
		}
		init(args[0]);
		processConsoleInput();
	}
	
	public static void init(String rosURL) {
		log("Initializing.");
		
		// Init ROS node and agent interface
		agent = new QRRobotAgent();
		agent.setRosURL(rosURL);
		agent.setConfigFile("QRRobotAgent.json");
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
            } else {
	            	agent.getAgentInterface().processSpeechRecognitionMessage(command);
            } 
		}
	}
}
