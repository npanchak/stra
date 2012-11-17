package com.stra;

import java.util.List;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class recvAdapter extends ArrayAdapter<listDS> {
		Context context; 
	    int layoutResourceId;  
	    List<listDS> data = null;
	    
	    public recvAdapter(Context context, int layoutResourceId) {
	        super(context, layoutResourceId);
	        this.layoutResourceId = layoutResourceId;
	        this.context = context;
	        this.data = Constants.recvlist;
	    }

	    public int getCount() {
	        return data.size();
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	        View row = convertView;
	        recvHolder holder = null;
	        if(row == null)
	        {
	            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	            row = inflater.inflate(layoutResourceId, parent, false);
	            
	            holder = new recvHolder();	            
	            holder.message = (TextView)row.findViewById(R.id.tweet);
	            row.setTag(holder);
	        }
	        else
	        {
	            holder = (recvHolder)row.getTag();
	        }
	        
	        listDS fbds = data.get(position);
	        holder.message.setText(fbds.tweet);
	        return row;
	    }
	    
	    static class recvHolder
	    {
	        TextView message;	        
	    }
	    	    
	    public void add(listDS newds)
		{
			data.add(newds);
			notifyDataSetChanged();
		}


}
