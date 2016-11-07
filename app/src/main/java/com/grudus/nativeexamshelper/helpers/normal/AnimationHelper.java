package com.grudus.nativeexamshelper.helpers.normal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/* expand and collapse animation copied from http://stackoverflow.com/a/13381228/6551568 */
public class AnimationHelper {

    public static final int DEFAULT_ANIMATION_DURATION = 400;

    private static int duration;

    static {
        duration = DEFAULT_ANIMATION_DURATION;
    }

    public static void setDuration(int duration) {
        AnimationHelper.duration = duration;
    }



    public static void expand(final View v) {
        v.measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? RelativeLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((duration));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                } else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(duration);
        v.startAnimation(a);
    }

    public static ViewPropertyAnimator rotateToReceiveBinIcon(final View iconView, final ImageView binIcon, int duration) {
        return iconView.animate()
                .rotationY(90)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        iconView.setVisibility(View.INVISIBLE);
                        binIcon.setRotationY(90);
                        binIcon.setVisibility(View.VISIBLE);
                        binIcon.animate().setDuration(duration).rotationY(180).setListener(null).start();

                    }
                });
    }

    public static ViewPropertyAnimator rotateToReceiveBinIcon(final View iconView, final ImageView binIcon) {
        return rotateToReceiveBinIcon(iconView, binIcon, DEFAULT_ANIMATION_DURATION);
    }



    public static ViewPropertyAnimator rotateToDisposeBinIcon(final View iconView, final ImageView binIcon, int duration) {
        return binIcon.animate()
                .rotationY(90)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        binIcon.setVisibility(View.INVISIBLE);
                        iconView.setRotationY(90);
                        iconView.setVisibility(View.VISIBLE);
                        iconView.animate().setDuration(duration).rotationY(0).setListener(null).start();
                    }
                });
    }

    public static ViewPropertyAnimator rotateToDisposeBinIcon(final View iconView, final ImageView binIcon) {
        return rotateToDisposeBinIcon(iconView, binIcon, DEFAULT_ANIMATION_DURATION);
    }
}
