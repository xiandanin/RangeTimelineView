package com.dyhdyh.view.rangetimelineview.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dyhdyh.view.rangetimelineview.RangeTimelineView;

public class MainActivity extends AppCompatActivity {
    RangeTimelineView rtv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rtv = (RangeTimelineView) findViewById(R.id.rtv);
    }
}
