package test;

import acl.SpeechRecognitionMessage;
import acl.SubscribeMessage;
import acl.Text2SpeechMessage;
import demo.common.ProductDB;

public class Test {

	public static void main(String[] args) {
		/*SubscribeMessage m = new SubscribeMessage("sender", "target", "recognitionTopic");
		m.addAcceptedPattern("regexp1", 1);
		m.addAcceptedPattern("regexp2", 2);
		
		String json = m.toJson();
		
		System.out.println(json);
		System.out.println("---");
		
		SubscribeMessage m2 = new SubscribeMessage(json);
		System.out.println(m2.toJson());*/
		
		/*Text2SpeechMessage m = new Text2SpeechMessage("sender", "target", "content");
		String json = m.toJson();
		System.out.println(json);
		System.out.println("---");
		
		Text2SpeechMessage m2 = new Text2SpeechMessage(json);
		
		System.out.println(m2.toJson());*/
		
		ProductDB db = new ProductDB();
	}

}
