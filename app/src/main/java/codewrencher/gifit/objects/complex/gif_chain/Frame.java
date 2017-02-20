package codewrencher.gifit.objects.complex.gif_chain;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.interfaces.ActionCompletedListener;
import codewrencher.gifit.helpers.async.server_connectors.FileUploader;
import codewrencher.gifit.helpers.files.FileManager;
import codewrencher.gifit.objects.simple.item.VideoFrame;
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
public class Frame extends UpdatableDataObject implements ActionCompletedListener {

    public static final String ID_FIELD_NAME = "frame_id";
    public static final String TABLE = "DEF_FRAME";
    public static final String DATA_PATH = "GetTableData";
    public static final String LOCAL_GIF_CHAIN_DIRECTORY = "Gif_Chains";
    public static final String UPLOAD_PATH = "UploadFile.php";

    public static final double WATERMARK_TO_FRAME_RATIO = 0.075;
    protected String frame_position;
    public VideoFrame video_frame;
    public VideoFrame original_video_frame;

    protected ActionCompletedListener action_completed_listener;
    protected int index;

    private String age;

    /***********************************************************************************************
     * Default Constructor for creating a new gif Frame
     * The object will not contain any Id parameters for the lengths of this UI session
     * New records will be created for it when the Gif Chain is shared and the notifications sent
     */
    public Frame() {
        super();

        this.age = "new";
        this.video_frame = new VideoFrame();
    }
    /***********************************************************************************************
     * Constructor
     * Initiate a complete Gif Frame by parsing a complete Json object
     * @param frame_object JSONObject: Json object representing all frame data
     */
    public Frame( JSONObject frame_object ) throws JSONException {
        super( frame_object.getString( ID_FIELD_NAME ), ID_FIELD_NAME, TABLE, DATA_PATH );

        this.name = frame_object.getString("frame_name");
        this.frame_position = frame_object.getString("frame_position");
        this.date_created = frame_object.getString("date_created");
        this.date_updated = frame_object.getString("date_updated");

        this.age = "old";
        this.video_frame = new VideoFrame();
    }

    /***********************************************************************************************
     * Create a non-referenced copy of this object
     * With all child objects also being non-referenced copies
     * @return Frame copy
     */
    public Frame getCopy() {

        Frame copy = new Frame();

        copy.setFragment(this.fragment);
        copy.setActivity(this.activity);
        copy.setParentObject(this.parent_object);

        copy.registerActionCompletedListener( this.action_completed_listener );

        copy.setId(this.id);
        copy.setIdFieldName(this.id_field_name);
        copy.setTable(this.table);
        copy.setDataPath(this.data_path);

        copy.setIndex(this.index);
        copy.setName(this.name);
        copy.setFramePosition(this.frame_position);
        copy.setDateCreated(this.date_created);
        copy.setDateUpdated(this.date_updated);

        copy.setDownloadFilePath(this.download_file_path);
        copy.setAge(this.age);

        copy.setVideoFrame( this.video_frame.getCopy() );

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

    public void setIndex( int index ) {
        this.index = index;
    }
    public int getIndex() {
        return this.index;
    }
    public void setAge(String age) {
        this.age = age;
    }
    public String getAge() {
        return this.age;
    }
    public void setFramePosition( String frame_position ) {
        this.frame_position = frame_position;
    }

    public void createDownloadedVideoFrame() {
        this.download_file_path = this.getDownloadPath( getParentObject().getName() );

        VideoFrame video_frame = new VideoFrame( this.fragment, null, this.download_file_path + "/" + this.name, this.index, "gif_frame");
        this.setVideoFrame( video_frame );
    }
    public void setVideoFrame( VideoFrame video_frame ) {

        video_frame.registerActionCompletedListener( this );
        video_frame.setFragment(fragment);
        video_frame.setParentObject(this);
        video_frame.setIndex( this.index );

        this.video_frame = video_frame;
    }
    public void setFragment( BaseFragment fragment ) {
        this.fragment = fragment;
        this.damen = fragment.damen;
        this.video_frame.setFragment(fragment);
    }

    public VideoFrame getVideoFrame() {
        return this.video_frame;
    }

    /***********************************************************************************************
     * Modify the child video frame image by adding another bitmap to it
     * Get a fresh copy from a backup image first
     * @param additional_bitmap the bitmap image we are merging with the Video Frame
     * @param x_offset the x offset of the additional image in the main image
     * @param y_offset the y offset of the additional image in the main image
     */
    public void addBitmapToImageItem(Bitmap additional_bitmap, int x_offset, int y_offset) {

        /** If this is the first time adding text, save a virgin VideoFrame copy first */
        if ( this.original_video_frame == null ) {

            this.original_video_frame = new VideoFrame();
            Bitmap copied_image = this.video_frame.getImage().copy(this.video_frame.getImage().getConfig(), true);

            // create and scale a new watermark only once per frame batch
            // it gets reset to null in GigChain right before a new batch of frames is created
            if (fragment.watermark == null) {

                fragment.watermark = BitmapFactory.decodeResource(fragment.getResources(), R.drawable.watermark_7);

                double current_watermark_ratio = (double) fragment.watermark.getHeight() / (double) damen.getVideoFrameHeight();
                if (current_watermark_ratio > WATERMARK_TO_FRAME_RATIO) {

                    double new_watermark_height = (double) damen.getVideoFrameHeight() * WATERMARK_TO_FRAME_RATIO;
                    double watermark_width_scale = new_watermark_height / (double) fragment.watermark.getHeight();
                    double new_watermark_width = (double) fragment.watermark.getWidth() * watermark_width_scale;

                    fragment.watermark = Bitmap.createScaledBitmap(fragment.watermark, (int) new_watermark_width, (int) new_watermark_height, false);
                }
            }

            this.original_video_frame.setImage(copied_image);
            Canvas frame_canvas = new Canvas( copied_image );

            Paint paint = new Paint();
            int height = damen.getVideoFrameHeight() - fragment.watermark.getHeight();

            frame_canvas.drawBitmap(fragment.watermark, 5, height, paint);
            video_frame.setImage(copied_image);
        }
        /** Otherwise load a fresh copy from a back-up so the text doesn't keep piling on and is editable */
        else {
            this.video_frame.setImage( this.original_video_frame.getImage().copy(this.original_video_frame.getImage().getConfig(), true ) );
        }

        if (additional_bitmap == null) {
            return;
        }
        Paint paint = new Paint();

        Bitmap frame_canvas_bitmap = Bitmap.createBitmap(fragment.damen.getVideoFrameWidth(), fragment.damen.getVideoFrameHeight(), Bitmap.Config.ARGB_8888);
        Canvas frame_canvas = new Canvas( frame_canvas_bitmap );

        frame_canvas.drawBitmap( video_frame.getImage(), 0, 0, paint );
        frame_canvas.drawBitmap( additional_bitmap, x_offset, y_offset, paint);

        video_frame.setImage( frame_canvas_bitmap);
    }

    /***********************************************************************************************
     * Upload the frame image to the server - using its local file path as reference
     * The initial server endpoint is a script file, but the save directory still has to be there
     */
    public void uploadImageFile() {

        this.download_file_path = this.getDownloadPath( getParentObject().getName() );
        String server_path = format("%s/%s", BASE_SERVER_URL, UPLOAD_PATH );

        Log.d("FRAME UPLOADER", fragment + " download_path : " + this.download_file_path + " file_name : " + this.name + " server_path : " + server_path );
        Log.d("NAME", this.parent_object.getName());
        FileUploader file_uploader = new FileUploader( activity, this.download_file_path, server_path, "image_uploaded", this.name, "frame", this.parent_object.getName() );
        file_uploader.registerListener(this);

        file_uploader.uploadFile();
    }

    /***********************************************************************************************
     * Save the image associated with this object in the local Android storage directory
     * @return String : local file path of the saved file including the file name
     */
    public String saveImageFile() {
        try {
            Bitmap bitmap = this.getVideoFrame().getImage();

            if (this.appStorageDirectoryExists() == null) return null;

            File base_frame_storage_dir_file = FileManager.tempStorageDirectoryExists(LOCAL_GIF_CHAIN_DIRECTORY);
            if ( base_frame_storage_dir_file == null ) return null;

            File frame_storage_dir_file = FileManager.tempStorageDirectoryExists( LOCAL_GIF_CHAIN_DIRECTORY + "/" + this.getParentObject().getName() );
            if ( frame_storage_dir_file == null ) return null;

            this.setName(APP_FILE_PREFIX + "frame_" + String.valueOf(ToolBox.getCurrentEpochMillis()) + ".gif");
            File image_file = new File( frame_storage_dir_file.getPath() + "/" + this.name );
            this.setDownloadFilePath( frame_storage_dir_file.getPath() );

            this.getVideoFrame().setImagePath(image_file.getPath());

            FileOutputStream output_stream = new FileOutputStream(image_file);

            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output_stream);
            output_stream.flush();
            output_stream.close();

            output_stream.close();

            return image_file.getPath();

        } catch (Throwable e) {
            e.printStackTrace();
            Log.d("ERROR SAVING FRAME", "was unable to save frame image");
        }
        return null;
    }

    public String getDownloadPath( String server_frame_dir_name ) {

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

    private void notifyActionCompletedListener( String action, String message ) {

        if (this.action_completed_listener != null ) {

            this.action_completed_listener.onActionCompleted( action, message );
        }
    }

    @Override
    public void onFileDownloaded( String message ) {
        if ( message.equals("image_downloaded") ) {

            this.createDownloadedVideoFrame();

            this.notifyActionCompletedListener( message, String.valueOf(this.index) );
        }
    }
    @Override
    public void onFileUploaded( String message ) {
        if ( message.equals("image_uploaded") ) {

            this.file_sent = true;


        }
        Log.d("FRAME UPLOADED", "FRAME UPLOADED");
        this.notifyActionCompletedListener( message, String.valueOf(this.index) );
    }

    @Override
    public void onActionCompleted( String action, String message ) {

        Log.d("FRAME onActionCompleted", action);
        Log.d("FRAME onActionCompleted", message);
        this.notifyActionCompletedListener( action, String.valueOf(this.index) );
    }
}
