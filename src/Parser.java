import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Parser {

	private static final String directory = "/Users/tlodge/Documents/clientlogs";
	private static final String separator = System.getProperty("file.separator");
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

	public static void main(String argv[]){
		parseFiles();
	}

	static String cleanClientJSON(String json){
		int index = json.indexOf("message");
		String tail = json.substring(index+"message".length());
		tail = tail.replace("message", "msg");
		String newjson = json.substring(0, index+"message".length())  + tail; 
		return newjson;
	}

	static String cleanClientState(String json, BufferedReader in){
		if (json.contains("ERROR_AFTER_STATE") || json.contains("ERROR_DOING_LOGIN")){
			try{
				String line;
				String remaining = "";
				do{
					line = in.readLine().trim();
					remaining += line.substring(0,line.length()-1);
				}while(!( line.trim().substring(line.length()-1, line.length()).equals("]")));
				
				json = json + remaining + "]";
			}catch(Exception e){
				
			}
		}
		json = json.replace("ClientState:", "ClientState:\"");
		json += "\"";
		return json;
	}

	
	static void parseIt(String json){
		try{
			JSONObject obj = new JSONObject(new JSONTokener("{"+json+"}"));
			/*Iterator keys = obj.keys();
			while (keys.hasNext()){
				System.err.println(keys.next());
			}*/
		}catch(JSONException e){
			System.err.println("json parse exception " + e.getMessage());
			System.err.println("json is " + json);
		}catch(Exception e){
			System.err.println("general exception " + e.getMessage());
			System.err.println("json is " + json);
		}
	}
	
	
	static void parseFiles(){
		File rootdir = new File(directory);
		String dirs[] = rootdir.list();
		String line;

		for (int i = 0; i < dirs.length; i++){

			String topdir = directory + separator + dirs[i];

			
			String[] files = new File(topdir).list();

			for (int j = 0; j < files.length; j++){

				BufferedReader in = null;

				try{
					System.out.println("parsing " + topdir+ separator + files[i]);
					in = new BufferedReader(new FileReader(topdir+ separator + files[i]));
				}
				catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}

				String json = null;

				try{
					while((line = in.readLine()) != null)		{


						String date =	DATE_FORMAT.format(	new Date(Long.parseLong(line.substring(0, line.indexOf(":")))));			
						json =  line.substring(line.indexOf(':') + 1, line.length());

						if (json.startsWith("ClientState")){
							json = cleanClientState(json, in);
							parseIt(json);
						}
						else if (json.startsWith("Client")){
							json = cleanClientJSON(json);
							parseIt(json);
						}
						else if (json.startsWith("LOCATION")){
							parseIt(json);
						}
						else if (json.startsWith("GameState")){
							parseIt(json);
						}
						else if (json.startsWith("BackgroundThread")){
							parseIt(json);
						}
						else if (json.startsWith("GameAction")){
							parseIt(json);
						}
						else if (json.startsWith("Activity")){
							parseIt(json);
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
}