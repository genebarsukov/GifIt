package codewrencher.gifit.helpers.camera;

import android.hardware.Camera;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.Animator;
import codewrencher.gifit.ui.fragments.CameraFragment;

/**
 * Created by Gene on 5/14/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 *
 * Handles the UI menu's flash buttons. Sets listeners, operates the flash drawer, and swaps icons
 */
public class FlashHandler {

    private static String TAG = "FlashHandler";

    private CameraFragment camera_fragment;
    private String camera_type;
    private Camera camera;
    private Camera2Video camera_2_video;
    private Animator animator;
    private LinearLayout flash_drawer;

    /**
     * Constructor
     * We pass along both a camera 1 instance and a camera 2 controller instance into the
     * constructor irregardless of which camera type we are actually going to use
     * This makes thing simpler and we can just choose which one to use based on the camera_type
     * It does not hurt to have a null instance if it is not used
     * @param camera_fragment Parent fragment
     * @param camera_type Either "camera_1" or "camera_2"
     * @param camera Camera instance
     * @param camera_2_video Camera 2 video controller instance
     * @param animator Animates the flash drawer
     */
    public FlashHandler(CameraFragment camera_fragment, String camera_type, Camera camera, Camera2Video camera_2_video, Animator animator ) {
        this.camera_fragment = camera_fragment;
        this.camera_type = camera_type;
        this.camera = camera;
        this.camera_2_video = camera_2_video;
        this.animator = animator;
    }

    /**
     * Sets up all the listeners for the flash actions
     */
    public void handleFlash() {
        this.setFlashDrawerAnimations();
        this.setFlashActions();
    }

    /**
     * Animates the flash drawer
     * When you click on the flash icon, it decides whether to slide the drawer in or out
     */
    private void setFlashDrawerAnimations() {
        ImageButton current_flash = (ImageButton) camera_fragment.getFragmentView().findViewById(R.id.current_flash);
        this.flash_drawer = (LinearLayout) camera_fragment.getFragmentView().findViewById(R.id.flash_drawer);

        current_flash.setImageResource( camera_fragment.shared_preferences.getInt("flash_button_image_resource", R.drawable.button_flash_auto) );

        current_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!flash_drawer.isShown()) {

                    animator.registerListener( camera_fragment );
                    animator.setAnimation( flash_drawer, "expand_horizontal" );
                    animator.animate( flash_drawer );
                    animator.expand( "horizontal", flash_drawer, 1000 );
                } else if (flash_drawer.isShown()) {

                    animator.registerListener( camera_fragment );
                    animator.setAnimation( flash_drawer, "collapse_horizontal" );
                    animator.animate( flash_drawer );
                    animator.collapse( "horizontal", flash_drawer, 1000 );
                }
            }
        });
    }

    /**
     * Set a flash mode listener for each button in the drawer, passing along the important params
     * to setFlashListener() which actually decides what to do, sets the listener, sets the correct
     * flash icon image on the main button, sets the flash mode, saves the current flash mode in
     * shared preferences
     */
    private void setFlashActions() {
        ImageButton flash_on = (ImageButton) camera_fragment.getFragmentView().findViewById(R.id.flash_on);
        ImageButton flash_off = (ImageButton) camera_fragment.getFragmentView().findViewById(R.id.flash_off);
        ImageButton flash_auto = (ImageButton) camera_fragment.getFragmentView().findViewById(R.id.flash_auto);
        ImageButton flash_red_eye = (ImageButton) camera_fragment.getFragmentView().findViewById(R.id.flash_red_eye);
        ImageButton flashlight = (ImageButton) camera_fragment.getFragmentView().findViewById(R.id.flashlight);

        this.setFlashListener(flash_on, R.drawable.button_flash_on, Camera.Parameters.FLASH_MODE_ON);
        this.setFlashListener(flash_off, R.drawable.button_flash_off, Camera.Parameters.FLASH_MODE_OFF);
        this.setFlashListener(flash_auto, R.drawable.button_flash_auto, Camera.Parameters.FLASH_MODE_AUTO);
        this.setFlashListener(flash_red_eye, R.drawable.button_flash_red_eye, Camera.Parameters.FLASH_MODE_RED_EYE);
        this.setFlashListener(flashlight, R.drawable.button_flashlight, Camera.Parameters.FLASH_MODE_TORCH);
    }

    /**
     * The workhorse of this class. Its performs all of the needed actions described above for
     * each flash mode.
     * It also decides which action to perform based on the camera_type param, whether to set the
     * flash mode for camera 1 or camera 2
     * @param button The button representing the flash mode in the UI
     * @param button_image_resource The drawable image resource id for this button
     * @param flash_mode The flash mode that this button triggers
     */
    private void setFlashListener(ImageButton button, final int button_image_resource, final String flash_mode) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View flash_button) {
                ImageButton current_flash = (ImageButton) camera_fragment.getFragmentView().findViewById(R.id.current_flash);
                current_flash.setImageResource(button_image_resource);

                // the following 3 lines are the only ones that are specific to Camera 1
                if (camera_type == "camera_1") {
                    Camera.Parameters params = camera.getParameters();
                    params.setFlashMode(flash_mode);
                    camera.setParameters(params);
                } // the following handles flash mode selection for Camera 2
                else if (camera_type.equals("camera_2")) {
                    // here we interpret camera 1 strings and use them to tell camera 2 what to do
                    switch (flash_mode) {
                        case Camera.Parameters.FLASH_MODE_ON:
                            camera_2_video.setFlash("on");
                            Log.d(TAG, "Setting Flash to ON >>>");
                            break;
                        case Camera.Parameters.FLASH_MODE_OFF:
                            camera_2_video.setFlash("off");
                            Log.d(TAG, "Setting Flash to OFF >>>");
                            break;
                        case Camera.Parameters.FLASH_MODE_AUTO:
                            camera_2_video.setFlash("auto");
                            Log.d(TAG, "Setting Flash to AUTO >>>");
                            break;
                        case Camera.Parameters.FLASH_MODE_RED_EYE:
                            camera_2_video.setFlash("redeye");
                            Log.d(TAG, "Setting Flash to REDEYE >>>");
                            break;
                        case Camera.Parameters.FLASH_MODE_TORCH:
                            camera_2_video.setFlash("torch");
                            Log.d(TAG, "Setting Flash to TORCH >>>");
                            break;
                        default:
                            break;
                    }
                }

                camera_fragment.shared_preferences.edit().putString("flash_mode", flash_mode).apply();
                camera_fragment.shared_preferences.edit().putInt("flash_button_image_resource", button_image_resource).apply();

                animator.registerListener( camera_fragment );
                animator.setAnimation( flash_drawer, "collapse_horizontal" );
                animator.animate( flash_drawer );
                animator.collapse("horizontal", flash_drawer, 1000);

            }
        });
    }
}
