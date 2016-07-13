/*
 * R5COP-WP24-AgentInterface
 */
package agentinterface;


import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 *	Transition from one state to another 
 *
 * @author Peter Eredics
 */
public class StateTransition {
	// The string mask
    private String mask = "";
    
    // The compiled pattern from the mask
    private Pattern maskPattern;
    
    // The next state
    private State newState = null;
    
    // The output messages to send when firing this transitions
    private ArrayList<OutputMessage> outputMessages = new ArrayList<OutputMessage>();
    
    // The priority of the transition / mask
    private int pririty = 0;
    
    
    /**
     * Init StateTransition object
     * 
     * @param mask				The mask to valiadate messages against
     * @param newState			The next state
     * @param priority			The priority
     */
    public StateTransition(String mask, State newState, int priority) {
        this.mask = mask;
        this.newState = newState;
        this.pririty = priority;
        
        // Compile the pattern
        maskPattern = Pattern.compile(mask);
    }
    
    
    /**
     * Add new output message
     * @param target			The target topic
     * @param message			The message content to send
     */
    public void addOutputMessage(String target, String message) {
        outputMessages.add(new OutputMessage(target, message));
    }
    
    
    /**
     * Fire the transition and send all registered output messages
     * 
     * @param ai				The AgentInterface object
     * @return					The new state to move into
     */
    public State fire(AgentInterface ai) {
        for (int i=0;i<outputMessages.size();i++) {
            OutputMessage message = outputMessages.get(i);
            ai.sendMessage(message.getTarget(), message.getMessage());
        }
        return newState;
    }
    
    
    /**
     * Getter for the mask
     * 
     * @return					The mask
     */
    public String getMask() {
        return mask;
    }
    
    
    /**
     * Check if this transition can be fired on the provided messages
     * 
     * @param message			The message to check for	
     * @return					True if the transition can be fired
     */
    public boolean canFire(String message) {
        // Test if the regular expression fits the message
        Matcher m = maskPattern.matcher(message);
        return (m.find());
    }
    
    
    /** 
     * Getter for the priority
     * @return					The priority value
     */
    public int getPriorty() {
    	return pririty;
    }
    
    
    /**
     * Setter for the priority
     * 
     * @param priority			The new priority value
     */
    public void setPriority(int priority) {
    	this.pririty = priority;
    }
}
