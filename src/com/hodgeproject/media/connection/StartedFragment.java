package com.hodgeproject.media.connection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.hodgeproject.media.R;

/**
 * Fragment for the Getting Started view
 * @author BMCJ
 *
 */
public class StartedFragment extends SherlockFragment {
	public static final String ARG_HIDE_READY = "HIDE READY BUTTON";
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        //setHasOptionsMenu(true);
	        
	    }
			
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_started, container, false);
			
	    	// Check supplied arguments if the Getting Started button should be hidden
	        if((getArguments()!= null) && getArguments().containsKey(ARG_HIDE_READY)){
				view.findViewById(R.id.button_ready).setVisibility(View.INVISIBLE);
			}
			
			return view;
		}
		
		
		//ABS change
	    @Override
	    public void onCreateOptionsMenu(Menu menu,  MenuInflater inflater) {
	    	//inflater.inflate(R.menu.fragment_started, menu);
	    	super.onCreateOptionsMenu(menu, inflater);
	    }
}
