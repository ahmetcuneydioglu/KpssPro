package com.arkay.quizpower;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.arkay.quizpower.bean.SingleAnswareLevelInfo;

/**
 * Adapter for display list of level on single answare.
 * @author Arkay
 *
 */
public class AllListAdaptor extends BaseAdapter {
    
	
    private Activity activity;
    private static ArrayList<SingleAnswareLevelInfo> data;
    private  LayoutInflater inflater = null;
    private TextView txtName;

    
    public AllListAdaptor(Activity a,  ArrayList<SingleAnswareLevelInfo> data) {
        activity = a;
        this.data=data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        
        if(convertView==null){
            vi = inflater.inflate(R.layout.list_row, null);
        }
        txtName = (TextView)vi.findViewById(R.id.textView1); 
        txtName.setText(""+data.get(position).getLevelName());
       
        return vi;
    }
   
}
