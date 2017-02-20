package codewrencher.gifit.helpers.share_gif;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.JsonParser;
import codewrencher.gifit.helpers.async.Animator;
import codewrencher.gifit.helpers.async.interfaces.AnimatorListener;
import codewrencher.gifit.helpers.async.interfaces.ServerResultsListener;
import codewrencher.gifit.helpers.async.interfaces.SharingHelperListener;
import codewrencher.gifit.helpers.async.server_connectors.ServerConnector;
import codewrencher.gifit.objects.complex.gif_chain.GifChain;
import codewrencher.gifit.objects.simple.ParsedResponse;
import codewrencher.gifit.tools.LayoutBuilder;
import codewrencher.gifit.tools.Printer;
import codewrencher.gifit.ui.MainActivity;
import codewrencher.gifit.ui.fragments.BaseFragment;
import codewrencher.gifit.ui.fragments.GifSpaceFragment;

/**
 * Created by Gene on 3/27/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class WindowHandler extends LayoutBuilder implements AnimatorListener,
                                                            ServerResultsListener,
                                                            SharingHelperListener {
    protected BaseFragment fragment;
    protected LinkedHashMap<String, LinkedHashMap<String, String>> contact_list;
    protected LinkedHashMap<String, LinkedHashMap<String, String>> registered_contact_list;
    protected LinkedHashMap<String, LinkedHashMap<String, String>> found_contact_list;
    protected GifItSharingController controller;
    protected GifChain gif_chain;

    /***********************************************************************************************
     * Constructor
     * @param fragment
     * @param gif_chain
     */
    public WindowHandler( BaseFragment fragment, GifChain gif_chain ) {
        super( fragment.getFragmentView(), fragment );

        this.fragment = fragment;
        this.gif_chain = gif_chain;
    }

    /***********************************************************************************************
     * Constructor
     * @param fragment
     * @param gif_chain
     * @param controller
     */
    public WindowHandler(BaseFragment fragment, GifChain gif_chain, GifItSharingController controller ) {
        super( fragment.getFragmentView(), fragment );

        this.fragment = fragment;
        this.gif_chain = gif_chain;
        this.controller = controller;

        this.contact_list = new LinkedHashMap<>();
        this.registered_contact_list = new LinkedHashMap<>();
        this.found_contact_list = new LinkedHashMap<>();
    }

    public void setContactList(LinkedHashMap<String, LinkedHashMap<String, String>> contact_list) {
        this.contact_list = contact_list;
    }
    public void setRegisteredContactList(LinkedHashMap<String, LinkedHashMap<String, String>> registered_contact_list) {
        this.registered_contact_list = registered_contact_list;
    }
    public void setFoundContactList(LinkedHashMap<String, LinkedHashMap<String, String>> found_contact_list) {
        this.found_contact_list = found_contact_list;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getContactList() {
        return this.contact_list;
    }
    public LinkedHashMap<String, LinkedHashMap<String, String>> getRegisteredContactList() {
        return this.registered_contact_list;
    }
    public LinkedHashMap<String, LinkedHashMap<String, String>> getFoundContactList() {
        return this.found_contact_list;
    }

    public void initiateServerConnection( String url_string, String param_string ) {
        ServerConnector server_connector = new ServerConnector();

        server_connector.registerListener(this);
        server_connector.execute(MainActivity.BASE_SERVER_URL + url_string, param_string);
    }
    public void initiateServerConnection( String url_string, String param_string, ProgressBar progress_bar ) {
        ServerConnector server_connector = new ServerConnector();
        server_connector.setProgressBar( progress_bar );

        server_connector.registerListener(this);
        server_connector.execute(MainActivity.BASE_SERVER_URL + url_string, param_string);
    }

    // Initiate Server Task ------------------------------------------------------------------------
    public void initiateServerConnection(String url_string, String param_string, String result_type, String result_msg) {
        ServerConnector server_connector = new ServerConnector();

        server_connector.registerListener(this);
        server_connector.setResultType(result_type);
        server_connector.setResultMsg(result_msg);
        server_connector.execute(MainActivity.BASE_SERVER_URL + url_string, param_string);
    }

    // Json Parsing --------------------------------------------------------------------------------
    protected LinkedHashMap<String, String> parseFlatJsonResponseObject(String response_string) {
        JsonParser json_parser = new JsonParser();
        LinkedHashMap<String, String> parsed_response = null;

        InputStream result_stream = new ByteArrayInputStream( response_string.getBytes() );
        try {
            JsonReader json_reader = new JsonReader( new InputStreamReader(result_stream, "UTF-8") );
            parsed_response = json_parser.parseFlatObject( json_reader );

            return parsed_response;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return parsed_response;
    }
    protected ParsedResponse parseVariableResponseObjects (String response_string) {
        JsonParser json_parser = new JsonParser();
        ParsedResponse parsed_response = null;

        InputStream result_stream = new ByteArrayInputStream( response_string.getBytes() );
        try {
            JsonReader json_reader = new JsonReader( new InputStreamReader(result_stream, "UTF-8") );

            parsed_response = json_parser.parseJson(result_stream);

            return parsed_response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parsed_response;
    }
    protected ParsedResponse parseJsonArray (String response_string) {
        JsonParser json_parser = new JsonParser();
        ParsedResponse parsed_response = null;

        InputStream result_stream = new ByteArrayInputStream( response_string.getBytes() );
        try {
            JsonReader json_reader = new JsonReader( new InputStreamReader(result_stream, "UTF-8") );

            parsed_response = json_parser.parseArray(json_reader, parsed_response, "parsed_array");

            return parsed_response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parsed_response;
    }


    // Handle UI Elements --------------------------------------------------------------------------
    protected View createContactView( final LinkedHashMap<String, String> contact ) {
        LayoutInflater layout_inflater = fragment.getActivity().getLayoutInflater();
        View contact_view = layout_inflater.inflate(R.layout.item_contact, null);

        final CheckBox check_box = (CheckBox) contact_view.findViewById(R.id.check);
        TextView display_name = (TextView) contact_view.findViewById(R.id.display_name);

        check_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleToggleCheckBox( check_box, contact );
            }
        });

        display_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCheckBox( check_box, contact );
            }
        });

        if ( contact.get("display_name") == null || contact.get("display_name").equals("") ) {
            display_name.setText( contact.get("first_name") + " " + contact.get("last_name") );
        }
        else {
            display_name.setText( contact.get("display_name") );
        }

    //    ((TextView) contact_view.findViewById(R.id.first_name)).setText( contact.get("first_name") );
    //    ((TextView) contact_view.findViewById(R.id.last_name)).setText( contact.get("last_name") );
    //    ((TextView) contact_view.findViewById(R.id.phone)).setText( contact.get("phone_number") );
    //    ((TextView) contact_view.findViewById(R.id.email)).setText( contact.get("email") );

        return contact_view;
    }
    protected ArrayList<String> getSelectedContactIds( LinkedHashMap<String, LinkedHashMap<String, String>> target_contact_list ) {
        ArrayList <String> selected_to_share_user_ids = new ArrayList<>();

        if (target_contact_list.size() > 0) {

            for ( String contact_id : target_contact_list.keySet() ) {

                LinkedHashMap<String, String> contact = target_contact_list.get( contact_id );

                Printer.printBreak();
                Printer.printHashMap(contact, contact_id);

                if ( contact.get( "selected" ) != null && contact.get( "selected" ).equals("true") ) {

                    selected_to_share_user_ids.add( contact_id );
                }
            }
        }
        return selected_to_share_user_ids;
    }

    /**
     * Old method used to share Gifs via email
     * Get a list of user emails to share the Gif with
     * @return  String[] Array of emails to share the Gif with
     */
    protected String[] getInviteEmailArray() {
        ArrayList<String> invite_emails = new ArrayList<>();

        if (this.contact_list.size() > 0) {

            for (String contact_id : contact_list.keySet()) {
                LinkedHashMap<String, String> contact = contact_list.get(contact_id);

                if (contact.get("selected_to_invite") != null && contact.get("selected_to_invite").equals("true")) {
                    if (contact.get("email") != null) {
                        invite_emails.add( contact.get("email") );
                    }
                }
            }
        }
        return invite_emails.toArray(new String[0]);
    }

    /**
     * Old method used to share Gifs via email
     * Get the sharing intent
     * @param email_array   String[] Array of emails to share the Gif with
     * @return  Intent: Email sharing intent
     */
    protected Intent getShareImageIntent( String[] email_array ) {
        File file = new File( ( (GifSpaceFragment) fragment ).getGifChain().getGif().getDownloadFilePath() + "/" +  ((GifSpaceFragment) fragment ).getGifChain().getGif().getName() );

        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/gif");
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        final Uri contentUriFile = fragment.getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        final Intent email_intent = new Intent(Intent.ACTION_SEND);

        email_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        email_intent.setType("image/jpeg");

        email_intent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");

        email_intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing A Gif With You!");
        email_intent.putExtra(Intent.EXTRA_TEXT, "Get The App: https://play.google.com/store/apps/details?id=codewrencher.gifit");
        email_intent.putExtra(android.content.Intent.EXTRA_STREAM, contentUriFile);
        email_intent.putExtra("type", "sharing");

        email_intent.putExtra(android.content.Intent.EXTRA_EMAIL, email_array);

        return email_intent;
    }

    protected void handleToggleCheckBox( CheckBox check, LinkedHashMap <String, String> contact ) {
        if (check.isChecked()) {
            contact.put( "selected", "true" );
        } else {
            contact.put( "selected", "false" );
        }
    }
    protected void toggleCheckBox( CheckBox check, LinkedHashMap <String, String> contact ) {
        if (check.isChecked()) {
            check.setChecked(false);
            contact.put( "selected", "false" );
        } else {
            check.setChecked(true);
            Log.d("SELECTED", contact.get("user_id") + " : " + contact.get("first_name"));
            contact.put("selected", "true");
        }
    }
    protected void animateWindow( View view, String type ) {
        Animator animator = new Animator(fragment.getDisplayWidth(), fragment.getDisplayHeight());
        animator.registerListener(this);
        animator.setAnimatedView(view);
        animator.setAnimation(view, type);
        animator.animate(view);
    }
    protected void setCloseListener( final View view, final String animation_type, final String pending_appearance ) {

        ImageButton close = (ImageButton) view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard();
                controller.onSetPendingAppearance(pending_appearance);
                animateWindow(view, animation_type);
            }
        });
    }
    protected void hideKeyboard() {
        View focused_view = fragment.getSavedActivity().getCurrentFocus();

        if ( focused_view != null ) {
            InputMethodManager imm = (InputMethodManager) fragment.getSavedActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow( focused_view.getWindowToken(), 0 );
        }
    }

    @Override
    public void onAnimationFinished(String animation_type) {}
    @Override
    public void onAnimationFinished(String animation_type, View animated_view) {}

    @Override
    public void onServerResultReturned(String result) {}

    @Override
    public void onServerRequestCompleted(String result_type, String result_msg) {

    }

    @Override
    public void onWindowClosed(String window_type, String message) {

    }

    @Override
    public void onWindowClosed(String window_type, String message, String param) {

    }

    @Override
    public void onLoginSuccessful(String param, String message) {

    }

    @Override
    public void onSetPendingAppearance(String window_type) {

    }


    @Override
    public void onSharingActionDetected(ArrayList<String> selected_to_share_user_ids) {

    }

    @Override
    public void onActionCompleted(String action_type, String parameter, String message) {

    }
}
