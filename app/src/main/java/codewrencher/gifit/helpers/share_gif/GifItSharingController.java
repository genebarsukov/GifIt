package codewrencher.gifit.helpers.share_gif;

import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.interfaces.ServerResultsListener;
import codewrencher.gifit.helpers.async.interfaces.SharingHelperListener;
import codewrencher.gifit.helpers.async.server_connectors.ServerConnector;
import codewrencher.gifit.helpers.db.DEF_REGISTERED_CONTACT;
import codewrencher.gifit.objects.complex.gif_chain.Frame;
import codewrencher.gifit.objects.complex.gif_chain.GifChain;
import codewrencher.gifit.services.GcmController;
import codewrencher.gifit.tools.Printer;
import codewrencher.gifit.tools.ToolBox;
import codewrencher.gifit.ui.MainActivity;
import codewrencher.gifit.ui.fragments.BaseFragment;
import codewrencher.gifit.ui.fragments.GifSpaceFragment;

import static java.lang.String.format;

/**
 * Created by Gene on 3/26/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class GifItSharingController implements ServerResultsListener,
                                               SharingHelperListener {

    private static final String tag = "GifItSharingController";

    private BaseFragment fragment;
    private LoginHandler login_handler;
    private SharingMenuHandler sharing_menu_handler;
    private InviteMenuHandler invite_menu_handler;
    private SearchMenuHandler search_menu_handler;
    private String pending_appearance_window;
    private Boolean open_window;
    private SharingManager old_sharing_manager;
    private String last_saved_gif_path;
    private ArrayList <String> selected_to_share_user_ids;
    public Boolean update_contact_lists;
    private GifChain gif_chain;

    public GifItSharingController( BaseFragment fragment, GifChain gif_chain ) {
        this.fragment = fragment;

        //this.login_handler = new LoginHandler( fragment, gif_chain, this );
        //this.sharing_menu_handler = new SharingMenuHandler( fragment, gif_chain, this );
        //this.invite_menu_handler = new InviteMenuHandler( fragment, gif_chain, this );
        //this.search_menu_handler = new SearchMenuHandler( fragment, gif_chain, this );

        this.pending_appearance_window = "";
        this.open_window = false;
        this.gif_chain = gif_chain;
    }

    public Boolean getOpenWindow() {
        return this.open_window;
    }
    public void setOpenWindow(Boolean open_window) {
        this.open_window = open_window;
    }

    /***********************************************************************************************
     * Main method that launches all the sharing windows
     * Invoked from the GifSpace fragment upon sharing a Gif
     */
    public void shareGif() {
        /* code for using advanced sharing
        Intent email_intent = this.sharing_menu_handler.getShareImageIntent(this.sharing_menu_handler.getInviteEmailArray() );
        fragment.getActivity().startActivity(email_intent);
        */
        // share using the old sharing manager for now
        old_sharing_manager = new SharingManager(fragment);
        File gif_file = new File( ( (GifSpaceFragment) fragment ).getGifChain().getGif().getDownloadFilePath() +
                "/" +  ((GifSpaceFragment) fragment ).getGifChain().getGif().getName() );
        old_sharing_manager.openShareImageDialog(gif_file);

       // this.login_handler.checkLoginStatus();
    }

    /***********************************************************************************************
     * Register your device with the Google Cloud Messaging Service - Required for Notifications
     * Currently the gcm id is checked for in local preferences
     * Then a new gcm id is obtained and stored on the backend. The backed is not checked secondly
     * The gcm ids on the server backend are used to select users to send notifications to
     */
    protected void registerGcm() {

        GcmController gcm_controller = new GcmController(fragment);
        gcm_controller.setContext(fragment.context);
        gcm_controller.setActivity(fragment.getSavedActivity());

        gcm_controller.registerInBackground();
    }

    /***********************************************************************************************
     * Send your buddies notifications that your Gif is on its way to them
     * The gif frames get uploaded
     * The frame file names get saved in the database
     * They are tied to the sent Gif id and unique file name
     * The Gif file name is delivered to the receiving user(s) via notification
     * They then hit the database to find the frame names associated with the Gif
     * The frames get downloaded and used to make a chained Gif
     *
     * The frame file names are also sent along during the Notification Server contact and are
     * used to update the frame database at this time
     *
     * @param gif_chain GifChain: The Gif Chain you are sharing
     * @throws JSONException
     */
    private void sendGifNotification( GifChain gif_chain, ArrayList<String> selected_to_share_user_ids ) throws JSONException {

        JSONArray user_id_array = new JSONArray();
        JSONObject message_object = new JSONObject();

        JSONArray frame_array = new JSONArray();
        JSONObject gif_object = new JSONObject();

        // build user id Json
        for (String user_id : selected_to_share_user_ids) {
            user_id_array.put(user_id);
        }
        // build message Json
        message_object.put( "title", "GifIt" );
        message_object.put( "message", "You Have Received a new Gif!" );

        // build frames Json
        for ( Frame frame : gif_chain.getFrames() ) {

            if ( frame.getAge().equals("new") ) {   /** Only send the frames you just created */

                if ( frame.getVideoFrame().getState().equals("on") ) { /** Only send frames which are still on */

                    JSONObject frame_object = new JSONObject();
                    frame_object.put("name", frame.getName());
                    frame_array.put(frame_object);
                }
            }
        }
        // build gif Json
        gif_object.put( "name", gif_chain.getGif().getName() );
        /**
         * @ToDo pass along the previous gif_chain_id if extending a gif chain
         * Don't pass along anything if creating a new chain
         * It will be null server_side;
         */
        String sender_id = fragment.shared_preferences.getString("user_id", "");
        String frame_string = frame_array.toString();
        String user_id_string = user_id_array.toString();
        String message_string = message_object.toString();
        String gif_string = gif_object.toString();

        String url = MainActivity.BASE_SERVER_URL + "/SendPushNotification.php";
        String post_string = format("gif_chain_id=%s" +
                                    "&sender_id=%s" +
                                    "&gif_chain_name=%s" +
                                    "&gif=%s" +
                                    "&user_ids=%s" +
                                    "&message=%s" +
                                    "&frames=%s",
                                    gif_chain.getId(),
                                    sender_id,
                                    gif_chain.getName(),
                                    gif_string,
                                    user_id_string,
                                    message_string,
                                    frame_string);

        String[] connection_params = new String[]{ url, post_string, MainActivity.BASIC_AUTH_USER, MainActivity.BASIC_AUTH_PASSWORD };

        Log.d("--------------", "-----------------------------");
        Log.d( "POST STRING", post_string );

        ServerConnector server_connector = new ServerConnector();
        server_connector.registerListener(this);
        server_connector.setResultType("sending_notification");
        server_connector.setResultMsg("complete");

        server_connector.execute( connection_params );
    }

    /***********************************************************************************************
     * Gets a list of file names from a list of file paths by splitting on the last /
     * @param frame_paths   ArrayList <String>: list if gif frames that were locally saved
     * @return  ArrayList <String>: list of frame names
     */
    private ArrayList<String> getFrameNamesFromFramePaths( ArrayList<String> frame_paths ) {
        ArrayList <String> frame_names = new ArrayList<>();

        for ( String path : frame_paths ) {

            frame_names.add(ToolBox.getFileNameFromFilePath( path ) );
        }
        return frame_names;
    }
    /***********************************************************************************************
     * The ImageItems got updated with the full file paths when they were saved. Get these
     * @return ArrayList <String>: saved frame paths
     */
    private ArrayList<String> getGifFramePathsFromImageItems() {
        ArrayList <String> gif_frame_paths = new ArrayList<>();

        for ( Frame frame : ( (GifSpaceFragment) fragment).getGifChain().getFrames() ) {
            if ( frame.getVideoFrame().getImagePath() != null ) {

                gif_frame_paths.add( frame.getVideoFrame().getImagePath() );
            }
        }
        return gif_frame_paths;
    }
    public void hideNotificationCounter() {
        View notification_counter = fragment.getSavedActivity().findViewById(R.id.notification_counter);
        if ( notification_counter != null ) {
            fragment.getSavedActivity().findViewById(R.id.notification_counter).setVisibility(View.GONE);
        }
    }
    public void showNotificationCounter() {
        View notification_counter = fragment.getSavedActivity().findViewById(R.id.notification_counter);
        if ( notification_counter != null ) {
            fragment.getSavedActivity().findViewById(R.id.notification_counter).setVisibility(View.VISIBLE);
        }
    }
    /***********************************************************************************************
     * Update the local database with records of registered users that you found by searching
     * @param new_registered_contacts Object: A hash mpa of user credential objects
     */
    public void insertNewRegisteredContactsIntoLocalDB( LinkedHashMap <String, LinkedHashMap <String, String>> new_registered_contacts ) {

        fragment.db_accessor.open();

        Printer.printBreak();

        for ( String user_id : new_registered_contacts.keySet() ) {
            Printer.printHashMap( new_registered_contacts.get(user_id), "INSERT_RECORDS" );
            Log.d("INSERTING CONTACT ID", user_id);
            Log.d("INSERTING CONTACT ID", new_registered_contacts.get(user_id).get("first_name"));
            Log.d("INSERTING CONTACT ID", new_registered_contacts.get(user_id).get("last_name"));
            Log.d("INSERTING CONTACT ID", new_registered_contacts.get(user_id).get("phone_number"));
            fragment.db_accessor.insertRecord(DEF_REGISTERED_CONTACT.TABLE_NAME, new_registered_contacts.get(user_id));
        }
        fragment.db_accessor.close();
    }

    /***********************************************************************************************
     * Update the user contacts that are available for app-to-app gif sharing
     * Update the local friends database
     * Update the UI
     * Happens when the user adds a new contact that has a registered account
     * For example when searching for and adding a user
     */
    private void updateUserRegisteredContacts() {
        // update the local SQLite database
        //this.insertNewRegisteredContactsIntoLocalDB(this.search_menu_handler.getFoundContactList() );

        // merge the found_contact_list   ==>>   registered_contact_list
        //LinkedHashMap registered_list = this.sharing_menu_handler.getRegisteredContactList();
        /*
        for ( String user_id : this.search_menu_handler.getFoundContactList().keySet() ) {

            if ( registered_list.get( user_id ) == null ) {

                LinkedHashMap <String, String> contact = this.search_menu_handler.getFoundContactList().get( user_id );
                if ( contact.get("selected") != null && contact.get("selected").equals("true") ) {

                    registered_list.put( user_id, contact );
                }
            }
        }*/
       // this.sharing_menu_handler.setRegisteredContactList(registered_list);

        // update the available sharing friends UI
       // this.sharing_menu_handler.buildSharingMenu();
    }
    // async listeners -----------------------------------------------------------------------------
    @Override
    public void onServerResultReturned(String result) {}

    @Override
    public void onServerRequestCompleted(String result_type, String result_msg) {

        if ( result_type.equals("sending_notification") ) {

            if ( result_msg.equals("complete") ) {

                this.fragment.getSavedActivity().hideLoading();

                this.fragment.torch.statusBlink("Gifs Successfully Shared!");

                ((GifSpaceFragment) this.fragment).destroy();
            }
        }
    }

    // helper callbacks listeners ------------------------------------------------------------------
    @Override
    public void onWindowClosed(String window_type, String message) {
        /*
        if (message.equals("open_invite_menu")) {
            if ( this.pending_appearance_window.equals("invite_menu") ) {
                this.invite_menu_handler.showContactsWindow();
            }
        }
        else if (message.equals("reopen_sharing_menu")) {
            if ( this.pending_appearance_window.equals("sharing_menu") ) {
                this.sharing_menu_handler.buildSharingMenu();
            }
        }
        else if (message.equals("finished_sharing")) {

            this.fragment.getSavedActivity().showLoading( "Sharing Gif", "blue" );
            this.showNotificationCounter();
        }
        */
    }

    @Override
    public void onWindowClosed(String window_type, String message, String param) {

        if (message.equals("open_search_window")) {
            if ( this.pending_appearance_window.equals("search_menu") ) {
                //this.search_menu_handler.showSearchResultsWindow(param);
            }
        } else if (message.equals("reopen_sharing_menu")) {
            if ( this.pending_appearance_window.equals("sharing_menu") ) {
                if ( param.equals("update") ) {

                    //this.updateUserRegisteredContacts();
                }

            }
        }
    }

    /***********************************************************************************************
     * Process after login if successful
     * @param param String: window type identifier
     * @param message String: success or failure
     *
     * @ToDO: Make sure the user is actually logged in before proceeding to this step
     */
    @Override
    public void onLoginSuccessful(String param, String message) {

        //this.registerGcm();
        //this.invite_menu_handler.buildContactList();
    }

    /***********************************************************************************************
     * Invoked ftom the handlers
     * Since a lot of the Controller callbacks are called when the current window handler closes its
     * window, this helps the controller know which window to open adfter the recently closed one
     * @param window_type String: the window type identifier describing one of the window handlers
     */
    @Override
    public void onSetPendingAppearance(String window_type) {
        this.pending_appearance_window = window_type;
    }

    /***********************************************************************************************
     * This guy is responsible for saving the Gif Frames in the Scatter Shot Directory
     * Invoked when a sharing swipe or button click is detected
     * Gif gets uploaded
     * Frames get saved
     * Frames get uploaded right after in the onActionCompleted callback
     * */
    @Override
    public void onSharingActionDetected(ArrayList<String> selected_to_share_user_ids) {

        /** Close the sharing menu and let the rest be taken care of in the background */
        this.onSetPendingAppearance("");
        //this.sharing_menu_handler.animateWindow( this.sharing_menu_handler.sharing_menu_view, "slide_down_finished" );

        /** Before uploading, set the names of the new gif chain and new frames */
        /*
        this.gif_chain.saveFrames();

    //    this.fragment.file_manager.saveGifFramesInLine( gif_chain.getFrames(), gif_chain.getName() );

        gif_chain.calculateRelativeUploadAblesCount();
        gif_chain.uploadGif();
        gif_chain.uploadFrames();
        try {
            this.sendGifNotification( this.gif_chain, selected_to_share_user_ids );
        }
            catch (JSONException e) {
                e.printStackTrace();
            }
        */
    }

    /***********************************************************************************************
     * This onListener method mainly serves as a callback when done uploading
     * or downloading to the server
     * Here we send the frame files to the server and simultaneously send out a notification to all
     * the users the Gif was shared with
     * @param action_type String: returns the action previously set for the Subject right before embarking on an async task
     * @param parameter String: Sometimes this is a file name returned from the server
     * @param message String: Whatever other info you want to pass along. Errors go here and get checked for here
     */
    @Override
    public void onActionCompleted(String action_type, String parameter, String message) {
        if (action_type.equals("upload_frames")) {

            fragment.torch.torch("Files Uploaded. Notifying Recipients");
        }
        /*
        else if (action_type.equals( "invite_menu" )) {
            if (message.equals("contact_list_built")) {

                Log.d("CONTACTS BUILT", "contact_list_built");
                */
                /** builds the class registered contact list variable from the database and returns true if it is populated */
        /*
                if (! this.sharing_menu_handler.getStoredRegisteredContacts() ) { // if we didnt find anything locally, build the list the hard way
                    Log.d("DB RECORDS", "could not GET");
                    this.sharing_menu_handler.setContactList(this.invite_menu_handler.getContactList());
                    this.sharing_menu_handler.buildSharingMenu();
                }
                else {  //otherwise proceed with the database data
                    Log.d("DISPLAYING ", "SHARING MENU");
                    this.sharing_menu_handler.displaySharingMenu();
                }
            }
        }*/
    }
}
