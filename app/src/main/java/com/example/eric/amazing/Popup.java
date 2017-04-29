package com.example.eric.amazing;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

/**
 * Created by Eric on 4/28/2017.
 */

public class Popup {

    private final Context context;

    public Popup(Context context, Message message) {
        this.context = context;
        Activity parent = (Activity) context;

        // here you can have the instance of the holder from glSurfaceView
        RelativeLayout parentLayout = (RelativeLayout) parent.findViewById(R.id.activity_main);

        // inflate your custom popup layout or create it dynamically
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view =  inflater.inflate(R.layout.finish_popup, null);

        // Now you should create a PopupWindow
        PopupWindow popupWindow = new PopupWindow(view,
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        // you can change the offset values or the Gravity. This will diaply the popup in the center
        // of your glSurfaceView
        popupWindow.showAtLocation(parentLayout, Gravity.CENTER, 0, 0);
    }
}
