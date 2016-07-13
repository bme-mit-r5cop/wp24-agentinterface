/*
 * R5COP-WP24-AgentInterface
 */
package agentinterface;

import java.util.ArrayList;
import java.util.HashMap;


/**
 *	Class holding stat parameters
 *
 * @author Peter Eredics
 */
public class State {
	// The ID of the state
    private String stateName = "";
    
    // Valid transitions from this state
    private ArrayList<StateTransition> transitions = null;

    
    /**
     * Create new empty state
     * 
     * @param stateName					The name of the new state
     */
    public State(String stateName) {
        this.stateName = stateName;
        transitions = new ArrayList<StateTransition>();
    }
    
    
    /**
     * Add new valid transition
     * 
     * @param transition				The transition to add	
     */
    public void addTransition(StateTransition transition) {
        transitions.add(transition);
    }
    
    
    /**
     * Getter for the agent name
     * 
     * @return							The agent name
     */
    public String getName() {
        return stateName;
    }
    
    
    /**
     * Get the next state from the current state and the received message
     * 
     * @param ai				The AgentInterface
     * @param message			The received message
     * @return					The new state
     */
    public State getNewState(AgentInterface ai, String message) {
        // Search for a transition to fire
        for (int i=0;i<transitions.size();i++) {
            StateTransition transition = transitions.get(i);
            
            // Check if this transition can be fired
            if (transition.canFire(message)) {
            	State newState = transition.fire(ai); 
            	if (newState != null) {
            		// State changed
            		return newState;
            	}
                return this;
            }
        } 
         
        // Keep the current state
        AgentInterface.log("No mask found for command '"+message+"' in state'"+stateName+"'.");
        return this;
    }
    
    
    /**
     * Compare States based on their ID
     * 
     * @param other				The other state to compare to
     * @return					True if the states equal
     */
    public boolean equals(State other) {
    	return other.getName().equals(stateName);
    }
    
    
    /**
     * Return the list of transitions registered in this state
     * 
     * @return					The list of transitions
     */
    public ArrayList<StateTransition> getTransitions() {
    	ArrayList<StateTransition> copy = new ArrayList<StateTransition>(transitions);
    	return copy;
    }
}
