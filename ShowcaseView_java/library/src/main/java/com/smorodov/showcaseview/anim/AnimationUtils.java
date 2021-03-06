package com.smorodov.showcaseview.anim;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;

public class AnimationUtils {

    public static final int DEFAULT_DURATION = 300;

    private static final String ALPHA = "alpha";
    private static final float INVISIBLE = 0f;
    private static final float VISIBLE = 1f;
    private static final String COORD_X = "x";
    private static final String COORD_Y = "y";
    private static final int INSTANT = 0;

    public static float getX(View view) {
        return view.getX();
    }

    public static float getY(View view) {
        return view.getY();
    }

    public static void hide(View view) {
        view.setAlpha(INVISIBLE);
    }

    public static ObjectAnimator createFadeInAnimation(Object target, final AnimationStartListener listener) {
        return createFadeInAnimation(target, DEFAULT_DURATION, listener);
    }

    public static ObjectAnimator createFadeInAnimation(Object target, int duration, final AnimationStartListener listener) {
        ObjectAnimator oa = ObjectAnimator.ofFloat((View) target, View.ALPHA, VISIBLE);
        oa.setDuration(duration).addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                listener.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        return oa;
    }

    public static ObjectAnimator createFadeOutAnimation(Object target, final AnimationEndListener listener) {
        return createFadeOutAnimation(target, DEFAULT_DURATION, listener);
    }

    public static ObjectAnimator createFadeOutAnimation(Object target, int duration, final AnimationEndListener listener) {
        ObjectAnimator oa = ObjectAnimator.ofFloat((View) target, View.ALPHA, INVISIBLE);
        oa.setDuration(duration).addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                listener.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        return oa;
    }

    public static AnimatorSet createMovementAnimation(View view, float canvasX, float canvasY,
                                                      float offsetStartX, float offsetStartY,
                                                      float offsetEndX, float offsetEndY, final AnimationEndListener listener) {
        view.setAlpha(INVISIBLE);
        ObjectAnimator alphaIn = ObjectAnimator.ofFloat(view, ALPHA, INVISIBLE, VISIBLE).setDuration(500);
        ObjectAnimator setUpX = ObjectAnimator.ofFloat(view, COORD_X, canvasX + offsetStartX).setDuration(INSTANT);
        ObjectAnimator setUpY = ObjectAnimator.ofFloat(view, COORD_Y, canvasY + offsetStartY).setDuration(INSTANT);
        ObjectAnimator moveX = ObjectAnimator.ofFloat(view, COORD_X, canvasX + offsetEndX).setDuration(1000);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(view, COORD_Y, canvasY + offsetEndY).setDuration(1000);
        moveX.setStartDelay(1000);
        moveY.setStartDelay(1000);

        ObjectAnimator alphaOut = ObjectAnimator.ofFloat(view, ALPHA, INVISIBLE).setDuration(500);
        alphaOut.setStartDelay(2500);

        AnimatorSet as = new AnimatorSet();
        as.play(setUpX).with(setUpY).before(alphaIn).before(moveX).with(moveY).before(alphaOut);

        as.addListener(new Animator.AnimatorListener()
                       {

                           @Override
                           public void onAnimationStart(Animator animation) {
                               Log.i("smorodov","animation start");
                           }

                           @Override
                           public void onAnimationEnd(Animator animation) {
                               Log.i("smorodov","animation end");
                               listener.onAnimationEnd();
                           }

                           @Override
                           public void onAnimationCancel(Animator animation) {

                           }

                           @Override
                           public void onAnimationRepeat(Animator animation) {

                           }
                       }
        );


        return as;
    }

    public static AnimatorSet createPointingAnimation(View view, float x, float y, final AnimationEndListener listener) {
        ObjectAnimator alphaIn = ObjectAnimator.ofFloat(view, ALPHA, INVISIBLE, VISIBLE).setDuration(1000);
        ObjectAnimator alphaOut = ObjectAnimator.ofFloat(view, ALPHA, VISIBLE, INVISIBLE).setDuration(1000);
        ObjectAnimator setUpX = ObjectAnimator.ofFloat(view, COORD_X, x).setDuration(INSTANT);
        ObjectAnimator setUpY = ObjectAnimator.ofFloat(view, COORD_Y, y).setDuration(INSTANT);

        AnimatorSet as = new AnimatorSet();
        as.addListener(new Animator.AnimatorListener()
                       {

                           @Override
                           public void onAnimationStart(Animator animation) {
                               Log.i("smorodov","animation start");
                           }

                           @Override
                           public void onAnimationEnd(Animator animation) {
                               Log.i("smorodov","animation end");
                               listener.onAnimationEnd();
                           }

                           @Override
                           public void onAnimationCancel(Animator animation) {
                           }

                           @Override
                           public void onAnimationRepeat(Animator animation) {

                           }
                       }
        );

        as.play(setUpX).with(setUpY);
        as.play(alphaIn);
        as.play(alphaOut).after(2000);
        return as;
    }

    public interface AnimationStartListener {
        void onAnimationStart();
    }

    public interface AnimationEndListener {
        void onAnimationEnd();
    }
}
