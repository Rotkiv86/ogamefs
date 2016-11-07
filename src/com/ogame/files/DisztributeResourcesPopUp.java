package com.ogame.files;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

public class DisztributeResourcesPopUp extends PopupWindow {
	
	private StoredParameters storedParameters;
	private TextView tIron, tCrystal, tDeuterium;
	
	public DisztributeResourcesPopUp(Context context) {
		super(context);
		
		View view = LayoutInflater.from(context).inflate(R.layout.popup_countresources, null, false);
		setContentView(view);
		setFocusable(true);
		
		setWidth(300);
		setHeight(500);
		
		storedParameters = new StoredParameters(context);
		
		
		
		
		
		
		
		//***********
		//**Counting
		//***********
		PageDownloader pageDownloader = new PageDownloader();
		String mainUrl = storedParameters.getUniversumNumber() + "-hu.ogame.gameforge.com";

		String login = pageDownloader.downloadPagePost("http://hu.ogame.gameforge.com/main/login", storedParameters.getProfileParams());
		String[] planets = countPlanets(login);
		//pProgressBar.setMax(planets.length);
		
		int[] resources = new int[6];
		
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
			//pProgressBar.setProgress(_i);
			
			if(i == 0) {
				_resources = getResources(login);
				addResources(resources, _resources);
			} else {
				_resources = getResources(pageDownloader.dowloadPageGet("http://" + mainUrl + "/game/index.php?page=overview&cp=" + planets[i].split("\\|")[0]));
				addResources(resources, _resources);
			}
		}
	}
	
	public void show(View anchor) {
		showAtLocation(anchor, Gravity.CENTER, 0, 0);
	}
	
	private String[] countPlanets(String source) {
		Pattern planetIdRule = Pattern.compile("planet-(\\d+)\">\\s*<[^>]*\\[(\\d*:\\d*:\\d*)\\][^>]*>\\s*<[^>]*>\\s*<[^>]*>([^<]*)<");
		Matcher planetMatcher = planetIdRule.matcher(source);

		String planetIds = "";
		while(planetMatcher.find()) {
			planetIds += planetMatcher.group(1) + "|" + planetMatcher.group(2) + "|" + planetMatcher.group(3) + ",";
		}
		planetIds = planetIds.replaceAll(",$", "");

		return planetIds.split(",");
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
}
