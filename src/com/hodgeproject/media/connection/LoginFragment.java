package com.hodgeproject.media.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.BitSet;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.hodgeproject.media.R;


/**
 * Fragment that finds and displays servers
 * @author BMCJ
 *
 */
public class LoginFragment extends SherlockFragment {
	public static final String ARG_HIDE_STARTED = "HIDE STARTED BUTTON";
	
	MCDBAdapter adapter;
	Cursor cursor;
	
	LoginFragmentListener mCallback;
	LinearLayout serverListLayout;
	LinearLayout favoriteListLayout;
	
	View lastConMenuView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
		
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_login, container, false);
		
    	// Check supplied arguments if the Getting Started button should be hidden
        if((getArguments()!= null) && getArguments().containsKey(ARG_HIDE_STARTED)){
			view.findViewById(R.id.button_started).setVisibility(View.INVISIBLE);
		} else {
			view.findViewById(R.id.button_started).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					mCallback.openStarted();
				}
			});
			
		}
        favoriteListLayout = (LinearLayout) view.findViewById(R.id.favoriteLayout1);
        refreshFavoriteList();
        
		serverListLayout = (LinearLayout) view.findViewById(R.id.serverLayout1);
		refreshServerList();
		return view;
	}
	
    @Override
    public void onAttach(Activity activity){
    	super.onAttach(activity);
    	
    	try{
    		mCallback = (LoginFragmentListener) activity;
    	} catch (ClassCastException e){
    		throw new ClassCastException(activity.toString() + " must implement LoginFragmentListener"); 
    	}
    }
	
    @Override
    public void onCreateOptionsMenu(Menu menu,  MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
    	inflater.inflate(R.menu.fragment_login, menu);
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    		switch (item.getItemId()) {
    			case R.id.menu_refresh:
    				refreshServerList();
    	            return true;
    			case R.id.menu_add:
    				mCallback.openFavorite(true);
    				return true;
    			default:
    				return super.onOptionsItemSelected(item);
    	      }

    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

    	// Store the view of the last button pressed (Must use old way of referencing contextItemSelected for older android versions)
    	lastConMenuView = v;
    	
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	menu.setHeaderTitle(R.string.menu_title_favorite);
    	
    	// Check item selected for a favorite id tag (A Favorite Item)
    	// Otherwise treat it as a regular server list item
    	if(v.getTag(R.id.favorite_id_tag) != null){
    		// show choice - edit favorite or delete favorite
    		menu.add(R.string.menu_option_favorite_edit);
    		menu.add(R.string.menu_option_favorite_delete);
    	} else {
    		// show choice - add the item to the favorite list
    		menu.add(R.string.menu_option_favorite_add);
    	}
    }
    
    
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item){
    	
    	if(item.getTitle().equals(getString(R.string.menu_option_favorite_edit)) ) {
    		// TODO: send intent to favorite fragment with ID in it
    		// lastConMenuView
    		return true;
    	} else if(item.getTitle().equals(getString(R.string.menu_option_favorite_delete))){
    		int x = Integer.parseInt((String)lastConMenuView.getTag(R.id.favorite_id_tag));
    		adapter.deleteEntry( x );
    		// refresh the new favorite list
    		refreshFavoriteList();
    		return true;
    	} else if(item.getTitle().equals(getString(R.string.menu_option_favorite_add))){
    		// TODO: Gather data to add a new favorite item
    		String sname = (String)lastConMenuView.getTag(R.id.favorite_name_tag);
    		String saddr = (String)lastConMenuView.getTag(R.id.favorite_addr_tag);
    		adapter.insertEntry(sname, saddr, null);
    		// refresh the new favorite list
    		refreshFavoriteList();
    		return true;
    	}
    	
    	Toast.makeText(getActivity(), "Error context", Toast.LENGTH_SHORT).show();
    	return super.onContextItemSelected(item);
    }
    
    
    /**
     * Refresh and Rebuild the server list
     */
    private void refreshServerList(){
    	if(serverListLayout==null) return;
    	new LocateServerTask(this,serverListLayout).execute();
    }
    
    
    /**
     * Refresh and Rebuild the Favorites list
     */
    public void refreshFavoriteList(){
    	if(favoriteListLayout==null) return;
    	
    	adapter = new MCDBAdapter(getSherlockActivity());
    	
    	favoriteListLayout.removeAllViews();
    	cursor = adapter.queryAll();
    	while (cursor.moveToNext()) {
    	    
    		Button but = new Button(getSherlockActivity().getApplicationContext());
			but.setText(cursor.getString(1)+" ("+cursor.getString(2)+")");
			
			
			// Store the entry ID in the TAG
			but.setTag(R.id.favorite_id_tag, ""+ cursor.getInt(0));
			but.setTag(R.id.favorite_name_tag, cursor.getString(1));
			but.setTag(R.id.favorite_addr_tag, cursor.getString(2));
			
			// TODO: Handle password storing
			
			//if has password
			String pass = cursor.getString(3);
			if(pass != null && !pass.equals("")){
				but.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite, 0, R.drawable.ic_password, 0);
				but.setTag(R.id.require_password_tag,String.valueOf(true));
				but.setTag(R.id.favorite_pswd_tag,pass);
			} else {
				but.setTag(R.id.require_password_tag,String.valueOf(false));
				but.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite, 0, 0, 0);
			}
			
        	but.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        	but.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					if(arg0.getTag(R.id.favorite_pswd_tag) != null){
						mCallback.onConnection((String)arg0.getTag(R.id.favorite_addr_tag),(String)arg0.getTag(R.id.favorite_pswd_tag)); 
					} else {
						mCallback.onConnection((String)arg0.getTag(R.id.favorite_addr_tag), false);
					}
				}
        	});

        	favoriteListLayout.addView(but);
    		
        	registerForContextMenu(but);
    	}
    	cursor.close();
    	adapter.close();
    	favoriteListLayout.invalidate();
    	
    }
    
    /**
     *  Interface for starting the connection
     *  TODO: rename the interface
     */
    public interface LoginFragmentListener {
    	public void openStarted();
    	public void openFavorite(boolean isNew);
    	public void onConnection(String address, boolean askPassword);
    	public void onConnection(String address, String password);
    }
    
    /**
     * AsyncTask that discovers running servers on network to connect to.
     * @author Bryan Hodge
     *
     */
    private static class LocateServerTask extends AsyncTask<Void,String,Void>{
    	private static final int SERVERBCAST_PORT = 4497; 		// Port for TCP/Multicast traffic to listen on
    	private static final int CLIENT_PORT = 4499;			// Port the client listens to Multicast traffic on 
    	private static final String GROUP_ADDR = "230.1.1.1";	// Address of Multicast group to join
    	private static final int BCAST_TIMEOUT = 2000;			// Timeout for socket
    	
    	private static volatile boolean searching = false;		// Used to restrict conflicts between threads
    	
    	private LoginFragment activity;							// The activity
    	private LinearLayout servLayout;						// The Layout for the server list
    	
    	/*
    	 * Constructor
    	 */
    	public LocateServerTask(LoginFragment loginFragment,LinearLayout servLayout){
    		this.activity = loginFragment;
    		this.servLayout = servLayout;
    	}
    	
    	
    	/**
    	 * This runs in another thread
    	 */
		@Override
		protected Void doInBackground(Void... arg0) {
			// Make sure another thread isn't already searching
			if(searching){
				return null;
			} else {
				searching = true;
			}
			
			MulticastSocket mcastSock;
			try{		
				mcastSock = new MulticastSocket(CLIENT_PORT);
				mcastSock.joinGroup(InetAddress.getByName(GROUP_ADDR));
				mcastSock.setSoTimeout(BCAST_TIMEOUT);
				
				byte[] msg = "request".getBytes();
				DatagramPacket lookHere = new DatagramPacket(msg, msg.length, InetAddress.getByName(GROUP_ADDR), SERVERBCAST_PORT);
				mcastSock.send(lookHere);	
				
				//Loop for responses till timeout
				while(true){
					try{
						byte[] buf = new byte[512];
						DatagramPacket recv = new DatagramPacket(buf, buf.length);
						mcastSock.receive(recv);
						
						// Check if the passRequird bit is set in the first byte from server
						boolean passRequired = ( (recv.getData()[0] & 1) != 0);
						
						String servName = new String(recv.getData(),1,recv.getLength()-1);						
						publishProgress(String.valueOf(passRequired), servName, recv.getAddress().getHostAddress());
							
					} catch ( SocketTimeoutException e){
						// Our socket will only listen for a few seconds and then timeout
						mcastSock.leaveGroup(InetAddress.getByName(GROUP_ADDR));
						mcastSock.close();
						break;
					}
				}
			} catch(IOException e){	
				//Log.e(TAG, e.getMessage());
			}
			
			searching = false;
			return null;
		}
		
		protected void onPreExecute(){
			if(!searching){
				servLayout.removeAllViews();
			}
		}
		
		protected void onProgressUpdate(final String... values) {
			boolean passRequired = Boolean.parseBoolean(values[0]);
			
			Button but = new Button(activity.getSherlockActivity().getApplicationContext());
			but.setText(values[1]+" ("+values[2]+")");
			but.setTag(R.id.favorite_name_tag, values[1]);
			but.setTag(R.id.favorite_addr_tag, values[2]);
			but.setTag(R.id.require_password_tag, values[0]);
			if(passRequired){
				but.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_password, 0);
			}
        	but.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        	but.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					//activity.startControl(values[1]);
					String needPass = (String)arg0.getTag(R.id.require_password_tag);
					
					if(Boolean.parseBoolean(needPass)){
						activity.mCallback.onConnection(values[2],true );
					} else {
						activity.mCallback.onConnection(values[2], false);
					}
					
				}
        	});

        	servLayout.addView(but);
        	
        	
        	activity.registerForContextMenu(but); 
		}
		
		protected void onPostExecute(Void result) {
			servLayout.invalidate();
		}

    }
    
}
