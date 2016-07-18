/*
 * R5COP-WP24-AgentInterface
 */
package agentinterface;


import java.util.regex.Pattern;

import acl.AcceptedPattern;

import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 *	Transition from one state to another 
 *
 * @author Peter Eredics
 */
public class StateTransition extends Transition {
    // The next state
    private State newState = null;
    
    
    /**
     * Init StateTransition object
     * 
     * @param mask				The mask to valiadate messages against
     * @param newState			The next state
     * @param priority			The priority
     */
    public StateTransition(String mask, int priority, State newState) {
        super(mask, priority);
        this.newState = newState;
    }
    
    

    /**
     * Fire the transition and send all registered output messages
     * 
     * @param ai				The AgentInterface object
     * @return					The new state to move into
     */
    public State fire(AgentInterface ai, String input) {
    	sendOutpuMessages(ai);
        return newState;
    }
}
