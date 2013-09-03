package com.fiskur.screenshotbuilder;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String version = pInfo.versionName;
			TextView aboutView = (TextView) findViewById(R.id.about_view);
			aboutView.setText(aboutView.getText().toString() + " (Version: " + version + ")");
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		finish();
		return super.onMenuItemSelected(featureId, item);
	}

}
