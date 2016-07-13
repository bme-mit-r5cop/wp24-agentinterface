package test;

public class TalkerMain extends Main {

	public static void main(String[] args) {
		init();
		nodeMainExecutor.execute(new Talker(), nodeConfiguration);
	}

}
