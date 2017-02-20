package codewrencher.gifit.objects.complex.gif_chain;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.interfaces.ActionCompletedListener;
import codewrencher.gifit.helpers.files.FileManager;
import codewrencher.gifit.helpers.gif.GifTimer;
import codewrencher.gifit.objects.simple.item.VideoFrame;
import codewrencher.gifit.ui.MainActivity;
import codewrencher.gifit.ui.fragments.BaseFragment;
import codewrencher.gifit.ui.fragments.GifSpaceFragment;
import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by Gene on 4/17/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class GifChain extends UpdatableDataObject implements ActionCompletedListener {

    private static final String tag = "GifChain";

    public static final String ID_FIELD_NAME = "gif_chain_id";
    public static final String TABLE = "DEF_GIF_CHAIN";
    public static final String DATA_PATH = "GetTableData";

    private String user_id;                // The id of this current user
    private String originator_user_id;
    private String chain_length;
    private String chain_status;

    private Gif gif;
    private User sender;

    /** Used a LinkedHashMap here because there is no case in which you will create a User without an id.
     * A user is created on login / register account at which point id and other data is present
     * It is also useful to be able to retrieve a user by id
     */
    private LinkedHashMap <String, User> users;

    /** Used and ArrayList here because there are times when frames are created in the UI
     * Which means they don't have an id to set as a key
     * Frames also usually come as a package deal and so being able to retrieve one by id would rarely be useful
     */
    private ArrayList <Frame> frames;
    private ArrayList <Frame> original_frames;

    private String chain_type;  /**     new | received | extended       */
    private boolean received_frames_loaded;
    private boolean captured_frames_loaded;
    private int upload_ables_count;
    int upload_completed_count;
    protected FFmpegMediaMetadataRetriever media_data_retriever;

    /***********************************************************************************************
     * Default Constructor for creating a new GifChain
     * The object will not contain any Id parameters for the lengths of this UI session
     * New records will be created for it when the Gif Chain is shared and the notifications sent
     * If the Gif Chain does not get shared, it will be deleted without being recorded
     * The personal Working Gif Chain gets saved for the time span of this app launch,
     * In case there are some interruptions as when viewing a notification or onPause
     */
    public GifChain() {
        super();

        this.users = new LinkedHashMap<>();
        this.frames = new ArrayList<>();
        this.gif = new Gif();
    }
    public GifChain(BaseFragment fragment) {
        super();
        this.fragment = fragment;
        this.users = new LinkedHashMap<>();
        this.frames = new ArrayList<>();
        this.gif = new Gif(fragment);
    }
    /***********************************************************************************************
     * Constructor
     * Initiate a complete GifChain by parsing a complete Json object
     * @param gif_chain_object JSONObject: Json object representing all Gif chain data
     */
    public GifChain( JSONObject gif_chain_object ) throws JSONException {
        super( gif_chain_object.getString( ID_FIELD_NAME ), ID_FIELD_NAME, TABLE, DATA_PATH );

        this.user_id = gif_chain_object.getString("user_id");
        this.name = gif_chain_object.getString("gif_chain_name");
        this.originator_user_id = gif_chain_object.getString("originator_user_id");
        this.chain_length = gif_chain_object.getString("chain_length");
        this.chain_status = gif_chain_object.getString("chain_status");
        this.date_created = gif_chain_object.getString("date_created");
        this.date_updated = gif_chain_object.getString("date_updated");

        this.users = new LinkedHashMap<>();
        this.frames = new ArrayList<>();
        this.gif = new Gif();
    }

    public void createObjects(JSONObject gif_chain_object) throws JSONException {

        this.createUsers(gif_chain_object.getJSONArray("users"));
        this.createFrames(gif_chain_object.getJSONArray("gif_frames"));
        this.setUpGif(gif_chain_object.getJSONObject("last_gif"));
    }
    public void setFragment( BaseFragment fragment ) {
        super.setFragment(fragment);

        this.fragment = fragment;
        // set the fragment for all child elements
        this.gif.setFragment(fragment);

        for (Frame frame : this.frames ) {
            frame.setFragment( fragment );
        }
        for (String key : this.users.keySet() ) {
            this.users.get( key ).setFragment(fragment);
        }
    }

    public void setFrames(ArrayList frames) {
        this.frames = frames;
    }
    public void setGif(Gif gif) {
        this.gif = gif;
    }

    public void setChainType( String chain_type ) {
        this.chain_type = chain_type;
    }

    /***********************************************************************************************
     * Set User objects from Json array
     * @param json_array: user json array
     */
    private void createUsers(JSONArray json_array) {

        for (int i=0; i<json_array.length(); i++) {

            try {
                User user = new User( json_array.getJSONObject(i) );
                user.setActivity(this.activity);
                user.setFragment(this.fragment);
                user.setParentObject(this);

                if ( user.getId() != this.user_id ) {
                    this.users.put(user.getId(), user);
                }
                if ( user.getUserType().equals("sender") ) {
                    this.sender = user;
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    /***********************************************************************************************
     * Set Frame objects from Json array
     * @param json_array: frame json array
     */
    private void createFrames(JSONArray json_array) {

        for (int i = 0; i < json_array.length(); i++) {
            try {
                Frame frame = new Frame( json_array.getJSONObject(i) );
                this.addFrame(frame);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /***********************************************************************************************
     * Add an existing Frame object to this GifChain
     * Usually called when the Frame was first created int the UI
     * @param frame: Frame
     */
    public void addFrame( Frame frame ) {

        frame.setActivity(this.activity);
        frame.setFragment(this.fragment);
        frame.setParentObject(this);
        frame.setIndex( this.frames.size() );
        frame.registerActionCompletedListener( this );

        this.frames.add( frame );
    }
    /***********************************************************************************************
     * Set Last Gif object from Json object
     * @param json_obj: frame json array
     */
    private void setUpGif(JSONObject json_obj) {
        if ( json_obj != null ) {
            try {
                this.gif = new Gif( json_obj );
                this.setUpGif();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    /***********************************************************************************************
     * Set a new Gif object
     */
    public void setUpGif() {

        this.gif.setActivity(this.activity);
        this.gif.setFragment(this.fragment);
        this.gif.setParentObject(this);
        this.gif.registerActionCompletedListener(this);
    }

    /**
     * Add a custom watermark to every frame of the gif
     */
    private void addWaterMarkToFrames() {
        ImageView watermark_view = (ImageView) fragment.getFragmentView().findViewById(R.id.watermark);
        watermark_view.setVisibility(View.VISIBLE);

        watermark_view.destroyDrawingCache();
        watermark_view.buildDrawingCache(true);
        Bitmap watermark = watermark_view.getDrawingCache(true);

        for( Frame frame : frames ) {
            if ( frame.getAge().equals("new") ) {
                frame.addBitmapToImageItem(watermark, 10, 500);
            }
        }
        watermark_view.setVisibility(View.GONE);
    }
    public Gif getGif() {
        return this.gif;
    }

    public ArrayList<Frame> getFrames() {
        return this.frames;
    }
    public User getSender() {
        return this.sender;
    }

    public void uploadFrames() {
        for ( Frame frame : this.frames ) {

            if ( frame.getAge().equals("new") ) {   /** Only upload the frames you just created */

                if ( frame.getVideoFrame().getState().equals("on") ) { /** Only upload frames which are still on */
                    frame.uploadImageFile();
                }
            }
        }
    }
    public void uploadGif() {
        this.upload_completed_count = 0;
        gif.uploadImageFile();
    }

    public void saveFrames() {
        for ( Frame frame : this.frames ) {

            if ( frame.getAge().equals("new") ) {   /** Only save the frames you just created */

                if ( frame.getVideoFrame().getState().equals("on") ) { /** Only save frames which are still on */
                    frame.saveImageFile();
                }
            }
        }
    }

    public void addTextToFrames(ImageView text_sprite_view) {
        text_sprite_view.destroyDrawingCache();
        text_sprite_view.buildDrawingCache(true);
        Bitmap text_bitmap = text_sprite_view.getDrawingCache(true);

        text_bitmap = Bitmap.createScaledBitmap(text_bitmap, damen.getVideoFrameWidth(), damen.getVideoFrameHeight(), false);

        for(Frame frame : this.frames) {

            if ( frame.getAge().equals("new") ) {   /** Only save the frames you just created */
                frame.addBitmapToImageItem(text_bitmap, 0, 0);
            }
        }
    }
    /***********************************************************************************************
     * MAIN - Create the UI object in the fragment View
     * Decide how to create it based on the chain type parameter
     * Either:
     *          1. Load the captured Video Frames and then create the gif
     *          2. Download and load the gif without downloading or loading frames
     *          3. Download and load the received frames first, then create the captured Video Frames, then load the Gif
     */
    public void createUIObject() {

        activity.showLoading("Loading Images", "blue");

        final int frame_count = (int) fragment.shared_preferences.getFloat("frame_count", MainActivity.DEFAULT_FRAME_COUNT);   // gotten from the UI frame rate switch, whose settings are saved in SharedPreferences
        final String video_file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + FileManager.APP_STORAGE_DIRECTORY + "/" + FileManager.TEMP_VIDEO_NAME;

        this.createFramesFromVideo(video_file_path, frame_count);

    }
    /***********************************************************************************************
     * Handles the user selecting which frames he wants to keep from his own new Gif
     * @return boolean true or false. Disallows all the frames to be turned off
     */
    public boolean checkForActiveFrames() {
        Log.d("checkForActiveFrames", "----------------------------");
        int inactive_item_count = 0;
        int frame_count = 0;

        if ( this.frames != null ) {

            for ( Frame frame : this.frames ) {
                if ( frame.getAge().equals("new")) {

                    frame_count ++;     /** need to calculate my own frame count here since
                                            Mot all frames will have functionality to turn on and off such as when chaining */

                    if ( ! frame.getVideoFrame().getState().equals("on") ) {     //look for any "off" items

                        inactive_item_count++;
                    }
                }
            }
            if ( inactive_item_count > 0 ) {                                        /** Some items are turned off */
                this.fragment.getFragmentView().findViewById(R.id.check).setVisibility(View.VISIBLE);
            }
            else if ( this.gif.getFrameCount() < this.frames.size() ) {        /** No items are turned off but the last gif was created with an omitted frame
             Leave the check button visible so the user can revert it to its original size if he wishes */
                this.fragment.getFragmentView().findViewById(R.id.check).setVisibility(View.VISIBLE);
            } else {
                this.fragment.getFragmentView().findViewById(R.id.check).setVisibility(View.GONE);
            }
            if ( inactive_item_count >= frame_count - 1 ) { //test
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public void createFramesFromVideo( final String video_file_path, final int frame_count ) {

        fragment.gif_timer = new GifTimer(fragment, frame_count);
        fragment.watermark = null;
        fragment.gif_timer.startRenderingFrames();

        for (int i = 0; i < frame_count; i++) {

            Frame frame = new Frame();
            this.addFrame(frame);

            VideoFrame video_frame = new VideoFrame( this.fragment, video_file_path, i, "img " + i) ;
            video_frame.setIndexInVideo(i);

            frame.setVideoFrame(video_frame);
            video_frame.createUIObject();
        }
    }

    public void reloadVideoFrames() {
        for(Frame frame : frames) {
            frame.setFragment(fragment);
            VideoFrame video_frame = frame.getVideoFrame();
            video_frame.setFragment(fragment);
            video_frame.reloadUIObject();
        }
    }

    private void loadFrameImage( Frame frame ) {

        frame.createDownloadedVideoFrame();
        frame.getVideoFrame().createUIObject();
    }

    public void calculateRelativeUploadAblesCount() {
        this.upload_ables_count = 0;
        for ( Frame frame : this.frames ) {
            if ( frame.getAge().equals("new") ) {   /** Only upload the frames you just created */

                if (frame.getVideoFrame().getState().equals("on")) { /** Only upload frames which are still on */
                    upload_ables_count++;
                }
            }
        }

        upload_ables_count++; // + 1 for gif upload
    }

    @Override
    public void onActionCompleted(String action, String message) {

        int item_index = Integer.parseInt( message );

        int upload_percent_complete;

        switch ( action ) {
            case "gif_created":
                if (fragment.gif_timer != null) {
                    fragment.gif_timer.stopCreatingGif();
              //      fragment.gif_timer.showStats();
                }
            case "image_uploaded" :   /** Update the Activity loading spinner with percentage complete */

                if ( this.upload_ables_count != 0 ) {

                    this.upload_completed_count ++;
                    upload_percent_complete = (int) ( ( (double) upload_completed_count / (double) this.upload_ables_count )  * 100 );
                    activity.updateLoadingPercentComplete( upload_percent_complete );
                }
                break;

            case "gif_image_uploaded" :   /** Update the Activity loading spinner with percentage complete */

                this.upload_completed_count ++;
                upload_percent_complete = (int) ( ( (double) upload_completed_count / (double) this.upload_ables_count )  * 100 );
                activity.updateLoadingPercentComplete( upload_percent_complete );

                break;

            case "image_downloaded" :   /** frames get downloaded only when they are immediately needed
                                            once downloaded the frame image is loaded */
                switch ( this.chain_type ) {

                    case "downloaded" :

                        gif.loadUIObject();
                        break;

                    default :
                        loadFrameImage( this.frames.get(item_index) );
                        break;

                }
                break;

            case "image_loaded" :   /** frames load their images after downloading or when creating frames from a video
                                        the Frame received this same callback from its Video Frame
                                        The individual image loaded callbacks are handled by their respective objects
                                        This case only fires when all the images have been loaded */

                /** show progress */

                int percent_complete;

                switch ( this.chain_type ) {
                    case "captured" :

                        percent_complete = (int) ( ( ((double) item_index + 1) / (double) this.frames.size() ) * 100 );

                        activity.updateLoadingPercentComplete( percent_complete );
                        break;

                    case "extended" :

                        int frame_count = this.frames.size();

                        if ( ! this.received_frames_loaded ) {
                            frame_count = this.frames.size() + (int) fragment.shared_preferences.getFloat("frame_count", MainActivity.DEFAULT_FRAME_COUNT);
                        }
                        percent_complete = (int) ( ( ((double) item_index + 1) / (double) frame_count ) * 100 );
                        activity.updateLoadingPercentComplete( percent_complete );
                        break;

                    default:
                        break;
                }

                if ( ( this.frames.size() - 1 ) == item_index ) {   /** When all the Gif Chain frames have images have been loaded
                                                                        There are 2 cases when this might happen */

            //        Log.d("ACTION COMPLETE", "ALL FRAMES LOADED");
              //      Log.d("ACTION CHAIN TYPE", this.chain_type);
                    switch ( this.chain_type ) {

                        case "captured" :   /** First, when the user shoots for the first time and creates a new chain
                                           In this case we simply load the Gif from the frames */

                            this.captured_frames_loaded = true;

                            if (fragment.gif_timer != null) {
                                fragment.gif_timer.stopRenderingFrames();
                                fragment.gif_timer.startCreatingGif();
                            }

                            Log.d("CREATING GIF", "---------------------------");

                            this.fragment.getFragmentView().post(new Runnable() {
                                public void run() {

                                    setUpGif();
                                    Runnable callback = new Runnable() {
                                        @Override
                                        public void run() {
                                            ((GifSpaceFragment) fragment).showEditTextBox();
                                        }
                                    };
                                    gif.setCallback(callback);
                                    gif.createUIObject();
                                }
                            });
                            break;

                        case "extended" :   /** Secondly when the image has been extended, these callbacks fire in 2 cases
                                                When loading old, received, downloaded images
                                                When creating and adding new Video Frames */

                            if ( ! this.received_frames_loaded ) {  /** If the received frames loaded flag has not been set yet,
                                                                        We know that this is what we were loading
                                                                        Load the Video Frames next and set the flag */
                                this.received_frames_loaded = true;

                                final int frame_count = (int) fragment.shared_preferences.getFloat("frame_count", MainActivity.DEFAULT_FRAME_COUNT);   // gotten from the UI frame rate switch, whose settings are saved in SharedPreferences
                                final String video_file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + FileManager.APP_STORAGE_DIRECTORY + "/" + FileManager.TEMP_VIDEO_NAME;

                                this.createFramesFromVideo(video_file_path, frame_count);
                            }
                            else {                                  /** if he received frames loaded flag has already been set,
                                                                        We know that this time we were loading captured frames
                                                                        So now load the Gif */
                                this.captured_frames_loaded = true;

                                setUpGif();
                                Runnable callback = new Runnable() {
                                    @Override
                                    public void run() {
                                        ((GifSpaceFragment) fragment).showEditTextBox();
                                    }
                                };
                                gif.setCallback(callback);
                                gif.createUIObject();
                            }
                            break;
                    }
                }
                break;
        }
    }
}
