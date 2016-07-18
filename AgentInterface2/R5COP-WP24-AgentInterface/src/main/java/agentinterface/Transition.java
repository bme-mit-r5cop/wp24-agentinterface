package agentinterface;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import acl.AcceptedPattern;

public abstract class Transition {
	// The string mask
    protected String mask = "";
    
    // The compiled pattern from the mask
    protected Pattern maskPattern;
    
    // The priority of the transition / mask
    protected int priority = 0;
    
    // The output messages to send when firing this transitions
    protected ArrayList<OutputMessage> outputMessages = new ArrayList<OutputMessage>();
    
    public Transition(String mask, int priority) {
    	// Set local variables
    	this.mask = mask;
        this.priority = priority;
        
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
    	return priority;
    }
    
    
    /**
     * Setter for the priority
     * 
     * @param priority			The new priority value
     */
    public void setPriority(int priority) {
    	this.priority = priority;
    }
    
    
    /**
     * Return the accepted pattern of the transition
     * @return					The accepted pattern
     */
    public AcceptedPattern getPattern() {
    	return new AcceptedPattern(mask, priority);
    }
    
    
    /**
     * Send all registered output messages
     * 
     * @param ai				The AgentInterface object
     */
    public void sendOutpuMessages(AgentInterface ai) {
    	for (int i=0;i<outputMessages.size();i++) {
            OutputMessage message = outputMessages.get(i);
            ai.sendMessage(message.getTarget(), message.getMessage());
        }
    }
    
    
    /**
     * Fire the transition
     * 
     * @param ai				The AgentInterface object
     * @param input				The user input
     * @return					The new state of the AgentInterface
     */
    public abstract State fire(AgentInterface ai, String input);
    
}
