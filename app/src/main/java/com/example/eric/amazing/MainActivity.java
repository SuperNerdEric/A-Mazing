package com.example.eric.amazing;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mGLView;
    private Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        LooperThread looper = new LooperThread();
        looper.start();

        mGLView = new MyGLSurfaceView(this, looper);
        button = new Button(getApplicationContext());
        button.setText("Play Again");

        button.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MyGLSurfaceView.restart();
                button.setVisibility(View.GONE);
            }
        });

        setContentView(mGLView);
    }

    class LooperThread extends Thread {
        public Handler mHandler;

        public void run() {
            Looper.prepare();

            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    if(msg.obj.equals(true)) {

                        ViewGroup test =((ViewGroup) button.getParent());
                        if(test!=null){
                            test.removeView(button);
                        }
                        addContentView(button, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        button.setVisibility(View.VISIBLE);
                    } else {
                        ViewGroup test =((ViewGroup) button.getParent());
                        if(test!=null){
                            test.removeView(button);
                        }
                    }
                }
            };

            Looper.loop();
        }
    }

}
