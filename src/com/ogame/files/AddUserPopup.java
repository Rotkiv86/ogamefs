package com.ogame.files;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;

public class AddUserPopup extends PopupWindow{
	private EditText eUserName, ePassword;
	private Spinner sUniversum;
	private FileHandler fileHandler;
	
	public AddUserPopup(Context context, OgameFSActivity ogameFSActivity) {
		super(context);
		setWidth(300);
		setHeight(500);
		
		LinearLayout llMain = new LinearLayout(context);
		
		fileHandler = new FileHandler(context);
		llMain.setOrientation(LinearLayout.VERTICAL);
		
		eUserName = new EditText(context);
		sUniversum = new Spinner(ogameFSActivity);
		ePassword = new EditText(context);
		Button bAddUser = new Button(context);
		bAddUser.setText("Save");
		
		List<String> universums = new ArrayList<String>();
		universums.add("Andromeda");	//1
		universums.add("Barym");		//2
		universums.add("Capella");		//3
		universums.add("Draco");		//4
		universums.add("Electra");		//5
		universums.add("Fromax");		//6
		universums.add("Gemini");		//7
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, universums);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		ePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		sUniversum.setAdapter(dataAdapter);

		if(fileHandler.readDataFromFile() != "") {
			String[] user = fileHandler.getUserData()[0].split(":")[1].split("\\|");
			eUserName.setText(user[0]);
			ePassword.setText(user[1]);
			for(int b = 0; b < sUniversum.getCount(); b++) {
				if(sUniversum.getItemAtPosition(b).equals(user[2])) {
					sUniversum.setSelection(b);
				}
			}
		}
		
		bAddUser.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	saveUser();
	        	dismiss();
	        }
	    });
		
		llMain.addView(eUserName);
		llMain.addView(sUniversum);
		llMain.addView(ePassword);
		llMain.addView(bAddUser);
		
		setContentView(llMain);
		setFocusable(true);
	}
	
	public void show(View anchor) {
		showAtLocation(anchor, Gravity.CENTER, 0, 0);
	}
	
	private void saveUser() {
		String data[] = fileHandler.readDataFromFile().split(";");
		String fileData = "user:" + eUserName.getText() + "|" + ePassword.getText() + "|" + sUniversum.getSelectedItem().toString() + ";\n";
		for(int a = 0; a < data.length; a++) {
			if(!data[a].split(":")[0].equals("user")) {
				fileData += data[a] + ";\n";	
			}
		}
		fileHandler.writeDataToFile(fileData);
	}
}
