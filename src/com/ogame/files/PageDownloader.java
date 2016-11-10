package com.ogame.files;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;



public class PageDownloader {
	
	private BufferedReader bufferedReader = null;
	private HttpClient httpClient = new DefaultHttpClient();
		
	public String downloadPagePost(String url, List<NameValuePair> params) {
		HttpPost request = new HttpPost(url);		
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params);
			request.setEntity(entity);
		
			HttpResponse response= httpClient.execute(request);
			bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					
			StringBuffer stringBuffer = new StringBuffer("");
			String line = "";
			String LineSeparator = System.getProperty("line.separator");
			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line + LineSeparator);
			}
			bufferedReader.close();
			//System.out.println(stringBuffer.toString());
			return(stringBuffer.toString());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}finally{
			if(bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public String dowloadPageGet(String url) {
		HttpGet request = new HttpGet();
		try {
			request.setURI(new URI(url));
			
			HttpResponse response = httpClient.execute(request);
			bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			
			StringBuffer stringBuffer = new StringBuffer("");
			String line = "";
			String LineSeparator = System.getProperty("line.separator");
			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line + LineSeparator);
			}
			bufferedReader.close();
			
			return(stringBuffer.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		} finally {
			if(bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
