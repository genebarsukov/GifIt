package codewrencher.gifit.helpers.async.server_connectors;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import codewrencher.gifit.helpers.async.interfaces.ServerFileListener;
import codewrencher.gifit.ui.MainActivity;

/**
 * Created by Gene on 4/23/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class FileMover {

    protected MainActivity activity;
    protected ServerFileListener server_file_listener;

    protected String download_path;
    protected String server_path;
    protected String file_name;
    protected String success_message;

    protected boolean error;
    protected String error_msg;

    /***********************************************************************************************
     * Constructor
     * To download a single file
     * @param activity          : Used to display error messages
     * @param download_path     : The fill download path minus the file name
     * @param server_path       : The full server path minus the file name
     * @param success_message   : The message to display when finished successfully
     * @param file_name         : The file name to download
     */
    public FileMover( MainActivity activity, String download_path, String server_path, String success_message, String file_name ) {

        this.activity = activity;
        this.download_path = download_path;
        this.server_path = server_path;
        this.success_message = success_message;
        this.file_name = file_name;
    }

    public void registerListener(ServerFileListener server_file_listener) {
        this.server_file_listener = server_file_listener;
    }

    void showError(final String error){
        Log.d( "FILE MOVER ERROR", error );
       /* activity.runOnUiThread(new Runnable() {
            public void run() {
                activity.torch.torch(error);
            }
        });*/
    }

    public String readResponseStream(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    public void notifyListener(String message) {
        if (error) {
            server_file_listener.onServerError( this.error_msg );
        }
        else {
            if ( message.equals("file_downloaded") ) {
                server_file_listener.onFileDownloaded( this.success_message );
            }
            else if ( message.equals("file_uploaded") ) {
                server_file_listener.onFileUploaded( this.success_message );
            }
        }
    }
}
