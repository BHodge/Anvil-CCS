package com.hodgeproject.media.controller;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.hodgeproject.media.R;
import com.hodgeproject.mediacontroller.network.NetworkTypes.QLUpdate;

/**
 * Displays a Quick Launch list on the screen
 * Grabs the list from the Activity onCreate (likely empty at first)
 * List updated incrementally on each server QLUpdate
 * List refreshed by activity every time service connects (screen rotate)
 * @author Bryan Hodge
 *
 */
public class QuickLaunchFragment extends SherlockFragment {

	private QuickLaunchFragmentListener mCallback;
	
	QLAdapter adapter;
	ListView list;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (QuickLaunchFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement QuickLaunchFragmentListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View currentView = inflater.inflate(R.layout.fragment_quicklaunchview, container, false);

		list = (ListView)currentView.findViewById(R.id.ql_list);
		
		adapter = new QLAdapter(getActivity(), mCallback.getQLList());
		list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				QLUpdate clicked = (QLUpdate) adapter.getItem(position);
				mCallback.sendRequest(clicked.itemIndex);
				Toast toast = Toast.makeText(getActivity(), "Launched Activity!", Toast.LENGTH_SHORT);
				toast.show();
			}
        });

		return currentView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		// Remove the QL button from Menu if it exists
		if (menu.findItem(R.id.menu_launch) != null) {
			menu.removeItem(R.id.menu_launch);
		}
	}

	// ***********************************
	// ******** Public QuickLaunch functions
	// ********

	/**
	 * Set a new count of QL Buttons NOTE: This will rebuild the layout
	 * 
	 * @param quickLaunchSize
	 */
	public void setQLList(ArrayList<QLUpdate> qlList) {
	
		if (qlList.size() == 0 || adapter == null) return;
		adapter.setList(qlList);
		
	}

	/**
	 * Update a specific QL Button with a QLUpdate
	 * 
	 * @param update
	 */
	public void setQLUpdate(QLUpdate update) {
		if(adapter == null) return;
		adapter.updateItem(update);
	}

	/**
	 * Interface the parent Activity will need to implement to respond to
	 * QuickLaunch events
	 * 
	 * @author Bryan Hodge
	 */
	public interface QuickLaunchFragmentListener {
		public ArrayList<QLUpdate> getQLList();
		public void sendRequest(int qlIndex);
	}

}
