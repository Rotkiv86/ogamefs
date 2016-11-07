package com.ogame.files;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class ShippingPopup extends PopupWindow {
	
	private FileHandler fileHandler;
	private PageDownloader pageDownloader;
	private StoredParameters storedParameters;
	private String[] planets;
	private Spinner sSelectedPlanet;
	private EditText eRemainIron, eRemainCrystal, eRemainDeuterium;
	private ProgressBar pShippingProgressBar;
	private String mainUrl;
	
	public ShippingPopup(Context context, OgameFSActivity ogameFSActivity) {
		super(context);
		setWidth(400);
		setHeight(600);
	
		fileHandler = new FileHandler(context);
		pageDownloader = new PageDownloader();
		storedParameters = new StoredParameters(context);
		mainUrl = storedParameters.getUniversumNumber() + "-hu.ogame.gameforge.com";
		
		LinearLayout llMain = new LinearLayout(context);
		llMain.setOrientation(LinearLayout.VERTICAL);
		
		pShippingProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);

		LinearLayout[] llSpacer = new LinearLayout[6];
		for(int a = 0; a < 6; a++) {
			llSpacer[a] = new LinearLayout(context);
			llSpacer[a].setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 20));			
		}
		
		TextView tvMsg = new TextView(context);
		tvMsg.setText("Set the remain resources per planet:");

		eRemainIron = new EditText(context);
		eRemainCrystal = new EditText(context);
		eRemainDeuterium = new EditText(context);
		
		eRemainIron.setInputType(InputType.TYPE_CLASS_NUMBER);
		eRemainCrystal.setInputType(InputType.TYPE_CLASS_NUMBER);
		eRemainDeuterium.setInputType(InputType.TYPE_CLASS_NUMBER);

		sSelectedPlanet = new Spinner(ogameFSActivity);
		planets = fileHandler.getPlanets();
		List<String> list = new ArrayList<String>();
		
		for(int j = 0; j < planets.length; j++) {
			list.add(planets[j].split("\\|")[2] + "-" + planets[j].split("\\|")[1]);
		}

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sSelectedPlanet.setAdapter(dataAdapter);
		sSelectedPlanet.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				
		Button bShipping = new Button(context);
		bShipping.setText("Shipping");
		bShipping.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new ShippingLongOperation().execute("");
			}
		});

		llMain.addView(tvMsg);
		llMain.addView(pShippingProgressBar);
		llMain.addView(llSpacer[0]);
		llMain.addView(eRemainIron);
		llMain.addView(llSpacer[1]);
		llMain.addView(eRemainCrystal);
		llMain.addView(llSpacer[2]);
		llMain.addView(eRemainDeuterium);
		llMain.addView(llSpacer[3]);
		llMain.addView(sSelectedPlanet);
		llMain.addView(llSpacer[4]);
		llMain.addView(bShipping);
		llMain.addView(llSpacer[5]);
		setContentView(llMain);
		setFocusable(true);
	}

	public void show(View anchor) {
		showAtLocation(anchor, Gravity.CENTER, 0, 0);
	}
		
	private class ShippingLongOperation extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String[] fD = new String[4];
			fD[0] = sSelectedPlanet.getSelectedItem().toString();
			fD[1] = eRemainIron.getText().toString();
			fD[2] = eRemainCrystal.getText().toString();
			fD[3] = eRemainDeuterium.getText().toString();
			
			sendFleetsToShipping(fD);
			dismiss();
			return null;
		}
		
		private void sendFleetsToShipping(String[] selected) {
			pageDownloader.downloadPagePost("http://hu.ogame.gameforge.com/main/login", storedParameters.getProfileParams());
			
			int cnt = 1;
			pShippingProgressBar.setMax(planets.length * 4);

			String[] planetNames = fileHandler.getPlanets();
			for(int a = 0; a < planetNames.length; a++) {
				String planetName = planetNames[a].split("\\|")[2];
				
				if(!planetName.equals(selected[0].split("-")[0])) {
					String _planetId = planetNames[a].split("\\|")[0].replaceAll("planet:", "");
					
					String[] fD = new String[5];
					fD[0] = _planetId;
					fD[1] = selected[0].split("-")[1];
					fD[2] = selected[1];
					fD[3] = selected[2];
					fD[4] = selected[3];
					
					//First flight page
					pShippingProgressBar.setProgress(cnt++);
					String firstFleetPage = pageDownloader.dowloadPageGet("http://" + mainUrl + "/game/index.php?page=fleet1&cp=" + _planetId);
									
					//Second flight page (select speed, destination planet)			
					pShippingProgressBar.setProgress(cnt++);
					String secondFleetPage = pageDownloader.downloadPagePost("http://" + mainUrl + "/game/index.php?page=fleet2", storedParameters.getFleetShipNumbersToShipping(firstFleetPage, fD));
					
					//Third flight page (select goal, resources)
					pShippingProgressBar.setProgress(cnt++);
					String thirdFleetPage = pageDownloader.downloadPagePost("http://" + mainUrl + "/game/index.php?page=fleet3", storedParameters.selectSpeedAndPlanetToShipping(secondFleetPage, fD));

					//Sending fleet to Shipping;
					pShippingProgressBar.setProgress(cnt++);
					pageDownloader.downloadPagePost("http://" + mainUrl + "/game/index.php?page=movement", storedParameters.loadResourcesToShipping(thirdFleetPage, fD));
				}			
			}
		}
	}
}
