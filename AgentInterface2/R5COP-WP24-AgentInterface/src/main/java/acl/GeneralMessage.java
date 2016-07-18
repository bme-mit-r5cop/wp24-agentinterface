package acl;

import org.json.JSONException;

public class GeneralMessage extends ACLMessage{
	// The sentence said by the user
		private String content = "";
		
		
		/**
		 * SpeechRecognitionMessage constructor from json string
		 * 
		 * @param jsonString
		 */
		public GeneralMessage (String jsonString) throws JSONException {
			super(jsonString);
			content = json.getString("content");
		}
		
		
		/**
		 * SpeechRecognitionMessage constructor from scratch
		 * 
		 * @param sender		The sender node
		 * @param target		The target topic
		 * @param content		The sentence said by the user
		 */
		public GeneralMessage(String sender, String target, String content) {
			super("inform", sender, target);
			this.content = content; 
		}
		
		
		/**
		 * Convert into JSON string
		 */
		public String toJson() {
			return toJson("  content : \""+content+"\"");
		}
		
		
		/**
		 * Getter for the content
		 * 
		 * @return				The content
		 */
		public String getContent() {
			return content;
		}
		
		
		/**
		 * Setter for the content
		 * 
		 * @param content		The content to set
		 */
		public void setContent(String content) {
			this.content = content;
		}
	}
