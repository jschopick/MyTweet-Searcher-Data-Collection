package Search;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.QueryBuilder;


public class search {
	
	public static void main(String[] args) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
		String fs = System.getProperty("file.separator");
		final String INDEX_DIRECTORY = "." + fs + "Index";
		System.out.println("Would you like to re-create index? (y/n)");
		Scanner scanner = new Scanner(System.in);
		String  answer = scanner.nextLine();
		if (answer.equals("y")) {
			System.out.println("enter index creation");
			createIndex(INDEX_DIRECTORY);
		}else {
			Analyzer analyzer = new StandardAnalyzer();
			Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
			 // Now search the index:
	        DirectoryReader indexReader = DirectoryReader.open(directory);
	        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
	        // Parse a simple query that searches for "text":
	        QueryParser lparser = new QueryParser("hashtags", analyzer);
	        Query query = lparser.parse("tbt");
	        int topHitCount = 100;
	        ScoreDoc[] hits = indexSearcher.search(query, topHitCount).scoreDocs;

	        // Iterate through the results:
	        for (int rank = 0; rank < hits.length; ++rank) {
	            Document hitDoc = indexSearcher.doc(hits[rank].doc);
	            System.out.println((rank + 1) + " (score:" + hits[rank].score + ") --> " +
	                               hitDoc.get("text") + ", hT" + hitDoc.get("hashtags"));
	           // System.out.println(indexSearcher.explain(query, hits[rank].doc));
	        }
	        indexReader.close();
	        directory.close();
		}
		
		
	}
	
	
	public static void createIndex(String INDEX_DIRECTORY) throws IOException, org.apache.lucene.queryparser.classic.ParseException{
		JSONParser parser = new JSONParser();
		Analyzer analyzer = new StandardAnalyzer();
        // Store the index in memory:
        //Directory directory = new RAMDirectory();
        // To store an index on disk, use this instead:
		
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
		
        Integer file_number = 1;
        
		try
		{
			String fs = System.getProperty("file.separator");
			String file = "." + fs + "StoredTweets" + fs + "Document" + file_number.toString() + ".json";
			System.out.println(file);
			Path path = Paths.get(file);
			while(Files.exists(path)) {
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;
				while((line = bufferedReader.readLine()) != null) {
					line = line.substring(1);
					if(!line.equals("")) {
						Object obj = parser.parse(line);
						JSONObject jsonObject = (JSONObject) obj;
						
						//Get Tweet ID
						String id = (String) jsonObject.get("id_str");
						
						//Get Tweet Geo-Location (Latitude and Longitude)
						JSONObject place = (JSONObject) jsonObject.get("place");
						String coord;
						if(place != null) {
							JSONObject bounding_box = (JSONObject) place.get("bounding_box");
							JSONArray coordinates = (JSONArray) bounding_box.get("coordinates");
							Iterator coordIter = coordinates.iterator();
							coord = String.valueOf(coordIter.next());
							coord = coord.substring(2,coord.indexOf("]") );
						} else {
							//Accounting for extending tweets
							JSONObject coordinates = (JSONObject) jsonObject.get("coordinates");
							coord = String.valueOf(coordinates.get(coordinates));
						}
												
						//Get Tweet Text
						String text = (String) jsonObject.get("text");
						
						//Get Tweet timestamp
						String timestamp = (String) jsonObject.get("timestamp_ms");
						String createdAt = (String) jsonObject.get("created_at");
						
						//Get Hashtags
						JSONObject entities = (JSONObject) jsonObject.get("entities");
						JSONArray hashtags_arr = (JSONArray) entities.get("hashtags");
						Iterator hashTagIter = hashtags_arr.iterator();
						String hashtags = "";
						while(hashTagIter.hasNext()) {
							JSONObject hash_json = (JSONObject) hashTagIter.next();
					        hashtags = hashtags + ((String) hash_json.get("text"));
					    }
						
						//Get link title
						String linkTitle = (String) jsonObject.get("linkTitle");
						if(linkTitle == null) {
							linkTitle = "";
						}
						
						//Get username
						JSONObject user = (JSONObject) jsonObject.get("user");
						String username = (String) user.get("screen_name");
						
						//System.out.println("id:" + id + ", geo: [" + coord + "]"
						//		+ ", text: " + text + ", timestamp: " + timestamp + ",hashtags: " + hashtags
						//		+ ", username: " + username + ", linkTitle: " + linkTitle);
						
						//Tweet twitterDoc = new Tweet(id, coord, text, timestamp, createdAt, hashtags, username, linkTitle);
						
						Document doc = new Document();
						doc.add(new Field("id", id, TextField.TYPE_STORED));
						doc.add(new Field("coord", coord, TextField.TYPE_STORED));
						doc.add(new Field("text", text, TextField.TYPE_STORED));
						doc.add(new Field("timestamp", timestamp, TextField.TYPE_STORED));
						doc.add(new Field("createdAt", createdAt, TextField.TYPE_STORED));
						doc.add(new Field("hashtags", hashtags, TextField.TYPE_STORED));
						doc.add(new Field("username", username, TextField.TYPE_STORED));
						doc.add(new Field("linkTitle", linkTitle, TextField.TYPE_STORED));
						
						indexWriter.addDocument(doc);
					}
				}
				bufferedReader.close();
				file_number = file_number + 1;
				file = "." + fs + "StoredTweets" + fs + "Document" + file_number.toString() + ".json";
				path = Paths.get(file);
				System.out.println("fileno: " + file_number);
			}	
			indexWriter.close();	
		}
		//catch (FileNotFoundException e) {e.printStackTrace();}
		//catch (IOException e) {e.printStackTrace();}
		catch (ParseException e) {e.printStackTrace();}
		catch (Exception e) {e.printStackTrace();}
		System.out.println("end index creation");
	}
}