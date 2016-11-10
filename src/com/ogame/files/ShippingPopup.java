package com.ogame.files;

import java.util.ArrayList;
import java.util.List;
import java.lang.String;

import android.content.Context;
import android.os.AsyncTask;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
	private EditText eDesiredIron, eDesiredCrystal, eDesiredDeuterium;
	private String iron, chrystal, deuterium, planet;
	private ProgressBar pShippingProgressBar;
	private String mainUrl, _pre = "https://";
	
	public ShippingPopup(Context context, OgameFSActivity ogameFSActivity) {
		super(context);
		setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
	
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
		tvMsg.setText("Set the desired resource to the planet:");

		eDesiredIron = new EditText(context);
		eDesiredCrystal = new EditText(context);
		eDesiredDeuterium = new EditText(context);
		
		eDesiredIron.setInputType(InputType.TYPE_CLASS_NUMBER);
		eDesiredCrystal.setInputType(InputType.TYPE_CLASS_NUMBER);
		eDesiredDeuterium.setInputType(InputType.TYPE_CLASS_NUMBER);

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
				String[] resourcesData = fileHandler.getData("resource");
				int planets = fileHandler.getData("planet").length;
				for(int i = 0; i < 3; i++) {
					switch(i) {
						case 0:
							iron = ((Integer.parseInt(resourcesData[i].split("\\|")[0].split(":")[1].replaceAll("\\.", "")) - Integer.parseInt(eDesiredIron.getText().toString())) / planets) + "";
							break;
						case 1:
							chrystal = ((Integer.parseInt(resourcesData[i].split("\\|")[0].split(":")[1].replaceAll("\\.", "")) - Integer.parseInt(eDesiredCrystal.getText().toString())) / planets) + "";
							break;
						case 2:
							deuterium = ((Integer.parseInt(resourcesData[i].split("\\|")[0].split(":")[1].replaceAll("\\.", "")) - Integer.parseInt(eDesiredDeuterium.getText().toString())) / planets) + "";
							break;
					}
				}
				new ShippingLongOperation().execute("");
			}
		});

		llMain.addView(tvMsg);
		llMain.addView(pShippingProgressBar);
		llMain.addView(llSpacer[0]);
		llMain.addView(eDesiredIron);
		llMain.addView(llSpacer[1]);
		llMain.addView(eDesiredCrystal);
		llMain.addView(llSpacer[2]);
		llMain.addView(eDesiredDeuterium);
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
			fD[0] = planet;
			fD[1] = iron;
			fD[2] = chrystal;
			fD[3] = deuterium;
			
			sendFleetsToShipping(fD);
			dismiss();
			return "";
		}
		
		private void sendFleetsToShipping(String[] selected) {
			pageDownloader.downloadPagePost(_pre + "hu.ogame.gameforge.com/main/login", storedParameters.getProfileParams());
			
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
					String firstFleetPage = pageDownloader.dowloadPageGet(_pre + mainUrl + "/game/index.php?page=fleet1&cp=" + _planetId);
									
					//Second flight page (select speed, destination planet)			
					pShippingProgressBar.setProgress(cnt++);
					String secondFleetPage = pageDownloader.downloadPagePost(_pre + mainUrl + "/game/index.php?page=fleet2", storedParameters.getFleetShipNumbersToShipping(firstFleetPage, fD));
					
					//Third flight page (select goal, resources)
					pShippingProgressBar.setProgress(cnt++);
					String thirdFleetPage = pageDownloader.downloadPagePost(_pre + mainUrl + "/game/index.php?page=fleet3", storedParameters.selectSpeedAndPlanetToShipping(secondFleetPage, fD));

					//Sending fleet to Shipping;
					pShippingProgressBar.setProgress(cnt++);
					pageDownloader.downloadPagePost(_pre + mainUrl + "/game/index.php?page=movement", storedParameters.loadResourcesToShipping(thirdFleetPage, fD));
				}			
			}
		}
	}
}
