package demo_dummy;

import acl.GeneralMessage;
import agent.AbstractAgent;
import agentinterface.AgentInterface;
import agentinterface.State;

public class BatteryAgent extends AbstractAgent {
	private static BatteryAgent agent;
	
	public static void main(String[] args) {        
		agent = new BatteryAgent();
		agent.setRosURL("http://10.5.0.1:11311/");
		agent.setConfigFile("BatteryAgent.json");
		agent.execute();
	}
	
	public void processUnhandledManagementMessages(GeneralMessage message) {
		//ManagementMessage mm = new ManagementMessage(message.getContent());
		
		if (message.getContent().equals("battery_low")) {
			// Change the current state to battery_low and let the AgentInterface handle the communication
			agent.getAgentInterface().changeState(agent.getAgentInterface().getStateByName("battery_low"));
			
		} else if (message.getContent().equals("battery_charged")) {
			// The battery is charged up, let the AgentInterface handle the communication
			agent.getAgentInterface().changeState(agent.getAgentInterface().getStateByName("battery_charged"));
		}
	}
	
	
	public State activateTrigger(AgentInterface ai, String code, String input) {
		if (code.equals("turn_off_trigger")) {
			// The user selected to turn off the whole robot
			agent.getAgentInterface().sendManagementMessage("terminate");
		}
		
		return null;
	}	
}
