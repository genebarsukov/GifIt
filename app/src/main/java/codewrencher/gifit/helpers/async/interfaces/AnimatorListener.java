package codewrencher.gifit.helpers.async.interfaces;

import android.view.View;

/**
 * Created by Gene on 12/26/2015.
 */
public interface AnimatorListener {
    void onAnimationFinished(String animation_type);
    void onAnimationFinished(String animation_type, View animated_view);
}
