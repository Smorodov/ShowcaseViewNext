package com.smorodov.showcaseview.sample.fragments;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.smorodov.showcaseview.sample.R;


public class ShowcaseFragmentActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity_layout);
    }

}