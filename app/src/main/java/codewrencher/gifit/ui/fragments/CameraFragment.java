package codewrencher.gifit.ui.fragments;

import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.LinkedHashMap;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.interfaces.AnimatorListener;
import codewrencher.gifit.helpers.camera.CameraHelper;
import codewrencher.gifit.helpers.camera.ValueScrollerHandler;
import codewrencher.gifit.helpers.camera.VideoDirector;
import codewrencher.gifit.helpers.files.FileManager;
import codewrencher.gifit.objects.complex.gif_chain.GifChain;
import codewrencher.gifit.tools.LayoutBuilder;
import codewrencher.gifit.tools.ToolBox;
import codewrencher.gifit.ui.MainActivity;

/**
 * Created by Gene on 12/24/2015.
 */
public class CameraFragment extends BaseFragment implements AnimatorListener {

    private static String tag = "CameraFragment";

    private Camera camera;
    private CameraHelper camera_helper;
    public CameraFragment() {}
    MediaActionSound shutter_click;
    boolean is_recording = false;

    private ValueScrollerHandler value_scroller_handler;
    private VideoDirector video_director;
    private Boolean help_overlay_open;
    private View help_view;

    public static CameraFragment newInstance(Bundle args) {
        CameraFragment fragment = new CameraFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private void setFlipCamClickListener() {
        ImageButton flip_cam = (ImageButton) fragment_view.findViewById(R.id.flip_cam);
        flip_cam.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            camera_helper.flipCamera();
                                        }
                                    }
        );
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.help_overlay_open = false;

        this.fragment_layout_id = R.layout.fragment_camera;
        this.tab_index = 0;

        if ( getArguments() != null ) {
            if ( getArguments().get("action") != null) {
                this.action = (String) getArguments().get("action");
                if (this.action == null) {
                    this.action = "captured";
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        this.configureGears();
        handleFirstLaunchEvents();

        return fragment_view;
    }
    @Override
    public void onPause() {
        camera_helper.pauseCamera();
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();

        camera_helper = new CameraHelper(this);
        camera_helper.setCameraFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        camera_helper.createTextureView();

        shutter_click = new MediaActionSound();
        shutter_click.load(MediaActionSound.SHUTTER_CLICK);

        // Value Scroller and Video Director
        this.value_scroller_handler = new ValueScrollerHandler(this);
        this.video_director = new VideoDirector(this, value_scroller_handler, camera_helper);

        video_director.operateCameraShutterButton();
        this.setFlipCamClickListener();

        this.value_scroller_handler.createValueScroller(MainActivity.MIN_FRAME_RATE, MainActivity.MAX_FRAME_RATE, MainActivity.FRAME_RATE_STEP,
                "frame_rate", MainActivity.DEFAULT_FRAME_RATE, R.id.frame_rate_container, R.drawable.frame_rate_100_darker);

        this.value_scroller_handler.createValueScroller(MainActivity.MIN_FRAME_COUNT, MainActivity.MAX_FRAME_COUNT, MainActivity.FRAME_COUNT_STEP,
                "frame_count", MainActivity.DEFAULT_FRAME_COUNT, R.id.frame_count_container, R.drawable.frame_count_100);

        camera_helper.resumeCamera();
    }

    private void handleFirstLaunchEvents() {
        SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(getSavedActivity());
        Boolean first_launch = shared_preferences.getBoolean("first_launch", true);
        if (first_launch) {
            shared_preferences.edit().putBoolean("first_launch", false).apply();
            this.displayHelpOverlay();
        }
    }
    /**
     * Launch the fragment where the Gif gets put together and edited
     */
    public void launchGifWorkspace() {
        if (! getSavedActivity().gif_space_launched) {
            getSavedActivity().gif_space_launched = true;

            LinkedHashMap<String, String> params = new LinkedHashMap<>();

            this.action = getSavedActivity().getAction();

            if (this.action == null || this.action.equals("") ) {
                this.action = "captured";
                getSavedActivity().setAction("captured" );
            }
            params.put("action", this.action);
            params.put("gif_chain_id", this.gif_chain_id);

            GifChain gif_chain = new GifChain(this);
            gif_chain.setName(FileManager.APP_FILE_PREFIX + "gif_chain_" + String.valueOf(ToolBox.getCurrentEpochMillis()));
            stash.store("gif_chain", gif_chain);

            openDetailsFragment( "gif_space", params, this.extra_param_list );
        }
    }

    /**
     * Configure actions on gear press
     */
    public void configureGears() {
        ImageButton gears = (ImageButton) fragment_view.findViewById(R.id.gears);
        gears.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (! help_overlay_open) {
                    displayHelpOverlay();
                } else {
                    closeHelpOverlay();
                }
            }
        });
    }

    /**
     * Displays a help overlay for the current fragment
     */
    private void displayHelpOverlay() {
        help_overlay_open = true;
        this.help_view = LayoutBuilder.addToLayout(R.layout.overlay_help_cam, R.id.image_container, fragment_view);

        ImageButton close_help = (ImageButton) help_view.findViewById(R.id.close_help);
        close_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeHelpOverlay();
            }
        });
    }

    /**
     * Closes the help overlay
     */
    private void closeHelpOverlay() {
        help_overlay_open = false;
        LayoutBuilder.removeFromLayout(help_view, R.id.image_container, fragment_view);
    }

    @Override
    public void onAnimationFinished(String animation_type) {}

    @Override
    public void onAnimationFinished(String animation_type, View animated_view) {}
}
