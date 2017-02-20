package codewrencher.gifit.helpers.share_gif;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.util.LinkedHashMap;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.interfaces.AnimatorListener;
import codewrencher.gifit.helpers.async.interfaces.ServerResultsListener;
import codewrencher.gifit.objects.complex.gif_chain.GifChain;
import codewrencher.gifit.objects.scrollers.SelectListScroller;
import codewrencher.gifit.objects.simple.ParsedResponse;
import codewrencher.gifit.tools.LayoutBuilder;
import codewrencher.gifit.ui.fragments.BaseFragment;

import static java.lang.String.format;

/**
 * Created by Gene on 4/11/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class SearchMenuHandler extends WindowHandler implements ServerResultsListener, AnimatorListener {

    private View search_menu_view;
    private String close_action;
    // UI listeners --------------------------------------------------------------------------------
    private View.OnClickListener done_click_listener;

    /***********************************************************************************************
     * Constructor
     * @param fragment
     * @param controller
     */
    public SearchMenuHandler( BaseFragment fragment, GifChain gif_chain, GifItSharingController controller ) {
        super( fragment, gif_chain, controller );

        this.close_action = "";
    }

    /**
     * Set listeners for all of the main UI buttons
     * Done
     * Close
     */
    private void setUIGestureListeners() {
        done_click_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close_action = "update";
                controller.onSetPendingAppearance("sharing_menu");
                controller.setOpenWindow(false);

                animateWindow(search_menu_view, "slide_up");
            }
        };

        ImageButton done = (ImageButton) search_menu_view.findViewById(R.id.done);

        done.setOnClickListener(done_click_listener);
        this.setCloseListener(search_menu_view, "slide_up", "sharing_menu");
    }

    /**
     * Main method invoked by the Controller
     * @param search_string     String: the search string entered by the user, passed from the invite window
     */
    public void showSearchResultsWindow( String search_string ) {

        LayoutBuilder layout_builder = new LayoutBuilder(fragment.getSavedActivity().findViewById(R.id.pager), fragment);
        this.search_menu_view = layout_builder.addToLayout(R.layout.list_search, R.id.image_container, fragment.getFragmentView());
        this.search_menu_view.setClickable(true);

        this.setUIGestureListeners();

        this.getUserSearchResults(search_string);

        controller.setOpenWindow(true);
        this.animateWindow(search_menu_view, "drop_down");
    }

    /**
     * Called on server response received callback if the response is not null
     * Populated its own registered_contact_list at the same time
     * @param parsed_response   ParsedResponse: the parsed server response returned from JsonParser
     */
    private void displaySearchResults( ParsedResponse parsed_response ) {
        this.close_action = "";
        SelectListScroller scroller = new SelectListScroller( R.id.scroll_item, this.search_menu_view );

        int contact_index = 0;
        for ( LinkedHashMap <String, String> found_user : parsed_response.getObjects() ) {

            this.found_contact_list.put( found_user.get("user_id"), found_user );

            found_user.put( "index", String.valueOf( contact_index ) );
            contact_index++;

            View contact_view = this.createContactView( found_user );
            scroller.addItem( contact_view );
        }
    }

    /**
     * Contacts the server and tries to find any registered users based on the entered Search string
     * @param search_string     String: the user - entered string in the search_box
     */
    private void getUserSearchResults( String search_string ) {

        String user_id = fragment.shared_preferences.getString("user_id", "");
        String param_string = format("user_id=%s&search_string=%s", user_id, search_string);
        ProgressBar progress_bar = (ProgressBar) search_menu_view.findViewById( R.id.progress_bar );

        initiateServerConnection("/SearchUsers.php", param_string, progress_bar);
    }

    @Override
    public void onServerResultReturned(String result) {

        ParsedResponse parsed_response = this.parseVariableResponseObjects( result );

        if ( parsed_response != null ){
            if ( parsed_response.getObjects() != null ) {

                this.displaySearchResults(parsed_response);
            }
        }
    }

    @Override
    public void onAnimationFinished(String animation_type) {}
    @Override
    public void onAnimationFinished(String animation_type, View animated_view) {
        if (animation_type.equals("slide_down") || animation_type.equals("slide_up")) {

            ((FrameLayout) animated_view.getParent()).removeView(animated_view);

            controller.setOpenWindow(false);
            if ( this.close_action == null || this.close_action.equals("") ) {
                controller.onWindowClosed("search_menu", "reopen_sharing_menu");
            } else if ( this.close_action == "update" ) {
                controller.onWindowClosed("search_menu", "reopen_sharing_menu", "update");
            }
        }
    }


}
