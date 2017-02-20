package codewrencher.gifit.objects.simple.rendered_layout;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.LinkedHashMap;

import codewrencher.gifit.R;
import codewrencher.gifit.objects.complex.gif_chain.GifChain;
import codewrencher.gifit.ui.MainActivity;

/**
 * Created by Gene on 4/3/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class AppNotification extends RenderedLayout {
    GifChain gif_chain;

    /***********************************************************************************************
     * Constructor
     * @param activity (Activity)   : Current main activity
     * @param layout_id (int)       : The id of the layout this object populates
     * @param container_id (int)    : The id of the parent View into which this layout is inserted
     * @param container_view (View) : The parent View into which this layout is inserted
     */
    public AppNotification ( MainActivity activity, int layout_id, int container_id, View container_view ) {
        super(activity, layout_id, container_id, container_view);
    }
    public void setGifChain( GifChain gif_chain ) {
        this.gif_chain = gif_chain;
    }
    public GifChain getGifChain() {
        return this.gif_chain;
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
        this.populate();
        this.animate();
    }

    /***********************************************************************************************
     * Method used to remove the object from the UI
     * Removes its view from the parent object and sets it to null
     */
    public void destroyUI() {

        ViewGroup parent = (ViewGroup) this.rendered_view.getParent();
        parent.removeView( this.rendered_view );
    }
    /**---------------------------------------------------------------------------------------------
     * Populate
     */
    /***********************************************************************************************
     * Populates all of the UI components with their data
     */
    protected void populate() {
        if ( this.gif_chain.getId() != null ) {

            this.setNotificationText();
        }
    }

    /***********************************************************************************************
     * Set the little text hint on the notification icon
     */
    private void setNotificationText() {
        TextView notification_text_view = (TextView) this.rendered_view.findViewById(R.id.notification_text);
        notification_text_view.setText( this.extractUserInitials() );
    }
    /***********************************************************************************************
     * Show the first and last initial of the sender in the notification box
     * @return
     */
    private String extractUserInitials() {
        String initials = "";

        String first_name = this.gif_chain.getSender().getFirstName();
        String last_name = this.gif_chain.getSender().getLastName();

        if ( (first_name == null || first_name.equals("")) && (last_name == null || last_name.equals("")) ) {
            initials = this.gif_chain.getSender().getId();
        }
        else if ( first_name == null || first_name.equals("") ) {
            initials = last_name.substring(0, 2).toUpperCase();
        }
        else if ( last_name == null || last_name.equals("") ) {
            initials = first_name.substring(0, 2).toUpperCase();
        }
        else {
            initials = first_name.substring(0, 1).toUpperCase() + last_name.substring(0, 1).toUpperCase();
        }
        return initials;
    }

    /**---------------------------------------------------------------------------------------------
     * Animate
     */
    /***********************************************************************************************
     * Take care of all of the layout animations
     * Take cate of all the layout interaction listeners
     * Invoked after the layout data is fully loaded
     */
    public void animate() {
        this.setOnNotificationClickListener();
    }

    /***********************************************************************************************
     * On Notification icon click:
     *      Load the Gif frames from the server,
     *      Create the Gif,
     *      Load the gif into view and switch to that view
     *      Update the notification not to show up again:
     *          Update record on server side database
     *          Update the Notification Drawer:
     *              count
     *              items
     */
    private void setOnNotificationClickListener() {
        ImageButton main_image = (ImageButton) rendered_view.findViewById(R.id.main_notification_image);
        main_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gif_chain.setChainType("downloaded");

                activity.gif_space_on = true;
                activity.setAction("downloaded");

                /** this is the key to loading the frames
                    It contains all of the frame and other data */
                activity.setReceivedGifChain(gif_chain);

                container_view.findViewById(R.id.main_notification_counter_image).callOnClick();    // close the notification drawer

                LinkedHashMap<String, String> gif_space_params = new LinkedHashMap<>();
                gif_space_params.put("action", "downloaded") ;

                activity.getCurrentFragment().openFragment("gif_space", 1, gif_space_params);
                removeOpenedNotification();


            }
        });
    }
    /***********************************************************************************************
     * Once the user clicks the notification icon, remove the notification from the UI
     * Remove or update the associated notification record
     * Once a notification is opened, the gif is loaded and the user can fool around with it
     * They can chain it and keep it going or they can close it and it will disappear
     */
    private void removeOpenedNotification() {
        this.activity.getAppNotificationDrawer().removeNotification( this.gif_chain );
    }

}
