package codewrencher.gifit.helpers.camera;

import android.content.SharedPreferences;
import android.media.MediaActionSound;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import codewrencher.gifit.R;
import codewrencher.gifit.tools.ToolBox;
import codewrencher.gifit.ui.fragments.BaseFragment;
import codewrencher.gifit.ui.fragments.CameraFragment;

/**
 * Created by Gene on 10/30/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 *
 * Directs most aspects of the film
 * Figures out how long it should be
 * Tells the media recorder when to start and to stop
 * Controls the shutter button
 *
 * All times are in milliseconds
 */
public class VideoDirector {

    private static String tag = "VideoDirector";

    private static int SHUTTER_CLICK_WAIT_TIME = 1500;
    BaseFragment fragment;
    ValueScrollerHandler value_scroller_handler;
    CameraHelper camera_helper;

    long video_start_millis;
    long video_end_millis;
    Boolean is_recording;
    Handler filming_timer_handler;
    Runnable filming_timer_runnable;
    Runnable recording_progress_1;
    Runnable recording_progress_2;
    Runnable recording_progress_3;
    Runnable recording_progress_4;
    Runnable recording_progress_5;
    Runnable recording_progress_6;
    Runnable recording_progress_7;
    Runnable recording_progress_8;
    Runnable recording_progress_9;

    /** Setting up shutter clicks on every picture */
    private MediaActionSound shutter_click;
    private int frames_left;
    private int frame_interval;
    /**
     * Constructor
     * @param fragment: Parent fragment
     * @param value_scroller_handler: Scroller handler used to get values for video calculations
     * @param camera_helper: Controls the cameras - We tell it when to start/stop
     */
    public VideoDirector(BaseFragment fragment, ValueScrollerHandler value_scroller_handler, CameraHelper camera_helper) {
        this.fragment = fragment;
        this.value_scroller_handler = value_scroller_handler;
        this.camera_helper = camera_helper;
        this.is_recording = false;
        this.shutter_click = new MediaActionSound();
        this.shutter_click.load(MediaActionSound.SHUTTER_CLICK);
        this.frames_left = 0;
        this.frame_interval = 0;

        this.setShutterSoundSwitchListener();
    }

    /**
     * Start the filming process
     * Direct the MediaRecorder and other entities
     */
    public void startFilming() {
        ImageButton shutter = (ImageButton) fragment.getFragmentView().findViewById(R.id.shutter);
        shutter.setImageResource(R.drawable.shutter_3_pressed_shadow);

        this.video_start_millis = ToolBox.getCurrentEpochMillis();
        /*
         The addition of half a second to the video length is crucial here because at higher
         frame rates and lower frame counts it seems the video may be too short for us to pick
         all of the correct frames out, resulting in garbled frames or crashes.

          The addition of the extra time here solves this problem
          */

        int recording_duration = (int) calcTotalVideoDuration() + 750;
        // start recording
        camera_helper.startRecording();
        this.is_recording = true;

        // post a delayed thread as a timer of when to end recording


        filming_timer_handler = new Handler();
        filming_timer_runnable = new Runnable() {
            @Override
            public void run() {
                // STOP
                endFilming(false);
            }
        };
        // delayed endFilming() callback
        filming_timer_handler.postDelayed(filming_timer_runnable, recording_duration);
        // delayed recording progress callbacks
        handleRecordingProgressButton(recording_duration);

        // operate shutter_sounds
        if (this.is_recording) {
            setUpShutterSound();
        }
    }

    /**
     * End the filming process
     * Stop the MediaRecorder
     * We need to make sure everything is stopped successfully before launching GifSpace
     * Maybe try a few delayed try-catches here before launching the GifSpace fragment
     * We also need to make sure the frame count, and frame interval are passed along to the
     * GifChain and Gif maker when getting frames from the video and making the Gif
     * We need to make sure these parameters are not just gotten from shared preferences
     * @param interruption Whether or not the end filming command is natural or an interruption
     */
    public void endFilming(Boolean interruption) {
        ImageButton shutter = (ImageButton) fragment.getFragmentView().findViewById(R.id.shutter);
        shutter.setImageResource(R.drawable.shutter_3_shadow);
        //  shutter.setImageState(new int[] {}, true);

        this.video_end_millis = ToolBox.getCurrentEpochMillis();
        camera_helper.stopRecording();
        // remove shutter progress updating callbacks
        filming_timer_handler.removeCallbacks(recording_progress_1);
        filming_timer_handler.removeCallbacks(recording_progress_2);
        filming_timer_handler.removeCallbacks(recording_progress_3);
        filming_timer_handler.removeCallbacks(recording_progress_4);
        filming_timer_handler.removeCallbacks(recording_progress_5);
        filming_timer_handler.removeCallbacks(recording_progress_6);
        filming_timer_handler.removeCallbacks(recording_progress_7);
        filming_timer_handler.removeCallbacks(recording_progress_8);
        filming_timer_handler.removeCallbacks(recording_progress_9);

        if (! interruption) {
            ((CameraFragment) fragment).launchGifWorkspace();
        } else {
            // remove the natural endFilming() callback when we are interrupted
            filming_timer_handler.removeCallbacks(filming_timer_runnable);
        }
        is_recording = false;
        //tryToStopRecording();
    }

    /**
     * Animates the shutter button to show recording progress
     * Posts 4 runnables spaced equally along the length of the gif capture session
     * These runnables each update the shutter progress image to show incremental progress
     * These delayed runnable callbacks need to be removed when filming ends either naturally or is
     * interrupted
     */
    private void handleRecordingProgressButton(int recording_duration) {
        int fractional_time = recording_duration / 8;

        recording_progress_1 = new Runnable() {
            @Override
            public void run() {
                ImageButton shutter = (ImageButton) fragment.getFragmentView().findViewById(R.id.shutter);
                shutter.setImageResource(R.drawable.shutter_recording_progress_1_2);
            }
        };
        recording_progress_2 = new Runnable() {
            @Override
            public void run() {
                ImageButton shutter = (ImageButton) fragment.getFragmentView().findViewById(R.id.shutter);
                shutter.setImageResource(R.drawable.shutter_recording_progress_2_2);
            }
        };
        recording_progress_3 = new Runnable() {
            @Override
            public void run() {
                ImageButton shutter = (ImageButton) fragment.getFragmentView().findViewById(R.id.shutter);
                shutter.setImageResource(R.drawable.shutter_recording_progress_3_2);
            }
        };
        recording_progress_4 = new Runnable() {
            @Override
            public void run() {
                ImageButton shutter = (ImageButton) fragment.getFragmentView().findViewById(R.id.shutter);
                shutter.setImageResource(R.drawable.shutter_recording_progress_4_2);
            }
        };
        recording_progress_5 = new Runnable() {
            @Override
            public void run() {
                ImageButton shutter = (ImageButton) fragment.getFragmentView().findViewById(R.id.shutter);
                shutter.setImageResource(R.drawable.shutter_recording_progress_5_2);
            }
        };
        recording_progress_6 = new Runnable() {
            @Override
            public void run() {
                ImageButton shutter = (ImageButton) fragment.getFragmentView().findViewById(R.id.shutter);
                shutter.setImageResource(R.drawable.shutter_recording_progress_6_2);
            }
        };
        recording_progress_7 = new Runnable() {
            @Override
            public void run() {
                ImageButton shutter = (ImageButton) fragment.getFragmentView().findViewById(R.id.shutter);
                shutter.setImageResource(R.drawable.shutter_recording_progress_7_2);
            }
        };
        recording_progress_8 = new Runnable() {
            @Override
            public void run() {
                ImageButton shutter = (ImageButton) fragment.getFragmentView().findViewById(R.id.shutter);
                shutter.setImageResource(R.drawable.shutter_recording_progress_8_2);
            }
        };
        recording_progress_9 = new Runnable() {
            @Override
            public void run() {
                ImageButton shutter = (ImageButton) fragment.getFragmentView().findViewById(R.id.shutter);
                shutter.setVisibility(View.GONE);
            }
        };

        filming_timer_handler.postDelayed(recording_progress_1, fractional_time);
        filming_timer_handler.postDelayed(recording_progress_2, fractional_time * 2);
        filming_timer_handler.postDelayed(recording_progress_3, fractional_time * 3);
        filming_timer_handler.postDelayed(recording_progress_4, fractional_time * 4);
        filming_timer_handler.postDelayed(recording_progress_5, fractional_time * 5);
        filming_timer_handler.postDelayed(recording_progress_6, fractional_time * 6);
        filming_timer_handler.postDelayed(recording_progress_7, fractional_time * 7);
        filming_timer_handler.postDelayed(recording_progress_8, (int) ((double) fractional_time * 7.7));
     //   filming_timer_handler.postDelayed(recording_progress_9, (int) ((double) fractional_time * 8.5));

    }

    /**
     * If stopping the MediaRecordeer does not work the first time, retry a few more times
     */
    private void tryToStopRecording() {

        Runnable  try_to = new Runnable() {
            @Override
            public void run() {
                camera_helper.stopRecording();  // try to achieve this
            }
        };
        Runnable  on_success = new Runnable() {
            @Override
            public void run() {
                is_recording = false;           // follow up on success
                value_scroller_handler.setFrameCount(calculateFramesToTake());
                ((CameraFragment) fragment).launchGifWorkspace();
            }
        };
        Runnable  on_failure = new Runnable() {
            @Override
            public void run() {                 // do this on failure
                Log.d(tag, "Can't stop recording. Please try again or reload");
                fragment.torch.torch("Can't stop recording. Please try again or reload");
            }
        };
        // launch the re-try method
        ToolBox.reTry(try_to, on_success, on_failure, 5, 200);
    }

    /**
     * Set a custom onClick listener on the camera shutter button
     * We need it to be un-clickable for at least a few milliseconds after filming starting to avoid
     * problems with files that are too small, etc.
     * We also need it to cancel our delayed timed if it interrupts filming
     */
    public void operateCameraShutterButton() {
        final ImageButton shutter = (ImageButton) fragment.getFragmentView().findViewById(R.id.shutter);

        shutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fragment.getSavedActivity().torch.dismissStatusWindow();
                fragment.getSavedActivity().gif_space_launched = false;

                if (is_recording) {
                    // INTERRUPT
                    if ((ToolBox.getCurrentEpochMillis() - video_start_millis) > SHUTTER_CLICK_WAIT_TIME) {
                        endFilming(true);
                    }
                } else { // remove saved gif chain and previously saved frames when recording starts
                    // START
                    startFilming();
                }
            }
        });
    }
    /**
     * Calculate the total video duration from frame rate and frame count.
     * Add on 100 ms for good measure
     * @return: Video duration in milliseconds
     */
    private double calcTotalVideoDuration() {

        double frame_rate = this.value_scroller_handler.getFrameRate();
        double frame_count = this.value_scroller_handler.getFrameCount();

        double video_duration = (frame_count / frame_rate) * 1000;

        return video_duration;
    }

    /**
     * Calculate frames to take from the video.
     * Usually this will be equal to the frame_count, but in cases where the video was interrupted,
     * this will be equal to the actual video length * fps
     * @return The number of frames to extract from the film
     */
    private int calculateFramesToTake() {

        double total_vid_duration = calcTotalVideoDuration();
        double actual_video_duration = video_end_millis - video_start_millis;

        // if the video didn't really run
        if (actual_video_duration == 0) {
            return 0;
        } // if the video overran its given time limit for some reason
        else if ((actual_video_duration - total_vid_duration) > 100) {
            return (int) total_vid_duration;
        } // if the video was cut short
        else if ((total_vid_duration - actual_video_duration) > 100) {
            return (int) (actual_video_duration * (this.value_scroller_handler.getFrameRate() / 1000));
        } else {
            return (int) this.value_scroller_handler.getFrameCount();
        }
    }

    /**
     * The time interval between frames in milliseconds
     * We take our calculates frames to take and divide them by the frame rate per millisecond in
     * order ro get the time interval beteeen each frame in milliseconds
     * @return The time distance between our gif frames in milliseconds
     */
    private int calculateFrameInterval() {
        return (int) (1000 / this.value_scroller_handler.getFrameRate());
    }

    /**
     * Set shutter sound switch listener - turns shutter sounds on and off
     */
    private void setShutterSoundSwitchListener() {
        final SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getSavedActivity());
        boolean play_shutter_sound_initial = shared_preferences.getBoolean("play_shutter_sound", false);

        final ImageButton button_shutter_sound = (ImageButton) fragment.getFragmentView().findViewById(R.id.shutter_sound);

        if (play_shutter_sound_initial) {
            button_shutter_sound.setImageResource(R.mipmap.shutter_sound_on);
            fragment.unMuteRinger();
        } else {
            button_shutter_sound.setImageResource(R.mipmap.shutter_sound_off);
            fragment.muteRinger();
        }

        button_shutter_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean play_shutter_sound = shared_preferences.getBoolean("play_shutter_sound", false);

                if (play_shutter_sound) {
                    button_shutter_sound.setImageResource(R.drawable.button_shutter_sound_off);
                    shared_preferences.edit().putBoolean("play_shutter_sound", false).apply();
                    fragment.muteRinger();
                } else {
                    button_shutter_sound.setImageResource(R.drawable.button_shutter_sound_on);
                    shared_preferences.edit().putBoolean("play_shutter_sound", true).apply();
                    fragment.unMuteRinger();
                }
            }
        });
    }

    /**
     * Set up Camera shutter sounds
     * Play a camera shutter sounds at intervals equal to when each picture is taken
     */
    private void setUpShutterSound() {
        SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getSavedActivity());
        boolean play_shutter_sound = shared_preferences.getBoolean("play_shutter_sound", false);
        Log.d("DIR", String.valueOf(play_shutter_sound));
        if (play_shutter_sound) {
            frames_left = (int) this.value_scroller_handler.getFrameCount();
            Log.d("DIR frames_left", String.valueOf(frames_left));
            // initiate recursive shutter clicks
            playShutterSound();

            // start first frame interval at 0, then calculate the rest
            frame_interval = calculateFrameInterval();
            Log.d("DIR frame_interval", String.valueOf(frame_interval));

        }
    }

    /**
     * Recursive method that plays camera actions sounds until we eun out of frames left to tale or
     * recording is off
     */
    private void playShutterSound() {

        Handler shutter_timer_handler = new Handler();
        Runnable shutter_timer_runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("DIR", "PLAYING SOUND");
                frames_left --;
                Log.d("FRAMES_LEFT", String.valueOf(frames_left));
                shutter_click.play(MediaActionSound.SHUTTER_CLICK);
                playShutterSound();
            }
        };
        if (frames_left > 0 && is_recording) {
            Log.d("FRAMES INTERVAL", String.valueOf(frame_interval));
            shutter_timer_handler.postDelayed(shutter_timer_runnable, frame_interval);
        }
    }

}
