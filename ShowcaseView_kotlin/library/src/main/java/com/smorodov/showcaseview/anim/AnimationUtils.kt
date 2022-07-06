package com.smorodov.showcaseview.anim

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.util.Log
import android.view.View

object AnimationUtils {
    const val DEFAULT_DURATION = 300
    private const val ALPHA = "alpha"
    private const val INVISIBLE = 0f
    private const val VISIBLE = 1f
    private const val COORD_X = "x"
    private const val COORD_Y = "y"
    private const val INSTANT = 0
    fun getX(view: View): Float {
        return view.x
    }

    fun getY(view: View): Float {
        return view.y
    }

    fun hide(view: View?) {
        view!!.alpha = INVISIBLE
    }

    fun createFadeInAnimation(target: Any?, listener: AnimationStartListener): ObjectAnimator {
        return createFadeInAnimation(target, DEFAULT_DURATION, listener)
    }

    fun createFadeInAnimation(
        target: Any?,
        duration: Int,
        listener: AnimationStartListener
    ): ObjectAnimator {
        val oa = ObjectAnimator.ofFloat(target as View?, View.ALPHA, VISIBLE)
        oa.setDuration(duration.toLong()).addListener(object : AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
                listener.onAnimationStart()
            }

            override fun onAnimationEnd(animator: Animator) {}
            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        return oa
    }

    fun createFadeOutAnimation(target: Any?, listener: AnimationEndListener): ObjectAnimator {
        return createFadeOutAnimation(target, DEFAULT_DURATION, listener)
    }

    fun createFadeOutAnimation(
        target: Any?,
        duration: Int,
        listener: AnimationEndListener
    ): ObjectAnimator {
        val oa = ObjectAnimator.ofFloat(target as View?, View.ALPHA, INVISIBLE)
        oa.setDuration(duration.toLong()).addListener(object : AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                listener.onAnimationEnd()
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        return oa
    }

    fun createMovementAnimation(
        view: View?, canvasX: Float, canvasY: Float,
        offsetStartX: Float, offsetStartY: Float,
        offsetEndX: Float, offsetEndY: Float, listener: AnimationEndListener
    ): AnimatorSet {
        view!!.alpha = INVISIBLE
        val alphaIn = ObjectAnimator.ofFloat(view, ALPHA, INVISIBLE, VISIBLE).setDuration(500)
        val setUpX = ObjectAnimator.ofFloat(view, COORD_X, canvasX + offsetStartX).setDuration(
            INSTANT.toLong()
        )
        val setUpY = ObjectAnimator.ofFloat(view, COORD_Y, canvasY + offsetStartY).setDuration(
            INSTANT.toLong()
        )
        val moveX = ObjectAnimator.ofFloat(view, COORD_X, canvasX + offsetEndX).setDuration(1000)
        val moveY = ObjectAnimator.ofFloat(view, COORD_Y, canvasY + offsetEndY).setDuration(1000)
        moveX.startDelay = 1000
        moveY.startDelay = 1000
        val alphaOut = ObjectAnimator.ofFloat(view, ALPHA, INVISIBLE).setDuration(500)
        alphaOut.startDelay = 2500
        val `as` = AnimatorSet()
        `as`.play(setUpX).with(setUpY).before(alphaIn).before(moveX).with(moveY).before(alphaOut)
        `as`.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                Log.i("smorodov", "animation start")
            }

            override fun onAnimationEnd(animation: Animator) {
                Log.i("smorodov", "animation end")
                listener.onAnimationEnd()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        }
        )
        return `as`
    }

    fun createPointingAnimation(
        view: View?,
        x: Float,
        y: Float,
        listener: AnimationEndListener
    ): AnimatorSet {
        val alphaIn = ObjectAnimator.ofFloat(view, ALPHA, INVISIBLE, VISIBLE).setDuration(1000)
        val alphaOut = ObjectAnimator.ofFloat(view, ALPHA, VISIBLE, INVISIBLE).setDuration(1000)
        val setUpX = ObjectAnimator.ofFloat(view, COORD_X, x).setDuration(INSTANT.toLong())
        val setUpY = ObjectAnimator.ofFloat(view, COORD_Y, y).setDuration(INSTANT.toLong())
        val `as` = AnimatorSet()
        `as`.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                Log.i("smorodov", "animation start")
            }

            override fun onAnimationEnd(animation: Animator) {
                Log.i("smorodov", "animation end")
                listener.onAnimationEnd()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        }
        )
        `as`.play(setUpX).with(setUpY)
        `as`.play(alphaIn)
        `as`.play(alphaOut).after(2000)
        return `as`
    }

    fun interface AnimationStartListener {
        fun onAnimationStart()
    }

    fun interface AnimationEndListener {
        fun onAnimationEnd()
    }
}