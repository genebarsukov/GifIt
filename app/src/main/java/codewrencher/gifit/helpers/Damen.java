package codewrencher.gifit.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.widget.FrameLayout;

import codewrencher.gifit.R;
import codewrencher.gifit.ui.fragments.BaseFragment;

import static java.lang.Math.min;

/**
 * Created by Gene on 11/14/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 *
 * Dimensions all important UI component
 *  1. Video Frames
 *  2. Gif
 *  3. Frame Thumbnails
 *  4. Layout windows
 *  5. Sprites
 *
 *  Used to maintain a central point for all sizing ops
 *  Knows what sizes all entities are and what sizes they should be as well as
 *  What need to be done to scale something or make it the proper size
 */

public class Damen {

    private static final String TAG = "Damen";

    private BaseFragment fragment;

    private int video_frame_thumbnail_scaler;
    private int ad_height;
    private int max_video_frame_width;

    /** Display and image dimensions **/
    private int display_width;
    private int display_height;

    private int usable_area_width;
    private int usable_area_height;

    private int video_frame_width;
    private int video_frame_height;

    private int rotated_video_frame_width;
    private int rotated_video_frame_height;

    private int recreated_video_frame_width;
    private int recreated_video_frame_height;

    private int thumbnail_width;
    private int thumbnail_height;

    private int adjusted_video_frame_width;
    private int adjusted_video_frame_height;

    /**
     * Constructor
     * @param fragment parent fragment which we use to access views anc activity stuff
     */
    public Damen(BaseFragment fragment) {
        this.fragment = fragment;
        this.loadStaticDims();

        this.display_width = fragment.getDisplayWidth();
        this.display_height = fragment.getDisplayHeight();
        this.usable_area_width = display_width;
        this.usable_area_height = display_height - ad_height;
        this.thumbnail_width = (int) ((double) display_width / (double) video_frame_thumbnail_scaler);
        this.thumbnail_height = thumbnail_width;
    }

    /**
     * Loads static dimensions from a resource file
     */
    private void loadStaticDims() {
        this.video_frame_thumbnail_scaler = fragment.getResources().getInteger(R.integer.video_frame_thumbnail_scaler);
        this.ad_height = dpToPx(fragment.getResources().getInteger(R.integer.ad_height_dp));
        this.max_video_frame_width = dpToPx(fragment.getResources().getInteger(R.integer.max_video_frame_width));
    }

    /** Setters **/
    public void setVideoFrameDimensions(int video_frame_width, int video_frame_height) {
        if (video_frame_width != this.video_frame_width || video_frame_height != this.video_frame_height) {

            this.video_frame_width = video_frame_width;
            this.video_frame_height = video_frame_height;

            this.calculateAdjustedVideoFrameDimensions();
        }
    }
    /** Getters **/
    public int getVideoFrameWidth() {
        return video_frame_width;
    }
    public int getVideoFrameHeight() {
        return video_frame_height;
    }
    public int getThumbnailWidth() {
        return thumbnail_width;
    }
    public int getThumbnailHeight() {
        return thumbnail_height;
    }
    public int getAdjustedVideoFrameHeight() {
        return adjusted_video_frame_height;
    }
    public int getAdjustedVideoFrameWidth() {
        return adjusted_video_frame_width;
    }

    public int findGifViewWidth() {
        WebView gif_view = (WebView) fragment.getFragmentView().findViewById(R.id.gif);
        return gif_view.getWidth();

    }
    public int findGifViewHeight() {
        WebView gif_view = (WebView) fragment.getFragmentView().findViewById(R.id.gif);
        return gif_view.getHeight();
    }
    public int findFragmentViewWidth() {
        FrameLayout fragment_view = (FrameLayout) fragment.getFragmentView().findViewById(R.id.image_container);
        return fragment_view.getWidth();

    }
    public int findFragmentViewHeight() {
        FrameLayout fragment_view = (FrameLayout) fragment.getFragmentView().findViewById(R.id.image_container);
        return fragment_view.getHeight();
    }
    /**
     * Converts dp to pixels. Useful for specifying a dp - equivalent size when creating a Bitmap
     * Most Android View sizes are specified and measured as pixes. If you want dp, you specify a dp
     * equivalent value based on the screen density of the device
     * @param context
     * @param dip
     * @return
     */
    public static int dpToPx(Context context, int dip) {
        float density = context.getResources().getDisplayMetrics().density;

        return (int) Math.ceil((float) dip * density);
    }
    /**
     * Same as above except for non-static an using its own context
     * @param dip
     * @return
     */

    public int dpToPx(int dip) {
        float density = fragment.getResources().getDisplayMetrics().density;

        return (int) Math.ceil((float) dip * density);
    }
    /**
     * Converts pixels to dip
     * @param context
     * @param px
     * @return
     */
    public static int pxToDp(Context context, int px) {
        float density = context.getResources().getDisplayMetrics().density;

        return (int) Math.ceil((float) px / density);
    }
    /**
     * Same as above except for non-static an using its own context
     * @param px
     * @return
     */
    public int pxToDp(int px) {
        float density = fragment.getResources().getDisplayMetrics().density;

        return (int) Math.ceil((float) px / density);
    }

    /**
     * Our video frame is going to be a big image
     * Here we calculate a smaller, usable image that will me manageable for making gif's
     */
    private void calculateAdjustedVideoFrameDimensions() {
        this.adjusted_video_frame_width = this.video_frame_width;
        this.adjusted_video_frame_height = this.video_frame_height;
    }

    public void report() {
        Log.d("TAG", "Reporting -----------------------------------------------------------------");

        Log.d("Display Width", String.valueOf(display_width));
        Log.d("Display Height", String.valueOf(display_height));
        Log.d("Usable Area Width", String.valueOf(usable_area_width));
        Log.d("Usable Area Height", String.valueOf(usable_area_height));
        Log.d("---", "---------------------------------------------------------------------------");
        Log.d("Video Frame Width", String.valueOf(video_frame_width));
        Log.d("Video Frame Height", String.valueOf(video_frame_height));
        Log.d("Rotated Frame Width", String.valueOf(rotated_video_frame_width));
        Log.d("Rotated Frame Height", String.valueOf(rotated_video_frame_height));
        Log.d("Recreated Frame Width", String.valueOf(recreated_video_frame_width));
        Log.d("Recreated Frame Height", String.valueOf(recreated_video_frame_height));
        Log.d("---", "---------------------------------------------------------------------------");
        Log.d("ThumbNail Width", String.valueOf(thumbnail_width));
        Log.d("ThumbNail Height", String.valueOf(thumbnail_height));
        Log.d("Adjusted Frame Width", String.valueOf(adjusted_video_frame_width));
        Log.d("Adjusted Frame Height", String.valueOf(adjusted_video_frame_height));

        Log.d("TAG", "---------------------------------------------------------------------------");
    }

    /**
     * Scale the given image so that it fits in the box specified by the given width and height
     *
     * To maintain the same aspect ratio, we will scale the image to to the minimum of the width
     * and height ratios between the input image and the given dimensions
     * scale_factor = min( width / image.getWidth(), height / image.getHeight() )
     *
     * @param input The image to scale
     * @param width The target width to scale to
     * @param height The target height to scale to
     * @return The scaled image. Return the original image if encountering division by 0
     */
    public static Bitmap scaleImageToGivenDimensions(Bitmap input, int width, int height) {
        Bitmap output;

        int input_width = input.getWidth();
        int input_height = input.getHeight();

        if (input_width == 0 || input_height == 0) {
            return input;
        }
        double scale_factor = min((double) width / (double) input_width,
                (double) height / (double) input_height);

        int output_width = (int) ((double) width * scale_factor);
        int output_height = (int) ((double) height * scale_factor);

        output = Bitmap.createScaledBitmap(input, output_width, output_height, false);

        return output;
    }
}
