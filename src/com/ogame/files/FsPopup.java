package com.ogame.files;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public class FsPopup extends PopupWindow {
	private FileHandler fileHandler;
	private Button saveRoutes;
	private Spinner[] sPlanet;
	private TextView[] tvPlanet;
	private String[] planets;
	
	public FsPopup(Context context, OgameFSActivity ogameFSActivity) {
		super(context);
		setWidth(400);
		setHeight(600);
		
		fileHandler = new FileHandler(context);
		String[] routes = fileHandler.getRoutes();
		planets = fileHandler.getPlanets();
		
		ScrollView svMain = new ScrollView(context);
		
		LinearLayout llMain = new LinearLayout(context);
		llMain.setOrientation(LinearLayout.VERTICAL);

		saveRoutes = new Button(context);
		saveRoutes.setText("Save");
		saveRoutes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	saveRoutes();
            	dismiss();
            }
        });
		
		sPlanet = new Spinner[planets.length];
		tvPlanet = new TextView[planets.length];
		for(int i = 0; i < planets.length; i++) {
			LinearLayout llRowHolder = new LinearLayout(context);
			llRowHolder.setOrientation(LinearLayout.HORIZONTAL);
			
			tvPlanet[i] = new TextView(context);
			tvPlanet[i].setText(planets[i].split("\\|")[2]);
			tvPlanet[i].setLayoutParams(new ViewGroup.LayoutParams(150, ViewGroup.LayoutParams.WRAP_CONTENT));

			sPlanet[i] = new Spinner(ogameFSActivity);
			List<String> list = new ArrayList<String>();
			for(int j = 0; j < planets.length; j++) {
				list.add(planets[j].split("\\|")[2]);
			}

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, list);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sPlanet[i].setAdapter(dataAdapter);
			sPlanet[i].setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			
			//**setspinner pos
			if(routes.length > 1) {
				for(int c = 0; c < sPlanet[i].getCount(); c++) {
					for(int d = 0; d < routes.length; d++) {		
						if(routes[d].split("#")[1].split("-")[0].equals(planets[i].split("\\|")[2])
							&& sPlanet[i].getItemAtPosition(c).equals(routes[d].split("#")[1].split("-")[1])) {
							sPlanet[i].setSelection(c);
						}
					}
				}				
			}
				
			llRowHolder.addView(tvPlanet[i]);
			llRowHolder.addView(sPlanet[i]);
			
			llMain.addView(llRowHolder);
		}

		llMain.addView(saveRoutes);
		svMain.addView(llMain);
		setContentView(svMain);
		setFocusable(true);
	}
	
	public void show(View anchor) {
		showAtLocation(anchor, Gravity.CENTER, 0, 0);
	}
	
	private void saveRoutes() {
		String data[] = fileHandler.readDataFromFile().split(";");
		String fileData = "";
		for(int a = 0; a < data.length; a++) {
			if(!data[a].split(":")[0].equals("route")) {
				fileData += data[a] + ";\n";	
			}
		}
		
		for(int b = 0; b < sPlanet.length; b++) {
			String departureCoords = "";
			String arrivalCoords = "";
			String departureName = "";
			String arrivalName = "";
			
			for(int e = 0; e < planets.length; e++) {
				if(planets[e].split("\\|")[2].equals(tvPlanet[b].getText())) {
					departureName = planets[e].split("\\|")[2];
					departureCoords = planets[e].split("\\|")[1];
				}
				if(planets[e].split("\\|")[2].equals(sPlanet[b].getSelectedItem())) {
					arrivalName = planets[e].split("\\|")[2];
					String[] ac = planets[e].split("\\|")[1].split(":");
					arrivalCoords = "galaxy|" + ac[0] + ",system|" + ac[1] + ",position|" + ac[2];
				}
			}
			fileData += "route:" + departureCoords + "-" + arrivalCoords + "#" + departureName + "-" + arrivalName +";\n";
		}
		fileHandler.writeDataToFile(fileData);
	}
}
