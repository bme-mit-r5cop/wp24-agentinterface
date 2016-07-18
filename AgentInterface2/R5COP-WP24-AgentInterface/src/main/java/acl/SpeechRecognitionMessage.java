/*
 * R5COP-WP24-AgentInterface
 */

package acl;

import org.json.JSONException;

public class SpeechRecognitionMessage extends GeneralMessage {
	public SpeechRecognitionMessage (String jsonString) throws JSONException {
		super(jsonString);
	}
	
	public SpeechRecognitionMessage(String sender, String target, String content) {
		super(sender, target, content);
	}
}
