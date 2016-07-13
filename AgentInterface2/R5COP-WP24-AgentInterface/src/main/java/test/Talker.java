package test;
/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


import java.util.Random;

import org.apache.commons.logging.Log;
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
    topic_name = "SpeechRecognitionRegister2";
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