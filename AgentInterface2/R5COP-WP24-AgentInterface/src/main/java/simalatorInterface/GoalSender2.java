package simalatorInterface;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import geometry_msgs.Point;
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

	boolean isMovePaused = false;
	double lastGoalX = 0;
	double lastGoalY = 0;
	
	public GoalSender2(ConnectedNode cn) {
		topic_name = "move_base_simple/goal";

		connectedNode = cn;

		publisher = connectedNode.newPublisher(topic_name, geometry_msgs.PoseStamped._TYPE);
		subscriber = connectedNode.newSubscriber("/move_base/result", move_base_msgs.MoveBaseActionResult._TYPE);
		cancelPublisher = connectedNode.newPublisher("move_base/cancel", actionlib_msgs.GoalID._TYPE);
		
		subscriber.addMessageListener(new MessageListener<move_base_msgs.MoveBaseActionResult>() {
			@Override
			public void onNewMessage(move_base_msgs.MoveBaseActionResult message) {
				String result = message.getStatus().getText();
				if(!isMovePaused){ //normal operation
					
					System.out.println("[SMI]Move result (normal): " + result);
					isMoveEnded = true;
					isMoveSuccess = result.contains("Goal reached");
				}else{
					System.out.println("[SMI]Move result (paused): " + result);
					isMoveEnded = false;
				}
			}
		});
	}
	
	public void pause() {
		isMovePaused = true;
		actionlib_msgs.GoalID cancelmsg = connectedNode.getTopicMessageFactory().newFromType(actionlib_msgs.GoalID._TYPE);
		cancelPublisher.publish(cancelmsg);
	}
	public void continueM() {
		isMovePaused = false;
		sendGoal(lastGoalX, lastGoalY, false);
	}
	public void cancel() {
		isMovePaused = false;
		actionlib_msgs.GoalID cancelmsg = connectedNode.getTopicMessageFactory().newFromType(actionlib_msgs.GoalID._TYPE);
		cancelPublisher.publish(cancelmsg);
		
	}
	public boolean sendGoal(double x, double y, boolean blocking) {
		lastGoalX = x;
		lastGoalY = y;
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
		System.out.println("[SMI]Goal(x=" + x + ", y=" + y + ") sent.");
		isMoveEnded = false;
		isMoveSuccess = false;
		if(blocking){
			while (!isMoveEnded) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if(isMoveSuccess){
			if(lastGoalX == x && lastGoalY == y){
				return true;
			} else {
				return false;
			}
		}
		return isMoveSuccess;

	}

}
