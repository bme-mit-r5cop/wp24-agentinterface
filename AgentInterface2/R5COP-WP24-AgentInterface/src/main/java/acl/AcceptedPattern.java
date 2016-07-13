/*
 * R5COP-WP24-AgentInterface
 */
package acl;


/**
 *	Accepted text pattern 
 *
 * @author Peter Eredics
 */
public class AcceptedPattern {
	// The string mask
    private String mask = "";
    
    // The priority of the transition / mask
    private int pririty = 0;
    
    
    /**
     * Init AcceptedPattern object
     * 
     * @param mask				The mask to valiadate messages against
     * @param priority			The priority
     */
    public AcceptedPattern(String mask, int priority) {
        this.mask = mask;
        this.pririty = priority;
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
