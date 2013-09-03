package com.fiskur.screenshotbuilder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ScreenshotAdapter extends BaseAdapter {
    private List<Screenshot> screenshots = new ArrayList<Screenshot>();
    private LayoutInflater inflater;

    public ScreenshotAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }
    
    public void addItem(Uri path, Drawable drawable, long id){
    	screenshots.add(new Screenshot(path, drawable, id));
    	ScreenshotAdapter.this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return screenshots.size();
    }
    
    public List<Screenshot> getScreenshots(){
    	return screenshots;
    }

    @Override
    public Object getItem(int i) {
        return screenshots.get(i);
    }

    @Override
    public long getItemId(int i) {
        return screenshots.get(i).drawableID;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        ImageView picture;

        if(v == null) {
            v = inflater.inflate(R.layout.grid_layout, viewGroup, false);
            v.setTag(R.id.picture, v.findViewById(R.id.picture));
        }

        picture = (ImageView)v.getTag(R.id.picture);
        Screenshot item = (Screenshot)getItem(i);
        picture.setImageDrawable(item.drawable);

        return v;
    }

    public class Screenshot {
        final Drawable drawable;
        final long drawableID;
        final Uri path;

        Screenshot(Uri path, Drawable drawable, long drawableID) {
        	this.path = path;
            this.drawable = drawable;
            this.drawableID = drawableID;
        }
        
        public Uri getPath(){
        	return path;
        }
    }
}
