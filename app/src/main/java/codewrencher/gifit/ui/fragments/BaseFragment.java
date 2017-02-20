package codewrencher.gifit.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.Damen;
import codewrencher.gifit.helpers.async.Animator;
import codewrencher.gifit.helpers.async.GestureListenerWrap;
import codewrencher.gifit.helpers.async.interfaces.SharingHelperListener;
import codewrencher.gifit.helpers.db.DBAccessor;
import codewrencher.gifit.helpers.files.FileManager;
import codewrencher.gifit.helpers.files.SharingManager;
import codewrencher.gifit.helpers.gif.GifTimer;
import codewrencher.gifit.objects.Stash;
import codewrencher.gifit.tools.Torch;
import codewrencher.gifit.ui.BreadCrumbMenu;
import codewrencher.gifit.ui.FragmentPager;
import codewrencher.gifit.ui.MainActivity;

public class BaseFragment extends Fragment implements SharingHelperListener {
    protected int fragment_layout_id;
    protected int tab_index;
    protected int fragment_count;
    protected View fragment_view;

    public FragmentPager fragment_pager;

    public DBAccessor db_accessor;
    public Torch torch;
    public FileManager file_manager;
    public SharingManager sharing_manager;
    public int screen_width;
    public int screen_height;
    protected GestureDetector gesture_detector;
    protected GestureListenerWrap gesture_listener;
    public Animator animator;
    public Context context;
    private MainActivity saved_activity;
    public SharedPreferences shared_preferences;

    protected String action;
    protected String gif_chain_id;
    protected ArrayList <String> extra_param_list;
    public Stash stash;
    protected String on_resume_action;
    public Damen damen;
    public GifTimer gif_timer;
    public Bitmap watermark;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.action = "";
        this.gif_chain_id = "";
        this.extra_param_list = new ArrayList<>();

        this.saved_activity = (MainActivity) getActivity();
        this.damen = new Damen(this);
        this.stash = saved_activity.stash;
        this.shared_preferences = PreferenceManager.getDefaultSharedPreferences(this.saved_activity);
        this.getDisplayWidth();
        this.getDisplayHeight();
        this.fragment_count = fragment_pager.getCount();

        this.torch = new Torch(this.getActivity().findViewById(R.id.pager));
        this.db_accessor = ((MainActivity) getActivity()).db_accessor;
        this.sharing_manager = new SharingManager(this);
        this.file_manager = new FileManager(this);

        this.gesture_listener = new GestureListenerWrap();
        this.gesture_detector = new GestureDetector(this.getContext(), gesture_listener);

        this.animator = new Animator(getDisplayWidth(), getDisplayHeight());

        this.context = getContext();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.fragment_view = inflater.inflate(fragment_layout_id, container, false);

        BreadCrumbMenu.createBreadCrumbMenu(fragment_view, inflater, container, tab_index, fragment_count);

        return this.fragment_view;
    }

    @Override
    public void onPause() {
        super.onPause();

    }
    public MainActivity getSavedActivity() {
        return saved_activity;
    }
    public View getFragmentView() {
        return fragment_view;
    }
    public void setFragmentPager(FragmentPager fragment_pager) {
        this.fragment_pager = fragment_pager;
    }
    public Animator getAnimator() {
        return this.animator;
    }
    protected void switchTab(int tab){
        MainActivity fragment_activity = (MainActivity) this.getSavedActivity();
        fragment_activity.switchTab(tab);
    }
    public void setAction( String action ) {
        this.action = action;
    }
    public String getAction() {
        return this.action;
    }
    public void removeFragmentByIndex(int index) {
        MainActivity fragment_activity = (MainActivity) this.getSavedActivity();
        fragment_activity.removeFragmentByIndex(index);
    }
    public void setOnResumeAction(String on_resume_action) {
        this.on_resume_action = on_resume_action;
    }
    public void lockPaging(){
        MainActivity fragment_activity = (MainActivity) this.getActivity();
        fragment_activity.view_pager.disablePaging();
    }
    public void unlockPaging(){
        MainActivity fragment_activity = (MainActivity) this.getActivity();
        fragment_activity.view_pager.enablePaging();
    }

    public int getCurrentTab(){
        MainActivity fragment_activity = (MainActivity) this.getActivity();
        return fragment_activity.getCurrentTab();
    }
    protected BaseFragment getThis() {
        return this;
    }
    private void setScreenDimensions(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        this.screen_width = size.x;
        this.screen_height = size.y;
    }
    public int getDisplayWidth() {
        if (this.screen_width == 0) {
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            screen_width = metrics.widthPixels;
        }
        return screen_width;
    }
    public int getDisplayHeight() {
        if (this.screen_height == 0) {
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            screen_height = metrics.heightPixels;
        }
        return screen_height;
    }
    public GestureDetector getGesureDtector() {
        return this.gesture_detector;
    }

    protected Bundle bundleDetailsArguments(int index, String title, Bitmap image) {
        ByteArrayOutputStream image_stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, image_stream);
        byte[] image_bytes = image_stream.toByteArray();

        Bundle args = new Bundle();

        args.putInt("index", index);
        args.putString("text", title);
        args.putByteArray("image", image_bytes);

        return args;
    }
    protected Bundle bundleDetailsArguments(String title, byte[] image_bytes) {
        Bundle args = new Bundle();
        args.putString("text", title);
        args.putByteArray("image", image_bytes);

        return args;
    }
    protected Bundle bundleDetailsArguments(String type, String detail_type) {
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putString("detail_type", detail_type);

        return args;
    }
    protected Bundle bundleDetailsArguments(String type, String title, String file_path) {
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putString("text", title);
        args.putString("image_path", file_path);

        return args;
    }
    protected Bundle bundleDetailsArguments(int index, String title, String file_path) {
        Bundle args = new Bundle();
        args.putInt("index", index);
        args.putString("text", title);
        args.putString("image_path", file_path);

        return args;
    }


    protected void openDetailsFragment(int index, String type, String text, String image_path) {
        Bundle args = this.bundleDetailsArguments(index, text, image_path);

        fragment_pager.addItem(type, args);
        fragment_pager.notifyDataSetChanged();

        if (type.equals("downloaded_gif")) {
            switchTab( fragment_pager.getLastTabIndex() );
        } else {
            switchTab(1);
        }
    }
    public void openDetailsFragment(String type, String text, String pic_file_path) {
        Bundle args = this.bundleDetailsArguments(type, text, pic_file_path);

        fragment_pager.addItem(type, args);
        fragment_pager.notifyDataSetChanged();
        switchTab(2);
    }

    protected void openDetailsFragment(String type, String text, ArrayList<String> file_paths) {
        Bundle args = this.bundleDetailsArguments(type, text, file_paths);

        fragment_pager.addItem(type, args);
        fragment_pager.notifyDataSetChanged();
        switchTab(1);
    }
    protected Bundle bundleDetailsArguments(String type, String title, ArrayList<String> file_paths) {
        Bundle args = new Bundle();
        args.putString("type", type);
        args.putString("text", title);
        args.putStringArrayList("file_paths", file_paths);

        return args;
    }

    protected void openDetailsFragment( String type, LinkedHashMap<String, String> parameters, ArrayList<String> gif_frame_names ) {
        Bundle args = this.bundleDetailsArguments( type, parameters, gif_frame_names );

        fragment_pager.addItem(type, args);

        switchTab(1);
    }
    protected Bundle bundleDetailsArguments( String type, LinkedHashMap<String, String> parameters, ArrayList<String> gif_frame_names ) {
        Bundle args = new Bundle();
        args.putString("type", type);

        for ( String key : parameters.keySet() ) {
            args.putString( key, parameters.get(key) );
        }
        args.putStringArrayList("frames", gif_frame_names);

        return args;
    }
    /***********************************************************************************************
     * Open any fragment and pass a flat Hash map as arguments
     * @param type              String: the fragment tag that will be used in the fragment pager to hold the fragment arguments
     * @param fragment_index    int: where in the pager the fragment is located (usually constant)
     * @param parameters        LinkedHashMap(): a map of key : value pairs that will be set as the fragment args
     */
    public void openFragment(String type, int fragment_index, LinkedHashMap<String, String> parameters) {
        Bundle args = new Bundle();

        args.putString("type", type);

        for ( String key : parameters.keySet() ) {
            args.putString( key, parameters.get(key) );
        }
        fragment_pager.addItem(type, args);

        switchTab(fragment_index);
    }

    /***********************************************************************************************
     * Open any fragment. Pass flat hash map of params as well as an ArrayList of Strings ase extra params
     * @param type              String: the fragment tag that will be used in the fragment pager to hold the fragment arguments
     * @param fragment_index    int: where in the pager the fragment is located (usually constant)
     * @param parameters        LinkedHashMap(): a map of key : value pairs that will be set as the fragment args
     * @param param_list        ArrayList: will be set as the gragment's args. Used for things like loading files from a list of paths
     */
    public void openFragment( String type, int fragment_index, LinkedHashMap<String, String> parameters, ArrayList<String> param_list ) {
        Bundle args = new Bundle();

        args.putString("type", type);

        for ( String key : parameters.keySet() ) {
            args.putString( key, parameters.get(key) );
        }
        args.putStringArrayList("frames", param_list);

        fragment_pager.addItem(type, args);
        fragment_pager.notifyDataSetChanged();

        switchTab(fragment_index);
    }


    public void loadDownloadedGif(String gif_file_path) {

        openDetailsFragment(this.tab_index, "downloaded_gif", "", gif_file_path);
    }

    /**
     * Set ringer to silent - silences the Media Recorder Beep
     */
    public void muteRinger() {
        ((AudioManager) getSavedActivity().getApplicationContext().getSystemService(Context.AUDIO_SERVICE)).setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    /**
     * Set ringer to normal - u-silences the Media Recorder Beep
     */
    public void unMuteRinger() {
        ((AudioManager) getSavedActivity().getApplicationContext().getSystemService(Context.AUDIO_SERVICE)).setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    @Override
    public void onWindowClosed(String window_type, String message) {}
    @Override
    public void onWindowClosed(String window_type, String message, String param) {}
    @Override
    public void onLoginSuccessful(String param, String message) {}
    @Override
    public void onSetPendingAppearance(String window_type) {}
    @Override
    public void onSharingActionDetected(ArrayList<String> selected_to_share_user_ids) {}
    @Override
    public void onActionCompleted(String action_type, String parameter, String message) {}
}
