package codewrencher.gifit.objects.simple.item;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.io.File;
import java.util.Set;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.interfaces.ActionCompletedListener;
import codewrencher.gifit.helpers.async.interfaces.ImageItemLoadedListener;
import codewrencher.gifit.objects.complex.gif_chain.Frame;
import codewrencher.gifit.objects.complex.gif_chain.GifChain;
import codewrencher.gifit.tools.ToolBox;
import codewrencher.gifit.ui.MainActivity;
import codewrencher.gifit.ui.fragments.BaseFragment;
import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by Gene on 3/12/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class VideoFrame extends ImageItem implements ImageItemLoadedListener {

    private static String TAG = "Video Frame";

    public static final int LAYOUT_ID = R.layout.item_gallery_image_loading;
    public static final int CONTAINER_ID = R.id.scroll_item;

    int index_in_video;               // when chaining items, this will be different then this.index
    protected String video_file_path;
    protected LayoutInflater layout_inflater;
    protected FFmpegMediaMetadataRetriever media_data_retriever;

    protected ActionCompletedListener action_completed_listener;


    public VideoFrame() {
        super();
    }
    public VideoFrame( BaseFragment fragment, String video_file_path, int index, String title ) {
        super( fragment, title );

        this.video_file_path = video_file_path;
        this.index = index;

        this.layout_inflater = fragment.getSavedActivity().getLayoutInflater();
    }
    public VideoFrame( BaseFragment fragment, String video_file_path, String image_path, int index, String title ) {
        super( fragment, title );

        this.video_file_path = video_file_path;
        this.image_path = image_path;
        this.index = index;

        this.layout_inflater = fragment.getSavedActivity().getLayoutInflater();
    }

    public VideoFrame getCopy() {

        VideoFrame copy = new VideoFrame();

        copy.setFragment(this.fragment);
        copy.setParentObject(this.parent_object);

        copy.registerActionCompletedListener(this.action_completed_listener);

        copy.setIndex(this.index);
        copy.setVideoFilePath(this.video_file_path);
        copy.setImagePath(this.image_path);
        copy.setState(this.state);
        copy.setTitle(this.title);
        copy.setThumbnail(this.thumbnail);
        copy.setImage(this.image);
        copy.setThumbnailSize(this.thumb_width, this.thumb_height );
        copy.setImageSize(this.img_width, this.img_height );
        copy.setImgThumbnailLayout(this.img_thumbnail_layout );
        copy.setImgFullSizeLayout( this.img_full_size_layout );

        return copy;
    }
    /***********************************************************************************************
     * Register a listener to wait for an action to complete
     * Currently the actions are:
     *                              image_downloaded
     *                              image_loaded
     * @param action_completed_listener ImageItemLoadedListener: notifies when the image has finished loading
     */
    public void registerActionCompletedListener(ActionCompletedListener action_completed_listener) {
        this.action_completed_listener = action_completed_listener;
    }

    public void setIndexInVideo( int index_in_video ) {
        this.index_in_video = index_in_video;
    }
    public void setMediaDataRetriever( FFmpegMediaMetadataRetriever media_data_retriever ) {
        this.media_data_retriever = media_data_retriever;
    }
    public void setVideoFilePath( String video_file_path ) {
        this.video_file_path = video_file_path;
    }
    public Bitmap checkImage() {
        return this.image;
    }
    public Bitmap getImage() {

        int image_rotation = 90;
        if (this.image == null) {
            if (this.video_file_path != null) {
                this.image = this.createVideoFrame();
            }
            else if (this.image_path != null) {

                this.image = fragment.file_manager.loadBitmapFromFilePath(this.image_path);
                image_rotation = 0;
            }
            if (this.image != null) {

                Matrix matrix = new Matrix();
                matrix.postRotate( image_rotation );

                this.image = Bitmap.createBitmap(this.image, 0, 0, img_width, img_height, matrix, false);
            }
        }
        return this.image;
    }

    public void createUIObject() {;

        this.thumb_width = damen.getThumbnailWidth();
        this.thumb_height = damen.getThumbnailHeight();

        this.renderUIObject();
        this.animateUIObject();
    }

    public void renderUIObject() {

        this.layout_inflater = fragment.getSavedActivity().getLayoutInflater();
        this.img_thumbnail_layout = layout_inflater.inflate(LAYOUT_ID, (ViewGroup) fragment.getFragmentView(), false);

        this.createVideoFrameAsync();

        this.updateThumbLayout();
    }

    public void reloadUIObject() {
        this.updateThumbLayout();
    }

    public void createVideoFrameAsync() {
        final int start = ToolBox.getCurrentEpochSeconds();
        this.setThumbnailViewSize(img_thumbnail_layout);
        final ProgressBar progress_bar = this.configureProgressBar();

        final ImageView item_image_view = (ImageView) img_thumbnail_layout.findViewById(R.id.image);

        AsyncTask<String, String, String> async_task = new AsyncTask<String, String, String>() {
            @Override
            protected void onPreExecute() {
                progress_bar.setVisibility(View.VISIBLE);
            }
            @Override
            protected String doInBackground(String... params) {
                createVideoFrame();
                createImageFromVideoFrame();
                //addWaterMarkToFrame();
                loadThumbnail();

                return null;
            }
            @Override
            protected void onPostExecute(String result) {
                if (thumbnail != null) {
                    item_image_view.setImageBitmap(thumbnail);
                }
                progress_bar.setVisibility(View.GONE);

                action_completed_listener.onActionCompleted( "image_loaded", String.valueOf(index) );
            }
        };
        async_task.execute();
    }

    /**
     * Create a video frame image from a video
     * @return video frame bitmap image
     */
    private Bitmap createVideoFrame() {
        Bitmap video_frame = null;

        SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getSavedActivity());

        float fps = shared_preferences.getFloat("frame_rate", MainActivity.DEFAULT_FRAME_RATE);
        float frame_interval = 1000000 / fps;   /** interval in microseconds */

        media_data_retriever =  new FFmpegMediaMetadataRetriever();
        File file = new File(video_file_path);
        media_data_retriever.setDataSource(file.getAbsolutePath()) ;

        try {
            video_frame = media_data_retriever.getFrameAtTime( ( this.index_in_video * (int) frame_interval ), FFmpegMediaMetadataRetriever.OPTION_CLOSEST );

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                media_data_retriever.release();
                media_data_retriever = null;
            }
            catch (RuntimeException ex) {
                ex.printStackTrace();
                media_data_retriever = null;
            }
        }
        this.image = video_frame;

        if (video_frame != null) {

        }
        return video_frame;
    }

    /**
     * Rotate and finalize the video frame image
     */
    private void createImageFromVideoFrame() {

        if (this.image != null) {

            damen.setVideoFrameDimensions(this.image.getWidth(), this.image.getHeight());

            Matrix matrix = new Matrix();

            float img_scale = 0.5f;
            if (fragment.gif_timer != null) {
                img_scale = fragment.gif_timer.getDesiredFrameScaler();
            }
            matrix.postScale(img_scale, img_scale);
            matrix.postRotate(90);

            this.image = Bitmap.createBitmap(this.image, 0, 0, this.image.getWidth(), this.image.getHeight(), matrix, false);

            damen.setVideoFrameDimensions(this.image.getWidth(), this.image.getHeight());
            this.img_width = this.image.getWidth();
            this.img_height = this.image.getHeight();
        }
    }

    /**
     * Add a custom watermark to the frame image so peeps know!
     */
    private void addWaterMarkToFrame() {
        if (((Frame) this.parent_object).original_video_frame == null) {
            ((Frame) this.parent_object).addBitmapToImageItem(null, 0, 0);
        }
    }

    public void loadThumbnailAsync() {
        this.setThumbnailViewSize(img_thumbnail_layout);
        final ProgressBar progress_bar = this.configureProgressBar();

        final ImageView item_image_view = (ImageView) img_thumbnail_layout.findViewById(R.id.image);

        AsyncTask async_task = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                progress_bar.setVisibility(View.VISIBLE);
            }
            @Override
            protected Object doInBackground(Object[] params) {

                Log.d(TAG, "LOADING THUMBNAIL ASYNC ");
                createVideoFrame();
                loadThumbnail();
                return null;
            }
            @Override
            protected void onPostExecute(Object result) {
                if (thumbnail != null) {
                    item_image_view.setImageBitmap(thumbnail);
                }
                progress_bar.setVisibility(View.GONE);

                notifyImageItemLoadedListener();
            }
        };
        async_task.execute();
    }

    public void animateUIObject() {

        this.img_thumbnail_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ( thumbnail != null ) {
                    ImageView overlay = (ImageView) v.findViewById(R.id.overlay);
                    /** The below getParent() nonsense is because VideoFrame is a child of Frame, and we need to get to GifChain
                     * which is the one tha holds all the items
                     * */
                    if (((Frame) getParentObject()).getAge().equals("new")) {     /** We can only turn on and off the frames we just created */
                        if (((GifChain) ((Frame) getParentObject()).getParentObject()).checkForActiveFrames()) {     // only switch states if there are more than one active items
                            switchState();
                            if (state.equals("on")) {
                                overlay.setVisibility(View.GONE);

                            } else if (state.equals("off")) {
                                overlay.setVisibility(View.VISIBLE);
                            }
                        } else {                                                        // allow for turning items back on if there is only one active
                            if (state.equals("off")) {
                                switchState();
                                overlay.setVisibility(View.GONE);
                            }
                        }
                    }
                    /** The below getParent() nonsense is because VideoFrame is a child of Frame, and we need to get to GifChain
                     * which is the one tha holds all the items
                     * */
                    ((GifChain) ((Frame) getParentObject()).getParentObject()).checkForActiveFrames();           // handle showing and hiding check mark after toggling an item
                } else {
                    registerImageItemLoaderListener(VideoFrame.this);
                    setFinishedLoadingMessage("thumbnail_loaded");
                    getThumbnail();

                    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
                    Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
                    for ( Thread thread : threadArray ) {
                        Log.d(TAG, thread.toString() );
                    }
                }
            }

        });
    }
    private void updateThumbLayout() {
        LinearLayout container_layout = (LinearLayout) fragment.getFragmentView().findViewById(CONTAINER_ID);

        if ( container_layout != null && this.img_thumbnail_layout != null ) {

            if (this.img_thumbnail_layout.getParent() != null) {
                ((ViewGroup) this.img_thumbnail_layout.getParent()).removeView(this.img_thumbnail_layout);
            }
            container_layout.addView( this.img_thumbnail_layout );
        }
    }
    private void notifyActionCompletedListener( String action, String message ) {

        if (this.action_completed_listener != null ) {

            this.action_completed_listener.onActionCompleted( action, message );
        }
    }

    protected void notifyImageItemLoadedListener() {

        if ( this.finished_loading_message == null || this.finished_loading_message.equals("image_loaded") ) {

            this.notifyActionCompletedListener("image_loaded", String.valueOf(this.index));
        }
        else {

            this.onImageItemLoad( this.finished_loading_message, this.index );
        }
    }

    @Override
    public void onImageItemLoad(String message, int item_index) {

        if ( message.equals( "thumbnail_loaded" )) {

            ImageView item_image_view = (ImageView) img_thumbnail_layout.findViewById(R.id.image);
            item_image_view.setImageBitmap(thumbnail);
        }

    }
}
