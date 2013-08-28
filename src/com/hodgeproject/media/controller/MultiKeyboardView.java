package com.hodgeproject.media.controller;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

public class MultiKeyboardView extends KeyboardView {
	public MultiKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public MultiKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/*
	@Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(25);
        paint.setColor(Color.RED);

        List<Key> keys = getKeyboard().getKeys();
        for(Key key: keys) {
            if(key.label != null && key.codes[0] > 0 && key.popupCharacters != null && key.popupCharacters.length()>0)
                canvas.drawText(key.popupCharacters.subSequence(0,1).toString(), key.x + (key.width/2), key.y + 25, paint);
        }
    }
	
	@Override
	protected boolean onLongPress(Key key) {
	    if (false && key.popupCharacters!=null && key.popupCharacters.length() > 0) {
	    	getOnKeyboardActionListener().onKey(key.popupCharacters.charAt(0),null);
	    	return true;
	    } else {
	        return super.onLongPress(key);
	    }
	}
	*/
	
}
