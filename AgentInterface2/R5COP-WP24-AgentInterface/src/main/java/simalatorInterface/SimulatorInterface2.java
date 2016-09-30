package simalatorInterface;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import actionlib_msgs.GoalStatusArray;
import demo.acl.Product;
import test.Talker;

public class SimulatorInterface2 extends AbstractNodeMain {
	public NodeConfiguration nodeConfiguration;
	public NodeMainExecutor nodeMainExecutor;
	private GoalSender2 goalSender;
	private long uid;

	boolean started = false;

	public SimulatorInterface2(String rosurl) {
		System.out.println("ROSURL: " + rosurl);
		init(rosurl);
		Random r = new Random();
		uid = r.nextLong();
		nodeMainExecutor.execute(this, nodeConfiguration);

		
		
	}
	
	public void init(String rosurl) {
		URI rosMaster = URI.create(rosurl);
		nodeConfiguration = null;
		try {
			java.net.Socket socket = new java.net.Socket(rosMaster.getHost(), rosMaster.getPort());
			java.net.InetAddress local_network_address = socket.getLocalAddress();
			nodeConfiguration = NodeConfiguration.newPublic(local_network_address.getHostAddress(), rosMaster);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
	}

	public boolean moveRobot(int goalX, int goalY) {
		return goalSender.sendGoal(goalX / 100.0, goalY / 100.0);
	}
	public boolean gotoExit() {
		return goalSender.sendGoal(-7.3, 8.9);
	}
	
	public void stopRobot() {
		goalSender.cancel();
		//return true;
	}

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("rosjava_tutorial_pubsub/talker/" + Math.abs(uid));
	}

	@Override
	public void onStart(ConnectedNode connectedNode) {
		
		goalSender = new GoalSender2(connectedNode);
		started = true;
	}

	@Override
	public void onShutdown(Node node) {
		// TODO Auto-generated method stub
		super.onShutdown(node);
	}

	public static void main(String[] args) {
		SimulatorInterface2 smi = new SimulatorInterface2("http://10.5.0.101:11311");
		while (!smi.started) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("starting movement");
		//smi.moveRobot(250, -270);
		//System.out.println("movement finisegg");
		smi.stopRobot();
	}
}

