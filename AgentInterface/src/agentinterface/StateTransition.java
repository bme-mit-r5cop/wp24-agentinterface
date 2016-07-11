/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agentinterface;


import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 *
 * @author Peter Eredics
 */
public class StateTransition {
    private String mask = "";
    private Pattern maskPattern;
    private State newState = null;
    private ArrayList<OutputMessage> outputMessages = null;
    
    public StateTransition(String mask, State newState) {
        this.mask = mask;
        this.newState = newState;
        outputMessages = new ArrayList<OutputMessage>();
        maskPattern = Pattern.compile(mask);
    }
    
    public void addOutputMessage(String target, String message) {
        outputMessages.add(new OutputMessage(target, message));
    }
    
    public State fire() {
        for (int i=0;i<outputMessages.size();i++) {
            OutputMessage message = outputMessages.get(i);
            AgentInterface.sendMessage(message.getTarget(), message.getMessage());
        }
        return newState;
    }
    
    public String getMask() {
        return mask;
    }
    
    public boolean canFire(String message) {
        // Test if the regular expression fits the message
        Matcher m = maskPattern.matcher(message);
        return (m.find());
    }
}
