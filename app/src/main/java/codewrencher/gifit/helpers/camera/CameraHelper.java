package codewrencher.gifit.helpers.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.files.FileManager;
import codewrencher.gifit.ui.fragments.BaseFragment;
import codewrencher.gifit.ui.fragments.CameraFragment;

/**
 * Created by Gene on 9/25/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class CameraHelper {

    private static final String TAG = "Camera Helper";  /** This Class Tag. */

    private BaseFragment fragment;          /** Current activity fragment used to grab contexts */
    private Camera2Video camera_2_video;

    private CameraManager camera_manager;   /** Handles a bunch of Camera 2 actions */
    public Camera camera_1;                /** Old Style Deprecated Camera */
    public CameraDevice camera_2;          /** New style Camera, available in API level 21 + (LOLLIPOP) */
    public Object camera;                  /** General Camera object. Can be either Camera1 or Camera2 */
    private int camera_type;                /** 1 or 2 */

    private boolean front_camera_1;
    private CameraPreview mPreview;
    private boolean camera_1_ready = true;
    boolean is_recording = false;
    private MediaRecorder media_recorder;

    /**
     * Constructor
     * @param fragment
     */
    public CameraHelper(BaseFragment fragment) {
        this.fragment = fragment;
        this.camera_manager = (CameraManager) fragment.getSavedActivity().getSystemService(Context.CAMERA_SERVICE);
        media_recorder = new MediaRecorder();
        this.chooseCamera();
    }

    /**
     * Choose Camera 1 or 2
     */
    /**
     * Decides whether to get the Camera object from the old API or the new API
     */
    private void chooseCamera() {
        media_recorder = new MediaRecorder();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            this.camera_type = 1;
            this.openCamera1();
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.camera_2_video = new Camera2Video(fragment);
            this.camera_type = 2;
        }
    }
    /**
     * Initialize old Camera 1 instance
     * @return
     */
    private android.hardware.Camera openCamera1() {
        camera_1 = getCamera1Instance(-1);

        this.setCamera1PictureSize();
        if (camera_1 != null) {
            fragment.fragment_pager.camera = camera_1;
        } else {
            camera_1 = fragment.fragment_pager.camera;
        }
        camera_1.setDisplayOrientation(90);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(fragment.getContext(), camera_1, (CameraFragment) fragment);

        FrameLayout preview = (FrameLayout) fragment.getFragmentView().findViewById(R.id.camera_preview);
        if (preview.getChildCount() > 0) {
            preview.removeAllViews();
        }
        preview.addView(mPreview);

        return camera_1;
    }
    /**
     * Initialize new Camera 2 instance
     * @return
     */
    private CameraDevice openCamera2() {
        camera_2_video.openCamera2();
        return camera_2_video.mCameraDevice;
    }

    public void chooseCamera1() {
        //if the camera preview is the front
        if (front_camera_1) {
            int camera_id = findBackFacingCamera1();
            if (camera_id >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                camera_1 = Camera.open(camera_id);
                setCamera1PictureSize();
                mPreview.refreshCamera(camera_1);
            }
        } else {
            int camera_id = findFrontFacingCamera1();
            if (camera_id >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                camera_1 = Camera.open(camera_id);
                setCamera1PictureSize();
                //       mPicture = getPictureCallback();
                mPreview.refreshCamera(camera_1);
            }
        }
    }
    private void setCamera1PictureSize() {
        if (camera_1 != null) {
            Camera.Parameters params = camera_1.getParameters();
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            Camera.Size size = sizes.get(0);
            for (int i = 0; i < sizes.size(); i++) {
                if (sizes.get(i).width > size.width)
                    size = sizes.get(i);
            }
            params.setPictureSize(size.width, size.height);
            camera_1.setParameters(params);
        }
    }
    private int findFrontFacingCamera1() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                front_camera_1 = true;
                break;
            }
        }
        return cameraId;
    }
    private int findBackFacingCamera1() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                front_camera_1 = false;
                break;
            }
        }
        return cameraId;
    }
    public static Camera getCamera1Instance(int camera_id){
        Camera c = null;
        try {
            if (camera_id != -1) {
                c = Camera.open(camera_id);
            } else {
                c = Camera.open();
            }
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Flip Camera
     */
    public void flipCamera() {
        if (camera_type == 1) {
            flipCamera1();
        } else if (camera_type == 2) {
            flipCamera2();
        }
    }
    public void flipCamera1() {
        camera_1.release();
        chooseCamera1();
        camera_1.setDisplayOrientation(90);
    }
    public void flipCamera2() {
        camera_2_video.flipCamera();
    }

    /**
     * Recording
     */
    public void startRecording() {
        if (camera_type == 1) {
            startRecordingCamera1();
        } else if (camera_type == 2) {
            startRecordingCamera2();
        }
    }
    public void stopRecording() {
        if (camera_type == 1) {
            stopRecordingCamera1();
        } else if (camera_type == 2) {
            stopRecordingCamera2();
        }
    }

    private boolean setUpMediaRecorder1() {
        Camera.Parameters params = camera_1.getParameters();
        Camera.Size closest_size = this.getClosestSizeToScreen( params.getSupportedVideoSizes(), + 1 );
        // We take the next size down from the closest here ( offset + 1 ) because bigger values are
        // at the beginning. With the other UI elements on the screen, it will fit better

        camera_1.unlock();

        media_recorder.setCamera(camera_1);
        media_recorder.setOrientationHint(90);
        media_recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        media_recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        media_recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        media_recorder.setOutputFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + FileManager.APP_STORAGE_DIRECTORY + "/" + FileManager.TEMP_VIDEO_NAME);
        media_recorder.setVideoSize( closest_size.width, closest_size.height );
        media_recorder.setVideoFrameRate(32); //might be auto-determined due to lighting
        media_recorder.setVideoEncodingBitRate(3000000);
        media_recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);// MPEG_4_SP
        media_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        media_recorder.setMaxDuration(6000000); //set maximum duration 60 sec.
        media_recorder.setMaxFileSize(50000000); //set maximum file size 50M

        try {
            media_recorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder1();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder1();
            return false;
        }
        return true;
    }
    private void releaseMediaRecorder1() {
        media_recorder.stop();
        media_recorder.reset();     // clear recorder configuration
        camera_1.lock();            // lock camera for later use
    }
    /***********************************************************************************************
     * Find a valid preview size that is closest to the current screen size
     * @param sizes : a list of sizes you want to look through
     * @param offset : if we want the next one up from the closest : +1, then next one down : -1
     * @return : the size you found
     */
    private Camera.Size getClosestSizeToScreen( List<Camera.Size> sizes, int offset ) {
        Camera.Size closest_size = null;

        int min_diff = 1000;    // set a max value that will go down to aminimum

        for (int i = 0; i < sizes.size(); i++) {

            if (closest_size == null) {
                closest_size = sizes.get(i);
            }

            /** The camera view is referenced sideways, so width and height are flipped in reference to the display */
            int found_height = sizes.get(i).width;
            int found_width = sizes.get(i).height;

            int diff = Math.abs( found_width - fragment.getDisplayWidth() ) + Math.abs( found_height - fragment.getDisplayHeight() );

            if ( diff < min_diff ) {

                min_diff = diff;

                // set our closest size to the offset of the closest value
                // as long as its not out of bounds
                if ( (i + offset) >=0 && (i + offset) < sizes.size() ) {
                    closest_size = sizes.get( i + offset );
                }
            }
            Log.d("FOUND SIZE", String.valueOf( found_width ) + " x " + String.valueOf( found_height ));

        }
        return closest_size;
    }
    public void startRecordingCamera1() {

        if (! setUpMediaRecorder1()) {
            Toast.makeText(fragment.getContext(), "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
        } else {

            // work on UiThread for better performance
            fragment.getSavedActivity().runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        media_recorder.start();

//                        ((CameraFragment) fragment).takeNextShot();

                    } catch (final Exception ex) {
                        Log.d("RECORDER FAILURE", "Exception in thread");
                    }
                }
            });
        }
    }

    public void startRecordingCamera2() {
        camera_2_video.startRecordingVideo();
       // ((CameraFragment) fragment).takeNextShot();
    }
    public void stopRecordingCamera1() {
        releaseMediaRecorder1();
    }
    public void stopRecordingCamera2() {
        camera_2_video.stopRecordingVideo();
    }

    /**
     * Flash Mode
     */
    public void setCameraFlashMode(String flash_mode) {
        if (camera_type == 1) {
            setCamera1FlashMode(flash_mode);
        } else if (camera_type == 2) {
            setCamera2FlashMode(flash_mode);
        }
    }
    public void setCamera1FlashMode(String flash_mode) {
        Camera.Parameters params = camera_1.getParameters();
        params.setFlashMode(flash_mode);
        camera_1.setParameters(params);

        FlashHandler flash_handler = new FlashHandler( (CameraFragment) fragment, "camera_1", camera_1, camera_2_video, fragment.animator );
        flash_handler.handleFlash();
    }
    public void setCamera2FlashMode(String flash_mode) {
        FlashHandler flash_handler = new FlashHandler( (CameraFragment) fragment, "camera_2", camera_1, camera_2_video, fragment.animator );
        flash_handler.handleFlash();
    }

    public void createTextureView() {
        if (camera_type == 2) {
            camera_2_video.createTextureView();
        }
    }
    public void pauseCamera() {
        if (camera_type == 1) {
            pauseCamera1();
        } else if (camera_type == 2) {
            pauseCamera2();
        }
    }
    public void pauseCamera1() {
        if (camera_1 != null) {
            camera_1.release();
            camera_1 = null;
        }
    }
    public void pauseCamera2() {
        camera_2_video.closeCamera2();
    }
    public void resumeCamera() {
        Log.d(TAG,"resumeCamera");
        if (camera_type == 1) {
            resumeCamera1();
        } else if (camera_type == 2) {
            resumeCamera2();
        }
    }
    public void resumeCamera1() {

        if (camera_1 == null) {
            //if the front facing camera does not exist
            if (findFrontFacingCamera1() == -1) {
                //release the old camera instance
                //switch camera, from the front and the back and vice versa
                if (camera_1 != null) {
                    camera_1.release();
                    camera_1 = null;
                }
                chooseCamera1();
            }
        }
        SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getActivity());
        if (camera_1 != null) {
            Camera.Parameters params = camera_1.getParameters();
            params.setFlashMode(shared_preferences.getString("flash_mode", Camera.Parameters.FLASH_MODE_AUTO));
            camera_1.setParameters(params);
        }
    }
    public void resumeCamera2() {
        camera_2_video.openCamera2();
    }
    /**
     * Get back facing camera id
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public String getBackFacingCameraId() {
        try {
            for (final String camera_id : camera_manager.getCameraIdList()) {
                CameraCharacteristics characteristics = camera_manager.getCameraCharacteristics(camera_id);

                int orientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (orientation == CameraCharacteristics.LENS_FACING_BACK) {
                    return camera_id;
                }
            }
        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Get front facing camera id
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public String getFrontFacingCameraId() {
        try {
            for (final String camera_id : camera_manager.getCameraIdList()) {
                CameraCharacteristics characteristics = camera_manager.getCameraCharacteristics(camera_id);

                int orientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (orientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    return camera_id;
                }
            }
        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
