package test;


import demo.acl.Product;
import demo.acl.ProductMessage;
import demo.sub_agents.PickupAgentDisplay;

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
		
		//ProductDB db = new ProductDB();
		
		/*Product p = new Product("name","id","type",(float)100.1,0,800,600);
		
		ProductMessage pm = new ProductMessage("type", "sender", "target", p,"status");
		
		System.out.println(pm.toJson());

		ProductMessage pm2 = new ProductMessage(pm.toJson());
		
		Product p2 = pm2.getProduct();
		System.out.println(p2.getId());*/
		
		/*PickupAgentDisplay pd = new PickupAgentDisplay();
		pd.setVisible(true);*/
		
		/*for (int i=0; i<30; i++) {
			System.out.print((int)(Math.round(Math.random()*(3)-0.5))+" ");
		}*/
		
		PickupAgentDisplay d = new PickupAgentDisplay();
		d.setVisible(true);
	}

}
