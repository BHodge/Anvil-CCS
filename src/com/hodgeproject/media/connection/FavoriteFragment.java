package com.hodgeproject.media.connection;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.hodgeproject.media.R;

/**
 * Fragment that adds/edits favorite servers and stores the data
 * @author BMCJ
 *
 */
public class FavoriteFragment extends SherlockFragment {
		FavoriteFragmentListener mCallback;
		Button saveBtn;
		EditText editTextSname, editTextSaddr, editTextSpswd;
		
		MCDBAdapter adapter;
		//Cursor cursor;
	
		@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setHasOptionsMenu(true);
	        
	        
	    }
	 
	    @Override
	    public void onCreateOptionsMenu(Menu menu,  MenuInflater inflater) {
	    	super.onCreateOptionsMenu(menu, inflater);

	    	// Remove the Add Favorite button from Menu if it exists
	    	// (The Login Fragment is also visible on a tablet)
	    	if(menu.findItem(R.id.menu_add) != null){
	    		menu.removeItem(R.id.menu_add);
	    	}
	    }
			
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_favorite, container, false);
			editTextSname = (EditText)view.findViewById(R.id.editTextSname);
			editTextSaddr = (EditText)view.findViewById(R.id.editTextSaddr);
			editTextSpswd = (EditText)view.findViewById(R.id.editTextSpswd);
			
			final FragmentManager fragmentManager = getSherlockActivity().getSupportFragmentManager();
			
			saveBtn = (Button)view.findViewById(R.id.save_button);
			saveBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					adapter = new MCDBAdapter(getSherlockActivity());
					//check if we are adding or editing! TODO:add edit functionality STORE ID IN FRAGMENT
					String snameVal = editTextSname.getText().toString();
					String saddrVal = editTextSaddr.getText().toString();
					String spswd = editTextSpswd.getText().toString();
					
					//check empty password
					if(spswd.trim().equals("")){
						long val = adapter.insertEntry(snameVal, saddrVal, null);
					} else {
						long val = adapter.insertEntry(snameVal, saddrVal, spswd);
					}
					
					adapter.close();
				
					//TODO: Have the LoginFragment refresh the favorite menu
					mCallback.refreshFavoriteList();
					
					//return the fragment
					fragmentManager.popBackStack();
				}
			});
			
			return view;
		}
		
		
	    @Override
	    public void onAttach(Activity activity){
	    	super.onAttach(activity);
	    	
	    	try{
	    		mCallback = (FavoriteFragmentListener) activity;
	    	} catch (ClassCastException e){
	    		throw new ClassCastException(activity.toString() + " must implement FavoriteFragmentListener"); 
	    	}
	    }
		
		
		public interface FavoriteFragmentListener {
			public void refreshFavoriteList();
		}
		
		@Override
		public void onDestroy(){
			super.onDestroy();
		}
}
