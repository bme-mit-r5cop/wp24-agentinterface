package ros_display;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.ros.message.MessageListener;
import org.ros.node.topic.Subscriber;

import static javax.swing.ScrollPaneConstants.*;

import agent.AbstractAgent;

public class ROSDisplayPanel extends JPanel {
	JTextArea area = new JTextArea(19,55);
	JScrollPane scrollPane = new JScrollPane(area);
	JLabel label;
	
	public ROSDisplayPanel(AbstractAgent agent, String topic, Color color) {
		if (topic.equals("")) {
				topic = "not_in_use";
		}
		
		label = new JLabel(topic);
		add(label);
		
		scrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
		
		area.setEditable(false);
		add(scrollPane);
		setBounds(0,0,300,300);
		setBackground(color);
		
		Subscriber<std_msgs.String> subscriber = agent.getAgentInterface().getConnectedNode().newSubscriber(topic, std_msgs.String._TYPE);
        subscriber.addMessageListener(new MessageListener<std_msgs.String>() {
          @Override
          public void onNewMessage(std_msgs.String message) {
        	  area.setText(area.getText()+"\n\n - - - - "+(new SimpleDateFormat("HH:mm:ss.SSS")).format(Calendar.getInstance().getTime())+" - - - - \n\n"+message.getData());
        	  area.setCaretPosition(area.getDocument().getLength());
          }
        });
	}
	
	public void reset() {
		area.setText("");
	}
	
}
