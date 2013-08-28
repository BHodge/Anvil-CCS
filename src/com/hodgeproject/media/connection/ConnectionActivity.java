package com.hodgeproject.media.connection;

import java.lang.reflect.Field;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.hodgeproject.media.R;
import com.hodgeproject.media.controller.ControllerActivity;


/**
 * Connection Activity Displays the local broadcasting devices, devices from the favorite list
 * and initiates a connection (sends intent to ControllerActivity). Serves as the entry
 * point in the App.
 * @author Bryan Hodge
 *
 */
public class ConnectionActivity extends SherlockFragmentActivity 
implements LoginFragment.LoginFragmentListener, FavoriteFragment.FavoriteFragmentListener {
	
	public static final int DEVICE_VERSION   = Build.VERSION.SDK_INT;
	public static final int DEVICE_HONEYCOMB = Build.VERSION_CODES.HONEYCOMB;
	public static final String LOGINFRAG_TAG = "LoginFragTag";
	public static final String FAVFRAG_TAG = "Favorite Tag";
	public static final String STARTFRAG_TAG = "Getting Started Tag";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connection);
		
		//ABS change
        if (DEVICE_VERSION >= DEVICE_HONEYCOMB){
        	try {
                ViewConfiguration config = ViewConfiguration.get(this);
                Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
                if(menuKeyField != null) {
                    menuKeyField.setAccessible(true);
                    menuKeyField.setBoolean(config, false);
                }
            } catch (Exception ex) {
                // Ignore
            }
        }
        
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        /*
        //--- TODO: Debug
        float density  = getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth  = outMetrics.widthPixels / density;
        Log.i(getClass().getSimpleName(),"dpHeight: "+dpHeight+" dpWidth: "+dpWidth+" Density: "+density);
        Log.i(getClass().getSimpleName(), "Two pane? :" + getResources().getBoolean(R.bool.has_two_panes));
        //---
         */

     
		// Do not recreate fragments unless this is the first instance
		if (savedInstanceState != null) {
			return;
		}

        //setup the view!
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        
        LoginFragment fragment1 = new LoginFragment();  
        
        // Manage two fragments being show for large screens
        if( getResources().getBoolean(R.bool.has_two_panes) ){
        	StartedFragment fragment2 = new StartedFragment();
        	
        	// tell fragment1 (login) to not show the getting started button
        	Bundle args = new Bundle();
        	args.putBoolean(LoginFragment.ARG_HIDE_STARTED, true);
        	fragment1.setArguments(args);
			
        	// tell fragment2 (getting started) to no show the ready button
        	Bundle args2 = new Bundle();
        	args.putBoolean(StartedFragment.ARG_HIDE_READY, true);
        	fragment2.setArguments(args2);
        	
            fragmentTransaction.add(R.id.fragment_container2,fragment2,STARTFRAG_TAG);
        }
        fragmentTransaction.add(R.id.fragment_container1,fragment1,LOGINFRAG_TAG);

        fragmentTransaction.commit();
	}
	
    
    //ABS Change
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	
    	// The Menu button on older versions of android
    	// need to open the Action Bar Sherlock overflow menu.
        if (DEVICE_VERSION < DEVICE_HONEYCOMB) {
            if (event.getAction() == KeyEvent.ACTION_UP &&
                keyCode == KeyEvent.KEYCODE_MENU) {
                openOptionsMenu();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
	
    @Override
    protected void onDestroy(){
    	super.onDestroy();
    }
    
    
    
    //**********************************
    //********  Callbacks from Fragments
    //********
    
    /**
     * Start the control activity
     */
    public void onConnection(final String serverAddress, boolean askPassword){
    	
    	
    	if(askPassword){
    		// Create an Alert Dialog that starts the controller Activity with 
    		// a provided password
    		LayoutInflater inflater = this.getLayoutInflater();
    		final View v = inflater.inflate(R.layout.dialog_password, null);
    		
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle("Password Required");
    		builder.setMessage("A password is required for this server.");
    		builder.setView(v);
    		builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent conAct = new Intent(ConnectionActivity.this, ControllerActivity.class);
					conAct.putExtra(ControllerActivity.EXTRA_SERVER_ADDRESS, serverAddress);
					EditText passText = (EditText) v.findViewById(R.id.password);
					conAct.putExtra(ControllerActivity.EXTRA_SERVER_PASSWORD, passText.getText().toString());
					startActivity(conAct);
					
					dialog.cancel();
				}
    			
    		});
    		builder.create().show();
    		
    	} else {
    		Intent conAct = new Intent(this, ControllerActivity.class);
    		conAct.putExtra(ControllerActivity.EXTRA_SERVER_ADDRESS, serverAddress);
    		startActivity(conAct);
    	}

    	
    }
    
    /**
     * Start the control activity with a password
     */
    public void onConnection(String serverAddress, String password){
    	//Intent conAct = new Intent(this, ControlActivity.class);
    	Intent conAct = new Intent(this, ControllerActivity.class);
    	conAct.putExtra(ControllerActivity.EXTRA_SERVER_ADDRESS, serverAddress);
    	conAct.putExtra(ControllerActivity.EXTRA_SERVER_PASSWORD, password);
    	startActivity(conAct);
    }
    
   
    /**
     * Add the favorite fragment, either edit or new
     */
    public void openFavorite(boolean isNew){

    	
    	FavoriteFragment frag = new FavoriteFragment();
    	FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    	
    	// Handle dual fragments on screen
    	if( getResources().getBoolean(R.bool.has_two_panes)  ){
    		transaction.replace(R.id.fragment_container2, frag, FAVFRAG_TAG);
    	} else {
    		transaction.replace(R.id.fragment_container1, frag, FAVFRAG_TAG);
    	}
    	//transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    	transaction.addToBackStack(FAVFRAG_TAG);
    	transaction.commit();
    	getSupportFragmentManager().executePendingTransactions();
    }
    
    /**
     * Swap the getting started fragment (only on small screen devices)
     */
    public void openStarted(){
    	StartedFragment frag = new StartedFragment();
    	FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    	
    	transaction.replace(R.id.fragment_container1, frag, STARTFRAG_TAG);
    	transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
    	transaction.addToBackStack(STARTFRAG_TAG);
    	transaction.commit();
    }


    /**
     * Refresh the favorite list in the login fragment (if it exists)
     */
	@Override
	public void refreshFavoriteList() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment frag = fragmentManager.findFragmentByTag(LOGINFRAG_TAG);
		if((frag != null) && frag instanceof LoginFragment ){
			((LoginFragment)frag).refreshFavoriteList();
		}
	}
	
}
