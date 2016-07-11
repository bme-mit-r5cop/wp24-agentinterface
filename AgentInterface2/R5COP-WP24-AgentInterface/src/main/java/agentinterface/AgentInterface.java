package agentinterface;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 *
 * @author Peter Eredics
 */
public class AgentInterface {
    private static final boolean logging = true;
    private State currentState = null;
    
    private HashMap<String,State> stateMap;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: AgentInterface config_json");
            System.exit(-1);
        } 
        
        AgentInterface ai = new AgentInterface(args[0]);
        ai.consoleRun();        
    }
    
    public AgentInterface(String configFileName) {
        // Loading JSON file
        File configFile = new File(configFileName);
        if (!configFile.exists()) {
            System.out.println("Invalid  configuration file: "+configFileName);
            System.exit(-1);
        } else {
            log("Config file exists: "+configFileName);
        }
        
        // Reading config file contents
        String configContent = "";
        try {
            configContent = readFile(configFileName, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Falied to read configuration file. Exiting. ");
            System.exit(-1);
        }
        
        // Prepare state map
        stateMap = new HashMap<String,State>();
        
        // Parsing the JSON config
        JSONObject json = new JSONObject(configContent);
               
        
        // Loading empty states
        log ("Loading states...");
        JSONArray stateArray = json.getJSONArray("states");
        JSONObject state = null;
        String stateName = "";
        for (int i = 0; i < stateArray.length(); i++)
        {
            // Read state
            state = stateArray.getJSONObject(i);
            stateName = state.getString("name");
            
            //Create state object
            State currentState = new State(stateName);
            
            // Store state into the state map
            stateMap.put(stateName, currentState);
            log("State found in configuration: "+stateName);
        }
        
        // Reading again states to load transitions
        String mask = "";
        String newState = "";
        for (int stateIndex = 0; stateIndex < stateArray.length(); stateIndex++) {
            state = stateArray.getJSONObject(stateIndex);
            stateName = state.getString("name");
            log("Processing state transitions for "+stateName);
            
            // Read transitions under selected state
            JSONArray transitionArray = state.getJSONArray("transitions");
            for (int transitionIndex=0; transitionIndex<transitionArray.length(); transitionIndex++) {
                JSONObject transition = transitionArray.getJSONObject(transitionIndex);

                // Reading mandatory mask parameter
                mask = transition.getString("mask");
                    
                // Reading optional new state parameter
                try {
                    // New state present in config
                    newState = transition.getString("new_state");
                } catch (Exception e) {
                    // Keeping current state
                    newState = state.getString("name");
                }
                    
                // Create new transition object
                StateTransition transitionObject = new StateTransition(mask, stateMap.get(newState));
                log("  - adding transition with mask '"+mask+"' and newState '"+newState+"'");
                    
                // Read output messages specifications
                try {
                    JSONArray messageArray = transition.getJSONArray("output_messages");
                    for (int messageIndex=0; messageIndex<messageArray.length(); messageIndex++) {
                        JSONObject message = messageArray.getJSONObject(messageIndex);
                        String targetName = message.getString("target");
                        String messageText = message.getString("message");

                        transitionObject.addOutputMessage(targetName, messageText);
                        log("    - adding output message to target '"+targetName+"' and content '"+messageText+"'");
                    }
                } catch (Exception e) {
                    log("    - no output messages declared");
                }
                
                stateMap.get(state.getString("name")).addTransition(transitionObject);
            }
        }
        
        // Reading start state
        String startStateName = json.getString("start_state");
        log ("Starting state set to: "+startStateName);
        currentState = stateMap.get(startStateName);
        
        log("Configuration processing ended.");
    }
    
    static String readFile(String path, Charset encoding) 
    throws IOException 
    {
      byte[] encoded = Files.readAllBytes(Paths.get(path));
      return new String(encoded, encoding);
    }
    
    public void consoleRun() {
        Scanner scanner = new Scanner(System.in);
        String command = "";
        while (true) {
            System.out.println("-------------------------------------------");
            System.out.println("Current state: "+currentState.getName());
            System.out.print("> ");
            command = scanner.nextLine();
            processMessage(command);
        }
    }
    
    public static void log(String line) {
        if (!logging) return;
        String timeStamp = new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime());
        System.out.println("["+timeStamp+"] "+line);
    }
    
    public static void sendMessage(String target, String message) {
        System.out.println(" - - {"+target+"} - - > "+message);
    }
    
    public void processMessage(String message) {
        currentState = currentState.getNewState(message);
    }
    
    public void exportCurrentMasks() {
        //TODO
    }
}
