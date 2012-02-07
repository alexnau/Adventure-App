package com.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class HttpHelper {	
	/**
	 * Performs an HTTP GET request on the specified url with the specified parameters.
	 * @param base_url - the URL to GET from
	 * @param parameters - the parameters to pass to the URL
	 * @return - the server's response
	 * @throws Exception 
	 */
	public static BufferedReader get(String base_url, HashMap<String, String> parameters) throws IOException {
		String url = base_url;
		url += "?";
		
		Iterator<Entry<String, String>> it = parameters.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
			url += pairs.getKey() + "=" + pairs.getValue();
			
			if (it.hasNext())
				url += "&";
		}
		
		URL u = new URL(url);
		URLConnection connection = u.openConnection();
		connection.connect();
    
		InputStream is = connection.getInputStream();

		return new BufferedReader(new InputStreamReader(is, "UTF-8"));
	}
	
	/**
	 * Performs an HTTP POST on the specified URL, using the map values as parameters and uploading an optional file f.
	 * @param url - the URL to perform the POST to
	 * @param values - the parameters for the POST
	**/
	public static BufferedReader post(String url, Map<String, String> values) throws IOException {
		URL Url = new URL(url);
		
		// create a boundary string
		String boundary = MultiPartFormOutputStream.createBoundary();
		URLConnection urlConn = MultiPartFormOutputStream.createConnection(Url);
		
		urlConn.setRequestProperty("Accept", "*/*");
		urlConn.setRequestProperty("Content-Type", 
				MultiPartFormOutputStream.getContentType(boundary));
		
		// set some other request headers...
		urlConn.setRequestProperty("Connection", "Keep-Alive");
		urlConn.setRequestProperty("Cache-Control", "no-cache");
		
		// no need to connect because getOutputStream() does it
		MultiPartFormOutputStream out = new MultiPartFormOutputStream(urlConn.getOutputStream(), boundary);
		
		// write a text field element
		Iterator<Map.Entry<String, String> > it = values.entrySet().iterator();
		
		// Load the values from the map
		while (it.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
			out.writeField(pairs.getKey().toString(), pairs.getValue().toString());
		}
		
		out.close();

		return new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
	}
}
