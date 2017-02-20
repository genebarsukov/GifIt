package codewrencher.gifit.helpers.gif;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;

import codewrencher.gifit.tools.ToolBox;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 11/24/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 *
 * Makes sure that frame and Gif loading takes a reasonable amount of time
 * Uses current frame count to figure out how long each frame loading and gif creation should take
 * in order to pass under the total threshold
 *
 * Dials back Gif quality and scales back frames if it is determined that loading will take too long
 *
 * Times and displays frame loading and gif loading times
 */
public class GifTimer {

    private static final String TAG = "GifTimer";
    public static final int RENDERING_THRESHOLD = 6000;
    public static final double TARGET_FRAME_RATE = 1.0;
    public static double CHOSEN_FRAME_RATE = 1.0;
    public static double MAX_FRAME_RATE = 1.4;
    public static final int DEFAULT_GIF_QUALITY = 10;

    BaseFragment fragment;
    private int frame_count;
    private int initial_frame_width;
    private int initial_frame_height
            ;
    private long frames_start;
    private long frames_end;
    private long gif_start;
    private long gif_end;

    /** Reporting Params **/
    private int gif_quality;
    private int gif_width;
    private int gif_height;
    private int frame_width;
    private int frame_height;

    private int gif_surface_area;
    private int frame_surface_area;

    /** Desired Params **/
    private int desired_gif_quality;
    private int desired_surface_area;
    private float desired_frame_scaler;

    /**
     * Constructor
     * @param fragment Current fragment
     */
    public GifTimer(BaseFragment fragment, int frame_count) {
        this.fragment = fragment;
        this.frame_count = frame_count;

        this.frames_start = 0;
        this.frames_end = 0;
        this.gif_start = 0;
        this.gif_end = 0;

        this.gif_quality = 0;
        this.gif_width = 0;
        this.gif_height = 0;
        this.frame_width = 0;
        this.frame_height = 0;

        this.gif_surface_area = 0;
        this.frame_surface_area = 0;

        this.desired_gif_quality = 0;
        this.desired_surface_area = 0;
        this.desired_frame_scaler = 0;

        CHOSEN_FRAME_RATE = TARGET_FRAME_RATE;
        if (frame_count > 10) {
            CHOSEN_FRAME_RATE = (double) frame_count / 10;
            if (CHOSEN_FRAME_RATE > MAX_FRAME_RATE) {
                CHOSEN_FRAME_RATE = MAX_FRAME_RATE;
            }
        }
    }

    /**
     * Set all of the important image and gif params that gif speed depends on
     */
    public void setIndependentParams(int gif_quality, int gif_width, int gif_height, int frame_width, int frame_height) {
        this.gif_quality = gif_quality;
        this.gif_width = gif_width;
        this.gif_height = gif_height;
        this.frame_width = frame_width;
        this.frame_height = frame_height;

        this.gif_surface_area = gif_width * gif_height;
        this.frame_surface_area = frame_width * frame_height;
    }

    /**
     * Get the gif quality to set when creating the gif - compute if not already calculated
     * @return Gif Quality
     */
    public int getDesiredGifQuality() {
        if (this.desired_gif_quality == 0) {
            computeDesirableGifParams();
        }
        return this.desired_gif_quality;
    }
    /**
     * Get the image scaler to scale the frames to - compute if not already calculated
     * @return Frame Scaler
     */
    public float getDesiredFrameScaler() {
        if (this.desired_frame_scaler == 0) {
            computeDesirableGifParams();
        }
        return this.desired_frame_scaler;
    }

    /**
     * Computes the desired params to make the gif render within the desired time frame
     *      - Gif quality
     *      - Frame Scaler
     * 1. Calculate current image area and quality
     * 2. Calculate what kind of frame rate we can expect with such parameters
     *      - If the calculated frame rate is > 1
     *      - if its > 1 but < 2, set scaler to 0.75 and quality to 20
     *      - if its > 2, calculate the area we need to set bring it down
     *      - phones have 4:3. 3:2, and 16:10 aspect ratios
     *      - calculate current phone's aspect ratio
     *      - for 4:3 we then have 4x * 3x = calculated_area, x = sqrt(calculated_area / 12)
     *          - then we solve for x.
     *          - find our target width and height
     *          - calculate the scaler we need
     *          - That's it
     * 4:3 = 1.333
     * 3:2 = 1.5
     * 16:10 = 1.6
     *
     * 300,000 & 10 ~ 3 seconds per frame
     * 300,000 & 2  0 ~ 2 seconds per frame
     */
    public void computeDesirableGifParams() {

        Log.d(TAG, "-----------------------------------------------------------------------");
        Log.d(TAG, String.valueOf(CHOSEN_FRAME_RATE));
        // set some default initial values for the 'desired' cariables
        desired_gif_quality = DEFAULT_GIF_QUALITY;
        desired_frame_scaler = 1.0f;

        if (initial_frame_width == 0 || initial_frame_height == 0) {
            initial_frame_width = fragment.damen.getVideoFrameWidth();
            initial_frame_height = fragment.damen.getVideoFrameHeight();
        }
        // calculate expected frame rate using surface area vs frame rate trend line equation
        // y = 0.0000078x + 0.6,  x = surface area, y = frame rate
        int initial_frame_surface_area = initial_frame_width * initial_frame_height;
        double expected_frame_rate = .0078 * initial_frame_surface_area + 0.61;

        Log.d(TAG, "initial_frame_surface_area " + String.valueOf(initial_frame_surface_area));
        Log.d(TAG, "expected_frame_rate " + String.valueOf(expected_frame_rate));

        // the expected frame rate is within the accepted range
        if (expected_frame_rate < CHOSEN_FRAME_RATE) {
            // @TODO: Nothing
        } // frame rate is almost good and needs only some minor tweaks
        else if (expected_frame_rate > 1 && expected_frame_rate < 2) {
            desired_gif_quality = 20;
            desired_frame_scaler = 0.75f;
        } // calculate the surface area, frame scaler, and gif quality to attain the target frame rate
        else {
            double target_surface_area = ((1 / CHOSEN_FRAME_RATE) -0.6) / 0.0000078;

            Log.d(TAG, "target_surface_area " + String.valueOf(target_surface_area));

            int[] aspect_ratio = calcFrameAspectRatio();
            // for a 4:3 aspect ratio, the equation is 4x * 3x = surface area
            double x = Math.sqrt(target_surface_area / (aspect_ratio[0] * aspect_ratio[1]));

            double target_width = aspect_ratio[1] * x;

            Log.d(TAG + "target_width ", String.valueOf(target_width));

            desired_frame_scaler = (float) (target_width / initial_frame_width);

            // here we trade some gif quality in return for a boost to the scaler - so we don't have
            // to scale as much. Scaling hurts picture quality more than lowering the gif quality
            // the operations should offset each other and leave the frame rate as is
            desired_gif_quality = 30;
            desired_frame_scaler += 0.25;

            // this is just for testing
            // @TODO: Remove this
            this.desired_surface_area = (int) target_surface_area;
        }
    }

    /**
     * Calculate the aspect ratio of the frame image we took
     * This will be used to calculate the image scaler
     * @return and array of 2 integers signifying the aspect ratioo
     */
    private int[] calcFrameAspectRatio() {
        int[] aspect_ratio = {4, 3};

        LinkedHashMap<String, Double> aspect_ratios = new LinkedHashMap();
        aspect_ratios.put("4:3", 1.33);
        aspect_ratios.put("3:2", 1.5);
        aspect_ratios.put("16:10", 1.6);

        double aspect_fraction = initial_frame_height / initial_frame_width;
        if (initial_frame_height < initial_frame_width) {
            aspect_fraction = initial_frame_width / initial_frame_height;
        }
        double closest_distance = 1000;
        String closest_aspect_ratio = "";

        for(String aspect_key: aspect_ratios.keySet()) {
            double diff = Math.abs(aspect_ratios.get(aspect_key) - aspect_fraction);

            if (diff < closest_distance) {
                closest_distance = diff;
                closest_aspect_ratio = aspect_key;
            }
        }

        Log.d(TAG, "closest_aspect_ratio " + closest_aspect_ratio);

        String[] closest_aspect_ratio_split = closest_aspect_ratio.split(":");

        aspect_ratio[0] = Integer.parseInt(closest_aspect_ratio_split[0]);
        aspect_ratio[1] = Integer.parseInt(closest_aspect_ratio_split[1]);

        return aspect_ratio;
    }
    /**
     * Display some diagnostics:
     *      - How long total frame rendering took
     *      - How long Gif creation took
     *      - How long each frame rendering took on average
     * Display these stats in a dialog
     */
    public void showStats() {
        String message = "";
        LinkedHashMap<String, Integer> stats = this.calcStats();

        DecimalFormat double_format = new DecimalFormat("#.#");

        for(String key: stats.keySet()) {
            String stat_value = String.valueOf(stats.get(key));

            if (key.contains("line_")) {
                message += "\n";
                continue;
            } else if (key.contains("s_")) {
                stat_value = String.valueOf(double_format.format((double) stats.get(key) / 1000));
                message += key.replace("s_", "").replace('_', ' ') + " : " + stat_value + "\n";
            } else {
                message += key.replace('_', ' ') + " : " + stat_value + "\n";
            }
        }
    //    displayStats(message);
    }

    /**
     * Calculate the diagnostic rendering stats and store them in a hash map
     * @return LinkedHashMap if the stat types and their values
     */
    public LinkedHashMap<String,Integer> calcStats() {
        LinkedHashMap<String,Integer> stats = new LinkedHashMap<>();

        int total_frame_time = (int) (frames_end - frames_start);
        int total_gif_time = (int) (gif_end - gif_start);
        int total_time = total_frame_time + total_gif_time;

        stats.put("gif_quality", gif_quality);
        stats.put("gif_width", gif_width);
        stats.put("gif_height", gif_height);
        stats.put("frame_width", frame_width);
        stats.put("frame_height", frame_height);

        stats.put("line_1", 0);

        stats.put("gif_surface_area", gif_surface_area);
        stats.put("frame_surface_area", frame_surface_area);

        stats.put("line_2", 0);

        stats.put("s_total_frame_time", total_frame_time);
        stats.put("s_total_gif_time", total_gif_time);
        stats.put("s_total_time", total_time);
        stats.put("s_avg_frame_time", (int) ((double) total_frame_time / (double) frame_count));
        stats.put("s_gif_per_frame_time", (int) ((double) total_gif_time / (double) frame_count));
        stats.put("s_time_per_frame", (int) ((double) total_time / (double) frame_count));

        stats.put("line_3", 0);

        stats.put("desired_gif_quality", desired_gif_quality);
        stats.put("desired_surface_area", desired_surface_area);
        stats.put("desired_frame_scaler_x_10", (int) (desired_frame_scaler * 10));

        return stats;
    }

    /**
     * Display a simple dismiss-able dialog dialog with the rendering stats
     * @param stats Stat  message string to display
     */
    public void displayStats(String stats) {
        new AlertDialog.Builder(fragment.getActivity())
                .setTitle("Gif Timer")
                .setMessage(stats)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create().show();
    }

    /**
     * Log the time when we first start rendering frames
     */
    public void startRenderingFrames() {
        this.frames_start = ToolBox.getCurrentEpochMillis();
    }
    /**
     * Log the time when we stop rendering frames
     */
    public void stopRenderingFrames() {
        this.frames_end = ToolBox.getCurrentEpochMillis();
    }
    /**
     * Log the time when we start creating the gif
     */
    public void startCreatingGif() {
        this.gif_start = ToolBox.getCurrentEpochMillis();
    }
    /**
     * Log the time when we stop creating the gif
     */
    public void stopCreatingGif() {
        this.gif_end = ToolBox.getCurrentEpochMillis();
    }

}
