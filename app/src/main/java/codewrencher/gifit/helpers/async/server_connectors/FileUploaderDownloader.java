package codewrencher.gifit.helpers.async.server_connectors;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import codewrencher.gifit.helpers.async.interfaces.ServerFileListener;
import codewrencher.gifit.tools.ToolBox;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 3/30/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class FileUploaderDownloader {

    private BaseFragment fragment;
    private ServerFileListener server_file_listener;
    private String local_file_path;
    private String file_name;
    private String server_file_path_base;
    private String server_file_path;
    private String frame_directory;
    private String file_type;
    private ArrayList<String> file_queue;
    private int response_code;
    private boolean error;
    private String error_msg;
    private String queue_type;

    /**
     * * This Constructor currently used for downloads where it hits a one or multiple direct file path endpoints
     * @param fragment
     * @param local_file_path
     * @param file_name
     * @param server_path
     */
    public FileUploaderDownloader(BaseFragment fragment, String local_file_path, String file_name, String server_path) {
        this.fragment = fragment;
        this.local_file_path = local_file_path;
        this.file_name = file_name;
        this.frame_directory = "";
        this.server_file_path_base = server_path;
        this.server_file_path = server_path;
    }

    public void registerListener(ServerFileListener server_file_listener) {
        this.server_file_listener = server_file_listener;
    }
    public void setFileName( String file_name ) {
        this.file_name = file_name;
    }
    public void setLocalFilePath( String local_file_path ) {
        this.local_file_path = local_file_path;
    }
    public void setServerFilePath( String server_file_path ) {
        this.server_file_path = server_file_path;
    }
    public void setFrameDirectory( String frame_directory ) {
        this.frame_directory = frame_directory;
    }
    public void setFileType( String file_type ) {
        this.file_type = file_type;
    }

    /***********************************************************************************************
     * We clone this list here so that it does not get modified in case we want to use it later
     * in the invoking class
     * @param file_queue ArrayList: a list of files passed from the operating class
     */
    public void setFileQueue( ArrayList<String> file_queue ) {
        this.file_queue = (ArrayList<String>) file_queue.clone();
    }

    /***********************************************************************************************
     * Checks the list of queue files or paths given to determine if the list has been exhausted yet
     * Also sets the class this.file_name variable to move on to the next file
     * @param queue_type String: either "path" or "file" Lets us know how to handle different item formats
     * @return Boolean: True = continue. False = wrap it up.
     */
    public boolean checkQueue(String queue_type) {
        this.queue_type = queue_type;

        if ( this.file_queue == null ) return false;
        else if ( this.file_queue.size() == 0 ) return false;
        else {
            String next_file = this.getNextInQueue();

            if (next_file == null) {
                return false;
            }

            /** for gif frame uploads when the files are full paths */
            if ( this.queue_type.equals( "path" ) ) {
                this.setLocalFilePath(next_file);
                this.setFileName( ToolBox.getFileNameFromFilePath(next_file) );
            }
            /** For frame downloads when the files are file names and the server file path needs
             *  to be adjusted for each file */
            else if ( this.queue_type.equals( "file" ) ) {
                this.setFileName(next_file);
                this.setServerFilePath(this.server_file_path_base + "/" + next_file);
            }
            /** True means continue downloading */
            return true;
        }
    }
    public String getNextInQueue() {
        int queue_size = this.file_queue.size();
        String next_file_path = null;

        if ( queue_size > 0) {
            next_file_path = this.file_queue.get(0);
            this.file_queue.remove(0);
        }

        return next_file_path;
    }

    public void uploadFileToServer() {
        AsyncTask async_task = new AsyncTask() {
            @Override
            protected void onPreExecute() {}
            @Override
            protected Object doInBackground(Object[] params) {
                response_code = uploadFile();
                if (response_code > 400) {
                    error = true;
                }
                return null;
            }
            @Override
            protected void onPostExecute(Object result) {
                if ( checkQueue( queue_type ) ) {
                    if (response_code < 400) {
                        uploadFileToServer();
                    }
                } else {
                    if (error) {
                        notifyListener("error: " + error_msg);
                    } else {
                        notifyListener("file_uploaded");
                    }
                }
            }
        };
        async_task.execute();
    }
    public void downloadFileFromServer() {
        AsyncTask async_task = new AsyncTask() {
            @Override
            protected void onPreExecute() {}
            @Override
            protected Object doInBackground(Object[] params) {
                downloadFile();
                return null;
            }
            @Override
            protected void onPostExecute(Object result) {
                if ( checkQueue( queue_type ) ) {
                    if (response_code < 400) {
                        downloadFileFromServer();
                    }
                } else {
                    if (error) {
                        notifyListener("error: " + error_msg);
                    } else {
                        notifyListener("file_downloaded");
                    }
                }
            }
        };
        async_task.execute();
    }

    public int uploadFile() {
        this.error = false;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 10000000;

        File sourceFile = new File(this.local_file_path);

        if (!sourceFile.isFile()) {

            Log.e("uploadFile", "Source File not exist :"
                    + server_file_path + "");
            return 0;
        }
        else {

            int server_response_code = 0;
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream( sourceFile );
                URL url = new URL(server_file_path);

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
                conn.setRequestProperty("frame_directory", this.frame_directory);

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

                String server_response_msg = readIt(stream, 10000);
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
    public void downloadFile(){
        try {
            this.error = false;

            URL url = new URL(server_file_path);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            urlConnection.connect();
            File file = new File(local_file_path, file_name);

            FileOutputStream fileOutput = new FileOutputStream(file);

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

    void showError(final String error){
        fragment.getSavedActivity().runOnUiThread(new Runnable() {
            public void run() {
                fragment.torch.torch(error);
            }
        });
    }
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }


    private void notifyListener(String message) {
        if (error) {
            server_file_listener.onServerError( this.error_msg );
        }
        else {
            if (message.contains("upload")) {
                server_file_listener.onFileUploaded(message);
            }
            else if (message.contains("download")) {
                server_file_listener.onFileDownloaded(message);
            }
        }
    }
}
