package com.smorodov.showcaseview.sample.v14;

import static com.smorodov.showcaseview.ShowcaseViews.ItemViewProperties;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.smorodov.showcaseview.ShowcaseView;
import com.smorodov.showcaseview.ShowcaseViews;
import com.smorodov.showcaseview.sample.R;

public class MultipleActionItemsSampleActivity extends Activity implements ActionBar.OnNavigationListener {

    public static final float SHOWCASE_SPINNER_SCALE = 1f;
    public static final float SHOWCASE_OVERFLOW_ITEM_SCALE = 0.5f;
    ShowcaseView.ConfigOptions mOptions = new ShowcaseView.ConfigOptions();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Item1", "Item2", "Item3"}), this);
        mOptions.block = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        ShowcaseViews views = new ShowcaseViews(this, R.layout.showcase_view_template, new ShowcaseViews.OnShowcaseAcknowledged() {
            @Override
            public void onShowCaseAcknowledged(ShowcaseView showcaseView) {
                Toast.makeText(getApplicationContext(), R.string.dismissed_message, Toast.LENGTH_SHORT).show();
            }
        });
        views.addView(new ItemViewProperties(ItemViewProperties.ID_SPINNER, R.string.showcase_spinner_title, R.string.showcase_spinner_message, ShowcaseView.ITEM_SPINNER, SHOWCASE_SPINNER_SCALE));
        views.addView(new ItemViewProperties(ItemViewProperties.ID_TITLE, R.string.showcase_simple_title, R.string.showcase_simple_message, ShowcaseView.ITEM_TITLE, SHOWCASE_SPINNER_SCALE));
        views.addView(new ItemViewProperties(ItemViewProperties.ID_OVERFLOW, R.string.showcase_overflow_title, R.string.showcase_overflow_message, ShowcaseView.ITEM_ACTION_OVERFLOW, SHOWCASE_OVERFLOW_ITEM_SCALE));
        views.addView(new ItemViewProperties(ItemViewProperties.ID_OVERFLOW, R.string.showcase_overflow_title, R.string.showcase_overflow_message, ShowcaseView.ITEM_ACTION_ITEM, SHOWCASE_OVERFLOW_ITEM_SCALE));
        views.show();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        return false;
    }
}
