package codewrencher.gifit.objects.complex;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.interfaces.ServerResultsListener;
import codewrencher.gifit.objects.complex.gif_chain.GifChain;
import codewrencher.gifit.objects.simple.rendered_layout.AppNotification;
import codewrencher.gifit.objects.simple.rendered_layout.RenderedLayout;
import codewrencher.gifit.ui.MainActivity;

import static java.lang.String.format;

/**
 * Created by Gene on 4/2/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class AppNotificationDrawer extends RenderedLayout implements ServerResultsListener {

    private ArrayList <AppNotification> app_notifications;
    private LinkedHashMap<String, GifChain> gif_chains;

    private RenderedLayout notification_counter;

    private String load_type;
    private String current_gif_chain_id;    /** User for updating a notification UI on server result */

    /***********************************************************************************************
     * Constructor
     * @param activity (Activity)   : Current main activity
     * @param layout_id (int)       : The id of the layout this object populates
     * @param container_id (int)    : The id of the parent View into which this layout is inserted
     * @param container_view (View) : The parent View into which this layout is inserted
     */
    public AppNotificationDrawer( MainActivity activity, int layout_id, int container_id, View container_view ) {
        super(activity, layout_id, container_id, container_view);

        app_notifications = new ArrayList<>();
        gif_chains = new LinkedHashMap<>();
    }

    public LinkedHashMap<String, GifChain> getGifChains() {
        return this.gif_chains;
    }
    /**---------------------------------------------------------------------------------------------
     * Create
     */
    /***********************************************************************************************
     * Create the UI object - Invoked explicitly by parent class
     * Populate the layout
     * Take care of any animations and actions
     */
    public void create() {

        this.loadLayout();
        this.retrieveNotifications();
    }
    /**---------------------------------------------------------------------------------------------
     * MAIN actions Populate / Remove
     */
    /***********************************************************************************************
     * Populates all of the UI components with their data
     */
    protected void populateNotifications( String server_result ) {

        gif_chains = this.createGifChains( server_result );
        this.loadLayoutNotifications(gif_chains);
        this.loadLayoutNotificationCounter();
    }

    /***********************************************************************************************
     * Adds a single new notification to the populated layout
     */
    private void addSingleNotification( String server_result ) {

        GifChain gif_chain = this.createSingleGifChain(server_result);
        this.addLayoutNotification(gif_chain);
        this.updateLayoutNotificationCounter();


    }

    /***********************************************************************************************
     * Remove notification from UI layout and update the database,
     * Flagging it as viewed
     */
    public  void removeNotification( GifChain gif_chain ) {

        this.current_gif_chain_id = gif_chain.getId();
        this.updateUserGifChainAction("viewed", gif_chain.getId());
    }

    /**---------------------------------------------------------------------------------------------
     * SEVER Contact - get / send data
     */
    /***********************************************************************************************
     * Go to the server and retrieve the gif chains / notifications that have been received by you
     */
    private void retrieveNotifications() {
        this.load_type = "get_all";  /** Used to decide how to parse the response after the server response is returned */

        SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String my_user_id = shared_preferences.getString("user_id", "");

        String param_string = format("database=gifit" +
                                     "&command=%s" +
                "&user_id=%s", this.load_type, my_user_id);

        initiateServerConnection( "/GetUserGifChains.php", param_string );
    }
    /***********************************************************************************************
     * Retrieve a single gif chain / notifications from the server using the gif chain's id
     * @param gif_chain_id the id to pass to the server
     */
    public void retrieveSingleNotification( String gif_chain_id ) {
        this.load_type = "get_single";  /** Used to decide how to parse the response after the server response is returned */

        SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String my_user_id = shared_preferences.getString("user_id", "");

        String param_string = format("database=gifit" +
                                     "&command=%s" +
                                     "&gif_chain_id=%s" +
                                     "&user_id=%s", this.load_type, gif_chain_id, my_user_id);

        initiateServerConnection( "/GetUserGifChains.php", param_string );
    }
    /***********************************************************************************************
     * Update the gif_chain status of a notification in the server-side database
     * @param user_chain_action : a field in the LKP_USER__GIF_CHAIN table
     */
    private void updateUserGifChainAction( String user_chain_action, String gif_chain_id ) {
        this.load_type = "update_user_chain_action";  /** Used to decide how to parse the response after the server response is returned */

        SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String my_user_id = shared_preferences.getString("user_id", "");

        String param_string = format("database=gifit" +
                "&command=%s" +
                "&gif_chain_id=%s" +
                "&user_id=%s" +
                "&user_chain_action=%s", this.load_type, gif_chain_id, my_user_id, user_chain_action);

        initiateServerConnection("/UpdateGifChain.php", param_string);
    }

    /**---------------------------------------------------------------------------------------------
     * HANDLING Server Response and building UI
     */
    /***********************************************************************************************
     * Creates Gif Chain objects from parsing a Json string returned from the server
     * @param server_result (String)    :  The Json string returned from the server
     * @return (ArrayList <GifChain>)   :  An array of Gif Chain objects
     */
    protected LinkedHashMap<String, GifChain> createGifChains( String server_result ) {
        LinkedHashMap<String, GifChain> gif_chains = new LinkedHashMap<>();

        try {
            JSONArray json_array = new JSONArray( server_result );

            for( int i=0; i < json_array.length(); i++ ) {

                JSONObject gif_chain_object = json_array.getJSONObject(i);

                GifChain gif_chain = new GifChain( gif_chain_object );
                gif_chain.setActivity( activity );
                gif_chain.setFragment( activity.getCurrentFragment() );
                gif_chain.createObjects(gif_chain_object);

                gif_chains.put( gif_chain.getId(), gif_chain );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return gif_chains;
    }

    /***********************************************************************************************
     * Creates a single Gif Chain object from parsing a Json string returned from the server
     * @param server_result (String)    :  The Json string returned from the server
     * @return                          :  A single Gif Chain object
     */
    private GifChain createSingleGifChain( String server_result ) {
        Log.d("NOTIFICATION", "CREATING CHAIN");

        GifChain gif_chain = new GifChain();

        try {
            JSONObject json_obj = new JSONObject( server_result );

            gif_chain = new GifChain( json_obj );
            Log.d("GIF CHAIN ID", gif_chain.getId());
            gif_chain.setActivity( activity );
            gif_chain.setFragment( activity.getCurrentFragment() );
            gif_chain.createObjects(json_obj);

            gif_chains.put( gif_chain.getId(), gif_chain );
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return gif_chain;
    }

    /***********************************************************************************************
     * Creates each notification UI element
     * Stores a list of notification objects in a class Array List
     * @param gif_chains (ArrayList<GifChain>) : The list of Gif Chain objects created from the server response
     */
    private void loadLayoutNotifications( LinkedHashMap<String, GifChain> gif_chains ) {

        for ( String gif_chain_id : gif_chains.keySet() ) {

            AppNotification app_notification = new AppNotification(activity, R.layout.item_app_notification, R.id.drawer_item, this.rendered_view);
            GifChain gif_chain = gif_chains.get(gif_chain_id);

            app_notification.setGifChain(gif_chain);
            gif_chain.setParentObject( app_notification );

          //  app_notification.setIndex( this.app_notifications.size() );
            app_notification.create();

            this.app_notifications.add( app_notification );
        }

        this.checkForGifChainsToBeViewed();
    }

    /***********************************************************************************************
     * Adds a single new notification object to the layout
     * @param gif_chain the Gif Chain object to create the new layout item from
     */
    private void addLayoutNotification( GifChain gif_chain ) {



        AppNotification app_notification = new AppNotification( activity, R.layout.item_app_notification, R.id.drawer_item, this.rendered_view );

        app_notification.setGifChain(gif_chain);
        gif_chain.setParentObject( app_notification );
        //app_notification.setIndex(this.app_notifications.size());
        app_notification.create();

        this.app_notifications.add(app_notification);

        this.checkForGifChainsToBeViewed();
        activity.fragment_pager.notifyDataSetChanged();
    }
    /**
     * Invoked after server response is returned
     * Check server response and if successful, remove the notification from the UI and
     * Update the notification counter
     * @param server_result : string Json formatter string;
     */
    private void removeNotificationFromUI( String server_result ) {
        try {

            JSONObject json_obj = new JSONObject( server_result );

            if ( json_obj.getString("result_status") != null ) {
                if ( json_obj.getString("result_status").equals("success") ) {

                    for ( AppNotification notification : this.app_notifications ) {

                        if ( notification.getGifChain().getId().equals( this.current_gif_chain_id ) ) {

                            notification.destroyUI();
                            this.app_notifications.remove(notification );
                            break;
                        }
                    }
                    this.updateLayoutNotificationCounter();
                }
            }

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**---------------------------------------------------------------------------------------------
     * UPDATING dependent elements - the Notification Counter
     */
    /***********************************************************************************************
     * Creates the Notification Drawer opener button displaying the number of new notifications
     */
    private void loadLayoutNotificationCounter() {
        if (app_notifications.size() > 0) {

            this.notification_counter = new RenderedLayout(activity, R.layout.item_app_notification_counter, R.id.drawer_trigger, this.rendered_view);
            this.notification_counter.create();

            TextView notification_counter_text = (TextView) notification_counter.getRenderedView().findViewById(R.id.notification_counter_text);
            notification_counter_text.setText(String.valueOf( app_notifications.size()) );
        }
        else {
            if ( this.notification_counter != null ) {
                TextView notification_counter_text = (TextView) notification_counter.getRenderedView().findViewById(R.id.notification_counter_text);
                notification_counter_text.setText("");

                this.notification_counter.getRenderedView().setVisibility(View.INVISIBLE);
            }
        }
    }

    /***********************************************************************************************
     * Updates the notification counter with the new notification count
     */
    private void updateLayoutNotificationCounter() {
        Log.d("NOTIFICATION", "UPDATING");
        if (app_notifications.size() > 0) {

            if ( this.notification_counter != null ) {
                this.notification_counter.getRenderedView().setVisibility(View.VISIBLE);

                TextView notification_counter_text = (TextView) notification_counter.getRenderedView().findViewById(R.id.notification_counter_text);
                notification_counter_text.setText(String.valueOf(app_notifications.size()));
            }
        }
        else {
            if ( this.notification_counter != null ) {
                TextView notification_counter_text = (TextView) notification_counter.getRenderedView().findViewById(R.id.notification_counter_text);
                notification_counter_text.setText("");

                this.notification_counter.getRenderedView().setVisibility(View.INVISIBLE);
            }
        }
    }

    public void goToGif(String gif_chain_id) {

        GifChain gif_chain  = this.gif_chains.get( gif_chain_id );
        gif_chain.setChainType("downloaded");

        activity.gif_space_on = true;
        activity.setAction("downloaded");

        /** this is the key to loading the frames
         It contains all of the frame and other data */
        activity.setReceivedGifChain(gif_chain);

  //      container_view.findViewById(R.id.main_notification_counter_image).callOnClick();    // close the notification drawer

        LinkedHashMap<String, String> gif_space_params = new LinkedHashMap<>();
        gif_space_params.put("action", "downloaded") ;

        this.activity.getAppNotificationDrawer().removeNotification(gif_chain);
        activity.getCurrentFragment().openFragment("gif_space", 1, gif_space_params);
    }
    private void checkForGifChainsToBeViewed() {

        if ( activity.getAction() != null && activity.getAction().equals("go_to_gif")) {
            if ( activity.getGoToGifChainId() != null ) {

                this.goToGif(activity.getGoToGifChainId());
            }
        }

    }
    /**---------------------------------------------------------------------------------------------
     * Animate
     */
    /***********************************************************************************************
     * Take care of all of the layout animations
     * Take cate of all the layout interaction listeners
     * Invoked after the layout data is fully loaded
     */
    protected void animate() {
        this.setNotificationDrawerAnimations();
    }

    /***********************************************************************************************
     * Handle Notification Drawer opener on click listener
     * Animate the drawer:
     *                      Open it
     *                      Close it
     */
    private void setNotificationDrawerAnimations() {
        if ( notification_counter != null ) {
            ImageButton notification_counter_view = (ImageButton) notification_counter.getRenderedView().findViewById(R.id.main_notification_counter_image);
            final LinearLayout notification_drawer = (LinearLayout) rendered_view.findViewById(R.id.drawer_container);

            notification_counter_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!notification_drawer.isShown()) {

                        animator.setAnimation(notification_drawer, "expand_horizontal");
                        animator.animate(notification_drawer);
                        animator.expand("horizontal", notification_drawer, 1000);
                    } else if (notification_drawer.isShown()) {

                        animator.setAnimation(notification_drawer, "collapse_horizontal");
                        animator.animate(notification_drawer);
                        animator.collapse("horizontal", notification_drawer, 1000);
                    }
                }
            });
        }
    }

    @Override
    public void onServerResultReturned(final String result) {

        // populate your layout in a new thread (there may be many items to load)
        this.rendered_view.post(new Runnable() {
            public void run() {

                switch ( load_type ) {

                    case "get_all" :
                        populateNotifications(result);
                        animate();
                        break;

                    case "get_single" :
                        addSingleNotification(result);
                        break;

                    case "update_user_chain_action" :
                        removeNotificationFromUI(result);
                        break;
                }
            }
        });

    }

    @Override
    public void onServerRequestCompleted(String result_type, String result_msg) {

    }


}
