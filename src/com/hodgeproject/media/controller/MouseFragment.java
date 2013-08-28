package com.hodgeproject.media.controller;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.hodgeproject.media.R;

/**
 * Mouse Fragment - Gather touch interaction and handle movements for mouse manipulation.
 * Parent Activity must implement MouseFragmentListener interface.
 * @author Bryan Hodge
 *
 */
public class MouseFragment extends SherlockFragment implements SurfaceHolder.Callback{
	public static final int MOUSE_MULT_THRESH = 6;		// Threshold to apply the mouse sensitivity multiplier
	
	private MouseFragmentListener mCallback;
	private GestureDetector gesture;
	private SurfaceView surface;
	private Button mouseL;
	private Button mouseR;
	
	private boolean longpress_enable;					// Enables using the Longpress gesture to Right-Click
	private boolean touchpad_buttons_enable;			// Enables displaying the Touchpad mouse buttons ("L" and "R")
	private boolean vol_mouse_enable;					// Enables using the VolumeRocker as mouse buttons
	private boolean vol_mouse_override;					// Enables override of the vol_mouse default functionality
	private String vol_mouse_up;						// Volume Up mouse button
	private String vol_mouse_down;						// Volume Down mouse button
	private int mouseSensitivityPercent;				// Multiplier for mouse movement
	
	private boolean vol_up_click;			// Temp state of volume up button
	private boolean vol_down_click;			// Temp state of volume down button

	/**
	 * Interface the parent Activity will need
	 * to implement to respond to mouse events
	 * @author Bryan Hodge
	 */
	public interface MouseFragmentListener {
		public void mouseClick(boolean l_click, boolean heldDown);
		public void mouseMove(int x, int y);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
        gesture = new GestureDetector(this.getActivity(), new MouseGestureListener());
        
    }

	@Override
    public void onAttach(Activity activity){
    	super.onAttach(activity);
    	
    	try{
    		mCallback = (MouseFragmentListener) activity;
    	} catch (ClassCastException e){
    		throw new ClassCastException(activity.toString() + " must implement MouseFragmentListener"); 
    	}
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_mouse, container, false);
		
		surface = (SurfaceView)view.findViewById(R.id.surfaceView1);
		surface.setOnTouchListener(ontouch);
		surface.setWillNotDraw(false);
		
		// Create/Link buttons
		mouseL = (Button)view.findViewById(R.id.buttonl);
		mouseR = (Button)view.findViewById(R.id.buttonr);
		mouseL.setOnTouchListener(new MouseTouchListener("MOUSE1"));
		mouseR.setOnTouchListener(new MouseTouchListener("MOUSE3"));
		
		// Enable Haptic Feedback (if allowed globally...)
		surface.setHapticFeedbackEnabled(true);
		
		surface.getHolder().addCallback(this);
		
		
		updateSharedPreferences();
		return view;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		updateSharedPreferences();
	}
	
	
	/**
	 * Gather stored shared preferences from the settings activity
	 */
    private void updateSharedPreferences() {
    	// Get current preferences
    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
    	longpress_enable = sharedPref.getBoolean(SettingsActivity.LONGPRESS_EN, true);
    	touchpad_buttons_enable  = sharedPref.getBoolean(SettingsActivity.TOUCHPAD_BUTTONS_EN, true);
    	vol_mouse_enable = sharedPref.getBoolean(SettingsActivity.VOLROCK_MOUSE_EN, true);
    	vol_mouse_override = sharedPref.getBoolean(SettingsActivity.VOLROCK_MOUSE_OVERRIDE, false);
    	vol_mouse_up = sharedPref.getString(SettingsActivity.VOLROCK_UP, "MOUSE1");
    	vol_mouse_down = sharedPref.getString(SettingsActivity.VOLROCK_DOWN, "MOUSE3");
    	mouseSensitivityPercent = sharedPref.getInt(SettingsActivity.MOUSE_SENSE, 100);
    	
    	// Update internal functionality
    	gesture.setIsLongpressEnabled(longpress_enable);
    	if(touchpad_buttons_enable) {
    		mouseL.setVisibility(View.VISIBLE);
    		mouseR.setVisibility(View.VISIBLE);
    	} else {
    		mouseL.setVisibility(View.GONE);
    		mouseR.setVisibility(View.GONE);
    	}
	}
    
    @Override
    public void onCreateOptionsMenu(Menu menu,  MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);

    	// Remove the mouse button from Menu if it exists
    	if(menu.findItem(R.id.menu_mouse) != null){
    		menu.removeItem(R.id.menu_mouse);
    	}
    }
	
    
    
    //**********************************
    //********  Mouse Control Functions
    //********

    /**
     * Called from containing activity to check keys being held down
     * Mouse Fragment checks for volume button presses if volume clicking
     * is enabled in the preferences.
     * @param keyCode - integer representing the event
     * @param event - The key event being checked
     * @return whether or not the event was handled.
     */
    public boolean checkKeyDown(int keyCode, KeyEvent event){
    	if(!vol_mouse_enable) return false;
    	
    	switch(keyCode){
    	case KeyEvent.KEYCODE_VOLUME_UP:
    		if(!vol_up_click){
    			mCallback.mouseClick(true, true);
    			vol_up_click = true;
    		}
    		return true;
    		
    	case KeyEvent.KEYCODE_VOLUME_DOWN:
    		if(!vol_down_click){
    			mCallback.mouseClick(false, true);
    			vol_down_click = true;
    		}
    		return true;
    		
    	default:
    		return false;
    	}
    }
    
    /**
     * Called from containing activity to check keys being released
     * Mouse Fragment checks for volume button presses if volume clicking
     * is enabled in the preferences.
     * @param keyCode - integer representing the event
     * @param event - The key event being checked
     * @return whether or not the event was handled.
     */
    public boolean checkKeyUp(int keyCode, KeyEvent event){
    	if(!vol_mouse_enable) return false;
    	
    	switch(keyCode){
    	case KeyEvent.KEYCODE_VOLUME_UP:
    		mCallback.mouseClick(true, false);
			vol_up_click = false;
    		return true;
    		
    	case KeyEvent.KEYCODE_VOLUME_DOWN:
    		mCallback.mouseClick(false, false);
			vol_down_click = false;  		
    		return true;
    		
    	default:
    		return false;
    	}
    }
    
    /**
     * Listens for gestures that relate to mouse clicks and drags
     */
    class MouseGestureListener extends SimpleOnGestureListener {  
    	private boolean is_double_click = false;
    	private int lastX, lastY;
    	private static final int DOUBLE_CLICK_MOVE_THRESH = 2;
    	
        /**
         * Used to detect mouse clicks
         */
    	@Override
    	public boolean onSingleTapConfirmed(MotionEvent e){
    		mCallback.mouseClick(true,true);
    		mCallback.mouseClick(true,false);
    		surface.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    		return true;
    	}
    	
    	/**
         * Used to detect mouse click and drag (when user double taps then drags)
         */
    	@Override
    	public boolean onDoubleTapEvent(MotionEvent e){
    		int curX = (int)e.getRawX();
    		int curY = (int)e.getRawY();
    		
    		if(e.getAction() == MotionEvent.ACTION_DOWN){
    			is_double_click = true;
    			gesture.setIsLongpressEnabled(false);
    			mCallback.mouseClick(true, true);
    			surface.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    		}else if(e.getAction() == MotionEvent.ACTION_MOVE){
    			if(Math.abs(curX - lastX) > DOUBLE_CLICK_MOVE_THRESH || Math.abs(curY - lastY) > DOUBLE_CLICK_MOVE_THRESH) {
    				// A drag has exceeded the threshold, gesture is not a double click
    				is_double_click = false;
    			}
        			
    		}else if(e.getAction() == MotionEvent.ACTION_UP){
    			if(is_double_click){
    				mCallback.mouseClick(true, false);
    				mCallback.mouseClick(true, true);
    			}
    			mCallback.mouseClick(true, false);
    			if(longpress_enable) {
    				gesture.setIsLongpressEnabled(true);
    			}
    		}
			lastX = (int)e.getRawX();
			lastY = (int)e.getRawY();
    		return true;
    	}
    	
    	/**
         * Used to detect right mouse clicks
         */
    	@Override
    	public void onLongPress(MotionEvent e){
    		mCallback.mouseClick(false,true);
    		mCallback.mouseClick(false,false);
    		surface.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    	}
    }
    
    
    /**
     * Touch Listener for on screen mouse buttons
     */
    class MouseTouchListener implements OnTouchListener {
    	private boolean mousebutton;
    	/*
    	 * Default constructor assumes left click
    	 */
    	public MouseTouchListener() {
    		mousebutton = true;
    	}
    	
    	public MouseTouchListener(String mousebutton) {
    		if(mousebutton.equals("MOUSE1")) {
    			this.mousebutton = true;
    		} else if(mousebutton.equals("MOUSE3")){
    			this.mousebutton = false;
    		} else {
    			 // TODO Mouse buttons should be an enum/int type, and clickMouse should not take boolean
    			this.mousebutton = true;
    		}
    		
    	}
    	@Override
    	public boolean onTouch(View v, MotionEvent event) {
    		switch(event.getAction()) {
    		case MotionEvent.ACTION_DOWN:
    			mCallback.mouseClick(mousebutton,true);
    			v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    			return false;
    		case MotionEvent.ACTION_UP:
    			mCallback.mouseClick(mousebutton,false);
    			return false;
    		default:
    			return false;
    		}
    	}
    }
    
    
    /**
     * The onTouch listener used by the control UI's surface
     */
    private OnTouchListener ontouch = new OnTouchListener(){
    	private int lastX, lastY;
    	double multiplier;
    	
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
    			lastX = (int) motionEvent.getX();
    			lastY = (int) motionEvent.getY();
    			multiplier = ((double)mouseSensitivityPercent)/100.0;
    			
    		}else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){
    			int moveX = (int)(motionEvent.getX() - lastX);
    			int moveY = (int)(motionEvent.getY() - lastY);
    			
    			
    			if(Math.abs(moveX) > MOUSE_MULT_THRESH ){
    				moveX = (int)(moveX * multiplier);
    			}
    			
    			if(Math.abs(moveY) > MOUSE_MULT_THRESH){
    				moveY = (int)(moveY * multiplier);
    			}
    			
    			mCallback.mouseMove(moveX, moveY);
    			
    			lastX = (int) motionEvent.getX();
    			lastY = (int) motionEvent.getY();
    		}
			
			gesture.onTouchEvent(motionEvent);		    
			return true;
		}
    };


    //**********************************
    //********  Drawing marks on surface
    //********
    
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		tryDrawing(holder);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		tryDrawing(holder);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {}
    
    private void tryDrawing(SurfaceHolder holder){
    	Canvas canvas = holder.lockCanvas();
    	if (canvas == null) {
            Log.e(getClass().getSimpleName(), "Cannot draw onto the canvas as it's null");
        } else {
        	drawMouseMarkings(canvas);
            holder.unlockCanvasAndPost(canvas);
        }
    }
    
    private void drawMouseMarkings(final Canvas canvas){
    	final int MARKING_SCALE = 8;
    	
    	//width and height factors
    	float wf = canvas.getWidth() / MARKING_SCALE;
    	float hf = canvas.getHeight() / MARKING_SCALE;
    	
    	Paint paint = new Paint();
    	paint.setColor(Color.DKGRAY);
    	paint.setStrokeWidth(2.5f);
    	paint.setStyle(Paint.Style.STROKE);
    	paint.setStrokeJoin(Paint.Join.ROUND);
    	paint.setStrokeCap(Paint.Cap.ROUND);
    	paint.setPathEffect(new CornerPathEffect(6));
    	paint.setAntiAlias(true);
    	
    	Path path = new Path();
    	path.moveTo((1*wf), (2*hf));
    	path.lineTo((1*wf), (1*hf));
    	path.lineTo(2*wf, (1*hf));
    	
    	path.moveTo((6*wf), (1*hf));
    	path.lineTo((7*wf), (1*hf));
    	path.lineTo(7*wf, (2*hf));
    	
    	path.moveTo((1*wf), (6*hf));
    	path.lineTo((1*wf), (7*hf));
    	path.lineTo(2*wf, (7*hf));
    	
    	path.moveTo((6*wf), (7*hf));
    	path.lineTo((7*wf), (7*hf));
    	path.lineTo(7*wf, (6*hf));
    	
    	canvas.drawPath(path, paint);
      	
    }

    
}
