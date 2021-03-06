package com.mrhuang.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mrhuang.demo.chapter1.SurfaceViewActivity;
import com.mrhuang.demo.chapter2.AudioRecordActivity;
import com.mrhuang.demo.chapter3.MediaExtractorActivity;
import com.mrhuang.demo.chapter3.VideoClipActivity;
import com.mrhuang.demo.opengl.chapter1.TriangleActivity;
import com.mrhuang.demo.opengl.chapter2.GLVideoActivity;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    ListView listView;

    String[] menuList = {
            "SurfaceView with image",
            "AudioRecord and AudioTrack",
            "MediaExtractor",
            "VideoClip",
            "glSurfaceView",
            "glVideo"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void getViews() {
        listView = findViewById(R.id.listView);
    }

    @Override
    protected void viewCreated() {

        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                startActivity(new Intent(this, SurfaceViewActivity.class));
                break;
            case 1:
                startActivity(new Intent(this, AudioRecordActivity.class));
                break;
            case 2:
                startActivity(new Intent(this, MediaExtractorActivity.class));
                break;
            case 3:
                startActivity(new Intent(this, VideoClipActivity.class));
                break;
            case 4:
                startActivity(new Intent(this, TriangleActivity.class));
                break;

            case 5:
                startActivity(new Intent(this, GLVideoActivity.class));
                break;
        }
    }
}