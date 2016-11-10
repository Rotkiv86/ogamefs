package com.ogame.files;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.String;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;


public class StoredParameters {
	private List<NameValuePair> params;
	private FileHandler fileHandler;
	private String[] user, routes;
	private java.lang.String universumNumber;
	
	public StoredParameters(Context fileContext) {
		fileHandler = new FileHandler(fileContext);
		user = fileHandler.getUserData()[0].split(":")[1].split("\\|");
		if(user[2].equals("Andromeda")) {
			universumNumber = "s101";
		}
		if(user[2].equals("Barym")) {
			universumNumber = "s102";
		}
		if(user[2].equals("Capella")) {
			universumNumber = "s103";
		}
		if(user[2].equals("Draco")) {
			universumNumber = "s104";
		}
		if(user[2].equals("Electra")) {
			universumNumber = "s105";
		}
		if(user[2].equals("Fromax")) {
			universumNumber = "s106";
		}
		if(user[2].equals("Gemini")) {
			universumNumber = "s107";
		}
		if(user[2].equals("Hydra")) {
			universumNumber = "s108";
		}
		
		java.lang.String[] _r = fileHandler.getRoutes();
		if(_r.length > 1) {
			routes = new String[_r.length];
			for(int a = 0; a < _r.length; a++) {
				routes[a] = _r[a].split("#")[0].split("-")[0].replaceAll("route:", "") + "#" + _r[a].split("#")[0].split("-")[1];
			}
		}
	}

	public java.lang.String getUniversumNumber() {
		return universumNumber;
	}
	
	public List<NameValuePair> getProfileParams() {
		params = new ArrayList<NameValuePair>();
		
		String profile[] = {
			"kid|",
			"uni|" + universumNumber + "-hu.ogame.gameforge.com",
			"login|" + user[0],
			"pass|" + user[1]
		};
		return setParams(profile);
	}
	
	public List<NameValuePair> getFleetShipNumbers(String _page, String _planet) {
		//am203 Big Carrier
		//am209 Garbage Collector
		
		Pattern shipsPattern = Pattern.compile("shipsChosen\",\\s(\\d+),(\\d+)");
		Matcher shipsMatcher = shipsPattern.matcher(_page);
		
		java.lang.String params = switchPlanet(_planet) + "," +
				"type|1," +
				"mission|0," +
				"speed|10,";
		while(shipsMatcher.find()) {
			params += "am" + shipsMatcher.group(1) + "|" + shipsMatcher.group(2) + ",";
		}
		params = params.replaceAll(",$", "");
		return setParams(params.split(","));
	}
	
	public List<NameValuePair> getFleetShipNumbersToShipping(String _page, String[] fD) {
		
		int remainIron = Integer.parseInt(fD[2]);
		int remainCrytal = Integer.parseInt(fD[3]);
		int remainDeuterium = Integer.parseInt(fD[4]);
		int actualIron = 0;
		int actualCrystal = 0;
		int actualDeuterium = 0;
		
		Pattern resourcesPattern = Pattern.compile("actual\":(\\d+),\"max");
		Matcher resourcesMatcher = resourcesPattern.matcher(_page);
		int cnt = 0;
		while(resourcesMatcher.find()) {
			switch(cnt) {
				case 0:
					actualIron = Integer.parseInt(resourcesMatcher.group(1));
					break;
				case 1:
					actualCrystal = Integer.parseInt(resourcesMatcher.group(1));
					break;
				case 2:
					actualDeuterium = Integer.parseInt(resourcesMatcher.group(1));
					break;
			}
			cnt++;
		}
		
		int allResources = (actualIron - remainIron < 0 ? 0 : actualIron - remainIron) + (actualCrystal - remainCrytal < 0 ? 0 : actualCrystal - remainCrytal) + (actualDeuterium - remainDeuterium < 0 ? 0 : actualDeuterium - remainDeuterium);
		int bigCarriers = (int)Math.ceil(allResources / 25000.0);
		
		String params = fD[1].replaceAll("(\\d+):(\\d+):(\\d+)", "galaxy|$1,system|$2,position|$3") + "," +
				"type|1," +
				"mission|0," +
				"speed|10," +
				"am203|" + bigCarriers;

		return setParams(params.split(","));
	}
	
	public List<NameValuePair> loadResourcesToShipping(String _page, String[] fD) {
		int remainIron = Integer.parseInt(fD[2]);
		int remainCrytal = Integer.parseInt(fD[3]);
		int remainDeuterium = Integer.parseInt(fD[4]);
		
		Pattern tokenRule = Pattern.compile("'token'\\s*value='(.*?)'");
		Matcher tokenMatcher = tokenRule.matcher(_page);

		String token = "";
		if(tokenMatcher.find()) {
			token = tokenMatcher.group(1);
		}
		
		Pattern hiddenPattern = Pattern.compile("name=\"([^\"]*)\"(?:\\sid=\"[^\"]*\"|)(?:\\stype=\"hidden\"|)\\svalue=\"(\\d*)\"");
		Matcher hiddenMatcher = hiddenPattern.matcher(_page);
		String params = "";
		while(hiddenMatcher.find()) {
			if(!hiddenMatcher.group(1).equals("retreatAfterDefenderRetreat")
					|| !hiddenMatcher.group(1).equals("mission")
					|| !hiddenMatcher.group(1).equals("speed")) {
				params += hiddenMatcher.group(1) + "|" + hiddenMatcher.group(2) + ",";
			}
		}

		params = "holdingtime|1," +
				"expeditiontime|1," +
				"token|" + token  + "," + 
				params + "," +
				"mission|3,speed|10,";
		
		Pattern resourcesPattern = Pattern.compile("actual\":(\\d+),\"max");
		Matcher resourcesMatcher = resourcesPattern.matcher(_page);
		int cnt = 0;
		while(resourcesMatcher.find()) {
			switch(cnt) {
				case 0:
					int shippedIron = Integer.parseInt(resourcesMatcher.group(1)) - remainIron;
					params += "metal|" + (shippedIron < 0 ? 1 : shippedIron) + ",";
					break;
				case 1:
					int shippedCrytal = Integer.parseInt(resourcesMatcher.group(1)) - remainCrytal;
					params += "crystal|" + (shippedCrytal < 0 ? 1 : shippedCrytal) + ",";
					break;
				case 2:
					int shippedDeuterium = Integer.parseInt(resourcesMatcher.group(1)) - remainDeuterium;
					params += "deuterium|" + (shippedDeuterium < 0 ? 1 : shippedDeuterium);
					break;
			}
			cnt++;
		}
		
		return setParams(params.split(","));
	}
	
	public List<NameValuePair> selectSpeedAndPlanetToShipping(String _page, String[] fD) {

		Pattern hiddenPattern = Pattern.compile("name=\"([^\"]*)\"(?:\\sid=\"[^\"]*\"|)(?:\\stype=\"hidden\"|)\\svalue=\"(\\d*)\"");
		Matcher hiddenMatcher = hiddenPattern.matcher(_page);
		String params = "";
		while(hiddenMatcher.find()) {
			params += hiddenMatcher.group(1) + "|" + hiddenMatcher.group(2) + ",";
		}
		params += fD[1].replaceAll("(\\d+):(\\d+):(\\d+)", "galaxy|$1,system|$2,position|$3") + ",speed|10";
		
		return setParams(params.split(","));
	}
	
	public List<NameValuePair> selectSpeedAndPlanet(String _page, String _planet) {
		
		Pattern hiddenPattern = Pattern.compile("name=\"([^\"]*)\"(?:\\sid=\"[^\"]*\"|)(?:\\stype=\"hidden\"|)\\svalue=\"(\\d*)\"");
		Matcher hiddenMatcher = hiddenPattern.matcher(_page);
		String params = "";
		while(hiddenMatcher.find()) {
			params += hiddenMatcher.group(1) + "|" + hiddenMatcher.group(2) + ",";
		}

		params += switchPlanet(_planet) + ",speed|10";
		
		return setParams(params.split(","));
	}
	
	public List<NameValuePair> loadResources(String _page, String _planet, String fleetSpeed) {

		Pattern tokenRule = Pattern.compile("'token'\\s*value='(.*?)'");
		Matcher tokenMatcher = tokenRule.matcher((CharSequence) _page);

		java.lang.String token = "";
		if(tokenMatcher.find()) {
			token = tokenMatcher.group(1);
		}
		
		Pattern hiddenPattern = Pattern.compile("name=\"([^\"]*)\"(?:\\sid=\"[^\"]*\"|)(?:\\stype=\"hidden\"|)\\svalue=\"(\\d*)\"");
		Matcher hiddenMatcher = hiddenPattern.matcher(_page);
		String params = "";
		while(hiddenMatcher.find()) {
			if(!hiddenMatcher.group(1).equals("retreatAfterDefenderRetreat")
					|| !hiddenMatcher.group(1).equals("mission")
					|| !hiddenMatcher.group(1).equals("speed")) {
				params += hiddenMatcher.group(1) + "|" + hiddenMatcher.group(2) + ",";
				
			}
		}
		
		//mission 3 shipping
		//mission 4 deployment
		params = "holdingtime|1," +
				"expeditiontime|1," +
				"token|" + token  + "," + 
				params + "," +
				"mission|4,speed|" + fleetSpeed.replaceAll("0$", "") + ",";
		
		Pattern resourcesPattern = Pattern.compile("actual\":(\\d+),\"max");
		Matcher resourcesMatcher = resourcesPattern.matcher(_page);
		int cnt = 0;
		while(resourcesMatcher.find()) {
			switch(cnt) {
				case 0:
					params += "metal|" + resourcesMatcher.group(1) + ",";
					break;
				case 1:
					params += "crystal|" + resourcesMatcher.group(1) + ",";
					break;
				case 2:
					params += "deuterium|" + resourcesMatcher.group(1);
					break;
			}
			cnt++;
		}
		
		return setParams(params.split(","));
	}
	
	private List<NameValuePair> setParams(String _array[]) {
		params.clear();
		for(int i = 0; i < _array.length; i++) {
			if(_array[i] != null) {
				String param[] = _array[i].split("\\|");
				String name = param[0];
				String value;
				try {
					value = param[1];
				} catch (ArrayIndexOutOfBoundsException e) {
					value = "";
				}		
				params.add(new BasicNameValuePair(name, value));
			}
		}
		//System.out.println(params.toString());
		return params;
	}
	
	private String switchPlanet(String _planet) {
		for(int b = 0; b < routes.length; b++) {
			if(routes[b].split("#")[0].equals(_planet)) {
				return routes[b].split("#")[1];
			}
		}
		return "";
	}
}
