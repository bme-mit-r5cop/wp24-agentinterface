package acl;

import org.json.JSONException;

public class ManagementMessage extends GeneralMessage{
	public ManagementMessage (String jsonString) throws JSONException {
		super(jsonString);
	}
	
	public ManagementMessage(String sender, String target, String content) {
		super(sender, target, content);
	}
}
