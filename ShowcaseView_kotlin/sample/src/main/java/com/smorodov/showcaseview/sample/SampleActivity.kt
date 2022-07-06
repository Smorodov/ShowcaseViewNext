package com.smorodov.showcaseview.sample

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.smorodov.showcaseview.ShowcaseView
import com.smorodov.showcaseview.sample.fragments.ShowcaseFragmentActivity
import com.smorodov.showcaseview.sample.legacy.MultipleShowcaseSampleActivity
import com.smorodov.showcaseview.sample.v14.ActionItemsSampleActivity
import com.smorodov.showcaseview.sample.v14.MultipleActionItemsSampleActivity
import com.smorodov.showcaseview.sample.R.id
class SampleActivity : Activity(), View.OnClickListener, ShowcaseView.OnShowcaseEventListener {
    var sv: ShowcaseView? = null
    var buttonTop: Button? = null
    var buttonMiddle: Button? = null
    var buttonDown: Button? = null
    var buttonLowest: Button? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        buttonTop = findViewById(id.buttonBlocked)
        buttonTop?.setOnClickListener(this)
        buttonMiddle = findViewById(id.buttonToMultipleItemsActivtiy)
        buttonMiddle?.setOnClickListener(this)
        buttonDown = findViewById(id.buttonToMultipleShowcaseViewsActivity)
        buttonDown?.setOnClickListener(this)
        buttonLowest = findViewById(id.buttonToShowcaseFragmentActivity)
        buttonLowest?.setOnClickListener(this)
        val co = ShowcaseView.ConfigOptions()
        co.hideOnClickOutside = true

        // The following code will reposition the OK button to the left.
        // RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        // lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        // int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        // lps.setMargins(margin, margin, margin, margin);
        // co.buttonLayoutParams = lps;
        sv = ShowcaseView.insertShowcaseView(
            id.buttonBlocked,
            this,
            R.string.showcase_main_title,
            R.string.showcase_main_message,
            co
        )
        assert(sv != null)
        sv!!.setOnShowcaseEventListener(this)
    }

    @SuppressLint("NonConstantResourceId")
    override fun onClick(view: View) {
        val viewId = view.id
        when (viewId) {
            id.buttonBlocked -> if (sv!!.isShown) {
                sv!!.animateGesture(0f, 0f, 0f, -400f)
            } else {
                startSdkLevelAppropriateActivity(id.buttonBlocked)
            }
            id.buttonToMultipleItemsActivtiy, id.buttonToMultipleShowcaseViewsActivity -> startSdkLevelAppropriateActivity(
                viewId
            )
            id.buttonToShowcaseFragmentActivity -> startFragmentActivity()
        }
    }

    private fun startFragmentActivity() {
        val startIntent = Intent(this, ShowcaseFragmentActivity::class.java)
        startActivity(startIntent)
    }

    private fun startSdkLevelAppropriateActivity(buttonId: Int) {
        if (buttonId == id.buttonBlocked) {
            startActivity(Intent(this, ActionItemsSampleActivity::class.java))
        } else if (buttonId == id.buttonToMultipleItemsActivtiy) {
            startActivity(Intent(this, MultipleActionItemsSampleActivity::class.java))
        } else if (buttonId == id.buttonToMultipleShowcaseViewsActivity) {
            startActivity(Intent(this, MultipleShowcaseSampleActivity::class.java))
        }
    }

    override fun onShowcaseViewHide(showcaseView: ShowcaseView?) {
        buttonTop!!.setText(R.string.button_show)
        buttonMiddle!!.visibility = View.VISIBLE
        buttonDown!!.visibility = View.VISIBLE
        buttonLowest!!.visibility = View.VISIBLE
    }

    override fun onShowcaseViewShow(showcaseView: ShowcaseView?) {
        buttonTop!!.setText(R.string.button_hide)
        buttonMiddle!!.visibility = View.GONE
        buttonDown!!.visibility = View.GONE
        buttonLowest!!.visibility = View.GONE
    }
}