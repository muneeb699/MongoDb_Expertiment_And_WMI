package mongotest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mongodb.MongoClient;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;


import org.apache.commons.codec.binary.*;
import org.json.JSONException;
import org.bson.Document;
import static com.mongodb.client.model.Projections.*;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.MalformedURLException;
import static com.mongodb.client.model.Filters.*;
import javax.net.ssl.X509TrustManager;


import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class mong {
	
	/*
	 * This method made a connection to MongoDB and get the collection which have the data stored in it.
	 */
	
	static Logger logger = Logger.getLogger("myLogger");
	public static MongoCollection<Document> connection() throws IOException /*throws IOException*/ {
		@SuppressWarnings("resource")
		MongoClient client = new MongoClient("localhost", 27017);
		String connectPoint = client.getConnectPoint();
		System.out.println(connectPoint);
		MongoDatabase db = client.getDatabase("test");
		MongoCollection<Document> collection = db.getCollection("collection1");
		System.out.println(db);
		
		// This chunk of code will help to read & parse the JSON file and save it to Database
		
	//	final long NANOSEC_PER_SEC = 1000l*1000*1000;
		//long startTime = System.nanoTime();
	//	while ((System.nanoTime()-startTime)< 1*60*NANOSEC_PER_SEC){
		/*String File = "output1.json";
		BufferedReader reader = new BufferedReader(new FileReader(File));
		try {
			String json;
			while ((json = reader.readLine()) != null) {
		        collection.insertOne(Document.parse(json));
		    } 
		}finally {
			reader.close();}
		
		
	//}*/
		return collection;
	}
	 /* Credentials of the Server
	 */
	private String GetMyCredentials () {
	    String rawUser = "admin";
	    String rawPass = "admin";
	    String rawCred = rawUser+":"+rawPass;
	    String myCred = Base64.encodeBase64String(rawCred.getBytes());
	    return "Basic "+myCred;
	  }
	
	/*
	 * Get the JSONObject from the server through REDFISH. 
	 */
	public JsonObject authen() throws Exception {
		JsonObject myRestData = new JsonObject();
		
		TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
    };

    // Install the all-trusting trust manager
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

    // Create all-trusting host name verifier
    HostnameVerifier allHostsValid = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    // Install the all-trusting host verifier
    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		try{
			logger.log(Level.INFO, "Tring to connect server REDFISH interface");
		      URL myUrl = new URL("https://tao-i134.tao.qanet/redfish/v1/Systems/0");
		      URLConnection urlCon = myUrl.openConnection();
		      urlCon.setRequestProperty("Method", "GET");
		      urlCon.setRequestProperty("Accept", "application/json");
		      urlCon.setConnectTimeout(5000);
		      //set the basic auth of the hashed value of the user to connect
		      urlCon.addRequestProperty("Authorization", GetMyCredentials() );
		      InputStream is = urlCon.getInputStream();
		      InputStreamReader isR = new InputStreamReader(is);
		      BufferedReader reader = new BufferedReader(isR);
		      StringBuffer buffer = new StringBuffer();
		      String line = "";
		      while( (line = reader.readLine()) != null ){
		        buffer.append(line);
		      }
		      reader.close();
		      JsonParser parser = new JsonParser();
		      myRestData = (JsonObject) parser.parse(buffer.toString());
		       
		      return myRestData;
		       
		    }catch( MalformedURLException e ){
		      e.printStackTrace();
		      myRestData.addProperty("error", e.toString());
		      return myRestData;
		    }catch( IOException e ){
		      e.printStackTrace();
		      myRestData.addProperty("error", e.toString());
		      return myRestData;
		    }
	}
	
	/*
	 * Saving the Data in a file
	 */
	public void writer(JsonObject o) throws JSONException, IOException {
		try (FileWriter file = new FileWriter("file1.json")) {
			file.write(o.toString());
			System.out.println("Successfully Copied JSON Object to File...");
			System.out.println("\nJSON Object: " + o);
		}
		
	}
	/*
	 * Export() function will save all the documents in the collection to JSON File
	 */

	
	public static void Export(MongoCollection<Document> coll) throws IOException {
		
		Runtime.getRuntime().exec("C:\\\\Program Files\\\\MongoDB\\\\Server\\\\3.6\\\\bin\\\\mongoexport.exe --host localhost --port 27017 --db test --collection collection1 --out output.json");
	}
	
	/*
	 *This function retreive all data from the Collection
	 */
	public static void RetreiveAllData(MongoCollection<Document> coll) {
		MongoCursor<Document> cursor = coll.find().iterator();
		try {
			logger.info("Trying to retreive data from Database");
		    while (cursor.hasNext()) {
		        System.out.println(cursor.next().toJson());
		    }
		} finally {
			logger.log(Level.INFO,"Closing the cursor after getting the data");
		    cursor.close();
		}
	}
	
	/*
	 This projection bring the specific data from the table.Here this projection is equal to
	 	select Chassis from collection where ID = 10.172.8.37
	 	provide the ID, you will get the value.
	 */
	@SuppressWarnings("unchecked")
	public static void findByIP(MongoCollection<Document> coll,String SearchVar,String val, String SearchVal1,String SearchVal2, String SearchVal3, String SearchVal4) throws ParseException {
		
		/*Instant  instant = Instant.parse("2018-01-19T14:45:54.031Z"); //Pass your date.
		Date timestamp = Date.from(instant);
		ArrayList<Document> docs = new ArrayList();
		FindIterable it;
		String[] SeachValArray = SearchVal.split(",");
		for(int i = 0; i < SeachValArray.length; i++) {
			 it = coll.find(eq(SearchVar, val)).projection(fields(include(SeachValArray[i]), excludeId()));
			 it.into(docs);
		}
		
		long count = 0;*/
		
		long start = System.nanoTime();
		
		long count = 0;
		@SuppressWarnings("rawtypes")
		FindIterable it = coll.find(eq(SearchVar, val)).projection(fields(include(SearchVal1,SearchVal2,SearchVal3,SearchVal4), excludeId()));
		//FindIterable it = coll.find(filter).projection(fields(include("SerialNumber","PartNumber","RedfishCopyright"), excludeId()));
		long diff = System.nanoTime() - start;
		System.out.println(diff);
		
		@SuppressWarnings("rawtypes")
		ArrayList<Document> docs = new ArrayList();
		it.into(docs);
		 for (Document doc : docs) {
			 count++;
	           System.out.println(doc);
	        } 
		 System.out.println(count);
	}
	
	/*
	 * Overload Method to pass date to the function.
	 * */
	
@SuppressWarnings("unchecked")
public static void findByIP(MongoCollection<Document> coll,String SearchVar,Date val, String SearchVal1,String SearchVal2, String SearchVal3, String SearchVal4) throws ParseException {
		
		
		/*ArrayList<Document> docs = new ArrayList();
		FindIterable it;
		String[] SeachValArray = SearchVal.split(",");
		for(int i = 0; i < SeachValArray.length; i++) {
			 it = coll.find(eq(SearchVar, val)).projection(fields(include(SeachValArray[i]), excludeId()));
			 it.into(docs);
		}
		
		long count = 0*/;
		
		long start = System.nanoTime();
		
		long count = 0;
		@SuppressWarnings("rawtypes")
		FindIterable it = coll.find(eq(SearchVar, val)).projection(fields(include(SearchVal1,SearchVal2,SearchVal3,SearchVal4), excludeId()));
		//FindIterable it = coll.find(filter).projection(fields(include("SerialNumber","PartNumber","RedfishCopyright"), excludeId()));
		long diff = System.nanoTime() - start;
		System.out.println(diff);
		
		@SuppressWarnings({ "rawtypes" })
		ArrayList<Document> docs = new ArrayList();
		it.into(docs);
		 for (Document doc : docs) {
			 count++;
	           System.out.println(doc);
	        } 
		 System.out.println(count);
	}
/*
 * Overload Method to get all the data.
 * */

@SuppressWarnings("unchecked")
public static void findByIP(MongoCollection<Document> coll,String SearchVar,String val) throws ParseException {
	
	
	/*ArrayList<Document> docs = new ArrayList();
	FindIterable it;
	String[] SeachValArray = SearchVal.split(",");
	for(int i = 0; i < SeachValArray.length; i++) {
		 it = coll.find(eq(SearchVar, val)).projection(fields(include(SeachValArray[i]), excludeId()));
		 it.into(docs);
	}
	
	long count = 0*/;
	
	long start = System.nanoTime();
	
	long count = 0;
	@SuppressWarnings("rawtypes")
	FindIterable it = coll.find(eq(SearchVar, val)).projection(fields(excludeId()));
	//FindIterable it = coll.find(filter).projection(fields(include("SerialNumber","PartNumber","RedfishCopyright"), excludeId()));
	long diff = System.nanoTime() - start;
	System.out.println(diff);
	
	@SuppressWarnings("rawtypes")
	ArrayList<Document> docs = new ArrayList();
	it.into(docs);
	 for (Document doc : docs) {
		 count++;
           System.out.println(doc);
        } 
	 System.out.println(count);
}

	/*
	 This function Modifies one of the field 
	 */
	
	public static void Update(MongoCollection<Document> coll) {
		coll.deleteOne(eq("RedfishCopyright","Copyright 2014-2017 "));
        coll.updateOne(new Document("RedfishCopyright", "Muneeb "),  
                new Document("$set", new Document("RedfishCopyright", "Muneeb")));
        
        System.out.println("update Successfully");
	}
	
	
	public static void main(String[] args) throws JSONException, IOException, Exception {
			
		
		
		 mong mon = new mong();
		 JsonObject o = mon.authen();
		 mon.writer(o);
		 connection();
		 //Update(connection()); 
		// RetreiveAllData(connection());
		
		 //Export(connection());
		findByIP(connection(),"HostName","TAO-P034","BiosVersion","Description","Manufacturer","Model");
		 
		 
		
	}
}
