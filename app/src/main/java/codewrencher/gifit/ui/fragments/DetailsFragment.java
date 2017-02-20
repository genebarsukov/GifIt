package codewrencher.gifit.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.interfaces.AnimatorListener;
import codewrencher.gifit.helpers.async.interfaces.GestureListenerWrapListener;
import codewrencher.gifit.helpers.files.SharingManager;

/**
 * Created by Gene on 12/25/2015.
 */
public class DetailsFragment extends BaseFragment implements AnimatorListener, GestureListenerWrapListener {

    protected int item_index;
    protected String text;
    protected String local_saved_file_path;
    protected Bitmap bitmap;

    public DetailsFragment() {}

    public View getFragmentView() {
        return fragment_view;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.tab_index = 2;
        gesture_listener.registerListener(this);
        if (getArguments() != null) {
            item_index = getArguments().getInt("index");
            text = getArguments().getString("text");
            /**
             * the local_saved_file_path gets passed down the line. It is at one point saved in the
             * FragmentPager. It initially comes either from the GalleyView items or the message
             * passed along with a notification
             */
            local_saved_file_path = getArguments().getString("image_path");
            bitmap = file_manager.loadBitmapFromFilePath(local_saved_file_path);
        }
        this.sharing_manager = new SharingManager(this);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return fragment_view;
    }
    @Override
    public void onResume() {
        super.onResume();
        fragment_pager.image_shared = false;
    }
    @Override
    public void onAnimationFinished(String animation_type) {}

    @Override
    public void onAnimationFinished(String animation_type, View animated_view) {}

    @Override
    public void onFlingUpDetected() {
        if (sharing_manager.share_list_view == null || (!sharing_manager.share_list_view.isShown())) {

            animator.registerListener(this);
            animator.setAnimation(fragment_view.findViewById(R.id.image), "fling_up");
            animator.setSecondaryAnimation(fragment_view.findViewById(R.id.share), "bulge");
            animator.animate(fragment_view.findViewById(R.id.image));
        }
    }
    @Override
    public void onFlingDownDetected() {}
    @Override
    public void onDownDetected() {}
}
