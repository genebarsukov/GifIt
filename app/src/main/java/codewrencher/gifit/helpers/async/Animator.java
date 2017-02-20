package codewrencher.gifit.helpers.async;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import codewrencher.gifit.helpers.async.interfaces.AnimatorListener;

/**
 * Created by Gene on 12/26/2015.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class Animator {
    private int display_width;
    private int display_height;
    private int from_x;
    private int from_y;
    private int to_x;
    private int to_y;
    private int fling_duration = 800;
    private AnimatorListener animator_listener;
    private Animation animation;
    private String animation_type;
    private View animated_view;

    public Animator(int display_width, int display_height) {
        this.display_width = display_width;
        this.display_height = display_height;
    }
    public Animator(int display_width, int display_height, int from_x, int to_x, int from_y, int to_y) {
        this.display_width = display_width;
        this.display_height = display_height;
        this.from_x = from_x;
        this.to_x = to_x;
        this.from_y = from_y;
        this.to_y = to_y;
    }
    public void setFlingDuration(int fling_duration) {
        this.fling_duration = fling_duration;
        if (this.fling_duration < 300) {
            this.fling_duration = 300;
        }
    }
    public void setAnimatedView(View animated_view) {
        this.animated_view = animated_view;
    }
    public void registerListener(AnimatorListener animator_listener) {
        this.animator_listener = animator_listener;
    }

    public void animate (View view) {
        if (animator_listener != null) {
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    if (animated_view != null) {
                        animator_listener.onAnimationFinished(animation_type, animated_view);
                    } else {
                        animator_listener.onAnimationFinished(animation_type);
                    }
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }
        if (animation != null) {
            view.startAnimation(animation);
        }
    }
    public void setAnimation (View view, String animation_type) {
        view.clearAnimation();
        this.animation_type = animation_type;
        this.animation = this.getAnimation(animation_type);
        if (this.animation == null) {
            this.animation = this.getAnimation(animation_type, view);
        }
        if (animation != null) {
            view.setAnimation(animation);
        }
    }
    public void setSecondaryAnimation (View view, String animation_type) {
        view.clearAnimation();
        Animation animation = this.getAnimation(animation_type);
        view.setAnimation(animation);
    }
    private Animation getAnimation(String type) {
        Animation animation;
        if (type.contains("fling_up"))           animation = this.fling(-1);
        else if (type.contains("fling_down"))    animation = this.fling(1);
        else if (type.contains("blink"))         animation = this.blink();
        else if (type.contains("blink_in"))      animation = this.blinkIn();
        else if (type.contains("bulge"))         animation = this.bulge();
        else if (type.contains("drop_down"))     animation = this.dropDown(1);
        else if (type.contains("slide_up"))      animation = this.dropDown(-1);
        else if (type.contains("drop_up"))     animation = this.dropUp(-1);
        else if (type.contains("slide_down"))      animation = this.dropUp(1);
        else if (type.contains("move_up"))       animation = this.move(-1);
        else if (type.contains("slide_right"))   animation = this.slideHorizontal(1);
        else if (type.contains("slide_left"))    animation = this.slideHorizontal(-1);
        else                                   animation = null;

        return animation;
    }
    private Animation getAnimation(String type, View view) {
        Animation animation;
        if (type.equals("expand_horizontal"))           animation = this.expand("horizontal", view, 0);
        else if (type.equals("expand_vertical"))        animation = this.expand("vertical", view, 0);
        else if (type.equals("collapse_horizontal"))    animation = this.collapse("horizontal", view, 0);
        else if (type.equals("collapse_vertical"))      animation = this.collapse("vertical", view, 0);
        else                                            animation = null;

        return animation;
    }
    private Animation fling(int direction) {
        AnimationSet animation_set = new AnimationSet(false);

        Animation fading = new AlphaAnimation(1, 0);
        fading.setInterpolator(new AccelerateInterpolator());
        fading.setDuration(fling_duration - 200);

        ScaleAnimation shrinking;
        shrinking = new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
        shrinking.setDuration(fling_duration);
        shrinking.setFillAfter(true);

        TranslateAnimation translation;
        translation = new TranslateAnimation(0f, 0F, 0f, (display_height/2)*direction);
        translation.setStartOffset(0);
        translation.setDuration(fling_duration);

        translation.setFillAfter(true);
        translation.setInterpolator(new BounceInterpolator());

        animation_set.addAnimation(fading);
        animation_set.addAnimation(shrinking);
        animation_set.addAnimation(translation);

        return animation_set;
    }
    private Animation blink() {
        AnimationSet animation_set = new AnimationSet(true);

        Animation fade_in = new AlphaAnimation(0, 1);
        fade_in.setDuration(250);
        fade_in.setInterpolator(new AccelerateInterpolator());

        Animation fade_out = new AlphaAnimation(1, 0);
        fade_out.setDuration(750);
        fade_out.setInterpolator(new AccelerateInterpolator());
        fade_out.setStartOffset(250);
        fade_out.setFillAfter(true);

        ScaleAnimation grow = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, (float)0.5);
        grow.setDuration(250);
        grow.setFillAfter(true);

        ScaleAnimation shrink = new ScaleAnimation(1.2f, 1f, 1.2f, 1f, Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, (float)0.5);
        shrink.setDuration(750);
        shrink.setStartOffset(250);
        shrink.setFillAfter(true);

        animation_set.addAnimation(fade_in);
        animation_set.addAnimation(fade_out);
        animation_set.addAnimation(grow);
        animation_set.addAnimation(shrink);

        return animation_set;
    }
    private Animation blinkIn() {
        AnimationSet animation_set = new AnimationSet(true);

        Animation fade_in = new AlphaAnimation(0, 1);
        fade_in.setDuration(250);
        fade_in.setInterpolator(new AccelerateInterpolator());


        ScaleAnimation grow = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, (float)0.5);
        grow.setDuration(250);
        grow.setFillAfter(true);

        ScaleAnimation shrink = new ScaleAnimation(1f, 0.8f, 1f, 0.8f, Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, (float)0.5);
        shrink.setDuration(250);
        shrink.setStartOffset(250);
        shrink.setFillAfter(true);

        animation_set.addAnimation(fade_in);
        animation_set.addAnimation(grow);
        animation_set.addAnimation(shrink);

        return animation_set;
    }
    private Animation bulge() {
        AnimationSet animation_set = new AnimationSet(true);

        ScaleAnimation grow = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, (float)0.5);
        grow.setDuration(250);
        grow.setFillAfter(true);

        ScaleAnimation shrink = new ScaleAnimation(1f, 0.8f, 1f, 0.8f, Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, (float)0.5);
        shrink.setDuration(250);
        shrink.setStartOffset(250);
        grow.setFillAfter(true);

        animation_set.addAnimation(grow);
        animation_set.addAnimation(shrink);

        return animation_set;
    }
    private Animation dropDown(int direction) {
        AnimationSet animation_set = new AnimationSet(false);

        TranslateAnimation translation;
        if (direction == 1) {
            translation = new TranslateAnimation(0f, 0f, -display_height * direction, 0f);
        } else if (direction == -1) {
            translation = new TranslateAnimation(0f, 0f, 0f, display_height * direction);
        } else {
            translation = new TranslateAnimation(0f, 0f, 0f, 0f);
        }
        translation.setStartOffset(0);
        translation.setDuration(500);
        translation.setFillAfter(true);

        animation_set.addAnimation(translation);

        return animation_set;
    }
    private Animation dropUp(int direction) {
        AnimationSet animation_set = new AnimationSet(false);

        TranslateAnimation translation;
        if (direction == 1) {
            translation = new TranslateAnimation(0f, 0f, 0f, 2 * display_height);
        } else if (direction == -1) {
            translation = new TranslateAnimation(0f, 0f, 2 * display_height, 0f);
        } else {
            translation = new TranslateAnimation(0f, 0f, 0f, 0f);
        }
        translation.setStartOffset(0);
        translation.setDuration(500);
        translation.setFillAfter(true);

        animation_set.addAnimation(translation);

        return animation_set;
    }
    private Animation slideHorizontal(int direction) {
        AnimationSet animation_set = new AnimationSet(false);

        TranslateAnimation translation;
        if (direction == 1) {
            translation = new TranslateAnimation(-display_width * direction, 0f, 0f, 0f);
        } else if (direction == -1) {
            translation = new TranslateAnimation(0f, display_width * direction, 0f, 0f);
        } else {
            translation = new TranslateAnimation(0f, 0f, 0f, 0f);
        }
        translation.setStartOffset(0);
        translation.setDuration(1000);
        translation.setFillAfter(true);

        animation_set.addAnimation(translation);

        return animation_set;
    }
    private Animation move(int direction) {
        AnimationSet animation_set = new AnimationSet(false);

        TranslateAnimation translation;
        if (direction == 1) {
            translation = new TranslateAnimation(0f, 0f, from_y, to_y);
        } else if (direction == -1) {
            translation = new TranslateAnimation(0f, 0f, 0f, -from_y + to_y);
        } else {
            translation = new TranslateAnimation(0f, 0f, 0f, 0f);
        }
        translation.setStartOffset(0);
        translation.setDuration(2000);
        translation.setFillAfter(true);

        animation_set.addAnimation(translation);

        return animation_set;
    }
    public Animation expand(final String dimension, final View expandable_layout, final int duration) {
        expandable_layout.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final int target_dimension_size;
        if (dimension == "vertical") {
            target_dimension_size = expandable_layout.getMeasuredHeight();
        } else {
            target_dimension_size = expandable_layout.getMeasuredWidth();
        }

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        expandable_layout.getLayoutParams().width = 1;
        expandable_layout.setVisibility(View.VISIBLE);
        final Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (dimension == "vertical") {
                    expandable_layout.getLayoutParams().height = interpolatedTime == 1
                            ? LinearLayout.LayoutParams.WRAP_CONTENT
                            : (int) (target_dimension_size * interpolatedTime);
                } else {
                    expandable_layout.getLayoutParams().width = interpolatedTime == 1
                            ? LinearLayout.LayoutParams.WRAP_CONTENT
                            : (int) (target_dimension_size * interpolatedTime);
                }
                expandable_layout.requestLayout();
            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        animation.setFillBefore(true);
        if (duration == 0) {
            animation.setDuration((int) (target_dimension_size / expandable_layout.getContext().getResources().getDisplayMetrics().density));    // 1dp/ms
        } else {
            animation.setDuration(duration);
        }
        return animation;
    }

    public Animation collapse(final String dimension, final View expandable_layout, int duration) {
        final int target_dimension_size;

        if (dimension == "vertical") {
            target_dimension_size = expandable_layout.getMeasuredHeight();
        } else {
            target_dimension_size = expandable_layout.getMeasuredWidth();
        }

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    expandable_layout.setVisibility(View.GONE);
                } else {
                    if (dimension == "vertical") {
                        expandable_layout.getLayoutParams().height = target_dimension_size - (int) (target_dimension_size * interpolatedTime);
                    } else {
                        expandable_layout.getLayoutParams().width = target_dimension_size - (int) (target_dimension_size * interpolatedTime);
                    }
                    expandable_layout.requestLayout();
                }
            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        if (duration == 0) {
            animation.setDuration((int) (target_dimension_size / expandable_layout.getContext().getResources().getDisplayMetrics().density));   // 1dp/ms
        } else {
            animation.setDuration(duration);
        }
        return animation;
    }
}
