package com.hodgeproject.media.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.hodgeproject.media.R;
import com.hodgeproject.media.service.ControlService;
import com.hodgeproject.media.service.LocalBinder;
import com.hodgeproject.media.service.ControlService.ControlListener;
import com.hodgeproject.mediacontroller.network.NetworkTypes.QLUpdate;

/**
 * Controller Activity opens the connection, creates/binds
 * to the ControlService and manages user interaction.
 * @author Bryan Hodge
 *
 */
public class ControllerActivity extends SherlockFragmentActivity implements
		MouseFragment.MouseFragmentListener,
		KeyboardFragment.KeyboardFragmentListener,
		QuickLaunchFragment.QuickLaunchFragmentListener{

	public static final int DEVICE_VERSION   = Build.VERSION.SDK_INT;
	public static final int DEVICE_HONEYCOMB = Build.VERSION_CODES.HONEYCOMB;
	public static final String EXTRA_SERVER_ADDRESS = "Address";
	public static final String EXTRA_SERVER_PASSWORD = "pass";
	public static final String TAG_MOUSE_FRAGMENT = "My Mouse Fragment";
	public static final String TAG_KEYBOARD_FRAGMENT = "My Keyboard Fragment";
	public static final String TAG_QUICKLAUNCH_FRAGMENT = "My Quicklaunch Fragment";
	
	private boolean isBound;
	private String servAddress;
	private String serverPass;
	private ControlService boundService;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_controller);
		
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
     
        getSupportActionBar().setSubtitle("Connecting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        isBound = false;
        serverPass = null;
          
		// Get the server's password
		if(getIntent().hasExtra(EXTRA_SERVER_PASSWORD)){
			serverPass = this.getIntent().getExtras().getString(EXTRA_SERVER_PASSWORD);
		}
		
		// get server address
		if(getIntent().hasExtra(EXTRA_SERVER_ADDRESS)){        	
			servAddress = this.getIntent().getExtras().getString(EXTRA_SERVER_ADDRESS);
			startService(new Intent(this, ControlService.class));
			//bind to the service to expose richer API
			doBindService();
		} else {
			Log.e(this.getClass().getName(),"Controller activity wasn't provided extra server address");
			
			finish();
		}
		
		// Do not recreate fragments unless this is the first instance
        if (savedInstanceState != null) {
            return;
        }
        
        
		FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        
        MouseFragment mfrag = new MouseFragment();
        KeyboardFragment kfrag = new KeyboardFragment();
        
        if( getResources().getBoolean(R.bool.has_two_panes) ){
        	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        	
        	QuickLaunchFragment ql = new QuickLaunchFragment();
			ft.add(R.id.fragment_container2, ql, TAG_QUICKLAUNCH_FRAGMENT);
			ft.commit();
			
        }
        fragmentTransaction.add(R.id.fragment_container1,mfrag,TAG_MOUSE_FRAGMENT);
        
        fragmentTransaction.add(R.id.keyboard_container1,kfrag,TAG_KEYBOARD_FRAGMENT);
        fragmentTransaction.commit();
	}
	
	
	//ABS change
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// we use the same menu as the connection activity -- for now..
    	getSupportMenuInflater().inflate(R.menu.activity_controller, menu);
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	FragmentTransaction ft;
    	
		switch (item.getItemId()) {
		case R.id.menu_mouse:
			ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.fragment_container1, new MouseFragment(), TAG_MOUSE_FRAGMENT);
			getSupportFragmentManager().popBackStack();
			ft.commit();
			return true;
		case R.id.menu_launch:
			ft = getSupportFragmentManager().beginTransaction();
			QuickLaunchFragment ql = new QuickLaunchFragment();
			ft.replace(R.id.fragment_container1, ql, TAG_QUICKLAUNCH_FRAGMENT);
			ft.addToBackStack(null);
			ft.commit();
			return true;
		case R.id.menu_pref:
			Intent startSettingsIntent = new Intent(this,
					SettingsActivity.class);
			startActivity(startSettingsIntent);
			return true;
		case android.R.id.home:
			doUnbindService();
			stopService(new Intent(this, ControlService.class));
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}
        
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
    	// MouseFragment may be looking for volume button events
    	MouseFragment mf = (MouseFragment) getSupportFragmentManager().findFragmentByTag(TAG_MOUSE_FRAGMENT);
    	if(mf != null && mf.checkKeyDown(keyCode, event)){
    		return true;
    	}
    	
    	return super.onKeyDown(keyCode, event);
    }
    
    //ABS Change
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	// MouseFragment may be looking for volume button events
    	MouseFragment mf = (MouseFragment) getSupportFragmentManager().findFragmentByTag(TAG_MOUSE_FRAGMENT);
    	if(mf != null && mf.checkKeyUp(keyCode, event)){
    		return true;
    	}
    	
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
	
    
    /**
     * The back button is overridden to allow disconnecting
     * from Service, swapping fragments and closing the
     * custom soft keyboard.
     */
    @Override
    public void onBackPressed(){
    
    	KeyboardFragment kf = (KeyboardFragment) getSupportFragmentManager().findFragmentByTag(TAG_KEYBOARD_FRAGMENT);
    	    	
    	// FragmentTransaction commits don't always update back stack count
    	getSupportFragmentManager().executePendingTransactions();
    	
    	// Check that no fragments exist in back stack
    	// before overriding the back button functionality
    	if(kf != null && kf.isKeyboardVisible()){
    		kf.closeKeyboard();
    	}else if(getSupportFragmentManager().getBackStackEntryCount() > 0){
    		super.onBackPressed();
    	} else {
			super.onBackPressed();
	    	doUnbindService();
	    	stopService(new Intent(this, ControlService.class));
	    	finish();
    	}
    }
    
    @Override
    public void onDestroy(){
    	doUnbindService();
    	isBound = false;
    	super.onDestroy();
    }
  
    //**********************************
    //********  Connection to Service
    //********
    
    public void doBindService() {
    	Intent startSvc = new Intent(this, ControlService.class);
    	bindService(startSvc, mConnection, Context.BIND_AUTO_CREATE);
    }
    
    public void doUnbindService() {
        if (isBound) {
            // Detach our existing connection.
        	boundService.removeControlListener(contListener);
            unbindService(mConnection);
            isBound = false;
        }
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
        	
        	if(service instanceof LocalBinder<?>){
        		Object ob = ((LocalBinder<?>)service).getService();
        		if(ob instanceof ControlService){
        			boundService = (ControlService)ob;
        		}
        	}
            
            //check connection
            boundService.addControlListener(contListener);
            
            if(serverPass != null){
            	boundService.openConnection(servAddress,serverPass); 
            }else {
            	boundService.openConnection(servAddress); 
            }
            
            
            isBound = true;
            
            // If the Quick Launch Fragment is attached and the screen is rotated
            // it will need to be manually updated (when the activity is re-bound to the service)
            QuickLaunchFragment qlf = (QuickLaunchFragment) ControllerActivity.this.getSupportFragmentManager().findFragmentByTag(TAG_QUICKLAUNCH_FRAGMENT);
            if(qlf != null){
            	qlf.setQLList(ControllerActivity.this.getQLList());
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            //boundService = null;
            isBound = false;
            ControllerActivity.this.finish();
        }
    };  
    

    //**********************************
    //********  Callbacks from Service
    //********
    
    private ControlListener contListener = new ControlListener(){
    	
    	public void connected(String address) {
    		View v = ControllerActivity.this.findViewById(R.id.connectBar);
    		if( v != null ){
    			v.setVisibility(View.GONE);
    		}
    		//ControllerActivity.this.setTitle(ControllerActivity.this.getTitle() + ": Connected");
    		ControllerActivity.this.getSupportActionBar().setSubtitle("Connected "+address);
    	}      

    	public void handleAlert(String title, String msg) {
    		CharSequence text = title + " - " + msg;
    		Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
    		toast.show();
    		boundService.removeControlListener(this);
    		stopService(new Intent(ControllerActivity.this, ControlService.class));
    		finish();
    	}

    	@Override
    	public void launchListUpdated(int index) {
    		
    		QuickLaunchFragment qlf = (QuickLaunchFragment) getSupportFragmentManager().findFragmentByTag(TAG_QUICKLAUNCH_FRAGMENT);
    		if(qlf != null){
    			qlf.setQLUpdate(boundService.getLaunchItem(index));
    		}
    	}
    };

  
    //**********************************
    //********  Callbacks from Fragments
    //********
    
	@Override
	public void mouseClick(boolean l_click, boolean heldDown) {
		if(!isBound) return;
		boundService.clickMouse(l_click, heldDown);
	}

	@Override
	public void mouseMove(int x, int y) {
		if(!isBound) return;
		boundService.moveMouse(x, y);
	}


	@Override
	public void pressKey(int keycode) {
		if(!isBound) return;
		boundService.pressKey(keycode);
		
	}

	@Override
	public ArrayList<QLUpdate> getQLList() {
		if(!isBound) return null;
		return boundService.getLaunchList();
	}
	
	@Override
	public void sendRequest(int qlIndex){
		if(isBound){
			boundService.sendQuickLaunch(qlIndex);
		}
	}
}
