package agentinterface;

public class TriggerTransition extends Transition {
	// The identifier of the transition
    private String code = "";
    
    
    /**
     * Init StateTransition object
     * 
     * @param mask				The mask to valiadate messages against
     * @param priority			The priority
     * @param code				The code identifying the transition
     */
    public TriggerTransition(String mask, int priority, String code) {
        super(mask, priority);
        this.code = code;
    }
    
    

    /**
     * Fire the transition and send all registered output messages
     * 
     * @param ai				The AgentInterface object
     * @return					The new state to move into
     */
    public State fire(AgentInterface ai, String input) {
        return ai.getAgent().activateTrigger(ai, code, input);
    }
}
