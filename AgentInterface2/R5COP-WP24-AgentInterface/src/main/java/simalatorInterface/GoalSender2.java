package simalatorInterface;

import java.util.Random;

import org.ros.concurrent.CancellableLoop;
import org.ros.internal.message.RawMessage;
import org.ros.message.MessageListener;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import geometry_msgs.Point;
import geometry_msgs.Pose;
import geometry_msgs.PoseStamped;
import geometry_msgs.Quaternion;
import std_msgs.Header;

public class GoalSender2 {
	private String topic_name;
	private Publisher<geometry_msgs.PoseStamped> publisher;
	private Publisher<actionlib_msgs.GoalID> cancelPublisher;
	private Subscriber<move_base_msgs.MoveBaseActionResult> subscriber;
	private int seq = 1;
	private ConnectedNode connectedNode;

	boolean isMoveEnded = false;
	boolean isMoveSuccess = false;

	public GoalSender2(ConnectedNode cn) {
		topic_name = "move_base_simple/goal";

		connectedNode = cn;

		publisher = connectedNode.newPublisher(topic_name, geometry_msgs.PoseStamped._TYPE);
		subscriber = connectedNode.newSubscriber("/move_base/result", move_base_msgs.MoveBaseActionResult._TYPE);
		cancelPublisher = connectedNode.newPublisher("move_base/cancel", actionlib_msgs.GoalID._TYPE);
		
		subscriber.addMessageListener(new MessageListener<move_base_msgs.MoveBaseActionResult>() {
			@Override
			public void onNewMessage(move_base_msgs.MoveBaseActionResult message) {
				// log.info("I heard: \"" + message.getData() + "\"");
				String result = message.getStatus().getText();
				System.out.println("Move result: " + result);
				isMoveEnded = true;
				isMoveSuccess = result.contains("Goal reached");
			}
		});
	}
	public void cancel() {
		actionlib_msgs.GoalID cancelmsg = connectedNode.getTopicMessageFactory().newFromType(actionlib_msgs.GoalID._TYPE);
		cancelPublisher.publish(cancelmsg);
		
	}
	public boolean sendGoal(double x, double y) {

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
		System.out.println("Goal(x=" + x + ", y=" + y + ") sent.");
		isMoveEnded = false;
		isMoveSuccess = false;
		while (!isMoveEnded) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return isMoveSuccess;

	}

}
