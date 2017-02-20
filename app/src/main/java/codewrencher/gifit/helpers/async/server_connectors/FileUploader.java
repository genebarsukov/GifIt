package codewrencher.gifit.helpers.async.server_connectors;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
public class FileUploader extends FileMover {

    private String file_type;
    private String file_dir;

    /***********************************************************************************************
     * Constructor
     * To upload a single file
     * @param activity          : Used to display error messages
     * @param download_path     : The fill download path minus the file name
     * @param server_path       : The full server path minus the file name
     * @param success_message   : The message to display when finished successfully
     * @param file_name         : The file name to download
     * @param file_type         : The type of file to be used server side to decide where to put it
     * @param file_dir          : The custom file storage directory to create server side
     */
    public FileUploader ( MainActivity activity, String download_path, String server_path, String success_message, String file_name, String file_type, String file_dir ) {
        super( activity, download_path, server_path, success_message, file_name );

        this.file_type = file_type;
        this.file_dir = file_dir;
    }

    public void uploadFile() {
        AsyncTask async_task = new AsyncTask() {
            @Override
            protected void onPreExecute() {}
            @Override
            protected Object doInBackground(Object[] params) {
                int response_code = sendFileToServer();
                if (response_code > 400) {
                    error = true;
                    if ( error_msg == null ) {
                        error_msg = "Server Response Code " + String.valueOf( response_code );
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(Object result) {

                if (error) {
                    notifyListener( "error: " + error_msg );
                } else {
                    notifyListener( "file_uploaded" );
                }

            }
        };
        async_task.execute();
    }

    public int sendFileToServer() {
        this.error = false;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 10000000;

        File sourceFile = new File( this.download_path + "/" + this.file_name );

        if (!sourceFile.isFile()) {

            Log.e("uploadFile", "Source File not exist: " +  this.download_path + "/" + this.file_name );
            return 0;
        }
        else {

            int server_response_code = 0;
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream( sourceFile );
                URL url = new URL( server_path );

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", this.file_name);
                conn.setRequestProperty("file_type", this.file_type);
                conn.setRequestProperty("frame_directory", this.file_dir);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data;name=uploaded_file;filename="
                        + file_name + "" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                Log.d("bytesAvailable", String.valueOf(bytesAvailable));
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    Log.d("bytesRead", "bytesRead");
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                server_response_code = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + server_response_code);
                InputStream stream = conn.getInputStream();

                String server_response_msg = readResponseStream(stream, 10000);
                Log.d("server_response_msg", server_response_msg);
                if(server_response_code == 200){
                    // Its all good
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException e) {
                this.error = true;
                this.error_msg = e.getMessage();
                showError("Error : MalformedURLException " + e);
                e.printStackTrace();

                Log.e("Upload file to server", "error: " + e.getMessage(), e);
            } catch (Exception e) {
                this.error = true;
                this.error_msg = e.getMessage();
                showError("Error : Exception " + e);
                e.printStackTrace();

                Log.e("Upload file Exception", "Exception : "
                        + e.getMessage(), e);
            }
            return server_response_code;
        }
    }

}
