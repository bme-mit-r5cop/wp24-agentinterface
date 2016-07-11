package test;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;

import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

public class Main {

	/**
	 * A small example to start a ROS node.
	 * Other method would be to use the built-in RosRun application in rosjava, with the following command line arguments: 
	 * 
	 * hu.bme.mit.r5cop_wp24.Talker __ip:=10.5.0.6 __master:=http://10.5.0.1:11311/
	 *   where __ip is the IP of the network interface that is used to communicate with the ROS master. 
	 * 
	 */
	
	public static void main(String[] args) {		
		URI rosMaster = URI.create("http://10.5.0.1:11311/");		
		NodeConfiguration nodeConfiguration = null;
		try {
			java.net.Socket socket = new java.net.Socket(rosMaster.getHost(), rosMaster.getPort());
			java.net.InetAddress local_network_address = socket.getLocalAddress();
			nodeConfiguration = NodeConfiguration.newPublic(local_network_address.getHostAddress(), rosMaster);
		} catch (UnknownHostException e) { 
			e.printStackTrace();
		} catch (IOException e) {		
			e.printStackTrace();
		}
        
		NodeMainExecutor nodeMainExecutor = DefaultNodeMainExecutor.newDefault();
	    
		
		//nodeMainExecutor.execute(new Talker(), nodeConfiguration);
		nodeMainExecutor.execute(new Listener(), nodeConfiguration);
	}

}
