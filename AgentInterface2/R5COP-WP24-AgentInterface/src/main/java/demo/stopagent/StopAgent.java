package demo.stopagent;

import java.util.Scanner;

import agent.AbstractAgent;
import agentinterface.AgentInterface;
import agentinterface.State;
import demo.QRRobotAgent;
import simalatorInterface.SimulatorInterface2;

public class StopAgent extends AbstractAgent {
	public static StopAgent agent;
	public static SimulatorInterface2 smi;
	
	public static void main(String[] args) {
		// Init demo main agent
		if (args.length != 1) {
			System.out.println("Missing ROS URL as command line argument.");
			System.exit(-1);
		}
		init(args[0]);
		processConsoleInput();
	}
	
	public static StopAgent init(String rosURL) {
		log("Initializing.");
		
		// Init ROS node and agent interface
		agent = new StopAgent();
		agent.setRosURL(rosURL);
		agent.setConfigFile("RobotStopAgent.json");
		agent.execute();
		
		smi = new SimulatorInterface2(rosURL);
		
		return agent;
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
	
	public State activateTrigger(AgentInterface ai, String code, String input) {
		if (code.equals("stop_robot")) {
			smi.stopRobot();
		} else {
			System.out.print("Invalid trigger.");
		}
		
		return null;
	}
	
	
}