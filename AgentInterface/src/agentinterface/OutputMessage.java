/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agentinterface;

/**
 *
 * @author Peter Eredics
 */
public class OutputMessage {
    private String target, message;
    
    public OutputMessage(String target, String message) {
        this.target = target;
        this.message = message;
    }
    
    public String getTarget() {
        return target;
    }
    
    public String getMessage() {
        return message;
    }
}
