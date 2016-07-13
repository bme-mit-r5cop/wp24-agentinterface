/*
 * R5COP-WP24-AgentInterface
 */
package agentinterface;

/**
 * Class holding output message parameter
 * @author Peter Eredics
 *
 */
public class OutputMessage {
	// Paramteres are: target and message
    private String target, message;
    
    
    /**
     * Default constructor
     * 
     * @param target				The target to set
     * @param message				The message to set
     */
    public OutputMessage(String target, String message) {
        this.target = target;
        this.message = message;
    }
    
    
    /**
     * Getter for target
     * @return						The target value
     */
    public String getTarget() {
        return target;
    }
    
    
    /**
     * Getter for messages
     * @return						The message value
     */
    public String getMessage() {
        return message;
    }
}
