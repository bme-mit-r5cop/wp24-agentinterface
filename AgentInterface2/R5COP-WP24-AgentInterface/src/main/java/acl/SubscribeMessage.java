/*
 * R5COP-WP24-AgentInterface
 */

package acl;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class for assembling and handling a subscribe message for the VoiceAgent to apply for 
 * recognized sentences that fit the set masks
 * 
 * @author Peter Eredics
 *
 */
public class SubscribeMessage extends ACLMessage {
	// The topic to send recognized sentences to 
	private String recognitionTopic = "";
	
	// The accepted patterns to subscribe for
	private ArrayList<AcceptedPattern> acceptedPatterns = new ArrayList<AcceptedPattern>();
	
	
	/**
	 * Constructor based on the JSON representation of the object
	 * 
	 * @param jsonString			The JSON string to load
	 */
	public SubscribeMessage(String jsonString) {
		// Load the standard message fields
		super(jsonString);
		
		// Load all accepted patterns
		JSONArray patternArray = json.getJSONArray("accepted_patterns");
        JSONObject patternObject = null;
        for (int i = 0; i < patternArray.length(); i++) {
        	patternObject = patternArray.getJSONObject(i);
        	addAcceptedPattern(patternObject.getString("regexp"),patternObject.getInt("priority"));
        }
	}
	
	
	/**
	 * Constructor from skratch 
	 * 
	 * @param sender				The sender node
	 * @param target				The target topic
	 * @param recognitionTopic		The topic to send recognized sentences to
	 */
	public SubscribeMessage(String sender, String target, String recognitionTopic) {
		super("subscribe", sender, target);
		setRecognitionTopic(recognitionTopic);
	}
	
	
	/**
	 * Convert into JSON string representation
	 */
	public String toJson() {
		String content = "  accepted_patterns : [\n";
		
		for (int i=0; i<acceptedPatterns.size(); i++) {
			content = content 
					+ "    {\n"
					+ "      regexp : \""+acceptedPatterns.get(i).getMask()+"\",\n"
					+ "      priority : \""+acceptedPatterns.get(i).getPriorty()+"\"\n"
					+ "    },\n";
					
		}
		
		content += "  ]\n";
		return toJson(content);
	}
	
	
	/**
	 * Setter for recognitionTopic
	 * 
	 * @param recognitionTopic			The new value
	 */
	public void setRecognitionTopic(String recognitionTopic) {
		this.recognitionTopic = recognitionTopic;
	}
	
	
	/**
	 * Getter for recognitionTopic
	 * 
	 * @return							The recognitionTopic value
	 */
	public String getRecognitionTopic() {
		return recognitionTopic;
	}
	
	
	/**
	 * Add new accepted pattern
	 * 
	 * @param regexp					The pattern to add
	 * @param priority					The priority
	 */
	public void addAcceptedPattern(String regexp, int priority) {
		acceptedPatterns.add(new AcceptedPattern(regexp, priority));
	}
	
	
	/**
	 * Add new accepted pattern
	 * 
	 * @param pattern					The pattern to add
	 */
	public void addAcceptedPattern(AcceptedPattern pattern) {
		acceptedPatterns.add(pattern);
	}
	
	
	/**
	 * Delete all registered patterns
	 */
	public void clearPatterns() {
		acceptedPatterns.clear();
	}
	
	
	/**
	 * Update pattern list based on transition list
	 *  
	 * @param transitions				The valid transitions to load patterns from
	 */
	public void updatePatternList(ArrayList<AcceptedPattern> patterns) {
		acceptedPatterns = patterns;
	}
}
