package codewrencher.gifit.helpers.share_gif;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import org.json.JSONArray;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.JsonParser;
import codewrencher.gifit.helpers.async.interfaces.AnimatorListener;
import codewrencher.gifit.helpers.async.interfaces.ServerResultsListener;
import codewrencher.gifit.helpers.db.DEF_REGISTERED_CONTACT;
import codewrencher.gifit.objects.complex.gif_chain.GifChain;
import codewrencher.gifit.objects.scrollers.SelectListScroller;
import codewrencher.gifit.objects.simple.ParsedResponse;
import codewrencher.gifit.tools.LayoutBuilder;
import codewrencher.gifit.ui.fragments.BaseFragment;

import static java.lang.String.format;

/**
 * Created by Gene on 3/27/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class SharingMenuHandler extends WindowHandler implements ServerResultsListener, AnimatorListener {

    private static final String tag = "SharingMenuHandler";

    protected View sharing_menu_view;
    // UI listeners --------------------------------------------------------------------------------
    private View.OnClickListener add_peeps_click_listener;
    private View.OnClickListener share_gif_click_listener;

    /***********************************************************************************************
     * Constructor
     * @param fragment
     * @param controller
     */
    public SharingMenuHandler( BaseFragment fragment, GifChain gif_chain, GifItSharingController controller ) {
        super( fragment, gif_chain, controller );
    }

    /***********************************************************************************************
     * Set listeners for all of the main UI buttons
     * Add / Invite Peeps
     * Share
     * Close
     */
    private void setUIGestureListeners() {
        // open the invite friends window ---------------------
        add_peeps_click_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onSetPendingAppearance("invite_menu");
                controller.setOpenWindow(false);

                animateWindow(sharing_menu_view, "slide_up");
            }
        };
        // see if any peeps were selected to share notify controller if so -------------------------
        share_gif_click_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleGifSharingAction();
            }
        };

        ImageButton add_peeps = (ImageButton) sharing_menu_view.findViewById(R.id.add_peeps);
        ImageButton share_gif = (ImageButton) sharing_menu_view.findViewById(R.id.share_gif);

        add_peeps.setOnClickListener(add_peeps_click_listener);
        share_gif.setOnClickListener(share_gif_click_listener);
     //   this.setCloseListener(sharing_menu_view, "slide_down", "");

        ImageButton close = (ImageButton) sharing_menu_view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard();
                controller.onSetPendingAppearance("");
                animateWindow(sharing_menu_view, "slide_down");
                controller.showNotificationCounter();
            }
        });
    }

    /***********************************************************************************************
     * Invoked when the sharing button is pressed
     * Finds the user credentials from the list of checked registered user names
     * Sends them gif Notifications
     * Uploads the Gif and the Gif frames to the server for them to download
     */
    private void handleGifSharingAction() {
        if (this.registered_contact_list.size() > 0) {
                ArrayList <String> selected_to_share_user_ids = this.getSelectedContactIds( this.registered_contact_list );

                if (selected_to_share_user_ids.size() > 0) {
                    this.controller.onSharingActionDetected(selected_to_share_user_ids);
                } else {
                    Log.d("SELECTED", "No selected users to sheare with");
                    fragment.torch.statusBlink("Please select some users to share with. Add and search users by clicking one the + button");
                }
        } else {
            fragment.torch.torch("You don't have any GifIt friends yet. Click the + to search and invite friends to share, or share via e-mail if you really have to");
        }
    }

    /***********************************************************************************************
     * Gets a list of user ids from the previously constructed contact list
     * Sent them to the server and finds the ones that are registered
     * Builds the registered list and the UI menu on server response callback
     */
    public void buildSharingMenu() {

        if ( this.registered_contact_list.size() > 0 ) {
            this.displaySharingMenu();
        }
        else {
            String my_email = fragment.shared_preferences.getString("login_email", "");

            if (this.contact_list.size() > 0) {

                String emails = this.buildAttributeString(this.contact_list, "email");
                String phone_numbers = this.buildAttributeString(this.contact_list, "phone_number");
                String param_string = format("my_email=%s&emails=%s&phone_numbers=%s", my_email, emails, phone_numbers);

                initiateServerConnection("/FindRegisteredContacts.php", param_string);
            }
        }
    }

    /***********************************************************************************************
     * Builds a Json encoded String from a hash map of objects
     * @param object_map   LinkedHashMap <String, LinkedHashMap <String, String>>  A hash map of objects composed of key - value pairs
     * @param attribute_type    String: the key in the object map you want to use to build the attribute string
     * @return String:  Json encoded string of attributes
     */
    private String buildAttributeString( LinkedHashMap<String, LinkedHashMap <String, String>> object_map, String attribute_type ) {
        JSONArray json_array = new JSONArray();

        for ( String object_id : this.contact_list.keySet() ) {
         //   Log.d("OBJECT ID", object_id);
            LinkedHashMap<String, String> object = object_map.get( object_id );

            String attribute = object.get( attribute_type );
       //     Log.d("USER ID", object.get( "user_id" ));
            if ( attribute != null && !attribute.equals("") ) {
                json_array.put(attribute);
            }
        }
        String attribute_string = json_array.toString();

        return attribute_string;
    }

    /***********************************************************************************************
     * Builds a list of registered contacts from the list of users found to be registered
     * Out of the total amount of valid contacts on the phone
     * @param result    String: Json encoded string of user credentials returned from the server
     */
    private void buildRegisteredContactList( String result ) {
        JsonParser json_parser = new JsonParser();

        InputStream result_stream = new ByteArrayInputStream(result.getBytes());
        try {
            ParsedResponse parsed_response = json_parser.parseJson(result_stream);

            for(LinkedHashMap<String, String> object : parsed_response.getObjects()) {
                LinkedHashMap <String, String> registered_contact = new LinkedHashMap<>();

                registered_contact.put("user_id", object.get("user_id"));
                registered_contact.put("display_name", object.get("first_name") + " " + object.get("last_name"));
                registered_contact.put("first_name", object.get("first_name"));
                registered_contact.put("last_name", object.get("last_name"));
                registered_contact.put("phone_number", object.get("phone_number"));
                registered_contact.put("email", object.get("email"));

                String user_id = object.get("user_id");

                this.registered_contact_list.put( user_id, registered_contact );
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***********************************************************************************************
     * If we have recently built a registered contact list, we can just retrieve it from the
     * local SQLite database instead of wasting time hitting the server
     * @return Boolean: returns true id we were able to get registered contacts from our local db
     *
     * @ToDO: Figure out a good way to periodically check for updated contacts
     */
    public boolean getStoredRegisteredContacts() {
        fragment.db_accessor.open();

        ArrayList <LinkedHashMap<String, String>> retrieved_contact_list = fragment.db_accessor.getAllRecords(DEF_REGISTERED_CONTACT.TABLE_NAME);

        // reformat the contact list because it is in a slightly different format than the data returned form the db ( HashMap vs ArrayList )
        for ( LinkedHashMap <String, String> retrieved_contact : retrieved_contact_list ){
            Log.d("REGISTERED CONTACTS", retrieved_contact.get( "user_id" ) );
            Log.d("REGISTERED CONTACTS", retrieved_contact.get( "first_name" ) );

            this.registered_contact_list.put( retrieved_contact.get( "user_id" ), retrieved_contact );
        }
        fragment.db_accessor.close();

        if (this.registered_contact_list.size() > 0 ) {
            return true;
        } else {
            return false;
        }

    }
    /***********************************************************************************************
     * Invoked right after the registered_contact_list list is built
     * Builds the UI list out of the user info in the registered_contact_list
     */
    public void displaySharingMenu() {
        LayoutBuilder layout_builder = new LayoutBuilder(fragment.getSavedActivity().findViewById(R.id.pager), fragment);
        this.sharing_menu_view = layout_builder.addToLayout(R.layout.window_sharing_gif, R.id.image_container, fragment.getFragmentView());
        this.sharing_menu_view.setClickable(true);

        this.setUIGestureListeners();

        SelectListScroller scroller = new SelectListScroller(R.id.scroll_item, sharing_menu_view);

        if (this.registered_contact_list.size() > 0) {

            int contact_index = 0;
            for (String contact_id : registered_contact_list.keySet()) {

                LinkedHashMap<String, String> contact = registered_contact_list.get(contact_id);

         //       Log.d("CONTACT_ID", contact_id);
       //         Log.d("USER_ID", contact.get("user_id"));
                contact.put("index", String.valueOf(contact_index));
                contact_index ++;

                View contact_view = this.createContactView( contact );
                scroller.addItem(contact_view);
            }
        }
        this.controller.setOpenWindow(true);
        this.animateWindow( sharing_menu_view, "drop_up" );
    }

    // async helpers -------------------------------------------------------------------------------
    @Override
    public void onAnimationFinished(String animation_type) {

    }

    @Override
    public void onAnimationFinished(String animation_type, View animated_view) {

        Log.d("ANIMATION FINISHED", "SHARING MENU");
        Log.d("ANIMATION TYPE", animation_type);
        if (animation_type.equals("slide_down") || animation_type.equals("slide_up")) {

            ( (FrameLayout) animated_view.getParent() ).removeView(animated_view);

            controller.setOpenWindow(false);
            controller.onWindowClosed("sharing_menu", "open_invite_menu");
        }
        else if (animation_type.equals( "slide_down_finished" )) {

            ( (FrameLayout) animated_view.getParent() ).removeView(animated_view);
            Log.d("SLIDE DOWN FINISHED", "Finished");
            controller.setOpenWindow(false);
            controller.onWindowClosed("sharing_menu", "finished_sharing");
        }
    }

    /***********************************************************************************************
     * Need to wait for this before we can proceed building the UI
     * @param result String: Json encoded string returned from server
     */
    @Override
    public void onServerResultReturned(String result) {

        Log.d("CONTACTS RESULT", result);
        this.buildRegisteredContactList( result );

        // update the database with the registered contact list
        this.controller.insertNewRegisteredContactsIntoLocalDB( this.registered_contact_list );

        this.displaySharingMenu();
    }
}
