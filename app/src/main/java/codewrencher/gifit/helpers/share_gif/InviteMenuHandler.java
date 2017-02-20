package codewrencher.gifit.helpers.share_gif;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.LinkedHashMap;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.interfaces.AnimatorListener;
import codewrencher.gifit.helpers.async.interfaces.ServerResultsListener;
import codewrencher.gifit.objects.complex.gif_chain.GifChain;
import codewrencher.gifit.objects.scrollers.SelectListScroller;
import codewrencher.gifit.tools.LayoutBuilder;
import codewrencher.gifit.ui.fragments.BaseFragment;

/**
 * Created by Gene on 3/27/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class InviteMenuHandler extends WindowHandler implements ServerResultsListener, AnimatorListener {

    private View invite_menu_view;
    private String search_string;
    // UI listeners --------------------------------------------------------------------------------
    private View.OnClickListener done_click_listener;
    private View.OnClickListener search_click_listener;

    /***********************************************************************************************
     * Constructor
     * @param fragment
     * @param gif_chain
     * @param controller
     */
    public InviteMenuHandler( BaseFragment fragment, GifChain gif_chain, GifItSharingController controller ) {
        super( fragment, gif_chain, controller );

        this.search_string = "";
    }

    /***********************************************************************************************
     * Set listeners for all of the main UI buttons
     * Search
     * Done
     * Close
     */
    private void setUIGestureListeners() {

        search_click_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard();
                controller.onSetPendingAppearance("search_menu");
                controller.setOpenWindow(false);

                TextView search_box = (TextView) invite_menu_view.findViewById(R.id.search_box);
                search_string = search_box.getText().toString();

                if ( search_string != null && !search_string.equals("") ) {
                    animateWindow(invite_menu_view, "slide_up");
                }
            }
        };

        done_click_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard();
                controller.onSetPendingAppearance("sharing_menu");
                controller.setOpenWindow(false);

                Intent email_intent = getShareImageIntent( getInviteEmailArray() );
                fragment.getActivity().startActivity(email_intent);

                animateWindow(invite_menu_view, "slide_up");
            }
        };

        ImageButton done = (ImageButton) invite_menu_view.findViewById(R.id.done);
        ImageButton search = (ImageButton) invite_menu_view.findViewById(R.id.search);

        done.setOnClickListener(done_click_listener);
        search.setOnClickListener(search_click_listener);
        this.setCloseListener(invite_menu_view, "slide_up", "sharing_menu");
    }

    public void showContactsWindow() {

        LayoutBuilder layout_builder = new LayoutBuilder(fragment.getSavedActivity().findViewById(R.id.pager), fragment);
        this.invite_menu_view = layout_builder.addToLayout(R.layout.list_invite, R.id.image_container, fragment.getFragmentView());
        this.invite_menu_view.setClickable(true);

        this.setUIGestureListeners();

        this.buildContactList();
        SelectListScroller scroller = new SelectListScroller(R.id.scroll_item, invite_menu_view);

        if (this.contact_list.size() > 0) {

            int contact_index = 0;
            for (String contact_id : this.contact_list.keySet()) {

                LinkedHashMap<String, String> contact = this.contact_list.get(contact_id);
                contact.put("index", String.valueOf(contact_index));
                contact_index++;

                View contact_view = this.createContactView( contact );
                scroller.addItem(contact_view);
            }
        }

        controller.setOpenWindow(true);
        this.animateWindow(invite_menu_view, "drop_down");
    }

    /***********************************************************************************************
     * Attempts to build a list of contacts from the data stored on the device
     * In order for a contact to be valid, it must have a:
     * Display Name
     * First Name
     * Phone NUmber
     */
    public void buildContactList() {
        this.search_string = "";
        if ( this.contact_list.size() > 0 ) return;

        LinkedHashMap<String, LinkedHashMap<String, String>> contact_list = new LinkedHashMap<>();

        ContentResolver content_resolver = fragment.getSavedActivity().getContentResolver();
        Cursor phone_cursor = content_resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        Cursor email_cursor = content_resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, null, null, null);
        Cursor name_cursor = content_resolver.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.Data.MIMETYPE + " = ?", new String[]{ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE}, ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME + " ASC");

        if (name_cursor.getCount() > 0) {
            while (name_cursor.moveToNext()) {
                String name_cursor_id = name_cursor.getString(name_cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID));
                this.addToContactList(contact_list, name_cursor_id, "display_name", name_cursor.getString(name_cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME)));
                this.addToContactList(contact_list, name_cursor_id, "first_name", name_cursor.getString(name_cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)));
                this.addToContactList(contact_list, name_cursor_id, "last_name", name_cursor.getString(name_cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)));
            }
        }
        name_cursor.close();

        if (phone_cursor.getCount() > 0) {
            while (phone_cursor.moveToNext()) {
                String phone_cursor_id = phone_cursor.getString(phone_cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                this.addToContactList(contact_list, phone_cursor_id, "phone_number", phone_cursor.getString(phone_cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            }
        }
        phone_cursor.close();

        if (email_cursor.getCount() > 0) {
            while (email_cursor.moveToNext()) {
                String email_cursor_id = email_cursor.getString(email_cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID));
                this.addToContactList(contact_list, email_cursor_id, "email", email_cursor.getString(email_cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
            }
        }
        email_cursor.close();

        this.contact_list = this.trimContactList( contact_list );

        controller.onActionCompleted("invite_menu", "", "contact_list_built");
    }

    /***********************************************************************************************
     * Removes invalid contacts that may have popped in there from a mail sync
     * @param contact_list List of objects: contains contact info
     * @return  List of objects: trimmed list with the junk contacts removed
     */
    LinkedHashMap<String, LinkedHashMap<String, String>> trimContactList(LinkedHashMap<String, LinkedHashMap<String, String>> contact_list) {
        LinkedHashMap<String, LinkedHashMap<String, String>> trimmed_contact_list = new LinkedHashMap<>();

        for (String contact_id : contact_list.keySet()) {

            LinkedHashMap<String, String> contact = contact_list.get(contact_id);
            if (contact.get("display_name") == null || contact.get("display_name").equals("")) {
                continue;
            } else if (contact.get("first_name") == null || contact.get("first_name").equals("")) {
                continue;
            } else if (contact.get("phone_number") == null || contact.get("phone_number").equals("") || contact.get("phone_number").length() < 7 || contact.get("phone_number").length() > 11) {
                continue;
            }
            trimmed_contact_list.put(contact_id, contact);
        }
        return trimmed_contact_list;
    }
    private LinkedHashMap<String, LinkedHashMap<String, String>> addToContactList(LinkedHashMap<String, LinkedHashMap<String, String>> contact_list, String cursor_id, String data_type, String data_value) {
        LinkedHashMap<String, String> contact = contact_list.get(cursor_id);
        if (contact == null) {
            contact = new LinkedHashMap<>();
        }
        contact.put(data_type, data_value);
        contact_list.put(cursor_id, contact);

        return contact_list;
    }

    @Override
    public void onServerResultReturned(String result) {}

    @Override
    public void onAnimationFinished(String animation_type) {}
    @Override
    public void onAnimationFinished(String animation_type, View animated_view) {
        if (animation_type.equals("slide_down") || animation_type.equals("slide_up")) {
            ( (FrameLayout) animated_view.getParent() ).removeView(animated_view);

            controller.setOpenWindow(false);

            if ( this.search_string == null || this.search_string.equals("") ) {
                controller.onWindowClosed("invite_menu", "reopen_sharing_menu");
            }
            else {
                controller.onWindowClosed("invite_menu", "open_search_window", search_string);
            }
        }
    }


}
