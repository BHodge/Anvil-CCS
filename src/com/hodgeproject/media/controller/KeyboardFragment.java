package com.hodgeproject.media.controller;


import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.hodgeproject.media.R;

/**
 * KeyboardFragment creates/manages the custom soft keyboard
 * manages open/closing and user interaction
 * Parent Activity must implement the KeyboardFragmentListener interface.
 * @author Bryan Hodge
 *
 */
public class KeyboardFragment extends SherlockFragment {

	private static final int KEYCODE_COMMAND = -200;
	private static final int KEYCODE_SYMBOLS = -201;

	private KeyboardFragmentListener mCallback;
	
	private KeyboardView mInputView;
	
	private Keyboard qwertyKeyboard;
	private Keyboard qwertyShiftKeyboard;
	private Keyboard commandKeyboard;
	private Keyboard symbolKeyboard;
	private Keyboard symbolShiftKeyboard;

	
	/**
	 * Interface the parent Activity will need
	 * to implement to respond to Keyboard events
	 * @author Bryan Hodge
	 */
	public interface KeyboardFragmentListener {
		public void pressKey (int keycode);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
    }
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.fragment_keyboard, container, false);
    	
    	qwertyKeyboard = new Keyboard(getActivity(),R.xml.qwertykbd);
    	qwertyShiftKeyboard = new Keyboard(getActivity(),R.xml.qwertykbd_shift);
    	commandKeyboard = new Keyboard(getActivity(),R.xml.commandkbd);
    	symbolKeyboard = new Keyboard(getActivity(),R.xml.symbolkbd);
    	symbolShiftKeyboard = new Keyboard(getActivity(),R.xml.symbolkbd_shift);
    	
    	
    	mInputView= (MultiKeyboardView)view.findViewById(R.id.keyboardview);
    	mInputView.setKeyboard( qwertyKeyboard );
    	mInputView.setPreviewEnabled(false);
    	
    	mInputView.setOnKeyboardActionListener(mOnKeyboardActionListener);
    	
    	return view;
    }
    
	@Override
    public void onAttach(Activity activity){
    	super.onAttach(activity);
    	
    	try{
    		mCallback = (KeyboardFragmentListener) activity;
    	} catch (ClassCastException e){
    		throw new ClassCastException(activity.toString() + " must implement KeyboardFragmentListener"); 
    	}
    }
	
   @Override
    public void onCreateOptionsMenu(Menu menu,  MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
    	inflater.inflate(R.menu.fragment_keyboard, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    		switch (item.getItemId()) {
    			case R.id.menu_keyboard:
    				// Toggle keyboard
    				
    				if(mInputView.getVisibility() == View.VISIBLE){
    					mInputView.setVisibility(View.GONE);
    					mInputView.setEnabled(false);
    				} else {
    					mInputView.setVisibility(View.VISIBLE);
    					mInputView.setEnabled(true);
    				}
    				
    	            return true;
    			default:
    				return super.onOptionsItemSelected(item);
    	      }
    }	
    
    
    private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {
    	
        @Override 
        public void onKey(int primaryCode, int[] keyCodes) {
        	Log.i(getClass().getSimpleName(), "Send Keycode: " + primaryCode);
        	
        	if (primaryCode == Keyboard.KEYCODE_SHIFT) {
                handleShift();
            }else if(primaryCode == KEYCODE_COMMAND){
            	//swap keyboards and disable shift
            	Keyboard current = mInputView.getKeyboard();
            	if(current == commandKeyboard){
            		qwertyShiftKeyboard.setShifted(false);
            		qwertyKeyboard.setShifted(false);
            		mInputView.setKeyboard(qwertyKeyboard);
            	} else {
            		qwertyShiftKeyboard.setShifted(false);
            		qwertyKeyboard.setShifted(false);
            		mInputView.setKeyboard(commandKeyboard);
            	}
            } else if(primaryCode == KEYCODE_SYMBOLS){
            	//swap keyboards and disable shift
            	Keyboard current = mInputView.getKeyboard();
            	if(current == symbolKeyboard || current == symbolShiftKeyboard){
            		qwertyShiftKeyboard.setShifted(false);
            		qwertyKeyboard.setShifted(false);
            		symbolKeyboard.setShifted(false);
            		symbolShiftKeyboard.setShifted(false);
            		mInputView.setKeyboard(qwertyKeyboard);
            	} else {
            		qwertyShiftKeyboard.setShifted(false);
            		qwertyKeyboard.setShifted(false);
            		symbolKeyboard.setShifted(false);
            		symbolShiftKeyboard.setShifted(false);
            		mInputView.setKeyboard(symbolKeyboard);
            	}
            } else {
            	// Notify parent of the keypress
            	mCallback.pressKey(primaryCode);
            }
        	
        }

        @Override public void onPress(int arg0) {}

        @Override public void onRelease(int primaryCode) {}

        @Override public void onText(CharSequence text) {}

        @Override public void swipeDown() {}

        @Override public void swipeLeft() {}

        @Override public void swipeRight() {}

        @Override public void swipeUp() {}
    };
    
    
    /**
     * Handle the swapping of appropriate keyboards
     * when shift key is pressed
     */
    private void handleShift(){
    	if(mInputView == null){
    		return;
    	}
    	
    	Keyboard currentKeyboard = mInputView.getKeyboard();
    	
    	if(currentKeyboard == qwertyKeyboard){
    		qwertyKeyboard.setShifted(true);
    		mInputView.setKeyboard(qwertyShiftKeyboard);
    		qwertyShiftKeyboard.setShifted(true);
    		
    	}else if(currentKeyboard == qwertyShiftKeyboard){
    		qwertyShiftKeyboard.setShifted(false);
    		mInputView.setKeyboard(qwertyKeyboard);
    		qwertyKeyboard.setShifted(false);
    		
    	}else if(currentKeyboard == symbolKeyboard){
    		symbolKeyboard.setShifted(true);
    		mInputView.setKeyboard(symbolShiftKeyboard);
    		symbolShiftKeyboard.setShifted(true);
    		
    	}else if (currentKeyboard == symbolShiftKeyboard){
    		symbolShiftKeyboard.setShifted(false);
    		mInputView.setKeyboard(symbolKeyboard);
    		symbolKeyboard.setShifted(false);
    	}
    } 
    
    
    //***********************************
    //********  Public Keyboard functions
    //********
    
    public boolean isKeyboardVisible(){
    	if(mInputView.getVisibility() == View.VISIBLE){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public void closeKeyboard(){
		mInputView.setVisibility(View.GONE);
		mInputView.setEnabled(false);
    }
    
}
