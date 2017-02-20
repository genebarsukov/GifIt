package codewrencher.gifit.services;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import codewrencher.gifit.helpers.async.server_connectors.ServerConnector;
import codewrencher.gifit.helpers.async.interfaces.ServerResultsListener;
import codewrencher.gifit.ui.MainActivity;
import codewrencher.gifit.ui.fragments.BaseFragment;

import static java.lang.String.format;

/**
 * Created by Gene on 7/5/2015.
 */
public class GcmController extends AsyncTask<String, Void, String> implements ServerResultsListener {
    public static final String PROPERTY_GCM_ID = "gcm_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    String SENDER_ID = "22547332432";
    static final String TAG = "GCM Controller";

    BaseFragment fragment;
    GoogleCloudMessaging gcm;
    Context context;
    View view;
    Activity activity;
    String[] connection_params;
    String gcm_id;
    public ServerResultsListener listener;

    public GcmController(BaseFragment fragment) {
        this.fragment = fragment;
    }
   // public ServerResultsListener listener;
    public void setOnResultsListener(ServerResultsListener listener) {
        this.listener = listener;
    }

    public void setContext(Context context) {
        this.context = context;
    }
    public void setView(View view) {
        this.view = view;
    }
    public void setActivity(Activity activity) {
        this.activity = activity;
    }
    public String getGcmId() {
        return this.gcm_id;
    }

    public void setConnectionParams(String[] connection_params) {
        this.connection_params = connection_params;
    }
    public String[] getConnectionParams() {
        return this.connection_params;
    }
    private String getRegistrationIdLocally(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_GCM_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    @Override
    protected String doInBackground(String... params) {
        setConnectionParams(params);
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(context);
            gcm_id = getRegistrationIdLocally(context);
            if (gcm_id.isEmpty()) {
                try {
                    getRegistrationIdFromBackEnd();
                } finally {
                }
            }
            if (gcm_id.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        return null;
    }
    private void storeRegistrationId(Context context, String regid) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_GCM_ID, gcm_id);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
    }
    private void sendRegistrationIdToBackend() {
        String url = MainActivity.BASE_SERVER_URL + "/RegisterGcmUserDevice.php";
        String user_id = fragment.shared_preferences.getString("user_id", "0");
        String gcm_id = String.valueOf(getGcmId());
        String post_string = format("user_id=%s&gcm_id=%s", user_id, gcm_id);

        String[] connection_params = new String[]{ url, post_string, MainActivity.BASIC_AUTH_USER, MainActivity.BASIC_AUTH_PASSWORD };

        ServerConnector server_connector = new ServerConnector();
        server_connector.registerListener(this);
        server_connector.execute(connection_params);
    }
    private void getRegistrationIdFromBackEnd () {
    }
    public void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    gcm_id = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + gcm_id;
            //        getTransactionController().getSelf().setGcmId(gcm_id);
                    storeRegistrationId(context, gcm_id);
                    sendRegistrationIdToBackend();

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d(TAG, msg);
            }

        }.execute(null, null, null);
    }
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    private SharedPreferences getGcmPreferences(Context context) {
        return activity.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    @Override
    public void onServerResultReturned(String result) {
        if (result != null) {
            if (!result.isEmpty()) {
                gcm_id = result;
            }
        }

    }

    @Override
    public void onServerRequestCompleted(String result_type, String result_msg) {

    }


}
