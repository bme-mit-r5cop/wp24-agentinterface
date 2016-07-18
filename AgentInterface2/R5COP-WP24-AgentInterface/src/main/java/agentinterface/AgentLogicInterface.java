package agentinterface;

/**
 * The interface for custom implemented agent busingess logic to be used for trigger handling
 * 
 * @author Peter Eredics
 *
 */

public interface AgentLogicInterface {
	
	/**
	 * Implement trigger logic here.
	 * 
	 * @param ai					The AgentInterface executing the trigger
	 * @param code					The trigger code specified in the AgentInterface config
	 * @param input					The user input
	 * @return						The new state of the AgentInterface to move into
	 */
	public State activateTrigger(AgentInterface ai, String code, String input);
	
}
