package codewrencher.gifit.objects.simple.rendered_layout;

import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import codewrencher.gifit.helpers.JsonParser;
import codewrencher.gifit.helpers.async.Animator;
import codewrencher.gifit.helpers.async.interfaces.ServerResultsListener;
import codewrencher.gifit.helpers.async.interfaces.SharingHelperListener;
import codewrencher.gifit.helpers.async.server_connectors.ServerConnector;
import codewrencher.gifit.objects.complex.gif_chain.UpdatableDataObject;
import codewrencher.gifit.objects.simple.ParsedResponse;
import codewrencher.gifit.tools.LayoutBuilder;
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
public class RenderedLayout extends UpdatableDataObject implements ServerResultsListener,
                                                                   SharingHelperListener {
    protected MainActivity activity;
    protected Animator animator;
    protected View container_view;
    protected View rendered_view;

    protected int layout_id;
    protected int container_id;
    protected int display_width;
    protected int display_height;


    /***********************************************************************************************
     * Constructor
     * @param activity (Activity)   : Current main activity
     * @param layout_id (int)       : The id of the layout this object populates
     * @param container_id (int)    : The id of the parent View into which this layout is inserted
     * @param container_view (View) : The parent View into which this layout is inserted
     */
    public RenderedLayout( MainActivity activity, int layout_id, int container_id, View container_view ) {
        this.activity = activity;
        this.layout_id = layout_id;
        this.container_id = container_id;
        this.container_view = container_view;
        this.display_width = activity.getDisplayWidth();
        this.display_height = activity.getDisplayWidth();
        this.animator = new Animator(this.display_width, this.display_height);
    }
    public View getRenderedView() {
        return this.rendered_view;
    }
    /***********************************************************************************************
     * Create the UI object - Invoked explicitly by parent class
     * Populate the layout
     * Take care of any animations and actions
     */
    public void create() {
        this.loadLayout();
    }
    protected void loadLayout() {
        this.rendered_view = LayoutBuilder.addToLayout(layout_id, container_id, this.container_view);
    }
    /***********************************************************************************************
     * Populates all of the UI components with their data
     */
    protected void populate(ParsedResponse parsed_response) {

    }
    /***********************************************************************************************
     * Take care of all of the layout animations
     * Take cate of all the layout interaction listeners
     * Invoked after the layout data is fully loaded
     */
    protected void animate() {}

    public void initiateServerConnection(String url_string, String param_string) {
        ServerConnector server_connector = new ServerConnector();
        server_connector.registerListener(this);
        server_connector.execute(MainActivity.BASE_SERVER_URL + url_string, param_string);
    }

    @Override
    public void onServerResultReturned(String result) {

        InputStream result_stream = new ByteArrayInputStream( result.getBytes() );
        JsonParser json_parser = new JsonParser();
        try {
            final ParsedResponse parsed_response = json_parser.parseJson(result_stream);

            // populate your layout in a new thread (there may be many items to load)
            this.rendered_view.post(new Runnable() {
                public void run() {
                    populate(parsed_response);
                    // must wait for the layout to load before animating it
                    animate();
                }
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServerRequestCompleted(String result_type, String result_msg) {}
    @Override
    public void onWindowClosed(String window_type, String message) {}
    @Override
    public void onWindowClosed(String window_type, String message, String param) {}
    @Override
    public void onLoginSuccessful(String param, String message) {}
    @Override
    public void onSetPendingAppearance(String window_type) {}
    @Override
    public void onSharingActionDetected(ArrayList<String> selected_to_share_user_ids) {}
    @Override
    public void onActionCompleted(String action_type, String parameter, String message) {}
}

