package com.ogame.files;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CbResponsePopup extends PopupWindow {
	private TextView tFleetsBack;
	private ProgressBar pProgressBar;
	private PageDownloader pageDownloader;
	private StoredParameters storedParameters;
	private String mainUrl, _pre = "https://";;
	
	public CbResponsePopup(Context context) {
		super(context);
		setWidth(400);
		setHeight(60);
		
		pageDownloader = new PageDownloader();
		storedParameters = new StoredParameters(context);
		
		mainUrl = storedParameters.getUniversumNumber() + "-hu.ogame.gameforge.com";
		
		LinearLayout llMain = new LinearLayout(context);
		llMain.setOrientation(LinearLayout.VERTICAL);
		
		pProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
		pProgressBar.setMinimumWidth(400);
		pProgressBar.setMinimumHeight(40);
		
		tFleetsBack = new TextView(context);
		
		llMain.addView(pProgressBar);
		llMain.addView(tFleetsBack);
		
		new CallingbackLongOperation().execute("");
		
		setContentView(llMain);
	}

	public void show(View anchor) {
		showAtLocation(anchor, Gravity.CENTER, 0, 0);
	}
	
	private class CallingbackLongOperation extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			pageDownloader.downloadPagePost(_pre + "hu.ogame.gameforge.com/main/login", storedParameters.getProfileParams());
			String[] fleetIds = getFleetIds(pageDownloader.dowloadPageGet(_pre + mainUrl + "/game/index.php?page=movement"));

			int cnt = 1;
			pProgressBar.setMax(fleetIds.length);
			for(int i = 0; i < fleetIds.length; i++) {
				pageDownloader.dowloadPageGet(_pre + mainUrl + "/game/index.php?page=movement&return=" + fleetIds[i]);
				pProgressBar.setProgress(cnt++);
			}
			return null;
		}
		
		private String[] getFleetIds(String source) {			
			Pattern fleetIdRule = Pattern.compile("return=(\\d+)\"");
			Matcher fleetMatcher = fleetIdRule.matcher(source);
			
			String fleetIds = "";
			while(fleetMatcher.find()) {
				fleetIds += fleetMatcher.group(1) + ";";
			}
			fleetIds = fleetIds.replaceAll(";$", "");
			
			return fleetIds.split(";");
		}
		
		@Override
		protected void onPostExecute(String result) {
			tFleetsBack.setText("Fleets are called back!");
			new Handler().postDelayed(new Runnable(){
			    public void run() {
			    	dismiss();
			    }
			}, 3000);
		}
	}
}
