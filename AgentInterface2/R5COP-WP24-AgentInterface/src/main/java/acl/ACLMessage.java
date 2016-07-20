package acl;

import org.json.JSONException;
import org.json.JSONObject;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

public abstract class ACLMessage {
	protected String sender = "";
	protected String target = "";
	protected String type = "unknown";
	protected JSONObject json;
	
	public abstract String toJson();
	
	public ACLMessage(String jsonString) throws JSONException {
		//System.err.println("Processing mesage: \n"+jsonString);
		json = new JSONObject(jsonString);
		
		type = json.getString("type");
		sender = json.getString("sender");
		target = json.getString("target");
	}
	
	public String toJson(String content) {
		String json = "{\n"
				+ "  type : \""+type+"\",\n"
				+ "  sender : \""+sender+"\",\n"
				+ "  target : \""+target+"\",\n"
				+ content + "\n"
				+ "}\n";
		return json;
	}
	
	public ACLMessage(String type, String sender, String target) {
		this.type = type;
		this.sender = sender;
		this.target = target;
	}
	
	public String getSender() {
		return sender;
	}
	
	public String getTarget() {
		return target;
	}
	
	public String getType() {
		return type;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	public void setTarget(String target) {
		this.target = target;
	}
	
	public void send(ConnectedNode connectedNode) {
		Publisher<std_msgs.String> publisher = connectedNode.newPublisher(target, std_msgs.String._TYPE);
		std_msgs.String str = publisher.newMessage();        
        str.setData(toJson());
        publisher.publish(str);
	}
}
