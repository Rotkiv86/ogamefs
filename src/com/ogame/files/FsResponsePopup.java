package com.ogame.files;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.String;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FsResponsePopup extends PopupWindow {
	
	private ProgressBar pProgressBar;
	private FileHandler fileHandler;
	private PageDownloader pageDownloader;
	private StoredParameters storedParameters;
	private String mainUrl, fleetSpeed, _pre = "https://";
	private TextView tFleetsSent;
	
	public FsResponsePopup(Context context, String _fleetSpeed) {	
		super(context);
		setWidth(400);
		setHeight(60);

		storedParameters = new StoredParameters(context);
		fileHandler = new FileHandler(context);
		pageDownloader = new PageDownloader();
		mainUrl = storedParameters.getUniversumNumber() + "-hu.ogame.gameforge.com";
		
		fleetSpeed = _fleetSpeed;
		
		LinearLayout llMain = new LinearLayout(context);
		llMain.setOrientation(LinearLayout.VERTICAL);
		
		pProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
		pProgressBar.setMinimumWidth(400);
		pProgressBar.setMinimumHeight(40);
		
		tFleetsSent = new TextView(context);
		
		llMain.addView(pProgressBar);
		llMain.addView(tFleetsSent);

		new FleetSavingLongOperation().execute("");
		
		setContentView(llMain);
	}
	
	public void show(View anchor) {
		showAtLocation(anchor, Gravity.CENTER, 0, 0);
	}
		
	private class FleetSavingLongOperation extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			sendFleetToFS(fleetSpeed);
			return null;
		}
		
		private void sendFleetToFS(String fleetSpeed) {
			String[] planets = countPlanets(pageDownloader.downloadPagePost(_pre + "hu.ogame.gameforge.com/main/login", storedParameters.getProfileParams()));
			
			int cnt = 1;
			pProgressBar.setMax(planets.length * 4);
			
			for(int j = 0; j < planets.length; j++) {
				String _planetId = planets[j].split("\\|")[0];
				String _planet = planets[j].split("\\|")[1];
				
				//First flight page
				String firstFleetPage = pageDownloader.dowloadPageGet(_pre + mainUrl + "/game/index.php?page=fleet1&cp=" + _planetId);
				pProgressBar.setProgress(cnt++);
				
				//Second flight page (select speed, destination planet)			
				String secondFleetPage = pageDownloader.downloadPagePost(_pre + mainUrl + "/game/index.php?page=fleet2", storedParameters.getFleetShipNumbers(firstFleetPage, _planet));
				pProgressBar.setProgress(cnt++);
				
				//Third flight page (select goal, resources)
				String thirdFleetPage = pageDownloader.downloadPagePost(_pre + mainUrl + "/game/index.php?page=fleet3", storedParameters.selectSpeedAndPlanet(secondFleetPage, _planet));
				pProgressBar.setProgress(cnt++);
				
				//Sending fleet to FS;
				pageDownloader.downloadPagePost(_pre + mainUrl + "/game/index.php?page=movement", storedParameters.loadResources(thirdFleetPage, _planet, fleetSpeed));
				pProgressBar.setProgress(cnt++);
			}
		}
		
		private String[] countPlanets(String source) {
			String dataToFile = "";
					
			Pattern planetIdRule = Pattern.compile("planet-(\\d+)\">\\s*<[^>]*\\[(\\d*:\\d*:\\d*)\\][^>]*>\\s*<[^>]*>\\s*<[^>]*>([^<]*)<");
			Matcher planetMatcher = planetIdRule.matcher(source);

			String planetIds = "";
			while(planetMatcher.find()) {
				planetIds += planetMatcher.group(1) + "|" + planetMatcher.group(2) + "|" + planetMatcher.group(3) + ",";
				dataToFile += "planet:" + planetMatcher.group(1) + "|" + planetMatcher.group(2) + "|" + planetMatcher.group(3) + ";\n";
			}
			planetIds = planetIds.replaceAll(",$", "");
					
			String data[] = fileHandler.readDataFromFile().split(";");
			for(int a = 0; a < data.length; a++) {
				if(!data[a].split(":")[0].equals("planet")) {
					dataToFile += data[a] + ";\n";	
				}
			}
			
			fileHandler.writeDataToFile(dataToFile);
			return planetIds.split(",");
		}
		
		@Override
		protected void onPostExecute(String result) {
			tFleetsSent.setText("Fleets are sent to FS!");
			new Handler().postDelayed(new Runnable(){
			    public void run() {
			    	dismiss();
			    }
			}, 3000);
		}
	}
}
