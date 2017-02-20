package codewrencher.gifit.helpers.async.server_connectors;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
public class FileDownloader extends FileMover {

    /***********************************************************************************************
     * Constructor
     * To download a single file
     * @param activity          : Used to display error messages
     * @param download_path     : The fill download path minus the file name
     * @param server_path       : The full server path minus the file name
     * @param success_message   : The message to display when finished successfully
     * @param file_name         : The file name to download
     */
    public FileDownloader( MainActivity activity, String download_path, String server_path, String success_message, String file_name ) {
        super(activity, download_path, server_path, success_message, file_name);
    }

    public void downloadFile() {
        AsyncTask async_task = new AsyncTask() {
            @Override
            protected void onPreExecute() {}
            @Override
            protected Object doInBackground(Object[] params) {
                getFileFromServer();
                return null;
            }
            @Override
            protected void onPostExecute(Object result) {
                if (error) {
                    notifyListener( "error: " + error_msg );
                } else {
                    notifyListener( "file_downloaded" );
                }
            }
        };
        async_task.execute();
    }

    public void getFileFromServer(){
        try {
            this.error = false;

            URL url = new URL( this.server_path + "/" + this.file_name );
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            urlConnection.connect();
            File file = new File( download_path, file_name );

            Log.d("SERVER PATH", server_path);
            Log.d("DOWNLOAD PATH", download_path);
            Log.d("FILE NAME", file_name);
            FileOutputStream fileOutput = new FileOutputStream( file );

            //Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            //this is the total size of the file which we are downloading
            int download_size = 0;
            int total_size = urlConnection.getContentLength();


            //create a buffer...
            byte[] buffer = new byte[10000000];
            int bufferLength = 0;

            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
                download_size += bufferLength;
            }
            if (download_size == 0) {
                error = true;
                error_msg = "Could not read target file on the server";
            }
            //close the output stream when complete //
            fileOutput.close();

        } catch (final MalformedURLException e) {
            this.error = true;
            this.error_msg = e.getMessage();
            showError("Error : MalformedURLException " + e);
            e.printStackTrace();
        } catch (final IOException e) {
            this.error = true;
            this.error_msg = e.getMessage();
            showError("Error : IOException " + e);
            e.printStackTrace();
        }
        catch (final Exception e) {
            this.error = true;
            this.error_msg = e.getMessage();
            showError("Error : Please check your internet connection " + e);
        }
    }


}
