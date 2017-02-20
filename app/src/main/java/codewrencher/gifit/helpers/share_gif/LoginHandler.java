package codewrencher.gifit.helpers.share_gif;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import codewrencher.gifit.R;
import codewrencher.gifit.helpers.async.interfaces.AnimatorListener;
import codewrencher.gifit.helpers.async.interfaces.ServerResultsListener;
import codewrencher.gifit.helpers.async.interfaces.SharingHelperListener;
import codewrencher.gifit.objects.complex.gif_chain.GifChain;
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
public class LoginHandler extends WindowHandler implements ServerResultsListener, AnimatorListener {
    private BaseFragment fragment;
    private SharingHelperListener controller;
    private View login_view;
    private String login_status;
    // UI listeners --------------------------------------------------------------------------------
    private View.OnClickListener login_click_listener;

    /***********************************************************************************************
     * Constructor
     * @param fragment
     * @param gif_chain
     * @param controller
     */
    public LoginHandler( BaseFragment fragment, GifChain gif_chain, GifItSharingController controller ) {
        super( fragment, gif_chain, controller );

        this.fragment = fragment;
        this.controller = controller;
        this.login_status = "";
    }

    /***********************************************************************************************
     * Set listeners for all of the main UI buttons
     * Login
     * Close
     */
    private void setUIGestureListeners() {

        login_click_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                login();
            }
        };

        Button login = (Button) login_view.findViewById( R.id.login_button) ;
        login.setOnClickListener( login_click_listener );

        this.setCloseListener(login_view, "slide_down", "");
    }

    /***********************************************************************************************
     * Main login window actions
     * First check for existing user credentials in local settings
     * Then contact server to log in or create new account if credentials not found
     */
    public void openLoginWindow(String message, String msg_type) {
        LayoutBuilder layout_builder = new LayoutBuilder(fragment.getSavedActivity().findViewById(R.id.pager), fragment);
        login_view = layout_builder.addToLayout(R.layout.window_login, R.id.image_container, fragment.getFragmentView());
        login_view.setClickable(true);

        this.setUIGestureListeners();

        TextView email = (TextView) login_view.findViewById(R.id.email);
        TextView password = (TextView) login_view.findViewById(R.id.password);

        String device_email = this.findPossibleDeviceEmail();
        String device_password = "";

        email.setText(device_email);
        password.setText(device_password);

        if ( message != null && !message.equals("") ) {
            this.displayLoginWarningMsg(login_view, message, msg_type);
        }

        this.animateWindow(login_view, "drop_up");
    }

    /***********************************************************************************************
     * Login status is checked in shared preferences
     * If the status is that of a new user, open the login window
     * The login window tries to login first using e-mail and a password
     * If it can't login, it tries to create a new account if the user name doesn't already exist
     * Once the account is created, local shared preferences are updated with the important user data
     */
    private void login() {
        TextView email_view = (TextView) fragment.getFragmentView().findViewById(R.id.email);
        TextView password_view = (TextView) fragment.getFragmentView().findViewById(R.id.password);

        String email = email_view.getText().toString();
        String password = password_view.getText().toString();

        String param_string = format("email=%s&password=%s", email, password);

        initiateServerConnection("/Login.php", param_string);
    }

    /***********************************************************************************************
     * Serves as a guide by pre-inputting the device's e-mail address when logging in or signing up
     * @return  String: The default e-mail address of the current device account
     */
    private String findPossibleDeviceEmail() {
        String possible_email = "";
        Pattern email_pattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(fragment.context).getAccounts();

        for (Account account : accounts) {

            if (email_pattern.matcher(account.name).matches()) {
                possible_email = account.name;
                break;
            }
        }
        return possible_email;
    }
    private void displayLoginWarningMsg(View login_view, String message, String msg_type) {
        int msg_view_id = 0;

        if (msg_type.equals("message")) {
            msg_view_id = R.id.message;
        } else if (msg_type.equals("message")) {
            msg_view_id = R.id.warning;
        }

        TextView msg_view = (TextView) login_view.findViewById(msg_view_id);
        msg_view.setVisibility(View.VISIBLE);
        msg_view.setText(message);
    }

    /***********************************************************************************************
     * Invoked by controller to check login status and decide what to do
     */
    public void checkLoginStatus() {
        String login_status = this.getLoginStatus();

        // current possible account statuses ----------------------------------------
        // new_user             - no account found
        // unverified_user      - account created but e-mail not verified yet
        // new_account          - just created a nice clean new account
        // logged_in            - regular user
        // login_failed         - failed to log in

        switch (login_status) {
            case "new_user":
                this.openLoginWindow("Please log in or create a new account to share Gif's", "message");
                break;
            case "unverified_user":
                this.openLoginWindow("Please verify your email so we know you are the person people will be sending things to. Check your e-mail please", "warning");
            case "login_failed":
                this.openLoginWindow("Please try logging in again", "message");
                break;
            case "new_account":     // these two cases are where the user is already logged in
                this.controller.onLoginSuccessful("window_login", "success");
                break;
            case "logged_in":
                this.controller.onLoginSuccessful("window_login", "success");
                break;
            default:
                this.openLoginWindow("Please log in", "message");
                break;
        }
    }
    public String getLoginStatus() {
        SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getSavedActivity());
        String login_status = shared_preferences.getString("login_status", "new_user");

        return login_status;
    }
    public void updateLoginStatus(String new_status) {
        SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(fragment.getSavedActivity());
        shared_preferences.edit().putString("login_status", new_status).apply();
    }
    private void updateLoginCredentials(LinkedHashMap<String, String> parsed_response) {
        fragment.shared_preferences.edit().putString("login_email", parsed_response.get("login_email"))
                                   .putString("login_password", parsed_response.get("login_password"))
                                   .putString("user_id", parsed_response.get("user_id")).apply();
    }

    // Async listeners -----------------------------------------------------------------------------
    @Override
    public void onServerResultReturned(String result) {
        LinkedHashMap<String, String> parsed_response = this.parseFlatJsonResponseObject(result);

        login_status = parsed_response.get("login_status");
        if (login_status == null) login_status = "fail";

        if (login_status.equals("success")) {
            fragment.torch.statusBlink("Login Successful");
            updateLoginCredentials(parsed_response);
        } else {
            fragment.torch.statusBlink("Login Failed");
        }
        this.animateWindow(login_view, "slide_down");
    }
    @Override
    public void onAnimationFinished(String animation_type) {

    }

    @Override
    public void onAnimationFinished(String animation_type, View animated_view) {
        if (animation_type.equals("slide_down") || animation_type.equals("slide_up")) {
            ( (FrameLayout) animated_view.getParent() ).removeView(animated_view);
        }
        if (animation_type.equals("slide_down")) {

            if (login_status.equals("success")) {
                if (this.getLoginStatus().equals("new_user") || this.getLoginStatus().equals("unverified_user")) {
                    this.updateLoginStatus("new_account");
                } else {
                    this.updateLoginStatus("logged_in");
                }

                this.controller.onLoginSuccessful("window_login", "success");
            } else {
                this.updateLoginStatus("login_failed");
            }
        }
    }
}

