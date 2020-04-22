package edu.asu.dlsandy;
/*
 * Author: Douglas L. Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * This class handles secure communications with canvas.asu.edu using the ASU SSO system.
 * 
 * If authentication has not yet been performed, the class will open a small browser window
 * to allow the user to enter login credentials.  Since this is a standard browser interface,
 * if two-factor authentication is required, the user will also be prompted for that.
 * 
 * Once authentication has completed, the authentication handshakes (handled in cookies) are
 * managed by an HttpCookieManager.
 * 
 * @author Doug Sandy
 * Copyright (C) 2020, Arizona State University
 * All Rights Reserved
 */
public class RequesterSso {
	static CookieManager cookieManager = new CookieManager();
	static boolean authenticated = false;
	static int lastResponse = 0;
	
	/**
	 * based on the previous canvas response, extract the next page to read from canvas.
	 * 
	 * @param link_response - the response from the last request from canvas
	 * @return a string representing the next url to read to continue a multi-page canvas
	 *   response.  If the last page has been read, "" is returned.
	 */
    static private String getNextLink(String link_response) {
        if (link_response!=null) {
            String[] links = link_response.split(",");
            for (String l:links) {
                if (l.contains("next")) {
                    // there is a continuation link in this response header - 
                    // return the value
                    return l.replaceAll(".*<","").replaceAll(">.+","");
                }
            }
        }
        return "";
    }
    
    /**
     * perform an http Get request from canvas using the uri specified.  Automatically
     * perform authentication and Canvas response pagination requests if required.
     * 
     * @param uri - the uri to read from 
     * @return A string that represents the body of the response packet
     * @throws MalformedURLException
     * @throws IOException
     */
    static public String httpGetRequest(String uri) throws MalformedURLException, IOException {
        // process a single get request to canvas, using supplied credentials 
    	
    	// if not yet authenticated, try to authenticate
    	if (!authenticated) {
    		// attempt to authenticate
        	CookieHandler.setDefault(cookieManager);
        	AuthenticationDlg adlg = new AuthenticationDlg();
        	adlg.showAndWait();
        	if (!adlg.athenticationSuccess()) {
        		return null;
        	}
    	}
    	authenticated = true;
    	
    	String nextUrl = uri;
        StringBuffer response = new StringBuffer();
        
        // loop for each page in the response
        while (!nextUrl.isEmpty()) {
            URL obj;
            obj = new URL(nextUrl);
            
            // create the connection object
            HttpURLConnection con;
            con = (HttpURLConnection) obj.openConnection();
            
            // add cookies to the header - this includes any required authentication information
            if (cookieManager.getCookieStore().getCookies().size() > 0) {
                StringBuffer cookies = new StringBuffer();
            	int cookie_count = 0;
            	for (HttpCookie c:cookieManager.getCookieStore().getCookies()) {
                	if (cookie_count != 0) {
                		cookies.append(";");
                	}
            		cookie_count ++;
            		cookies.append(c.toString());
                }
                con.setRequestProperty("Cookie",cookies.toString());
            }
            // request json responses
            con.setRequestProperty("Accept", "application/json");
            
            // Set request method for HTTP GET
            con.setRequestMethod("GET");

            // get the response
            lastResponse = con.getResponseCode();
            if (lastResponse != 200) return null;

            BufferedReader in;
            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;

            // concatenate the input pages, adding comma characters between
            // each page - strip any enclosing braces/parenthesis
            while ((inputLine = in.readLine()) != null) {
                String trimmed;
            	if (inputLine.startsWith("while(1);")) {
                	trimmed = inputLine.substring(10,inputLine.length()-1);
                } else {
                    trimmed = inputLine.substring(1,inputLine.length()-1); 
                }
                if (response.length()!=0) response.append(",");
                response.append(trimmed);
            }
            in.close();
            nextUrl = getNextLink(con.getHeaderField("Link"));
        }

        // wrap the result in the appropriate brackets
        if ((response.length()>0) && (response.charAt(0) != '{')) {
            response.insert(0,"{");
            response.append("}");
        } else {
            response.insert(0,"[");
            response.append("]");
        }

        return response.toString();  
    }

    /**
     * send a GET request through the canvas API.
     * 
     * @param uri - the resource to request (not including "https://canvas.asu.edu/api/vi/")
     * @return a JsonAbstractValue representing the results of the request
     * @throws MalformedURLException
     * @throws IOException
     */
    static public JsonAbstractValue apiGetRequest(String uri) throws MalformedURLException, IOException {
        // request the information from canvas
    	String strResponse = httpGetRequest("https://canvas.asu.edu/api/v1/"+uri);
        if (strResponse == null) {
        	return null;
        }
        
        // convert the JSON-coded response into JSON objects
        JsonResultFactory rf = new JsonResultFactory();
        JsonAbstractValue av = rf.build(strResponse);
        return av;
    }
 
}
