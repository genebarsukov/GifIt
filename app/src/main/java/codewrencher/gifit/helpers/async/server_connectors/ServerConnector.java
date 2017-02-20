package codewrencher.gifit.helpers.async.server_connectors;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import codewrencher.gifit.helpers.async.interfaces.ServerResultsListener;
import codewrencher.gifit.ui.MainActivity;

/**
 * Created by Gene on 5/3/2015.
 */
public class ServerConnector extends AsyncTask<String, Void, String> {
    public static final String TAG = "Network Connect";
    public static final String connection_error = "Encountered A Connection Error :(";
    private String result_type;
    private String result_msg;
    private ProgressBar progress_bar;

    String parsing_action;

    public ServerResultsListener listener;

    public ServerConnector() {}

    public void registerListener(ServerResultsListener listener) {
        this.listener = listener;
    }
    public void setResultType(String result_type) {
        this.result_type = result_type;
    }
    public void setResultMsg(String result_msg) {
        this.result_msg = result_msg;
    }
    public void setProgressBar( ProgressBar progress_bar ) {
        this.progress_bar = progress_bar;
    }

    @Override
    protected String doInBackground(String... params) {
        String url_string = params[0];
        String param_string = params[1];
        String auth_user = MainActivity.BASIC_AUTH_USER;
        String auth_pass = MainActivity.BASIC_AUTH_PASSWORD;

        try {
            return loadFromNetwork(url_string, param_string, auth_user, auth_pass);
        } catch (IOException e) {
            return connection_error;
        }
    }

    // ---------------------------------------------------------------------------------------------
    private String loadFromNetwork(String url_string, String param_string, String auth_user, String auth_pass) throws IOException {
        InputStream stream = null;
        String result ="";

        try {
            stream = downloadUrl(url_string, param_string, auth_user, auth_pass);
            result = readIt(stream, 100000);
        } finally {

            if (stream != null) {
                stream.close();
            }
        }
        return result;
    }
    private InputStream downloadUrl(String url_string, String post_string, String auth_user, String auth_pass) throws IOException {
        URL url = new URL(url_string);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(100000 /* milliseconds */);
        conn.setConnectTimeout(150000 /* milliseconds */);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);

        String auth_sting = auth_user + ":" + auth_pass;
        String basicAuth = "Basic " + new String(Base64.encode(auth_sting.getBytes(), Base64.NO_WRAP));
        conn.setRequestProperty ("Authorization", basicAuth);
        OutputStreamWriter out = new OutputStreamWriter(
                conn.getOutputStream());

        out.write(post_string);
        out.close();
        // Start the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }
    // ---------------------------------------------------------------------------------------------
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
    @Override
    protected void onPreExecute() {
        if ( this.progress_bar != null ) {
            progress_bar.setVisibility(View.VISIBLE);
        }
    }
    @Override
    protected void onPostExecute(String result) {
        if ( this.progress_bar != null ) {
            progress_bar.setVisibility(View.GONE);
        }
        Log.i(TAG, result);
        listener.onServerResultReturned(result);
        listener.onServerRequestCompleted(this.result_type, this.result_msg);
    }


}
