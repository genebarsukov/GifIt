package codewrencher.gifit.tools;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.ArrayList;

import codewrencher.gifit.ui.fragments.BaseFragment;
import codewrencher.gifit.ui.fragments.DetailsFragment;

/**
 * Created by Gene on 12/19/2015.
 */
public class LayoutBuilder {
    private View root_view;
    private ArrayList<PopupWindow> open_windows;
    public int screen_width;
    public int screen_height;

    public View getRootView() {
        return root_view;
    }
    public LayoutBuilder(View root_view, DetailsFragment fragment) {
        this.root_view = root_view;
        this.open_windows = new ArrayList();

        this.setScreenDimensions(fragment.getActivity());
    }
    public LayoutBuilder(View root_view, BaseFragment fragment) {
        this.root_view = root_view;
        this.open_windows = new ArrayList();

        this.setScreenDimensions(fragment.getActivity());
    }
    private void setScreenDimensions(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        this.screen_width = size.x;
        this.screen_height = size.y;
    }
    public void closeOpenWindows() {
        if (open_windows.size() > 0) {
              for (PopupWindow window : open_windows) {
                window.dismiss();
            }
        }
    }
    public View createWindow(int layout_id, final View anchor_view) {
        LayoutInflater inflater = LayoutInflater.from(anchor_view.getContext());

        View  popup_view = inflater.inflate(layout_id, null);

        anchor_view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        PopupWindow popup_window = new PopupWindow(popup_view, root_view.getWidth(), root_view.getHeight());
        open_windows.add(popup_window);
        popup_window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                open_windows.remove(open_windows.size() - 1);
            }
        });
        popup_window.setFocusable(true);
        popup_window.setBackgroundDrawable(new ColorDrawable());
        int location[] = new int[2];

        popup_view.getLocationOnScreen(location);
        anchor_view.getLocationOnScreen(location);

        int x_offset = anchor_view.getWidth() /  2 - popup_window.getWidth() / 2;
        int y_offset = anchor_view.getHeight() /  2 - popup_window.getHeight() / 2;

        popup_window.showAtLocation(root_view, Gravity.NO_GRAVITY,
                location[0] + x_offset, location[1] + y_offset);

        return popup_view;
    }
    public View createWindow(int layout_id, final View anchor_view, boolean persist, int width, int height) {
        LayoutInflater inflater = LayoutInflater.from(anchor_view.getContext());

        View  popup_view = inflater.inflate(layout_id, null);

        anchor_view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        PopupWindow popup_window = new PopupWindow(popup_view, width, height);
        open_windows.add(popup_window);
        popup_window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                open_windows.remove(open_windows.size() - 1);
            }
        });
        popup_window.setFocusable(true);
        if (persist == false) {
            popup_window.setBackgroundDrawable(new ColorDrawable());
        }
        int location[] = new int[2];
        popup_view.getLocationOnScreen(location);
        anchor_view.getLocationOnScreen(location);

        int x_offset = anchor_view.getWidth() /  2 - popup_window.getWidth() / 2;
        int y_offset = anchor_view.getHeight() /  2 - popup_window.getHeight() / 2;

        popup_window.showAtLocation(root_view, Gravity.NO_GRAVITY,
                location[0] + x_offset, location[1] + y_offset);

        return popup_view;
    }
    public View createWindow(int layout_id, final View anchor_view, Boolean persist, int popup_width, int popup_height, int center_x, int center_y) {
        LayoutInflater inflater = LayoutInflater.from(anchor_view.getContext());

        View  popup_view = inflater.inflate(layout_id, null);

        if (center_x == 0) {
            center_x = anchor_view.getWidth() /  2;
        }
        if (center_y == 0) {
            center_y = anchor_view.getHeight() /  2;
        }
        if (popup_width == 0 || popup_height == 0) {
            anchor_view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            popup_width = anchor_view.getWidth();
            popup_height = anchor_view.getHeight();
        }
        PopupWindow popup_window = new PopupWindow(popup_view, popup_width, popup_height);
        open_windows.add(popup_window);
        popup_window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                open_windows.remove(open_windows.size() - 1);
            }
        });
        popup_window.setFocusable(true);
        if (persist == false) {
            popup_window.setBackgroundDrawable(new ColorDrawable());
        }
        int location[] = new int[2];

        popup_view.getLocationOnScreen(location);
        anchor_view.getLocationOnScreen(location);

        int x_offset = center_x - popup_window.getWidth() / 2;
        int y_offset = center_y - popup_window.getHeight() / 2;

        popup_window.showAtLocation(root_view, Gravity.NO_GRAVITY,
                    location[0] + x_offset, location[1] + y_offset);

        return popup_view;
    }

    /***********************************************************************************************
     * Inflate new layout and insert it into the container which resides in achor view
     * @param new_layout_id the id of the layout you are going t inflate
     * @param container_id the id of the existing container you are planning to put the layout
     * @param anchor_view the parent view that contains the container
     * @return the new layout View
     */
    public static View addToLayout(int new_layout_id, int container_id, final View anchor_view) {
        LayoutInflater inflater = LayoutInflater.from(anchor_view.getContext());

        View new_layout = inflater.inflate(new_layout_id, null);
        View container = anchor_view.findViewById(container_id);

        if (container instanceof FrameLayout) {
            ((FrameLayout) container).addView(new_layout);
        }
        else if (container instanceof LinearLayout) {
            ((LinearLayout) container).addView(new_layout);
        }

        return new_layout;
    }

    /***********************************************************************************************
     * Removes a View from a container layout using the container id and anchor view
     * Worsk on several different types of layouts
     * @param view_to_remove : The View you want to trash
     * @param container_layout_id : The id of the container the view_to_remove is located in
     * @param anchor_view : Holds the container. Used to get the container view
     */
    public static void removeFromLayout( View view_to_remove, int container_layout_id, View anchor_view ) {

        View container = anchor_view.findViewById( container_layout_id );

        if (container instanceof FrameLayout) {
            ((FrameLayout) container).removeView(view_to_remove);
        }
        else if (container instanceof LinearLayout) {
            ((LinearLayout) container).removeView(view_to_remove);
        }
    }

    /***********************************************************************************************
     * Removes a View from a container layout using the container view and child view
     * Worsk on several different types of layouts
     * @param view_to_remove : The View you want to trash
     * @param container_layout : THe container View holding the view_to_remove
     */
    public static void removeFromLayout( View view_to_remove, View container_layout ) {

        if (container_layout instanceof FrameLayout) {
            ((FrameLayout) container_layout).removeView( view_to_remove );
        }
        else if (container_layout instanceof LinearLayout) {
            ((LinearLayout) container_layout).removeView(view_to_remove );
        }
    }

    /***********************************************************************************************
     * Simply gets the parent layout of the View in question
     * And removes the View from the prent
     * @param view_to_remove : The View you wish to remove from the UI
     */
    public static void removeView( View view_to_remove ) {

        ViewGroup parent = (ViewGroup) view_to_remove.getParent();
        parent.removeView( view_to_remove );
    }
    public static void addViewToContainer(View add_me, int container_id, final View anchor_view) {
        LinearLayout container = (LinearLayout) anchor_view.findViewById(container_id);
        container.addView(add_me);
    }
}
