package com.smorodov.showcaseview

import android.app.Activity
import android.view.View
import com.smorodov.showcaseview.anim.AnimationUtils.AnimationEndListener

class ShowcaseViewBuilder {
    private val showcaseView: ShowcaseView

    constructor(activity: Activity) {
        showcaseView = ShowcaseView(activity)
    }

    constructor(showcaseView: ShowcaseView) {
        this.showcaseView = showcaseView
    }

    constructor(activity: Activity, showcaseLayoutViewId: Int) {
        showcaseView = activity.layoutInflater.inflate(showcaseLayoutViewId, null) as ShowcaseView
    }

    fun setShowcaseNoView(): ShowcaseViewBuilder {
        showcaseView.setShowcaseNoView()
        return this
    }

    fun setShowcaseView(view: View?): ShowcaseViewBuilder {
        showcaseView.setShowcaseView(view)
        return this
    }

    fun setShowcasePosition(x: Float, y: Float): ShowcaseViewBuilder {
        showcaseView.setShowcasePosition(x, y)
        return this
    }

    fun setShowcaseItem(itemType: Int, actionItemId: Int, activity: Activity): ShowcaseViewBuilder {
        showcaseView.setShowcaseItem(itemType, actionItemId, activity)
        return this
    }

    fun overrideButtonClick(listener: View.OnClickListener?): ShowcaseViewBuilder {
        showcaseView.overrideButtonClick(listener)
        return this
    }

    fun animateGesture(
        offsetStartX: Float,
        offsetStartY: Float,
        offsetEndX: Float,
        offsetEndY: Float
    ): ShowcaseViewBuilder {
        showcaseView.animateGesture(offsetStartX, offsetStartY, offsetEndX, offsetEndY)
        return this
    }

    //    public ShowcaseViewBuilder setTextColors(int titleTextColor, int detailTextColor) {
    //        showcaseView.setTextColors(titleTextColor, detailTextColor);
    //        return this;
    //    }
    fun setText(titleText: String?, subText: String?): ShowcaseViewBuilder {
        showcaseView.setText(titleText, subText)
        return this
    }

    fun setText(titleText: Int, subText: Int): ShowcaseViewBuilder {
        showcaseView.setText(titleText, subText)
        return this
    }

    fun pointTo(view: View, listener: AnimationEndListener?): ShowcaseViewBuilder {
        showcaseView.pointTo(view)
        return this
    }

    fun pointTo(x: Float, y: Float): ShowcaseViewBuilder {
        showcaseView.pointTo(x, y)
        return this
    }

    fun build(): ShowcaseView {
        return showcaseView
    }
}