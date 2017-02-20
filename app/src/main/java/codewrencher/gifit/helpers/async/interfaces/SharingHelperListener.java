package codewrencher.gifit.helpers.async.interfaces;

import java.util.ArrayList;

/**
 * Created by Gene on 3/27/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public interface SharingHelperListener {

    void onWindowClosed(String window_type, String message);
    void onWindowClosed(String window_type, String message, String param);

    void onLoginSuccessful(String param, String message);
    void onSetPendingAppearance(String window_type);
    void onSharingActionDetected(ArrayList<String> selected_to_share_user_ids);

    void onActionCompleted(String action_type, String parameter, String message);
}
