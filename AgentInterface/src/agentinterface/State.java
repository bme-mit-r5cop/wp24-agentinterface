/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agentinterface;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Peter Eredics
 */
public class State {
    private String stateName = "";
    private ArrayList<StateTransition> transitions = null;
    
    public State(String stateName) {
        this.stateName = stateName;
        transitions = new ArrayList<StateTransition>();
    }
    
    public void addTransition(StateTransition transition) {
        transitions.add(transition);
    }
    
    public String getName() {
        return stateName;
    }
    
    public State getNewState(String message) {
        // Search for a transition to fire
        for (int i=0;i<transitions.size();i++) {
            StateTransition transition = transitions.get(i);
            if (transition.canFire(message)) {
                return transition.fire();
            }
        } 
         
        // Keep the current state
        AgentInterface.log("No mask found for command '"+message+"' in state'"+stateName+"'.");
        return this;
    }
}
