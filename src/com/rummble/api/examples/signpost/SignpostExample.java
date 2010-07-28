package com.rummble.api.examples.signpost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.http.HttpParameters;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;



public class SignpostExample  {

	private static String consumerKey = "<enter here>";
	private static String consumerSecret = "<enter here>";
	private static String accessToken = "<set up with calls below or enter here>";
	private static String accessSecret = "<set up with calls below or enter here>";
	
	private static final String HOST = "http://api.rummble.com";
	
	public static void getAccessToken()
	{
		// create a consumer object and configure it with the access
        // token and token secret obtained from the service provider
        OAuthConsumer consumer = new DefaultOAuthConsumer(consumerKey,consumerSecret);
        OAuthProvider provider = new DefaultOAuthProvider(
        		HOST + "/oauth/request_token", HOST+"/oauth/access_token",
        		HOST+ "/oauth/authorize");
     // fetches a request token from the service provider and builds
        // a url based on AUTHORIZE_WEBSITE_URL and CALLBACK_URL to
        // which your app must now send the user
        try {
			String url = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
        	//String url = provider.retrieveRequestToken(consumer, "http://news.bbc.co.uk");
			
			// follow instructions below by pasting url in a browser and authorizing the rummble app
			// after logging into Rummble.
			System.out.println("Go to the url: " + url);
			System.out.print("Enter oauth verifier:");
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String pin = br.readLine();
			
			provider.retrieveAccessToken(consumer, pin);
			
			System.out.println("token="+consumer.getToken());
			System.out.println("secret="+consumer.getTokenSecret());
			
			//
			// In a real app you would need to persist this information so it can be used in future
			//
			accessToken = consumer.getToken();
			accessSecret = consumer.getTokenSecret();
			
        } catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}
	
	
	private static String getQueryString(HttpParameters params)
	{
		StringBuilder sb = new StringBuilder();
		Iterator<String> iter = params.keySet().iterator();
		for (int i = 0; iter.hasNext(); i++) 
		{
			String param = iter.next();
			if (i > 0) {
				sb.append("&");
			}
			sb.append(params.getAsQueryString(param));
		}
		return sb.toString();
	}
	
	private static void defaultPostCall(HttpParameters params,boolean addAccessToken)
	{
		OAuthConsumer consumer = new DefaultOAuthConsumer(consumerKey,consumerSecret);
		// NOTE
		// Query string signing does not work with Request objects as URL can't be changed
		// consumer.setSigningStrategy(new QueryStringSigningStrategy());
		
		// NOTE (2)
		// Mashery (at present) does not like URLs containing query parameters so you will need to do a POST
		// or place in Header the values
		
		if (addAccessToken)
			consumer.setTokenWithSecret(accessToken, accessSecret);
		
		
		consumer.setAdditionalParameters(params);
		
		try
		{
			
			System.setProperty( "debug", "true" );
			
			URL methodURL = new URL(HOST);
			
			HttpURLConnection request = (HttpURLConnection) methodURL.openConnection();
			// Make it a POST
			request.setDoOutput(true);
			request.setRequestMethod("POST");
			
			// sign the request
			consumer.sign(request);
		
			// send the request
			request.connect();
			
			// Add our additional parameters specific to this API call
			// Here the example just adds the method
			String parameters = getQueryString(params);
			System.out.println("params = [" + parameters + "]");
			OutputStreamWriter writer = new OutputStreamWriter(request.getOutputStream());
            //write parameters
            writer.write(parameters);
            writer.flush();	
			
			// debug
			System.out.println(request.getResponseCode());
			System.out.println(request.getResponseMessage());
			for(String key : request.getHeaderFields().keySet())
				System.out.println(request.getHeaderField(key));
	        
			BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) 
			System.out.println(inputLine);
			in.close();
			
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			
		}
	}
	
	private static void apacheMethodCall(HttpParameters params,boolean addAccessToken)
	{
		// Uses GET
		try
		{
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumerKey,consumerSecret);
		if (addAccessToken)
			consumer.setTokenWithSecret(accessToken, accessSecret);
		
		consumer.setAdditionalParameters(params);
		String parameters = getQueryString(params);
		 // create an HTTP request to a protected resource
        HttpGet request = new HttpGet(HOST+"?" + parameters);

        // sign the request
        consumer.sign(request);

        // send the request
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);
        
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String inputLine;

		while ((inputLine = in.readLine()) != null) 
		System.out.println(inputLine);
		in.close();
        
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			
		}
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// uncomment to get an access token
		//
		//getAccessToken();
		
		// N.B.
		// don't add access token if calling a call to create a new user which isn't expecting it as then
		// you will get an invalid signature because the token passed and resultant signature will not be the same
		// as the server one created without a token.
		// e.g. 
		// user.createuser
		
		//
		// add the required parameters for the API call excluding oauth ones
		//
		HttpParameters params = new HttpParameters();
		params.put("method", "user.getLoggedInUser");
		
		//
		// Uncomment to do a call using default java HttpUrlConnection with signpost
		//
		//defaultPostCall(params,true);
		

		//
		// Uncomment to get Apache Commons method call with signpost
		//
		apacheMethodCall(params,true);
		
		
		
	}

}
