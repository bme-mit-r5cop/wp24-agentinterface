package simalatorInterface;

import java.util.Random;

import org.ros.concurrent.CancellableLoop;
import org.ros.internal.message.RawMessage;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Publisher;

import geometry_msgs.Point;
import geometry_msgs.Pose;
import geometry_msgs.PoseStamped;
import geometry_msgs.Quaternion;
import std_msgs.Header;

public class GoalSender {
	private String topic_name;
	
	private int seq = 1;
	private ConnectedNode connectedNode;

	public GoalSender(ConnectedNode cn) {
		topic_name = "move_base_simple/goal";
		
		connectedNode = cn;
	}

	public void sendGoal(double x, double y) {
		final Publisher<geometry_msgs.PoseStamped> publisher = connectedNode.newPublisher(topic_name,
				geometry_msgs.PoseStamped._TYPE);
		

		// geometry_msgs.PoseStamped goal = publisher.newMessage();
		geometry_msgs.PoseStamped goal = connectedNode.getTopicMessageFactory()
				.newFromType(geometry_msgs.PoseStamped._TYPE);

		Header head = connectedNode.getTopicMessageFactory().newFromType(std_msgs.Header._TYPE);
		head.setSeq(seq++);
		head.setFrameId("/map");
		goal.setHeader(head);
		geometry_msgs.Pose pose = connectedNode.getTopicMessageFactory().newFromType(geometry_msgs.Pose._TYPE);
		Point p = connectedNode.getTopicMessageFactory().newFromType(geometry_msgs.Point._TYPE);
		p.setX(x);
		p.setY(y);
		pose.setPosition(p);
		Quaternion q = connectedNode.getTopicMessageFactory().newFromType(geometry_msgs.Quaternion._TYPE);
		q.setW(1);
		pose.setOrientation(q);
		goal.setPose(pose);

		publisher.publish(goal);
		System.out.println("Goal(x="+x+", y="+y+") sent.");
	}

}

