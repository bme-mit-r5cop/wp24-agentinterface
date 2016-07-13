package test;

public class ListenerMain extends Main {

	public static void main(String[] args) {
		init();
		nodeMainExecutor.execute(new Listener(), nodeConfiguration);
	}

}
