package com.hodgeproject.media.controller;

import com.hodgeproject.media.R;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener  {
	private static final String androidns="http://schemas.android.com/apk/res/android";
	private static final String hodgens="http://hodgeproject.com";

	private SeekBar mSeekBar;
	private TextView mSplashText,mValueText;
	private Context mContext;

	private String mDialogMessage, mSuffix;
	private int mDefault, mMax, mMin, mValue = 0;
	private int saveValue;
	
	public SeekBarPreference(Context context, AttributeSet attrs) { 
		super(context,attrs);//, R.style.SeekBarPreferenceCustom); 
		mContext = context;
		mDialogMessage = attrs.getAttributeValue(androidns,"dialogMessage");
		mSuffix = attrs.getAttributeValue(androidns,"text");
		mDefault = attrs.getAttributeIntValue(androidns,"defaultValue", 100);
		mMax = attrs.getAttributeIntValue(androidns,"max", 200);
		mMin = attrs.getAttributeIntValue(hodgens,"min", 25);
	}

	@Override 
	protected View onCreateDialogView() {
		//ContextThemeWrapper(this, R.style.SeekBarPreferenceCustom);
		LinearLayout.LayoutParams params;
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(6,6,6,6);

		mSplashText = new TextView(mContext);
		mSplashText.setTextAppearance(mContext, R.style.MyTextView);
		if (mDialogMessage != null)
			mSplashText.setText(mDialogMessage);
		layout.addView(mSplashText);

		mValueText = new TextView(mContext);
		mValueText.setTextAppearance(mContext, R.style.MyTextView);
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		mValueText.setTextSize(32);
		params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.addView(mValueText, params);

		mSeekBar = new SeekBar(mContext);
		mSeekBar.setOnSeekBarChangeListener(this);
		layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		if (shouldPersist())
			mValue = getPersistedInt(mDefault);

		mSeekBar.setMax(mMax-mMin);
		mSeekBar.setProgress(mValue-mMin);
		return layout;
	}
	@Override 
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mSeekBar.setMax(mMax-mMin);
		mSeekBar.setProgress(mValue-mMin);
	}
	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue)  
	{
		super.onSetInitialValue(restore, defaultValue);
		if (restore) 
			mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
			else 
				mValue = (Integer)defaultValue;
	}

	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch){
		value += mMin;
		String t = String.valueOf(value);
		mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));
		saveValue = value;
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult){
		if(positiveResult){
			if (shouldPersist()){
				persistInt(saveValue);
			}
			callChangeListener(Integer.valueOf(saveValue));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {}

	public void setMax(int max) { mMax = max; }
	public int getMax() { return mMax; }

	public void setProgress(int progress) { 
		mValue = progress;
		if (mSeekBar != null)
			mSeekBar.setProgress(progress-mMin); 
	}
	public int getProgress() { return mValue; }

}
