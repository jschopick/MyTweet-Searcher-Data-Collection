package crawler;

// Jsoup
import org.jsoup.nodes.Document;
import org.jsoup.*;
// Gson
import com.google.gson.Gson;
import com.google.gson.JsonObject;
// Twitter API
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.URLEntity;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.TwitterObjectFactory;
// Other Libraries
import java.io.*;
import java.nio.file.Files;

public class streamTest {
	
private static Integer fileCount = 1;
	
	public static void main(String[] args) throws TwitterException {
	
        //Configures Authentication to twitter
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
			.setOAuthConsumerKey("BzKgbpBROkYMu2n3Jh6M7di9T")
			.setOAuthConsumerSecret("MBDKYG46xFawRCNyVmRQ0H6081UE9oTHeMlogyCGXtRIs5LYuA")
			.setOAuthAccessToken("932805344467632128-eyOBjVIlnV29nAyrauC56tVleBk1fnA")
			.setOAuthAccessTokenSecret("DTITX6UWKCZ3SC4o9tYqlFXOn6tTJ8IR5Vf1Hc0gNLBL2");
		cb.setJSONStoreEnabled(true);
		
		Gson gson = new Gson();
		
		StatusListener listener = new StatusListener(){
			
			public void onStatus(Status s) {	
				if(s.getGeoLocation() != null) {	
					String ss = TwitterObjectFactory.getRawJSON(s);
					JsonObject jsonObject = gson.fromJson(ss, JsonObject.class); // parse
					for(URLEntity url : s.getURLEntities()){
						String addr = url.getURL();
						Document temp = null;
						try {
							temp = Jsoup.connect(addr).get();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String title = temp.title();
						System.out.print("URL: " + url.getURL() + ", title: " + title);
						jsonObject.addProperty("linkTitle", title); // modify
					}
					
					// Append json info to json file
					String fs = System.getProperty("file.separator");
					String filename = "." + fs + "StoredTweets" + fs + "Document" + fileCount + ".json";
					File f = new File(filename);
					if(!fileAlreadyExists(filename)) { // If file doesn't exist, create new one.
						try {
							System.out.println("Creating file: " + filename);
							f.createNewFile();
							FileWriter fw = new FileWriter(filename);
							PrintWriter pw = new PrintWriter(fw);
							pw.print("[");
							pw.println(jsonObject);
							pw.close();
						} catch(IOException e) {
							// Do nothing
						}
					}
					// Add each jsonObject into the json file, followed by a comma.
					try(FileWriter appendToFile = new FileWriter(filename, true);
						BufferedWriter bw = new BufferedWriter(appendToFile);
						PrintWriter append = new PrintWriter(bw)) {
						// If this is the last one, add a square end bracket and increment fileCount.
						if(f.length() >= 10485760) {
							// If the next file hasn't been created already, end the current one
							String filename2 = "." + fs + "StoredTweets" + fs + "Document" + (fileCount + 1) + ".json";
							if(!fileAlreadyExists(filename2)) {
								append.print("]");
								System.out.println("Ending file: " + filename);
							} else { 
								// Adds to next file
								try(FileWriter appendToFile2 = new FileWriter(filename2, true);
										BufferedWriter bw2 = new BufferedWriter(appendToFile2);
										PrintWriter app = new PrintWriter(bw2)) {
										app.println("," + jsonObject);
										app.close();
								} catch (IOException e) {
									// Do Nothing
								}							}
							fileCount++; // Move to next file name
						} else { // Adds to current file
							append.println("," + jsonObject);
						}
						System.out.println(" -> Document" + fileCount);
						append.close();
					} catch(IOException e) {
						// Do nothing
					}
				}
			}
			
			//To implement when needed
			public void onException(Exception arg0) {}
			public void onDeletionNotice(StatusDeletionNotice arg0) {}
			public void onScrubGeo(long arg0, long arg1) {}
			public void onStallWarning(StallWarning arg0) {}
			public void onTrackLimitationNotice(int arg0) {}
			
        };
		
	    //Calls TwitterStream
	    TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		twitterStream.addListener(listener);
		// sample() method internally creates a thread which manipulates TwitterStream
		// and calls these adequate listener methods continuously.
	
		twitterStream.sample();

	}
	
	private static boolean fileAlreadyExists(String fileName) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
		} catch(FileNotFoundException e) {
			return false;
		}
		return true;
	}

}