package com.happyRedis;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class HelperFunctions {
	
	/**
	 * Function to send JSON POst Request(Request with POST DATA as JSON)
	 * 
	 * @param url
	 * @param postJson
	 * @param timeout
	 * @return HashMap(With two keys , responseCode and time in request)
	 */
	public HashMap<String, String> sendJsonPostRequest(String url,
			String postJson, int timeout) {
		
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HashMap<String, String> result = new HashMap<String, String>();
		try {
			// Create request Object
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(postJson);
			
			// Set timeout
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeout).setConnectTimeout(timeout).build();
			request.setConfig(requestConfig);
			
			// Set Headers
			request.addHeader("content-type", "application/json");
			// Set Post Params
			request.setEntity(params);

			long startTime = System.currentTimeMillis();
			// Send Post Request
			HttpResponse response = httpClient.execute(request);
			

			long endTime = System.currentTimeMillis();
			long diff = endTime - startTime;

			int responseCode = response.getStatusLine().getStatusCode();

			result.put("responseCode", responseCode + "");
			result.put("totalTime", diff + "");
			 
			//Capture the Output
	        InputStream is = response.getEntity().getContent();
	        Reader reader = new InputStreamReader(is);
	        BufferedReader bufferedReader = new BufferedReader(reader);
	        StringBuilder builder = new StringBuilder();
	        while (true) {
	            try {
	                String line = bufferedReader.readLine();
	                if (line != null) {
	                    builder.append(line);
	                } else {
	                    break;
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	        System.out.println(builder);
			return result;
		} catch (ClientProtocolException ex) {
			result.put("responseCode", "0");
			result.put("totalTime", timeout + "");
			result.put("customException",
					"ClientProtocolException with timeout as " + timeout);
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			result.put("responseCode", "0");
			result.put("totalTime", timeout + "");
			result.put("customException",
					"Exception Occurs while sending Request : "
							+ ex.getClass().getName());
			return result;
		} finally {
			try {
				httpClient.close();
			} catch (Exception e) {
				result.put("responseCode", "0");
				result.put("totalTime", timeout + "");
			}
		}
		
	}
}
