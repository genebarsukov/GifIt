package codewrencher.gifit.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import codewrencher.gifit.R;
import codewrencher.gifit.ui.MainActivity;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Receiver";

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("RECEIVED INTENT", "GCM");
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendUINotification("Error", "Send error: ", extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendUINotification("Messages Deleted", "Deleted messages on server: ", extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                decodeMessage(extras);
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
    private void decodeMessage(Bundle extras) {

        String title = extras.getString("title");
        String message = extras.getString("message");
        String gif_chain_id = extras.getString("gif_chain_id");

        Log.d("TITLE title", title);
        Log.d("MESSAGE message", message);
        Log.d("GIF CHAIN ID", gif_chain_id);

        if (message != null) {
            sendUINotification(title, message, gif_chain_id);
        }
    }

    // Put the message into a main_image and post it.
    private void sendUINotification(String title, String message, String gif_chain_id) {

        Intent updated_intent = new Intent(this, MainActivity.class);
        updated_intent.putExtra("instruction", "download_gif");
        updated_intent.putExtra("gif_chain_id", gif_chain_id);

        PendingIntent pending_intent = PendingIntent.getActivity( this, 0, updated_intent, PendingIntent.FLAG_UPDATE_CURRENT );

        NotificationCompat.Builder notification_builder = new NotificationCompat.Builder(this)
                        .setSmallIcon( R.mipmap.incoming_gif )
                        .setLargeIcon( BitmapFactory.decodeResource( getResources(), R.mipmap.gifit ) )
                        .setContentTitle( title )
                        .setStyle( new NotificationCompat.BigTextStyle().bigText(message) )
                        .setContentText( message )
                        .setContentIntent( pending_intent )
                        .setAutoCancel( true );

        /** Build the global Wakeful Notification that will launch the app on click */
        NotificationManager notification_manager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        notification_manager.notify(NOTIFICATION_ID, notification_builder.build());

        /** Build the local Broadcast that will simply update the Notification Drawer and counter */
        Intent local_broadcast_intent = new Intent("received-new-gif");
        local_broadcast_intent.putExtra("gif_chain_id", gif_chain_id);

        LocalBroadcastManager.getInstance(this).sendBroadcast( local_broadcast_intent );
    }
}