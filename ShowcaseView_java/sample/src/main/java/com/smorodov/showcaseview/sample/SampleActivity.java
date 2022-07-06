package com.smorodov.showcaseview.sample;

import static com.smorodov.showcaseview.sample.R.id.buttonToMultipleItemsActivtiy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.smorodov.showcaseview.ShowcaseView;
import com.smorodov.showcaseview.sample.legacy.MultipleShowcaseSampleActivity;
import com.smorodov.showcaseview.sample.v14.MultipleActionItemsSampleActivity;
import com.smorodov.showcaseview.sample.R;
import com.smorodov.showcaseview.sample.fragments.ShowcaseFragmentActivity;
import com.smorodov.showcaseview.sample.v14.ActionItemsSampleActivity;

public class SampleActivity extends Activity implements View.OnClickListener,
        ShowcaseView.OnShowcaseEventListener {

    ShowcaseView sv;
    Button buttonTop;
    Button buttonMiddle;
    Button buttonDown;
    Button buttonLowest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        buttonTop = findViewById(R.id.buttonBlocked);
        buttonTop.setOnClickListener(this);
        buttonMiddle = findViewById(buttonToMultipleItemsActivtiy);
        buttonMiddle.setOnClickListener(this);
        buttonDown = findViewById(R.id.buttonToMultipleShowcaseViewsActivity);
        buttonDown.setOnClickListener(this);
        buttonLowest = findViewById(R.id.buttonToShowcaseFragmentActivity);
        buttonLowest.setOnClickListener(this);

        ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.hideOnClickOutside = true;

        // The following code will reposition the OK button to the left.
        // RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        // lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        // int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        // lps.setMargins(margin, margin, margin, margin);
        // co.buttonLayoutParams = lps;

        sv = ShowcaseView.insertShowcaseView(R.id.buttonBlocked, this, R.string.showcase_main_title, R.string.showcase_main_message, co);
        assert sv != null;
        sv.setOnShowcaseEventListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        int viewId = view.getId();
        switch (viewId) {
            case R.id.buttonBlocked:
                if (sv.isShown()) {
                    sv.animateGesture(0, 0, 0, -400);
                } else {
                    startSdkLevelAppropriateActivity(R.id.buttonBlocked);
                }
                break;
            case R.id.buttonToMultipleItemsActivtiy:
            case R.id.buttonToMultipleShowcaseViewsActivity:
                startSdkLevelAppropriateActivity(viewId);
                break;
            case R.id.buttonToShowcaseFragmentActivity:
                startFragmentActivity();
                break;
        }
    }

    private void startFragmentActivity() {
        Intent startIntent = new Intent(this, ShowcaseFragmentActivity.class);
        startActivity(startIntent);
    }

    private void startSdkLevelAppropriateActivity(int buttonId) {
        if (buttonId == R.id.buttonBlocked) {
            startActivity(new Intent(this, ActionItemsSampleActivity.class));
        } else if (buttonId == buttonToMultipleItemsActivtiy) {
            startActivity(new Intent(this, MultipleActionItemsSampleActivity.class));
        } else if (buttonId == R.id.buttonToMultipleShowcaseViewsActivity) {
            startActivity(new Intent(this, MultipleShowcaseSampleActivity.class));
        }
    }

    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {
        buttonTop.setText(R.string.button_show);
        buttonMiddle.setVisibility(View.VISIBLE);
        buttonDown.setVisibility(View.VISIBLE);
        buttonLowest.setVisibility(View.VISIBLE);
    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {
        buttonTop.setText(R.string.button_hide);
        buttonMiddle.setVisibility(View.GONE);
        buttonDown.setVisibility(View.GONE);
        buttonLowest.setVisibility(View.GONE);
    }
}
