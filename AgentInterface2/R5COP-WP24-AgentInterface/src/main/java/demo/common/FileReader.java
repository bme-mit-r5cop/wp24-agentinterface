package demo.common;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileReader {
	 /**
     * Read file into String
     * 
     * @param path					The file to read
     * @param encoding				The encoding to use
     * @return						The file content as String
     * @throws IOException			Exception when something goes wrong
     */
    public static String readFile(String path, Charset encoding) throws IOException {
      byte[] encoded = Files.readAllBytes(Paths.get(path));
      return new String(encoded, encoding);
    }
    
    
    /**
     * Read file into String 
     * 
     * @param path					The file to read
     * @return						The file content as String
     * @throws IOException			Exception when something goes wrong
     */
    public static String readFile (String path) throws IOException {
    	return readFile(path,StandardCharsets.UTF_8);
    	
    }
}
