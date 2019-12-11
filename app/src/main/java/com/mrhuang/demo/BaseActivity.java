package com.mrhuang.demo;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getViews();
        viewCreated();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        getViews();
        viewCreated();
    }

    protected abstract void getViews();

    protected abstract void viewCreated();
}
