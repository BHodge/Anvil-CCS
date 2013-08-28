package com.hodgeproject.media.controller;

import com.hodgeproject.media.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.Menu;

public class SettingsActivity extends PreferenceActivity{ // implements OnSharedPreferenceChangeListener {
	public static final String MOUSE_SENSE = "mouse_sensitivity";
	public static final String LONGPRESS_EN = "long_press_en";
	public static final String TOUCHPAD_BUTTONS_EN = "touchpad_buttons_en";
	public static final String VOLROCK_MOUSE_EN = "volrock_mouse_en";
	public static final String VOLROCK_MOUSE_OVERRIDE = "volrock_mouse_override";
	public static final String VOLROCK_UP = "volrock_up";
	public static final String VOLROCK_DOWN = "volrock_down";
	
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_settings);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// No menu
		return true;
	}

	/**
	 * Used mainly for updating preferences used by the control service.
	 */
	/*
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// Notify the control service of changed preferences
		//Intent controlService_update = new Intent(this,ControlService.class);
		if(key.equals(MOUSE_SENSE)){
			//controlService_update.putExtra(ControlService.EXTRA_MOUSE_SENSE, sharedPreferences.getInt(SettingsActivity.MOUSE_SENSE, 100));
		}
		//startService(controlService_update);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
	*/
}
