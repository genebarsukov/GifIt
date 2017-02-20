package codewrencher.gifit.helpers.camera;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.adapters.ListViewAdapter;
import codewrencher.gifit.ui.MainActivity;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 3/6/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 *
 * Primarily handles the value scrollers for frame rate and frame count
 * Creates scroller views and populates their values
 * Monitors their state and updates the dependent data when they are scrolled
 * Sets the scrollers to their last visible value
 */
public class ValueScrollerHandler {

    private static String tag = "ValueScrollerHandler";

    private BaseFragment fragment;
    private View fragment_view;
    private LinkedHashMap<String, Boolean> settings_scrolled;
    private boolean scrolling;

    private double frame_rate = 1;
    private double frame_count = 1;

    /**
     * Constructor
     * @param fragment: Parent fragment, usually a camera
     */
    public ValueScrollerHandler(BaseFragment fragment) {
        this.fragment = fragment;
        this.fragment_view = fragment.getFragmentView();
        this.settings_scrolled = new LinkedHashMap();
        this.settings_scrolled.put("frame_rate", false);
        this.settings_scrolled.put("frame_count", false);
        this.scrolling = false;

        updateScrollerTickerText();
    }

    public double getFrameRate() {
        return this.frame_rate;
    }
    public double getFrameCount() {
        return this.frame_count;
    }

    /**
     * Create the frame rate and frame count scrollers
     * Set listeners for when they change
     * Update the settings in the preferences
     * @param min_value: Min value fo the scroller you want to create
     * @param max_value: Max scroller value
     * @param step: Its interval from one value to the next
     * @param scroller_type: Wither frame rate or frame count for now
     * @param default_value: What the scroller starts out as
     * @param list_id: The list view where the scroller lives
     * @param img_id: The spinning image of scroller
     */
    public void createValueScroller(double min_value, double max_value, double step, final String scroller_type, float default_value, int list_id, int img_id) {

        double saved_value = (double) fragment.shared_preferences.getFloat(scroller_type, default_value);
        int saved_position = 0;

        ArrayList<Double> value_list = new ArrayList<>();
        int position = 0;

        if (step > 0) {
            for (double i = min_value; i <= max_value; i += step) {
                if (saved_value == i) {
                    saved_position = position;
                }
                value_list.add( Double.valueOf( new DecimalFormat("#.#").format(i) ) );
                position ++;
            }
        }

        final ListView scroller_view = (ListView) fragment_view.findViewById(list_id);
        final ListViewAdapter list_adapter = new ListViewAdapter(fragment, R.layout.item_value, value_list);

        list_adapter.setImageResourceId(img_id);
        list_adapter.setItemType(scroller_type);

        scroller_view.setAdapter(list_adapter);
        // set saved scroller values as selected
        scroller_view.setSelection(saved_position);

        setOnScrollListener(scroller_view, scroller_type, list_adapter);
    }

    /**
     * Set a listener to listen for every time the scrollers move so that we can get the new values
     * and update the dependent data sets
     * @param scroller_view: The view where the scroller lives in the UI
     * @param scroller_type: A simple tag identifying the type of the scroller. Different types may
     *                       have slighly different functionalities
     */
    public void setOnScrollListener(ListView scroller_view, final String scroller_type, final ListAdapter list_adapter) {

        scroller_view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    settings_scrolled.put(scroller_type, true);
                }

                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (scrolling){
                            // get first visible item
                            View itemView = view.getChildAt(0);
                            int top = Math.abs(itemView.getTop()); // top is a negative value
                            int bottom = Math.abs(itemView.getBottom());
                            if (top >= bottom){
                                settings_scrolled.put(scroller_type, true);
                                view.setSelection(view.getFirstVisiblePosition() + 1);
                               // view.setSelectionFromTop(view.getFirstVisiblePosition() +1, 0);
                            } else {
                                settings_scrolled.put(scroller_type, true);
                                view.setSelection(view.getFirstVisiblePosition());
                               // view.setSelectionFromTop(view.getFirstVisiblePosition(), 0);
                            }
                        }
                        scrolling = false;
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        scrolling = true;
                        break;
                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (settings_scrolled.get(scroller_type)) {
                    settings_scrolled.put(scroller_type, false);

                    int value_index = firstVisibleItem;
                    double chosen_scroller_value = (double) list_adapter.getItem(value_index);

                    // Update the data dependent on the scrollers
                    if (scroller_type.equals("frame_rate")) {
                        setFrameRate(chosen_scroller_value);
                    } else if (scroller_type.equals("frame_count")) {
                        setFrameCount(chosen_scroller_value);
                    }
                }
            }
        });
    }

    /**
     * Gats the frame rate from the UI and updates shared preferences as well as UI text
     * @param frame_rate: Frames per second
     */
    public void setFrameRate(double frame_rate) {
        // update dependent data
        this.frame_rate = frame_rate;
        // update shared preferences
        SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getSavedActivity());
        shared_preferences.edit().putFloat("frame_rate", (float) frame_rate).apply();
        // update UI fps text
        setFrameRateText(frame_rate);
    }

    /**
     * * Gats the frame count from the UI and updates shared preferences as well as UI text
     * @param frames_to_take: Number of frames to take
     */
    public void setFrameCount(double frames_to_take) {
        // update dependent data
        this.frame_count = frames_to_take;
        // update shared preferences
        SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getSavedActivity());
        shared_preferences.edit().putFloat("frame_count", (float) frames_to_take).apply();
        // update UI frame count text
        setFrameCountText(frames_to_take);
    }

    /**
     * Attempts to get saved scroller data from shared preferences and uses it to update UI text
     * tickers for the scrollers
     */
    private void updateScrollerTickerText() {
        SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getSavedActivity());

        float frame_rate = shared_preferences.getFloat("frame_rate", MainActivity.DEFAULT_FRAME_RATE);
        float frame_count =  shared_preferences.getFloat("frame_count", MainActivity.DEFAULT_FRAME_COUNT);

        this.frame_rate = frame_rate;
        this.frame_count = frame_count;

        setFrameRateText(frame_rate);
        setFrameCountText(frame_count);
    }

    /**
     * Updates text ticker for the frame rate scroller
     * @param frame_rate: Frames per second
     */
    private void setFrameRateText(double frame_rate) {
        TextView frame_rate_text = (TextView) fragment.getFragmentView().findViewById(R.id.frame_rate);
        frame_rate = Double.valueOf( new DecimalFormat("#.##").format(frame_rate) );

        String fps = String.valueOf(frame_rate);
        frame_rate_text.setText(fps + " fps");
    }

    /**
     * Updates text ticker for the frame count scroller
     * @param frames_to_take: Number of frames to take
     */
    private void setFrameCountText(double frames_to_take) {
        TextView frame_count_text = (TextView) fragment.getFragmentView().findViewById(R.id.frame_count);
        String frame_count = String.valueOf(frames_to_take);
        frame_count_text.setText(frame_count + " frames");
    }
}
