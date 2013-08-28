package com.hodgeproject.media.controller;

import java.util.ArrayList;

import com.hodgeproject.media.R;
import com.hodgeproject.mediacontroller.network.NetworkTypes.QLUpdate;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class QLAdapter extends BaseAdapter {
	
	private Activity activity;
	private ArrayList<QLUpdate> data;
	private static LayoutInflater inflater;
	
	public QLAdapter(Activity a, ArrayList<QLUpdate> d){
		activity = a;
		if(d == null){
			data = new ArrayList<QLUpdate>();
		}else{
			data = d;
		}
		
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	/**
	 * Sets the data array equal to a new list, refreshes list
	 * @param list
	 */
	public void setList(ArrayList<QLUpdate> list){
		data = list;
		notifyDataSetChanged();
	}
	
	/**
	 * Updates the data array with a QLUpdate,
	 * if it doesn't exist, it will be added. Refreshes list.
	 * @param update
	 */
	public void updateItem(QLUpdate update){
		boolean updated = false;
		for(QLUpdate qlu : data){
			if(qlu.itemIndex == update.itemIndex){
				data.set(data.indexOf(qlu),update);
				updated = true;
			}
		}
		if(!updated){
			data.add(update);
		}
		notifyDataSetChanged();
	}
	

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return data.get(position).itemIndex;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi=convertView;
        if(convertView==null) vi = inflater.inflate(R.layout.ql_listrow, null);
        
        ImageView thumbnail = (ImageView)vi.findViewById(R.id.thumbnail_image);
        TextView title = (TextView)vi.findViewById(R.id.title);
        
        QLUpdate item = data.get(position);
        
        if(item.fileName != null) title.setText(item.fileName);
        	
        
		if (item.icon != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(item.icon, 0, item.icon.length);
			bitmap.setDensity(80);
			
			thumbnail.setImageBitmap(bitmap);		
		} else {
			thumbnail.setImageResource(R.drawable.image_empty);
		}
        
		return vi;
	}

}
