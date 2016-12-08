package com.ogame.files;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ResourceCountingResponsePopUp extends PopupWindow {
	private ProgressBar pProgressBar;
	private FileHandler fileHandler;
	private StoredParameters storedParameters;
	private static TextView tIron, tCrystal, tDeuterium, tPiron, tPcrystal, tPdeuterium;
	private String _pre = "https://";
	private Calendar timeNow;
	
	public ResourceCountingResponsePopUp(Context context, boolean force) {
		super(context);
		View view = LayoutInflater.from(context).inflate(R.layout.popup_countresources, null, false);
		setContentView(view);
		setFocusable(true);

		setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

		
		storedParameters = new StoredParameters(context);
		fileHandler = new FileHandler(context);
		timeNow = Calendar.getInstance();

		tIron = (TextView)view.findViewById(R.id.textview_iron);
		tCrystal =  (TextView)view.findViewById(R.id.textview_crystal);
		tDeuterium =  (TextView)view.findViewById(R.id.textview_deuterium);
		tPiron =  (TextView)view.findViewById(R.id.textview_iron_production);
		tPcrystal =  (TextView)view.findViewById(R.id.textview_crystal_production);
		tPdeuterium=  (TextView)view.findViewById(R.id.textview_deuterium_production);

		pProgressBar = (ProgressBar)view.findViewById(R.id.progressbar_countresources);

		String[] resourcesData = fileHandler.getData("resource");
		if(resourcesData.length > 1 && !force) {
			Calendar resourceCheckTime = Calendar.getInstance();
			resourceCheckTime.setTimeInMillis(Long.parseLong(resourcesData[0].split("\\|")[1]));
			if(((timeNow.getTimeInMillis() - resourceCheckTime.getTimeInMillis()) / 60000) > 60) {
				new ResourceCountingLongOperation().execute("");
			} else {
				String[] resourcesArray = new String[6];
				for(int i = 0; i < 6; i++) {
					resourcesArray[i] = resourcesData[i].split("\\|")[0].split(":")[1];
				}
				ResourceCountingResponsePopUp.showResources(resourcesArray);
			}
		} else {
			new ResourceCountingLongOperation().execute("");
		}
	}

	public void show(View anchor) {
		showAtLocation(anchor, Gravity.CENTER, 0, 0);
	}

	static void showResources(String[] resourcesArray) {
		tIron.setText(resourcesArray[0]);
		tCrystal.setText(resourcesArray[1]);
		tDeuterium.setText(resourcesArray[2]);

		tPiron.setText(resourcesArray[3]);
		tPcrystal.setText(resourcesArray[4]);
		tPdeuterium.setText(resourcesArray[5]);
	}
	
	private class ResourceCountingLongOperation extends AsyncTask<String, Void, String> {
		private int[] resources = new int[6];
		
		@Override
		protected String doInBackground(String... params) {
			PageDownloader pageDownloader = new PageDownloader();
			String mainUrl = storedParameters.getUniversumNumber() + "-hu.ogame.gameforge.com";
			
			String login = pageDownloader.downloadPagePost(_pre + "hu.ogame.gameforge.com/main/login", storedParameters.getProfileParams());
			String[] planets = countPlanets(login);
			pProgressBar.setMax(planets.length);
			
			//**Resouces
			resources[0] = 0;
			resources[1] = 0;
			resources[2] = 0;
			//**Production
			resources[3] = 0;
			resources[4] = 0;
			resources[5] = 0;
			int[] _resources = new int[6];
			
			for(int i = 0; i < planets.length; i++) {
				final int _i = i + 1;
				pProgressBar.setProgress(_i);
				
				if(i == 0) {
					_resources = getResources(login);
					addResources(resources, _resources);
				} else {
					_resources = getResources(pageDownloader.dowloadPageGet(_pre + mainUrl + "/game/index.php?page=overview&cp=" + planets[i].split("\\|")[0]));
					addResources(resources, _resources);
				}
			}
			return "";
		}      

		private int[] getResources(String source) {
			int[] planetResources = new int[6];
			Pattern resourceRule = Pattern.compile("\"actual\":(\\d*),\"max\".*?undermark\\\\\">.(\\d*.\\d*)<");
			
			Matcher resourceMatcher = resourceRule.matcher(source);
			int cnt = 0;
			while(resourceMatcher.find()) {
				planetResources[cnt] = Integer.parseInt(resourceMatcher.group(1));
				int production = Integer.parseInt(resourceMatcher.group(2).replaceAll("\\.", ""));
				switch(cnt) {
					case 0:
						planetResources[3] = production;
						break;
					case 1:
						planetResources[4] = production;
						break;
					case 2:
						planetResources[5] = production;
						break;
				}
				cnt++;
			}
			return planetResources;
		}
		
		private void addResources(int[] res1, int[] res2) {
			//**Resources
			res1[0] += res2[0];
			res1[1] += res2[1];
			res1[2] += res2[2];
			//**Production
			res1[3] += res2[3];
			res1[4] += res2[4];
			res1[5] += res2[5];
		}
		
		@Override
		protected void onPostExecute(String result) {
			String[] resourcesArray = new String[6];
			resourcesArray[0] = formatNumber(resources[0]);
			resourcesArray[1] = formatNumber(resources[1]);
			resourcesArray[2] = formatNumber(resources[2]);
			resourcesArray[3] = formatNumber(resources[3]);
			resourcesArray[4] = formatNumber(resources[4]);
			resourcesArray[5] = formatNumber(resources[5]);

			ResourceCountingResponsePopUp.showResources(resourcesArray);

			String dataToFile = "";
			for(int j = 0; j < 6; j++) {
				dataToFile += "resource:" + formatNumber(resources[j]) + "|" + timeNow.getTimeInMillis() + ";\n";
			}
			fileHandler.refreshDataFile("resource", dataToFile);
		}
		
		private String[] countPlanets(String source) {
			Pattern planetIdRule = Pattern.compile("planet-(\\d+)\">\\s*<[^>]*\\[(\\d*:\\d*:\\d*)\\][^>]*>\\s*<[^>]*>\\s*<[^>]*>([^<]*)<");
			Matcher planetMatcher = planetIdRule.matcher(source);

			String planetIds = "";
			String dataToFile = "";
			while(planetMatcher.find()) {
				planetIds += planetMatcher.group(1) + "|" + planetMatcher.group(2) + "|" + planetMatcher.group(3) + ",";
				dataToFile += "planet:" + planetMatcher.group(1) + "|" + planetMatcher.group(2) + "|" + planetMatcher.group(3) + ";\n";
			}
			planetIds = planetIds.replaceAll(",$", "");

			fileHandler.refreshDataFile("planet", dataToFile);
			return planetIds.split(",");
		}
		
		private String formatNumber(int n) {
			String numb = (n + "");
			switch(numb.length()) {
				case 0:
				case 1:
				case 2:
				case 3:
					//Nothing to do
					break;
				case 4:
				case 5:
				case 6:
					numb = numb.replaceAll("^(\\d*)(\\d{3})$", "$1.$2");
					break;
				case 7:
				case 8:
				case 9:
					numb = numb.replaceAll("^(\\d*)(\\d{3})(\\d{3})$", "$1.$2.$3");
					break;
			}
			return numb;
		}
		
		@Override
		protected void onPreExecute() {
		
		}
	}
}
