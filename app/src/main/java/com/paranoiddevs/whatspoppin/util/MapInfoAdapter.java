package com.paranoiddevs.whatspoppin.util;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.paranoiddevs.whatspoppin.R;

/**
 * <p>Created by Alcha on Mar 27, 2018 @ 22:42.</p>
 */

public class MapInfoAdapter implements GoogleMap.InfoWindowAdapter {
    private Activity mContext;

    public MapInfoAdapter(Activity context) {
        mContext = context;
    }

    @Override
    // Return null here, so that getInfoContents() is called next.
    public View getInfoWindow(Marker arg0) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        // Inflate the layouts for the info window, title and snippet.
        View infoWindow = mContext.getLayoutInflater().inflate(R.layout.custom_info_contents,
                (FrameLayout) mContext.findViewById(R.id.map), false);

        TextView title = (infoWindow.findViewById(R.id.title));
        title.setText(marker.getTitle());

        TextView snippet = (infoWindow.findViewById(R.id.snippet));
        snippet.setText(marker.getSnippet());

        return infoWindow;
    }
}
