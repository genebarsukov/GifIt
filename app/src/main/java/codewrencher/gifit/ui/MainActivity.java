package codewrencher.gifit.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.Animator;
import codewrencher.gifit.helpers.async.interfaces.AnimatorListener;
import codewrencher.gifit.helpers.db.DBAccessor;
import codewrencher.gifit.helpers.files.FileManager;
import codewrencher.gifit.objects.Stash;
import codewrencher.gifit.objects.complex.AppNotificationDrawer;
import codewrencher.gifit.objects.complex.gif_chain.GifChain;
import codewrencher.gifit.tools.ToolBox;
import codewrencher.gifit.tools.Torch;
import codewrencher.gifit.ui.fragments.BaseFragment;

public class MainActivity extends GeneralActivity implements AnimatorListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String tag = "MainActivity";

    public static final float DEFAULT_FRAME_RATE = 2.0f;
    public static final float MIN_FRAME_RATE = 0.5f;
    public static final float MAX_FRAME_RATE = 10f;
    public static final float FRAME_RATE_STEP = 0.5f;

    public static final float DEFAULT_FRAME_COUNT = 6.0f;
    public static final float MIN_FRAME_COUNT = 3;
    public static final float MAX_FRAME_COUNT = 30;
    public static final float FRAME_COUNT_STEP = 1;

    public static final String APP_STORAGE_DIRECTORY = "GifIt";

    public static final String BASE_SERVER_URL = "http://www.codewrencher.com/gifit";
    public static final String SERVER_SHARED_FILE_PATH = "http://www.codewrencher.com/gifit";
    public static final String BASIC_AUTH_USER = "basic_auth_user";
    public static final String BASIC_AUTH_PASSWORD = "basic_auth_password";

    public int last_fragment_index;
    private boolean shutter_closed;
    public boolean gif_space_launched;
    public boolean gif_space_on;

    public FragmentPager fragment_pager;
    public CustomViewPager view_pager;
    public DBAccessor db_accessor;
    private LinearLayout ad_shutter;
    private AppNotificationDrawer app_notification_drawer;
    private BroadcastReceiver local_notification_receiver;
    private boolean app_running;
    private String go_to_gif_chain_id;
    Runnable run_when_permissions_granted;
    public Boolean permissions_granted = false;
    public boolean camera_created = false;
    public Stash stash;
    private BaseFragment gif_space_fragment;
    public int ad_height;

    public static final String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean alerted_user_permissions = false;

    /**
     * Restore the Activity state
     * @param savedInstanceState Holds all the saved fragment variables
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("tag", "Activity Created");
        setContentView(R.layout.activity_main);

        // Initialize the Stash!
        this.stash = new Stash();

        // set the ad height for later calculations
        View ad_view = findViewById(R.id.ad_view);
        this.ad_height = ad_view.getHeight();

        // Here we resume the GifSpace Fragments if it was previously created
        if (savedInstanceState != null) {
            gif_space_fragment = (BaseFragment) getSupportFragmentManager().getFragment(savedInstanceState, "gif_space");
        }
        this.app_running = true;
        this.torch = new Torch(findViewById(R.id.pager));
        this.fragment_pager = new FragmentPager(getSupportFragmentManager());

        this.view_pager = (CustomViewPager) findViewById(R.id.pager);

        super.fragment_pager = fragment_pager;
        super.view_pager = view_pager;

        this.db_accessor = new DBAccessor(this);

        this.clearTempPicFolder();

        if (! this.handleFirstLaunchEvents()) {
            this.handleOnUpgrade();
        }
        this.checkPermissions(null);
        this.loadBannerAd(false);
        this.setAdCloserAnimation();

        view_pager.setAdapter(fragment_pager);
    }

    /**
     * Save the activity state
     * @param outState Used to store Activity variables
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        stash.store("gif_space_fragment", gif_space_fragment);
        stash.store("last_visible_fragment_index", getCurrentTab());
    }

    @Override
    protected void onDestroy() {
        Log.d("tag", "Activity Destroyed");
        this.app_running = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver( local_notification_receiver );
        super.onDestroy();
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d("tag", "Activity Resumed");

        view_pager.addOnPageChangeListener(new CustomViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                last_fragment_index = position;
                view_pager.getCurrentItem();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // On Resume, switch to the last visible tab
        if (stash.get("last_visible_fragment_index") != null) {
            if ((Integer) stash.get("last_visible_fragment_index") != getCurrentTab()) {
                switchTab((Integer) stash.get("last_visible_fragment_index"));
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onBackPressed() {
        int current_tab = getCurrentTab();
        if (current_tab > 0) {
            switchTab(current_tab - 1);
        } else {
            switchTab(0);
        }
    }

    public void removeFromStash(String key) {
        if (this.stash != null) {
            this.stash.remove(key);
        }
    }
    /**
     * Check all relevant permissions to run the app: camera, file read and write, etc
     * Perform a runnable action on callvack if the permission have been granted
     * @param run_when_permissions_granted: The action to perform when permissions have been granted
     */
    public void checkPermissions(Runnable run_when_permissions_granted) {
        this.run_when_permissions_granted = run_when_permissions_granted;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            this.onPermissionsGranted(); // Don't need to check permissions for older versions
            return;
        }
        if (this.permissions_granted) {
            this.onPermissionsGranted();                    // if already granted, skip to callback
        } else {                                            // otherwise, check all permissions
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

    /**
     * Turn off the Touch gestures for the view pager if we don't have camera permissions yet
     * because we don't want the user flipping to camera prematurely and crashing it
     */
    private void setViewPagerListener() {
        final View pager_view = findViewById(R.id.pager);
        pager_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (! permissions_granted) {
                    return true;    // don't fire touch event
                } else {
                    return false;   // fire touch event as usual
                }
            }
        });
    }

    public String getGoToGifChainId() {
        return this.go_to_gif_chain_id;
    }

    public AppNotificationDrawer getAppNotificationDrawer() {
        return this.app_notification_drawer;
    }

    /***********************************************************************************************
     * Extracts the gif chain id from the Local Notification and updates the Notification Drawer UI
     * @param intent
     */
    private void receiveNewNotification( Intent intent ) {

        String gif_chain_id = intent.getStringExtra("gif_chain_id");

        if ( gif_chain_id != null ) {

            Set existing_chain_id_set = this.app_notification_drawer.getGifChains().keySet();

            if ( existing_chain_id_set.contains( gif_chain_id ) ) {

                Log.d( "GIF CHAIN", "ALREADY EXIST,  gif_chain_id: " + gif_chain_id );

                if ( this.action != null && this.action.equals("go_to_gif") ) {
                    if ( this.go_to_gif_chain_id != null ) {

                 //       AppNotification app_notification = (AppNotification) this.app_notification_drawer.getGifChains().get( gif_chain_id ).getParentObject();
               //         app_notification.getRenderedView().findViewById(R.id.main_notification_image).callOnClick();

                        this.app_notification_drawer.goToGif( gif_chain_id );
                    }
                }
            }
            else {
                Log.d( "NEW GIF CHAIN", "new gif_chain_id: " + gif_chain_id );
                this.app_notification_drawer.retrieveSingleNotification(gif_chain_id);
            }
        }
    }
    private void createAppNotificationMenu() {
        app_notification_drawer = new AppNotificationDrawer(this, R.layout.drawer_notifications, R.id.notification_container, findViewById(R.id.notification_container));
        app_notification_drawer.create();
    }
    private void createLocalNotificationReceiver() {

        local_notification_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String gif_chain_id = intent.getStringExtra("gif_chain_id");
                if ( gif_chain_id != null ) {
                    receiveNewNotification( intent );
                }
                Log.d( "RECEIVED BROADCAST", "With gif_chain_id: " + gif_chain_id );
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(local_notification_receiver, new IntentFilter("received-new-gif"));
    }
    public LinkedHashMap<String, GifChain> getGifChains() {
        return this.app_notification_drawer.getGifChains();
    }
    public GifChain getGifChainById( String id ) {
        return this.app_notification_drawer.getGifChains().get(id);
    }

    public void clearTempPicFolder() {
        FileManager file_maneger = new FileManager();
        ArrayList<String> image_paths = file_maneger.getSavedFilePaths("temp");
        if (image_paths != null) {
            for (String image_path : image_paths) {
                File file = new File(image_path);
                file.delete();
            }
        }
    }
    private Boolean handleFirstLaunchEvents() {
        return false; // the logic in this method was moved to Camera fragment instead
    }

    /**
     * Create the Camera and the GifSpace Fragments
     * @param extras Bundle that contains data needed to initiate the Fragments
     */
    public void createInitialFragments(Bundle extras) {

        fragment_pager.addItem("camera", extras);
        fragment_pager.notifyDataSetChanged();

        if ( extras != null ) {
            if ( extras.getString("gif_chain_id") != null) {

                this.action = "go_to_gif";
                this.go_to_gif_chain_id = extras.getString("gif_chain_id");
            }
        }
    }
    public void showLoading( String text, String color ) {

        if ( text == null ) {
            text = "";
        }
        int color_id;

        switch (color) {

            case "blue":
                color_id = R.color.nice_blue;
                break;

            case "green":
                color_id = R.color.spinner_green;
                break;

            default:
                color_id = R.color.nice_blue;
                break;
        }

        ProgressBar progress_bar = (ProgressBar) findViewById(R.id.main_progress_bar);
        progress_bar.getIndeterminateDrawable().setColorFilter(getResources().getColor(color_id), android.graphics.PorterDuff.Mode.SRC_IN);

        TextView loading_text =  (TextView) findViewById(R.id.loading_text);
        TextView loading_text_2 =  (TextView) findViewById(R.id.loading_text_2);

        /** If text contains spaces, split it into 2 lines on the space */
        if ( text.contains(" ") ) {

            String text_1 = text.split(" ")[0];
            String text_2 = text.split(" ")[1];

            loading_text.setText( text_1 );
            loading_text_2.setText( text_2 );
            loading_text_2.setVisibility(View.VISIBLE);

        } else {
            loading_text.setText(text);
        }

        progress_bar.setVisibility(View.VISIBLE);
        loading_text.setVisibility(View.VISIBLE);
    }
    public void hideLoading() {
        ProgressBar progress_bar = (ProgressBar) findViewById(R.id.main_progress_bar);
        progress_bar.setVisibility(View.GONE);

        TextView loading_text =  (TextView) findViewById(R.id.loading_text);
        loading_text.setVisibility(View.GONE);

        TextView loading_text_2 =  (TextView) findViewById(R.id.loading_text_2);
        loading_text_2.setVisibility(View.GONE);

        TextView loading_percent_complete =  (TextView) findViewById(R.id.loading_percent_complete);
        loading_percent_complete.setVisibility(View.INVISIBLE);
    }
    public void updateLoadingPercentComplete( int percent_complete ) {

        TextView loading_percent_complete =  (TextView) findViewById(R.id.loading_percent_complete);
        loading_percent_complete.setVisibility(View.VISIBLE);

        loading_percent_complete.setText( String.valueOf(percent_complete) + "%" );

        int color_id = ToolBox.pickColorByPercent( percent_complete );
        loading_percent_complete.setTextColor(ContextCompat.getColor(this, color_id));
    }
    public void handleOnUpgrade() {
        try {
            PackageInfo package_info = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(this);

            int actual_version_code = package_info.versionCode;
            int saved_version_code = shared_preferences.getInt("version_code", 0);

            if (actual_version_code != saved_version_code) {
                shared_preferences.edit().putInt("version_code", actual_version_code).apply();
                /**
                 * @ToDo: Perform on App Upgrade Tasks
                 */
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void switchTab(int tab_index) {
        view_pager.setAdapter(fragment_pager);
        view_pager.setCurrentItem(tab_index);
    }

    public void loadBannerAd(Boolean test) {
        final AdView ad_view = (AdView) findViewById(R.id.ad_view);
        final AdRequest ad_request;
        if (test) {
            ad_request = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("F9FA92D8425D036A97C801421136BD7A")
                    .build();
        } else {
            ad_request = new AdRequest.Builder().build();
        }
        ad_view.loadAd(ad_request);
    }
    public void setAdCloserAnimation() {
        final ImageView close_ad = (ImageView) findViewById(R.id.close_ad);
        ad_shutter = (LinearLayout) findViewById(R.id.ad_shutter);

        close_ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Animator animator = new Animator(getDisplayWidth(), getDisplayHeight());

                if (! shutter_closed) {
                    shutter_closed = true;

                    ad_shutter.setVisibility(View.VISIBLE);
                    ViewGroup.LayoutParams shutter_params = ad_shutter.getLayoutParams();
                //    shutter_params.width = getDisplayWidth();
                    ad_shutter.setLayoutParams(shutter_params);

                    animator.registerListener(MainActivity.this);
                    animator.setAnimation(ad_shutter, "slide_right");
                    animator.animate(ad_shutter);
                } else {
                    shutter_closed = false;

                    animator.registerListener(MainActivity.this);
                    animator.setAnimation(ad_shutter, "slide_left");
                    animator.animate(ad_shutter);
                }

            }
        });
    }
    @Override
    public void onAnimationFinished(String animation_type) {
        if (animation_type.equals("slide_left")) {
            ImageView close_ad = (ImageView) findViewById(R.id.close_ad);
            close_ad.setImageResource(R.mipmap.arrow_double_right_shiny);
            ad_shutter.setVisibility(View.GONE);
        } else if (animation_type.equals("slide_right")) {
            ImageView close_ad = (ImageView) findViewById(R.id.close_ad);
            close_ad.setImageResource(R.mipmap.arrow_double_left_shiny);
        }
    }

    @Override
    public void onAnimationFinished(String animation_type, View animated_view) {

    }

    @Override
    protected void onNewIntent(Intent intent) {

        if ( intent.getStringExtra("gif_chain_id") != null) {

            this.action = "go_to_gif";
            this.go_to_gif_chain_id = intent.getStringExtra("gif_chain_id");
        }
  //      this.app_notification_drawer.retrieveSingleNotification(gif_chain_id);

        this.receiveNewNotification(intent);
    }

    /**
     * Callback performed when PermissionChecker is finished and permissions have been granted
     */
    public void onPermissionsGranted() {
        this.permissions_granted = true;

        if (! camera_created) {

            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            this.createInitialFragments(extras);

            camera_created = true;
        }
        if (this.run_when_permissions_granted != null) {
            this.run_when_permissions_granted.run();
        }
    }
    /**
     * Callback performed when PermissionChecker is finished one or more permission was denied
     * @param first_denied_permission: The name of the permission that was denied
     */
    public void onPermissionsDenied(String first_denied_permission) {
        this.permissions_granted = false;

        if (! alerted_user_permissions) {
            torch.longTorch("You are missing some necessary permissions required to run the app.");
            torch.longTorch("Please allow the requested permissions");

            // Rerun the request permissions dialogs
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkPermissions(null);
                }
            }, 2000);
        } else {
            finish();
        }
    }

    /**
     * Permission Request Result Callback: Either notify the listener that permission was denied,
     * or continue checking permissions
     * @param request_code: The permission index in the this.permissions array
     * @param permissions: the
     * @param grant_results
     */
    @Override
    public void onRequestPermissionsResult(int request_code, @NonNull String[] permissions,
                                           @NonNull int[] grant_results) {

        if (grant_results.length == 0) {
            this.onPermissionsDenied("Permission Request Interrupted");
        } else {
            for(int result : grant_results) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    this.onPermissionsDenied("Failed to get Permissions");
                    return;
                }
            }
            this.onPermissionsGranted();
        }
    }
}
