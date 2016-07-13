/*
 * R5COP-WP24-AgentInterface
 */

package acl;

/**
 * Class for sending and receiving Text2Speech messages
 * 
 * @author Peter Eredics
 *
 */
public class Text2SpeechMessage extends ACLMessage {
	// The text to speak
	private String text = "";
	
	/**
	 * Constructor from JSON string
	 * 
	 * @param jsonString				The JSON string to load from
	 */
	public Text2SpeechMessage (String jsonString) {
		super(jsonString);
		text = json.getString("text");
	}
	
	
	/**
	 * Constructor from parameters
	 *  
	 * @param sender					The sender node
	 * @param target					The target topic
	 * @param text						The text to speak
	 */
	public Text2SpeechMessage(String sender, String target, String text) {
		super("request", sender, target);
		this.text = text; 
	}
	
	
	/**
	 * Convert into JSON string representation
	 */
	public String toJson() {
		return toJson("  text : \""+text+"\"");
	}
	
	
	/**
	 * Getter for the text field
	 * 
	 * @return							The text field
	 */
	public String getText() {
		return text;
	}
	
	
	/**
	 * Setter for the text field
	 * @param text						The text field
	 */
	public void setText(String text) {
		this.text = text;
	}
}
