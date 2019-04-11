package com.p_bottomtab.p_bottomtab;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bottomtab.bottomtab.BottomTab;

public class MainActivity extends AppCompatActivity {

    public String TAG = "MainActivity";

    int[] imgs = {R.drawable.default_icon, R.drawable.default_icon, R.drawable.default_icon};
    int[] titles = {R.string.app_name, R.string.app_name, R.string.app_name};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomTab bottomtab = findViewById(R.id.bottomtab);
        bottomtab.setOnBottomTabFinishListener(new BottomTab.OnBottomTabFinishListener() {
            @Override
            public void bottomTabFinish(int position) {
                Log.i(TAG, "init position: " + position);
            }
        });

        bottomtab.setOnBottomTabItemClickListener(new BottomTab.OnBottomTabItemClickListener() {
            @Override
            public void bottomTabItemClick(int position) {
                Log.i(TAG, "click position: " + position);
            }
        });
        bottomtab.create(imgs, titles);

    }
}
