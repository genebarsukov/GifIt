package codewrencher.gifit.helpers.files;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import codewrencher.gifit.helpers.async.interfaces.FileSavedListener;
import codewrencher.gifit.objects.complex.gif_chain.Frame;
import codewrencher.gifit.tools.ToolBox;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 12/13/2015.
 */
public class FileManager {
    public static final String APP_STORAGE_DIRECTORY = "GifIt";
    public static final String GIF_FRAME_DIRECTORY = "Gif_Frames";
    public static final String GIF_SAVE_DIRECTORY = "Saved_Gifs";
    public static final String GIF_DOWNLOAD_DIRECTORY = "Downloaded_Gifs";

    public static final String TEMP_PIC_DIRECTORY = "Temp_GifIt";
    public static final String APP_FILE_PREFIX = "gifit_";
    public static final String TEMP_VIDEO_NAME = "temp_video.mp4";
    public static final String[] ACCEPTED_FILE_EXTENSIONS = {".jpg", ".bmp", ".png", ".gif"};
    private BaseFragment fragment;
    protected FileSavedListener file_saved_listener;
    private String saved_scatter_shot_path;

    public FileManager() {}
    public FileManager(BaseFragment fragment) {
        this.fragment = fragment;
    }
    public String saveTempPic(byte[] pic_data) {
        try {
            if (this.appStorageDirectoryExists() == null) return null;
            File temp_media_storage_dir = this.tempStorageDirectoryExists(TEMP_PIC_DIRECTORY);
            if (temp_media_storage_dir == null) return null;

            String file_name = "temp_pic_" + String.valueOf(ToolBox.getCurrentEpochMillis()) + ".jpg";
            String file_path = temp_media_storage_dir.getPath() + "/" + file_name;
            File image_file = new File(file_path);

            FileOutputStream output_stream = new FileOutputStream(image_file);
            output_stream.write(pic_data);
            output_stream.close();

            return file_path;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
    private File appStorageDirectoryExists() {
        File media_storage_dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_STORAGE_DIRECTORY);
        if (! media_storage_dir.exists()){
            if (! media_storage_dir.mkdirs()){
                Log.d(APP_STORAGE_DIRECTORY, "failed to create directory");
                return null;
            }
        }
        return media_storage_dir;
    }
    public static File tempStorageDirectoryExists(String temp_type) {
        File temp_media_storage_dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_STORAGE_DIRECTORY + "/" + temp_type);
        if (! temp_media_storage_dir.exists()){
            if (! temp_media_storage_dir.mkdirs()){
                Log.d(APP_STORAGE_DIRECTORY + "/" + temp_type, "failed to create directory");
                return null;
            }
        }
        return temp_media_storage_dir;
    }
    public void saveGifFramesInLine(final ArrayList<Frame> frames, String save_folder_name) {
        for ( Frame frame : frames ) {
            saveGifFrame( frame, save_folder_name );
        }
    }

    /**
     * Gif frames are saved and the ImageItems are updated with the frame path data
     * @param frame Frame : Frame object that contains the image to save
     * @return String: last saved frame file path
     */
    public String saveGifFrame( Frame frame, String save_folder_name ) {
        try {
            Bitmap bitmap = frame.getVideoFrame().getImage();

            if (this.appStorageDirectoryExists() == null) return null;

            File temp_media_storage_dir = tempStorageDirectoryExists(GIF_FRAME_DIRECTORY);
            if (temp_media_storage_dir == null) return null;

            temp_media_storage_dir = tempStorageDirectoryExists( GIF_FRAME_DIRECTORY + "/" + save_folder_name );
            if (temp_media_storage_dir == null) return null;

            String file_name = APP_FILE_PREFIX + "frame_" + String.valueOf( ToolBox.getCurrentEpochMillis() ) + ".gif";
            String file_path = temp_media_storage_dir.getPath() + "/" + file_name;
            File image_file = new File( file_path );
            frame.getVideoFrame().setImagePath( file_path );
            frame.setDownloadFilePath(file_path);

            FileOutputStream output_stream = new FileOutputStream(image_file);

            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output_stream);
            output_stream.flush();
            output_stream.close();

            output_stream.close();

            return file_path;

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public String saveGif( byte[] gif_byte_array ) {
        try {
            File media_storage_dir = this.appStorageDirectoryExists();
            if (media_storage_dir == null) return null;

            File temp_media_storage_dir = tempStorageDirectoryExists(GIF_SAVE_DIRECTORY);
            if (temp_media_storage_dir == null) return null;

            String file_name = APP_FILE_PREFIX + "gif_" + String.valueOf(ToolBox.getCurrentEpochMillis()) + ".gif";
            String file_path = temp_media_storage_dir.getPath() + "/" + file_name;
            File image_file = new File(file_path);

            FileOutputStream output_stream = new FileOutputStream(image_file);

            output_stream.write(gif_byte_array);
            output_stream.flush();
            output_stream.close();

            return file_path;
        } catch (Throwable e) {
            e.printStackTrace();
            Log.d("ERROR SAVING GIF", "was unable to save gif byte array");
            return "";
        }
    }

    public String saveGif(byte[] gif_byte_array, String file_name ) {
        try {
            File media_storage_dir = this.appStorageDirectoryExists();
            if (media_storage_dir == null) return null;

            File temp_media_storage_dir = tempStorageDirectoryExists(GIF_SAVE_DIRECTORY);
            if (temp_media_storage_dir == null) return null;

            String file_path = temp_media_storage_dir.getPath() + "/" + file_name;
            File image_file = new File(file_path);

            FileOutputStream output_stream = new FileOutputStream(image_file);

            output_stream.write(gif_byte_array);
            output_stream.flush();
            output_stream.close();

            return file_path;
        } catch (Throwable e) {
            e.printStackTrace();
            Log.d("ERROR SAVING GIF", "was unable to save gif byte array");
            return "";
        }
    }

    public static String getGifDownloadPath() {
        File download_directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_STORAGE_DIRECTORY + "/" + GIF_DOWNLOAD_DIRECTORY);
        if (! download_directory.exists()){
            if (! download_directory.mkdirs()){
                Log.d(APP_STORAGE_DIRECTORY + "/" + GIF_DOWNLOAD_DIRECTORY, "failed to create directory");
                return null;
            }
        }
        return download_directory.getPath();
    }
    public static String getGifSavePath() {
        File download_directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_STORAGE_DIRECTORY + "/" + GIF_SAVE_DIRECTORY);
        if (! download_directory.exists()){
            if (! download_directory.mkdirs()){
                Log.d(APP_STORAGE_DIRECTORY + "/" + GIF_SAVE_DIRECTORY, "failed to create directory");
                return null;
            }
        }
        return download_directory.getPath();
    }
    public static String getFrameDownloadPath(String server_frame_dir_name) {
        File download_directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_STORAGE_DIRECTORY + "/" +
                GIF_FRAME_DIRECTORY + "/" + server_frame_dir_name);
        if (! download_directory.exists()){
            if (! download_directory.mkdirs()){
                Log.d(APP_STORAGE_DIRECTORY + "/" + GIF_FRAME_DIRECTORY + "/" + server_frame_dir_name, "failed to create directory");
                return null;
            }
        }
        return download_directory.getPath();
    }

    public File cacheImage(Bitmap bitmap) {
        try {
            String file_name = APP_FILE_PREFIX + String.valueOf(ToolBox.getCurrentEpochMillis()) + ".jpg";
            File image_file = new File(fragment.getContext().getCacheDir(), file_name);

            FileOutputStream output_stream = new FileOutputStream(image_file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output_stream);
            output_stream.flush();
            output_stream.close();

            image_file.setReadable(true, false);

            return image_file;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
    public ArrayList<String> getSavedFilePaths(String storage_type) {
        String path = "";
        ArrayList<String> saved_file_paths = new ArrayList<>();

        if (storage_type.equals("meme_gallery")) {

            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + APP_STORAGE_DIRECTORY;
            saved_file_paths = this.getFilesFromFolder(path);

        } else if (storage_type.equals("workspace_gallery")) {

            // regular emulated camera pic paths
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM";
            try {
                saved_file_paths = this.getFilesFromDirectoryTree(path);
            } catch (Exception e) {}
            if (saved_file_paths == null) {
                saved_file_paths = new ArrayList<>();
            }
            try {
                saved_file_paths = this.getSDCameraFilePaths(saved_file_paths);
            } catch (Exception e) {}
            try {
                saved_file_paths = this.getScreenShotFilePaths(saved_file_paths);

            } catch (Exception e) {}
            try {
                saved_file_paths = this.getDownloadFilePaths(saved_file_paths);
            } catch (Exception e) {}

        } else if (storage_type.equals("temp")) {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + APP_STORAGE_DIRECTORY + "/" + TEMP_PIC_DIRECTORY;
            saved_file_paths = this.getFilesFromFolder(path);
        }

        return saved_file_paths;
    }
    private ArrayList<String> getSDCameraFilePaths(ArrayList<String> saved_file_paths) {
        String sd_path = System.getenv("SECONDARY_STORAGE");
        if (sd_path != null) {
            sd_path += "/DCIM";
            ArrayList<String> sd_file_paths = this.getFilesFromFolder(sd_path);
            if (sd_file_paths != null) {
                for (String sd_file_path : sd_file_paths) {
                    saved_file_paths.add(sd_file_path);
                }
            }
        }
        return saved_file_paths;
    }
    private ArrayList<String> getScreenShotFilePaths(ArrayList<String> saved_file_paths) {
        String screen_shot_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Screenshots";
        ArrayList<String> screen_shot_file_paths = this.getFilesFromFolder(screen_shot_path);
        if (screen_shot_file_paths != null) {
            for (String screen_shot_file_path : screen_shot_file_paths) {
                saved_file_paths.add(screen_shot_file_path);
            }
        }
        return saved_file_paths;
    }
    private ArrayList<String> getDownloadFilePaths(ArrayList<String> saved_file_paths) {
        String downloaded_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download";
        ArrayList<String> downloaded_file_paths = this.getFilesFromFolder(downloaded_path);
        if (downloaded_file_paths != null) {
            for (String downloaded_file_path : downloaded_file_paths) {
                saved_file_paths.add(downloaded_file_path);
            }
        }
        return saved_file_paths;
    }
    private ArrayList<String> getFilesFromFolder(String path) {
        ArrayList<String> file_paths = new ArrayList();

        File directory = new File(path);
        File file[] = directory.listFiles();
        if (file == null) return null;
        for (int i=0; i < file.length; i++) {
            String file_name = file[i].getName().toLowerCase();
            if (acceptedFileExtension(file_name)) {
                file_paths.add(path + "/" + file[i].getName());
            }
        }
        return file_paths;
    }
    private ArrayList<String> getFilesFromDirectoryTree(String path) {
        ArrayList<String> file_paths = new ArrayList();
        ArrayList<String> folder_paths = new ArrayList();

        File directory = new File(path);
        File sub_dir[] = directory.listFiles();

        if (sub_dir == null) return null;

        for (int i=0; i < sub_dir.length; i++) {
            String file_name = sub_dir[i].getName().toLowerCase();
            if (acceptedFileExtension(file_name)) {
                file_paths.add(path + "/" + sub_dir[i].getName());
            } else if (! sub_dir[i].getName().contains(".")) {
                folder_paths.add(path + "/" + sub_dir[i].getName());
            }
        }
        if (folder_paths.size() <= 0) {
            return file_paths;
        } else {
            for (int i=0; i < folder_paths.size(); i++) {
                path = folder_paths.get(i);
                directory = new File(path);
                File sub_sub_dir[] = directory.listFiles();

                for (int j=0; j < sub_sub_dir.length; j++) {
                    String file_name = sub_sub_dir[j].getName().toLowerCase();
                    if (acceptedFileExtension(file_name)) {
                        file_paths.add(path + "/" + sub_sub_dir[j].getName());
                    }
                }
            }
        }
        return file_paths;
    }
    public Bitmap loadBitmapFromFilePath(String file_path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap bitmap = this.getSampledBitmapFromFile(file_path, options);

        return bitmap;
    }
    public Bitmap getSampledBitmapFromFile(String file_path, BitmapFactory.Options options) {
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file_path, options);
        options.inSampleSize = calculateInSampleSize(options, fragment.screen_width / 3, fragment.screen_width / 3);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(file_path, options);

        return bitmap;
    }
    static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int inSampleSize = 1;	//Default subsampling size
        // See if image raw height and width is bigger than that of required view
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            //bigger
            final int halfHeight = options.outHeight / 2;
            final int halfWidth = options.outWidth / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    public boolean acceptedFileExtension(String file_name) {
        for (String accepted_extension : ACCEPTED_FILE_EXTENSIONS) {
            if (file_name.endsWith(accepted_extension)) {
                return true;
            }
        }
        return false;
    }

    public void registerListener(FileSavedListener file_saved_listener) {
        this.file_saved_listener = file_saved_listener;
    }
    public void notifyListener(String saved_scatter_shot_path) {
        file_saved_listener.onFileSaved(saved_scatter_shot_path);
    }
}
