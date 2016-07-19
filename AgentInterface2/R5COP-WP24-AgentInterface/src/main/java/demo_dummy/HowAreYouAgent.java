package demo_dummy;

import acl.ManagementMessage;
import agent.AbstractAgent;
import agentinterface.AgentInterface;
import agentinterface.State;
import demo_moving.MovingAgent;

public class HowAreYouAgent extends AbstractAgent {
	private static HowAreYouAgent agent;
	
	public static void main(String[] args) {        
		agent = new HowAreYouAgent();
		agent.setRosURL("http://10.5.0.1:11311/");
		agent.setConfigFile("HowAreYouAgent.json");
		agent.execute();
	}
}
