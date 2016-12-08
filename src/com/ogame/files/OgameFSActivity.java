package com.ogame.files;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

public class OgameFSActivity extends Activity {
	private Button bSaveFleet, bCallBackFleet, bCheckResources, bDistributeResouces;
	private CheckBox cForceResources;
	private Spinner	sSpeedSelector;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		sSpeedSelector = (Spinner)findViewById(R.id.spinner_speed);

		bSaveFleet = (Button)findViewById(R.id.button_save);
		bCallBackFleet = (Button)findViewById(R.id.button_callback);

		cForceResources = (CheckBox) findViewById(R.id.checkbox_force_resources);
		bCheckResources = (Button)findViewById(R.id.button_checkresources);
		bDistributeResouces = (Button)findViewById(R.id.button_distributeresources);

		bCheckResources.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ResourceCountingResponsePopUp resourceCountingResponsePopUp = new ResourceCountingResponsePopUp(getApplicationContext(), cForceResources.isChecked());
				resourceCountingResponsePopUp.show(findViewById(R.id.layout_main));
			}
		});
		
		bSaveFleet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	FsResponsePopup fsResponsePopup = new FsResponsePopup(getApplicationContext(), sSpeedSelector.getSelectedItem().toString());
				fsResponsePopup.show(findViewById(R.id.layout_main));
            }
        });
		
		bCallBackFleet.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				CbResponsePopup cbResponsePopup = new CbResponsePopup(getApplicationContext());
				cbResponsePopup.show(findViewById(R.id.layout_main));
			}
		});
		
		bDistributeResouces.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				DisztributeResourcesPopUp disztributeResourcesPopUp = new DisztributeResourcesPopUp(getApplicationContext());
				disztributeResourcesPopUp.show(findViewById(R.id.layout_main));
			}
		});
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.layout.menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_adduser:
				AddUserPopup addUserPopup = new AddUserPopup(getApplicationContext(), this);
				addUserPopup.show(findViewById(R.id.layout_main));
				return true;
			case R.id.menu_setfsroutes:
				FsPopup fsPopup = new FsPopup(getApplicationContext(), this);
				fsPopup.show(findViewById(R.id.layout_main));
				return true;
			case R.id.menu_shipping:
				ShippingPopup shippingPopup = new ShippingPopup(getApplicationContext(), this);
				shippingPopup.show(findViewById(R.id.layout_main));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	} 
}
