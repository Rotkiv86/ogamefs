package com.ogame.files;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

public class FileHandler {
	Context fileContext;

	public FileHandler(Context fileContext){
	    this.fileContext = fileContext;
	}
	
	public void writeDataToFile(String data) {
		try {
			FileOutputStream fileOutputStream = fileContext.openFileOutput("data.ini", Context.MODE_PRIVATE);
			fileOutputStream.write(data.getBytes());
			fileOutputStream.close();
		} catch(IOException e) {
			System.out.println("File write error: " + e);
		}
	}
	
	public String readDataFromFile() {
		String data = "";
		try {
			InputStream inputStream =  fileContext.openFileInput("data.ini");
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			
			String receiveString = "";
			StringBuilder stringBuilder = new StringBuilder();
			
			while((receiveString = bufferedReader.readLine()) != null) {
				stringBuilder.append(receiveString);
			}
			
			inputStream.close();
			
			data = stringBuilder.toString();
		} catch(Exception e) {
			System.out.println("File read error: " + e);
		}
		return data;
	}
	
	public String[] getUserData() {
		String[] fileData = readDataFromFile().split(";");
		String userData = "";
		for(int i = 0; i < fileData.length; i++) {
			if(fileData[i].split(":")[0].equals("user")) {
				userData += fileData[i] + ";";
			}
		}
		return userData.split(";");
	}	
	
	public String[] getPlanets() {
		String[] fileData = readDataFromFile().split(";");
		String planets = "";
		for(int i = 0; i < fileData.length; i++) {
			if(fileData[i].split(":")[0].equals("planet")) {
				planets += fileData[i] + ";";
			}
		}
		return planets.split(";");
	}
	
	public String[] getRoutes() {
		String[] fileData = readDataFromFile().split(";");
		String routes = "";
		for(int i = 0; i < fileData.length; i++) {
			if(fileData[i].split(":")[0].equals("route")) {
				routes += fileData[i] + ";";
			}
		}
		return routes.split(";");
	}

	public String[] getData(String row) {
		String[] fileData = readDataFromFile().split(";");
		String data = "";
		for(int i = 0; i < fileData.length; i++) {
			if(fileData[i].split(":")[0].equals(row)) {
				data += fileData[i] + ";";
			}
		}
		return data.split(";");
	}

	public void refreshDataFile(String removable, String newData) {
		String dataToFile = "";
		String data[] = this.readDataFromFile().split(";");
		for(int i = 0; i < data.length; i++) {
			if(!data[i].split(":")[0].equals(removable)) {
				dataToFile += data[i] + ";\n";
			}
		}
		dataToFile += newData;
		this.writeDataToFile(dataToFile);
	}
}
