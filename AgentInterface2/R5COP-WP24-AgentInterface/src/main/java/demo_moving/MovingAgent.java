package demo_moving;

import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import acl.ManagementMessage;
import agent.AbstractAgent;
import agentinterface.AgentInterface;
import agentinterface.State;
import geometry_msgs.Vector3;

public class MovingAgent extends AbstractAgent {
	private static MovingAgent agent;
	
	private static Publisher<geometry_msgs.Twist> publisher;
	private static geometry_msgs.Twist msg, left, right;
	
	private static boolean isRunning = false;
	
	
	
	/**
	 * Create a new agent and execute it
	 * 
	 * @param args
	 */
	public static void main(String[] args) {        
		agent = new MovingAgent();
		agent.setRosURL("http://10.5.0.1:11311/");
		agent.setConfigFile("MovingAgent.json");
		agent.execute();
	}

	public State activateTrigger(AgentInterface ai, String code, String input) {
		System.out.println("Running trigger: '"+code+"' on input '"+input+"'.");
		
		if (code.equals("turn_left")) {
			publisher.publish(left);
			agent.getAgentInterface().sendText2SpeechMessage("Turning left.");
			
		} else if (code.equals("turn_right")) {
			publisher.publish(right);
			agent.getAgentInterface().sendText2SpeechMessage("Turning right.");
			
		} else if (code.equals("start")) {
			isRunning = true;
			agent.getAgentInterface().sendText2SpeechMessage("Agent stopping.");
			return agent.getAgentInterface().getStateByName("moving");
			
		} else if (code.equals("stop")) {
			isRunning = false;
			agent.getAgentInterface().sendText2SpeechMessage("Agent starting.");
			return agent.getAgentInterface().getStateByName("standing");
		}
		
		return null;
	}

	
	public void processUnhandledManagementMessages(ManagementMessage message) {
		System.out.println("Unhandled management message received: "+message.getContent());
	}
	
	
	public void onStart(ConnectedNode connectedNode) {
		publisher = connectedNode.newPublisher("cmd_vel", geometry_msgs.Twist._TYPE);
		msg = publisher.newMessage();
		
		// Movement commands
		Vector3 v = msg.getLinear();
		v.setX(2);
		v.setY(0);
		v.setZ(0);
		msg.setLinear(v);
		
		left = publisher.newMessage();
		v = left.getAngular();
		v.setZ(10);
		left.setAngular(v);
		
		right = publisher.newMessage();
		v = right.getAngular();
		v.setZ(-10);
		right.setAngular(v);
		
		// Automation thread
		Thread t = new Thread () {
			public void run() {
				while (true) {
					if (isRunning) publisher.publish(msg);
					try {
						Thread.sleep(500);
					} catch (Exception e) {}
				}
			}
		};
		(new Thread(t)).start();
	}

}
