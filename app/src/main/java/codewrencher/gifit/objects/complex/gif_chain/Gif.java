package codewrencher.gifit.objects.complex.gif_chain;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.interfaces.ActionCompletedListener;
import codewrencher.gifit.helpers.async.server_connectors.FileUploader;
import codewrencher.gifit.helpers.files.FileManager;
import codewrencher.gifit.helpers.gif.AnimatedGifEncoder;
import codewrencher.gifit.objects.simple.item.TextSprite;
import codewrencher.gifit.tools.ToolBox;
import codewrencher.gifit.ui.fragments.BaseFragment;

import static java.lang.String.format;

/**
 * Created by Gene on 4/17/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class Gif extends UpdatableDataObject {

    public static final String TAG = "Gif";

    public static final String ID_FIELD_NAME = "gif_id";
    public static final String TABLE = "DEF_GIF";
    public static final String DATA_PATH = "GetTableData";
    public static final String LOCAL_GIF_CHAIN_DIRECTORY = "Gif_Chains";
    public static final String LOCAL_GIF_SAVE_DIRECTORY = "Saved_Gifs";
    public static final String UPLOAD_PATH = "UploadFile.php";

    private String gif_chain_id;

    private String save_file_path;
    private int frame_count;    /** the number of frames actually comprising the Gif
                                    Could be lower than the total frame count if some got turned off */

    byte[] encoded_images;

    protected ActionCompletedListener action_completed_listener;
    private String text;
    private int gif_speed;
    private TextSprite text_sprite;
    private Runnable callback;

    /***********************************************************************************************
     * Default Constructor for creating a new Gif
     * The object will not contain any Id parameters for the lengths of this UI session
     * New records will be created for it when the Gif Chain is shared and the notifications sent
     */
    public Gif() {
        super();
        this.gif_speed = 550;
    }
    public Gif(BaseFragment fragment) {
        super();
        this.gif_speed = 550;

        this.fragment = fragment;
        this.damen = fragment.damen;
    }
    /***********************************************************************************************
     * Constructor
     * Initiate a complete Gif by parsing a complete Json object
     * @param gif_object JSONObject: Json object representing all Gif data
     */
    public Gif( JSONObject gif_object ) throws JSONException {
        super(gif_object.getString(ID_FIELD_NAME), ID_FIELD_NAME, TABLE, DATA_PATH);

        this.name = gif_object.getString("gif_name");
        this.gif_chain_id = gif_object.getString("gif_chain_id");
        this.date_created = gif_object.getString("date_created");
        this.date_updated = gif_object.getString("date_updated");

        this.gif_speed = 550;
    }

    public Gif getCopy() {

        Gif copy = new Gif();
        copy.setId(this.id);
        copy.setIdFieldName( this.id_field_name );
        copy.setTable( this.table );
        copy.setDataPath( this.data_path );
        copy.setName(this.name);
        copy.setGifChainId(this.gif_chain_id);


        return copy;
    }

    /**
     * Set the callback to be executed when the Gid is finished rendering
     * @param callback Callback function to execute
     */
    public void setCallback(Runnable callback) {
        this.callback = callback;
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

    /***********************************************************************************************
     * Simple Getters and Setters
     */
    public void setGifChainId( String gif_chain_id ) {
        this.gif_chain_id = gif_chain_id;
    }
    public void setGifSpeed( int gif_speed ) {
        this.gif_speed = gif_speed;
    }

    public String getSaveFilePath() {
        return this.save_file_path;
    }
    public void setSaveFilePath(String save_file_path) {
        this.save_file_path = save_file_path;
    }
    public void setFrameCount( int frame_count ) {
        this.frame_count = frame_count;
    }
    public int getFrameCount() {
        return this.frame_count;
    }
    public String getText() {
        return this.text;
    }

    /***********************************************************************************************
     * Upload the Gif image to the server - using its local file path as reference
     * The initial server endpoint is a script file, but the save directory still has to be there
     */
    public void uploadImageFile() {

        this.download_file_path = this.buildDownloadPath(getParentObject().getName());
        String server_path = format("%s/%s", BASE_SERVER_URL, UPLOAD_PATH );

        FileUploader file_uploader = new FileUploader( activity, this.download_file_path, server_path, "gif_image_uploaded", this.name, "frame", this.parent_object.getName() );
        file_uploader.registerListener(this);

        file_uploader.uploadFile();
    }

    /***********************************************************************************************
     * Get the local download path as a String
     * Check for and create the local directory if it doesn't exist
     * @param server_frame_dir_name : The unique name of the directory, the gif chain name in thie case
     * @return The download directory path
     */
    public String buildDownloadPath(String server_frame_dir_name) {

        File download_directory = new File(Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES), APP_STORAGE_DIRECTORY + "/" +
                LOCAL_GIF_CHAIN_DIRECTORY + "/" + server_frame_dir_name );

        if (! download_directory.exists()){
            if (! download_directory.mkdirs()){
                Log.d( APP_STORAGE_DIRECTORY + "/" + LOCAL_GIF_CHAIN_DIRECTORY + "/" + server_frame_dir_name, "failed to create directory" );
                return null;
            }
        }
        return download_directory.getPath();
    }
    /***********************************************************************************************
     * Save
     */
    /***********************************************************************************************
     * Save the image associated with this object in the local Android storage directory
     * Temporary files get cleared out by the main activity on app launch
     * @param type : Specify to save as a tmeporary file or permanent: "temp" or "perm"
     * @param gif_byte_array : The byte array of gif frames created by the AnimatedGifEncoder
     * @return True if the save operation succeeds. False is something goes wrong
     */
    public Boolean saveGif(String type, byte[] gif_byte_array) {
        // external classes do not need to pass along data to save since encoded_images is saved as class variable
        if (gif_byte_array == null) {
            gif_byte_array = encoded_images;
            if (encoded_images == null) {
                return false;
            }
        }
        try {
            // decide which folder to save the file in
            File gif_storage_dir;

            if (type.equals("temp")) {
                gif_storage_dir = FileManager.tempStorageDirectoryExists(FileManager.TEMP_PIC_DIRECTORY);
            } else if (type.equals("perm")) {
                gif_storage_dir = FileManager.tempStorageDirectoryExists(FileManager.GIF_SAVE_DIRECTORY);
            } else {
                return false;
            }
            // update path data if necessary and write the file
            return writeGifFile(gif_storage_dir, gif_byte_array);
        }
        catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Save the passed data in a file
     * Update class name and path variables if this is the first time saving this gif
     * @param gif_storage_dir The directory where to save the Gif to: can be temp or perm
     * @param gif_byte_array The data to write to the file
     * @return True if the save operation succeeds. False is something goes wrong
     */
    private Boolean writeGifFile(File gif_storage_dir, byte[] gif_byte_array) {
        // first time saving the gif - set the name and file path
        if (gif_storage_dir == null) {
            return false;
        }
        if (name == null) {
            this.setName(APP_FILE_PREFIX + "gif_" + String.valueOf(ToolBox.getCurrentEpochMillis()) + ".gif");
        }
        this.setDownloadFilePath(gif_storage_dir.getPath());
        this.setSaveFilePath(gif_storage_dir.getPath() + "/" + this.name);

        // write the file data
        File image_file = new File(save_file_path);

        try {
            FileOutputStream output_stream = new FileOutputStream(image_file);
            output_stream.write(gif_byte_array);
            output_stream.flush();
            output_stream.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    /***********************************************************************************************
     * Load the Gif into the UI. 2 Options:
     *                                      1. Load the gif directly from a downloaded file
     *                                      2. Create the Gif from Frames and then loaded
     */
    /***********************************************************************************************
     * MAIN 1 - Make the Gif object create the Gif - from frames
     */
    public void createUIObject() {

        activity.hideLoading();
        this.renderUIObject();
    }

    /***********************************************************************************************
     * MAIN 2 Load the Gif object: load the Gif - from and existing file
     */
    public void loadUIObject() {

        this.loadGifIntoView();
        this.animateUIObject();
    }

    int start = 0;
    /***********************************************************************************************
     * Render a new Gif from separate frames
     */
    public void renderUIObject() {
        AsyncTask<String, String, String> async_task = new AsyncTask<String, String, String>() {
            @Override
            protected void onPreExecute() {
                activity.showLoading("Creating Gif", "green" );
            }
            @Override
            protected String doInBackground(String... params) {
                encoded_images = encodeFramesToByteArray( this );
                saveGif("temp", encoded_images);
                return null;
            }
            @Override
            protected void onPostExecute(String result) {
                loadGifIntoView();
                animateUIObject();
                activity.hideLoading();
                fragment.getFragmentView().findViewById( R.id.gif_animation_speed ).setVisibility(View.VISIBLE);

                action_completed_listener.onActionCompleted("gif_created", "1");
            }
        };
        async_task.execute();
    }

    /**
     * Sets gesture and touch listeners
     */
    public void animateUIObject() {

        this.rendered_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return fragment.getGesureDtector().onTouchEvent(event);
            }
        });
    }
    /**
     * Encodes the image frames from the ImageItem array into a byte array using the GifEncoder
     * Gif parameters such as frame interval and size are set here also
     * @return  byte[]: returns the encoded Gif as byte array for easy saving
     * @param async_task
     */
    private byte[] encodeFramesToByteArray(AsyncTask<String, String, String> async_task ) {
        ByteArrayOutputStream output_stream = new ByteArrayOutputStream();

        AnimatedGifEncoder gif_encoder = new AnimatedGifEncoder();
        int gif_width = damen.getVideoFrameWidth();
        int gif_height = damen.getVideoFrameHeight();
        int gif_quality = 20;

        if (fragment.gif_timer != null) {
            gif_quality = fragment.gif_timer.getDesiredGifQuality();
        }
        if (fragment.gif_timer != null) {
            fragment.gif_timer.setIndependentParams(gif_quality, gif_width, gif_height, damen.getVideoFrameWidth(), damen.getVideoFrameHeight());
        }

        gif_encoder.setDelay(this.gif_speed);
        gif_encoder.setRepeat(0);
        gif_encoder.setSize(gif_width, gif_height);
        gif_encoder.setQuality(gif_quality);

        gif_encoder.start(output_stream);

        this.frame_count = ((GifChain) getParentObject()).getFrames().size();

        int frame_index = 1;
        for ( Frame frame : ((GifChain) getParentObject()).getFrames() ) {

            if ( frame.getVideoFrame().getState().equals("on") ) {

                Bitmap bitmap = frame.getVideoFrame().getImage();
                gif_encoder.addFrame(bitmap);

                final int percent_complete = (int) ( ( (double) frame_index / (double) ((GifChain) getParentObject()).getFrames().size() ) * 100 );
                activity.runOnUiThread(new Thread(new Runnable() {
                    public void run() {
                        activity.updateLoadingPercentComplete( percent_complete );
                    }
                }));

                frame_index ++;
            } else {
                this.frame_count --;
            }
        }
        gif_encoder.finish();

        return output_stream.toByteArray();
    }

    private int getScale(int pic_height){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        fragment.getSavedActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int height = displaymetrics.widthPixels;// - Damen.dpToPx(fragment.getSavedActivity(), 50);

        Double scale = (double) height / (double) pic_height;
        scale = scale * 100d;

        return scale.intValue();
    }

    /***********************************************************************************************
     * Loads the previously saved Gif file into a custom GifWebView layout so that it can animate
     * This part takes a long time
     */
    public void loadGifIntoView() {
        rendered_view = fragment.getFragmentView().findViewById(R.id.gif);

        ((WebView) rendered_view).loadUrl("file:" + save_file_path);
        Log.d("FILE PATH", save_file_path);
        rendered_view.setPadding(0, 0, 0, 0);
        ((WebView) rendered_view).setInitialScale(getScale(damen.getVideoFrameWidth()));

        // hide gif_text sprite after gif is done loading
        ImageView gif_text_view = (ImageView) fragment.getFragmentView().findViewById(R.id.gif_text);
        gif_text_view.setVisibility(View.GONE);

        if (callback != null) {
            callback.run();
        }
    }

    /**
     * Creates the text sprite here
     * This is important !!!
     * @param text
     */
    public void createTextSprite(final String text) {
        this.text = text;
        Log.d("THIS TEXT", this.text);
        if (text_sprite == null) {
            text_sprite = new TextSprite(this.fragment, this.text);
        } else {
            text_sprite.setText(text);
        }
        final ImageView gif_text_view = (ImageView) this.fragment.getFragmentView().findViewById(R.id.gif_text);
        final ImageView check = (ImageView) this.fragment.getFragmentView().findViewById(R.id.check);
        final ImageView glove = (ImageView) this.fragment.getFragmentView().findViewById(R.id.glove);

        Log.d("GIF WIDTH", String.valueOf(fragment.damen.findGifViewWidth()));
        Log.d("GIF HEIGHT", String.valueOf(fragment.damen.findGifViewHeight()));
        Log.d("FRAG WIDTH", String.valueOf(fragment.damen.findFragmentViewWidth()));
        Log.d("FRAG HEIGHT", String.valueOf(fragment.damen.findFragmentViewHeight()));

        Log.d("GIF TEXT WIDTH", String.valueOf(gif_text_view.getWidth()));
        Log.d("GIF TEXT HEIGHT", String.valueOf(gif_text_view.getHeight()));

//        ViewGroup.LayoutParams gif_text_params = gif_text_view.getLayoutParams();
  //      gif_text_params.width = fragment.damen.findGifViewWidth();
    //    gif_text_params.height = fragment.damen.findGifViewHeight();
      //  gif_text_view.setLayoutParams(gif_text_params);

        text_sprite.setImageView(gif_text_view);
        text_sprite.setOnTouchListener();
  //      text_sprite.setImageSize(damen.getVideoFrameWidth(), damen.getVideoFrameHeight());

     //   final Bitmap hair_bitmap = Bitmap.createScaledBitmap(text_sprite.getImage(), text_sprite.getImageWidth(), text_sprite.getImageHeight(), false);
        text_sprite.createImage();
        gif_text_view.setImageBitmap(text_sprite.getImage());

        check.setVisibility(View.VISIBLE);
        glove.setVisibility(View.VISIBLE);
        gif_text_view.setVisibility(View.VISIBLE);

        glove.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                glove.setVisibility(View.GONE);
                return false;
            }
        });
    }

    private void notifyActionCompletedListener( String action, String message ) {

        if (this.action_completed_listener != null ) {

            this.action_completed_listener.onActionCompleted( action, message );
        }
    }

    @Override
    public void onFileDownloaded( String message ) {
        if ( message.equals("image_downloaded") ) {

            this.file_loaded = true;

            this.notifyActionCompletedListener( message, "0" );
        }
    }

    @Override
    public void onFileUploaded( String message ) {
        if ( message.equals("gif_image_uploaded") ) {

            this.file_sent = true;

            this.notifyActionCompletedListener( message, "0" );
        }
    }
}
