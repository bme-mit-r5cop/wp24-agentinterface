/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.Random;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

/**
 * A simple {@link Publisher} {@link NodeMain}.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Talker extends AbstractNodeMain {
  private String topic_name;
  private long uid;
  public Talker() {
    topic_name = "Text2Speech";
    Random r = new Random();
    uid = r.nextLong();
  }

  public Talker(String topic)
  {
    topic_name = topic;
  }

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("rosjava_tutorial_pubsub/talker/" + Math.abs(uid));
  }

  @Override
  public void onStart(final ConnectedNode connectedNode) {
    final Publisher<std_msgs.String> publisher =
        connectedNode.newPublisher(topic_name, std_msgs.String._TYPE);

    // This CancellableLoop will be canceled automatically when the node shuts
    // down.
    connectedNode.executeCancellableLoop(new CancellableLoop() {
      private int sequenceNumber;      

      @Override
      protected void setup() {
        sequenceNumber = 0;        
      }

      @Override
      protected void loop() throws InterruptedException {
    	  Thread.sleep(1000);
        std_msgs.String str = publisher.newMessage();        
        str.setData("Hello? " + sequenceNumber);
        System.out.println(str.getData());
        publisher.publish(str);
        
        
        sequenceNumber++;
        
        
      }
    });
        
  }
}