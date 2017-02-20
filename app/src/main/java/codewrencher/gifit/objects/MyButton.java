package codewrencher.gifit.objects;

import android.app.Activity;
import android.view.View;

/**
 * Created by Gene on 10/30/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 *
 * Handles some simple button actions, mainly so a button is only pressed once
 */
public class MyButton {

    public static final String tag = "MyButton";

    private Activity activity;
    private View button_view;
    private View container_view;
    private Boolean button_pressed;

    /**
     * Constructor
     * @param button_id: The id of the button view that you press
     * @param container_view: The view where the button resides
     */
    public MyButton(Activity activity, View container_view, int button_id) {
        this.activity = activity;
        this.container_view = container_view;
        this.button_view = container_view.findViewById(button_id);
        this.button_pressed = false;
    }

    /**
     * Sets the actions that will be performed on button click
     * @param on_click_actions: Runnable actions to perform
     */
    public void setOnClickListener(final Runnable on_click_actions) {

        this.button_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (! button_pressed) {
                    button_pressed = true;
                    performOnClickActions(on_click_actions);
                }
            }
        });
    }

    /**
     * Run the button's OnClick actions in a new UI Thread on click actions
     * @param on_click_actions: Runnable actions to perform
     */
    private void performOnClickActions(final Runnable on_click_actions) {
        new Thread() {
            public void run() {
                try {
                    activity.runOnUiThread(on_click_actions);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    button_pressed = false;
                }
            }
        }.start();
    }
}
