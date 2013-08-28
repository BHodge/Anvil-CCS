package com.hodgeproject.media.service;



import java.io.IOException;
import java.util.ArrayList;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.hodgeproject.media.R;
import com.hodgeproject.media.controller.ControllerActivity;
import com.hodgeproject.mediacontroller.network.NetworkTypes;
import com.hodgeproject.mediacontroller.network.NetworkTypes.Click;
import com.hodgeproject.mediacontroller.network.NetworkTypes.ClientPass;
import com.hodgeproject.mediacontroller.network.NetworkTypes.Keypress;
import com.hodgeproject.mediacontroller.network.NetworkTypes.Move;
import com.hodgeproject.mediacontroller.network.NetworkTypes.QLRequest;
import com.hodgeproject.mediacontroller.network.NetworkTypes.QLUpdate;
import com.hodgeproject.mediacontroller.network.NetworkTypes.ServerMsg;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * ControlService connects to a supplied address running an Anvil Control server
 * and manages all communication.
 * @author Bryan Hodge
 *
 */
public class ControlService extends Service {
	public static final String TAG = "MediaController";
	
	//Message Id's used by handler
   	public static final int UNKNOWN_HOST = 1;
	public static final int CONNECTING_ERROR = 2;
	public static final int PASSWORD_REQ = 3;
	public static final int CONNECTION_LOST = 5;
	public static final int CONNECTING_SUCCESS = 6;
	public static final int LAUNCH_UPDATE = 7;
	public static final int PASSWORD_WRONG = 8;
	
	// Server Settings
	public static final int TIMEOUT = 0;
	public static final int SERVER_PORT = 4498;

	// Service Flags
	public boolean bound;
	public boolean connectedToServer;
	
	private NotificationManager mNM;						// Used to set notifications
	private ArrayList<ControlListener> controlListeners;	// Callback for controls
	private Handler handler; 								// Handler for messages
	private Client client;									// KryoNet client
	private ArrayList<QLUpdate> launchListCache;			// Most recent launch list
	
	
    @Override
    public void onCreate() {
    	super.onCreate();
    	bound = false;
    	connectedToServer = false;
    	launchListCache = new ArrayList<QLUpdate>();

    	controlListeners = new ArrayList<ControlListener>();
    	handler = new Handler(new ControlCallback());
    	
    	// Create the client communication thread
    	client = new Client(8192, 6000); //FIXME: Buffer sizes should be constants
    	
    	// Register the sendable data types with client
    	NetworkTypes.register(client);
    	
    	// Handled 
    	client.addListener(new MyListener());
    	client.start();
    	    	
    	// Start our notification
    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    	showNotification(false);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	// Remove the notification
    	mNM.cancel(R.string.favorite);	
    	Log.i(getClass().getSimpleName(),"Service being destroyed");
    	
    	closeConnection();
    }

    
    /**
     * Adds/shows the Service Notification to the notification bar
     */
    private void showNotification(boolean connected){
    	
    	NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
    		.setSmallIcon(R.drawable.ic_notify)
    		.setContentTitle(getString(R.string.app_name))
    		.setOngoing(true);
    	
    	if(connected){
    		mBuilder.setContentText(getString(R.string.notification_connected));
    	} else {
    		mBuilder.setContentText(getString(R.string.notification_connecting));
    	}
    	
    	// Activity to open  	
    	// Open the last activity from the stack (ConnectionActivity is the main activity)
    	Intent notificationIntent = new Intent(this, ControllerActivity.class);

    	PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    	mBuilder.setContentIntent(pendingIntent);

    	mNM.notify(R.string.favorite, mBuilder.build());
    }
    
    
    //**********************************
    //********  Public Service Functions
    //********
    
    
    /**
     * Open a connection to a server
     * @param address
     */
    public void openConnection(final String address){
    	
    	if(!client.isConnected()){
	        new Thread("Connect") {
	            public void run () {
	                    try {
                            client.connect(5000, address, 4498, 4498);
                            // Server communication after connection can go here, or in Listener#connected().
                            
                            /* Server waits for accept
                            connectedToServer = true;
                            Message msg = handler.obtainMessage();
	            			msg.what = ControlService.CONNECTING_SUCCESS;
	            			msg.obj = address;
	            			handler.sendMessage(msg);
	            			showNotification(true);
	            			*/
	            			
	                    } catch (IOException ex) {
	                    	// Could not connect                   	
	                    	// Send an error message out if any components are still listening
	            			Message msg = handler.obtainMessage();
	            			msg.what = ControlService.CONNECTING_ERROR;
	            			handler.sendMessage(msg);
	                    }
	            }
	        }.start();
    	} else {
    		// tell anyone listening that we are already connected
    		for(ControlListener cont : controlListeners) cont.connected(address);
    	}
    	
    }
    
    
    
    
    
    
    
    /**
     * Open a connection to a server
     * @param address
     */
    public void openConnection(final String address, final String password){
    	
    	if(!client.isConnected()){
	        new Thread("Connect") {
	            public void run () {
	                    try {
                            client.connect(5000, address, 4498, 4498);
                            // Server communication after connection can go here, or in Listener#connected().
                            
                            // send the password if supplied
                            ClientPass cpass = new ClientPass();
                            cpass.password = password;
                            client.sendTCP(cpass);
                            
                            Log.i(getClass().getSimpleName(),"Password sent to server"); //TODO: remove
	            			
	                    } catch (IOException ex) {
	                    	// Could not connect                   	
	                    	// Send an error message out if any components are still listening
	            			Message msg = handler.obtainMessage();
	            			msg.what = ControlService.CONNECTING_ERROR;
	            			handler.sendMessage(msg);
	                    }
	            }
	        }.start();
    	} else {
    		// tell anyone listening that we are already connected
    		for(ControlListener cont : controlListeners) cont.connected(address);
    	}
    	
    }
    
    
    
    
    
    
    
    
    /**
     * Close a connection to a server
     */
    public void closeConnection(){
    	connectedToServer = false;
    	if(client!=null){
    		
    		// Run the blocking call in a different thread (keep UI thread non-blocking)
    		new Thread("Disconnect") {
	            public void run () {
	            	client.stop();
	            }
    		}.start();
    	}
    }
    
    
    /**
     * Add a listener to the ControlService
     * @param cont
     */
    public void addControlListener(ControlListener cont){
    	controlListeners.add(cont);
    }
    
    
    /**
     * Remove a listener of the ControlService
     * @param cont
     */
    public void removeControlListener(ControlListener cont){
    	controlListeners.remove(cont);
    }
  
    
    /**
     * Send mouse movement to the server
     * @param x - Movement difference in X
     * @param y - Movement difference in Y
     */
    public void moveMouse(int x, int y){
    	if(!connectedToServer) return;
    	
    	final Move m = new Move();

    	m.x = x;
    	m.y = y;
    	
    	// Potentially blocks, run in its own thread
    	new Thread(new Runnable() {
    		public void run() {
    			client.sendUDP(m);
    		}
    	  }).start();
    }
    
    /**
     * Send a mouse click to the server
     * @param leftclick - The left mouse button was pressed (else right)
     * @param clickdown - If the button is down or up
     */
    public void clickMouse(boolean leftclick, boolean clickdown){
    	if(!connectedToServer) return;
    	
    	final Click c = new Click();
    	c.leftClick = leftclick;
    	c.clickDown = clickdown;
    	
    	// Potentially blocks, run in its own thread
    	new Thread(new Runnable() {
    		public void run() {
    			client.sendUDP(c);
    		}
    	}).start();
    }
    
    /**
     * Send a keycode to the server
     * @param keycode - key to push
     */
    public void pressKey(int keycode){
    	if(!connectedToServer) return;
    	
       	final Keypress k = new Keypress();
       	k.keycode = keycode;
    	
    	// Potentially blocks, run in its own thread
    	new Thread(new Runnable() {
    		public void run() {
    			client.sendTCP(k);
    		}
    	}).start();
    }
    
    /**
     * Get the most recent Quick Launch list
     */
    public ArrayList<QLUpdate> getLaunchList(){
    	return launchListCache;
    }
    
    /**
     * Get a single QLUpdate with the associated itemIndex
     * @param itemIndex - index from server NOT array position
     * @return
     */
    public QLUpdate getLaunchItem(int itemIndex){
    	for(QLUpdate qlu: launchListCache){
    		if(qlu.itemIndex == itemIndex){
    			return qlu;
    		}
    	}
    	return null;
    }
    
    /**
     * Get the list position of a specific QLUpdate
     * @param update - The QLUpdate that want to know the position of
     * @return  array position of update, -1 if not in array
     */
    public int getLaunchItemPosition(QLUpdate update){
    	for(QLUpdate qlu: launchListCache){
    		if(qlu.itemIndex == update.itemIndex){
    			return launchListCache.indexOf(qlu);
    		}
    	}
    	return -1;
    }
    
    
    /**
     * Send a request to server to launch
     * the file at the supplied index.
     */
    public void sendQuickLaunch(int index){
    	if(!connectedToServer) return;
    	
    	final QLRequest req = new QLRequest();
    	req.fileRequested = index;
    	
    	// Potentially blocks, run in its own thread
    	new Thread(new Runnable() {
    		public void run() {
    			client.sendTCP(req);
    		}
    	}).start();
    }

    //**********************************
    //********  Binding Functions
    //********
    
	/*
	 * Called by system when bound to service
	 */
    @Override
    public IBinder onBind(Intent intent) {
    	bound = true;
    	return new LocalBinder<ControlService>(this);
    }
    
    @Override
    public boolean onUnbind(Intent intent){
    	bound = false;    	
    	return false;
    }
    
    
    /**
     * Private inner class that handles sending messages to listeners
     * of the ControlService from different threads.
     * @author Bryan Hodge
     *
     */
	private class ControlCallback implements Handler.Callback {
		@Override
		public boolean handleMessage(Message msg) {

			switch (msg.what) {
			case UNKNOWN_HOST:
				for (ControlListener cont : controlListeners)
					cont.handleAlert("Unknown Host", "Could not find host.");
				break;

			case CONNECTING_ERROR:
				for (ControlListener cont : controlListeners)
					cont.handleAlert("Connection Error",
							"Cannot connect to server.");
				break;

			case PASSWORD_REQ:
				for (ControlListener cont : controlListeners)
					cont.handleAlert("Connection Error",
							"Password required for server.");
				break;
				
			case PASSWORD_WRONG:
				for (ControlListener cont : controlListeners)
					cont.handleAlert("Connection Error",
							"Incorrect password for server.");
				break;

			case CONNECTION_LOST:
				for (ControlListener cont : controlListeners)
					cont.handleAlert("Connection Lost",
							"Lost connection to server.");
				break;

			case CONNECTING_SUCCESS:
				for (ControlListener cont : controlListeners)
					cont.connected((String) msg.obj);
				break;

			case LAUNCH_UPDATE:
				QLUpdate update =(QLUpdate) msg.obj;
				
				int arrayPosition = getLaunchItemPosition(update);
				if(arrayPosition == -1){
					launchListCache.add(update);
				}else{
					launchListCache.set(arrayPosition, update);
				}
				
				for (ControlListener cont : controlListeners)
					cont.launchListUpdated(update.itemIndex);
				break;
			default:
				Log.e(TAG, "Handler Recieved Invalid Message");
				break;
			}
			return true; // prevent any additional handling of the message
		}
	}
    
    /**
     * Listens to TCP and UDP client, runs in client thread 
     * (Use Message Handling to talk to UI Thread)
     * @author BMCJ
     */
    private class MyListener extends Listener{

		public void received(Connection connection, Object object){
			if(object instanceof QLUpdate){		
				Message msg = handler.obtainMessage();
				msg.what = ControlService.LAUNCH_UPDATE;
				msg.obj = object;
				handler.sendMessage(msg);	
			} else if(object instanceof ServerMsg){
				ServerMsg servM = (ServerMsg) object;
				
				if(servM.connectionAccepted){
					connectedToServer = true;
                    Message msg = handler.obtainMessage();
        			msg.what = ControlService.CONNECTING_SUCCESS;
        			msg.obj = connection.getRemoteAddressTCP().getAddress().getHostAddress();
        			handler.sendMessage(msg);
        			showNotification(true);
					
				} else if(servM.incorrectPassword){
					Message msg = handler.obtainMessage();
					msg.what = ControlService.PASSWORD_WRONG;
					handler.sendMessage(msg);
				} else {
					Message msg = handler.obtainMessage();
					msg.what = ControlService.PASSWORD_REQ;
					handler.sendMessage(msg);
					
				}
			}
		}
		
		public void disconnected(Connection connection){
			//if(connectedToServer){
			connectedToServer = false;
			Message msg = handler.obtainMessage();
			msg.what = ControlService.CONNECTION_LOST;
			handler.sendMessage(msg);
			//}
		}
    }
    
    /**
     * Interface that listeners to this service need to implement
     * @author BMCJ
     *
     */
    public interface ControlListener {
    	void handleAlert(String title, String msg);
		void connected(String address);
    	void launchListUpdated(int index);
    }
  
}
