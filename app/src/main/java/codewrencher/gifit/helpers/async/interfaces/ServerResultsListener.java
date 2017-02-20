package codewrencher.gifit.helpers.async.interfaces;

/**
 * Created by Gene on 4/19/2015.
 */
public interface ServerResultsListener {

    void onServerResultReturned(String result);

    void onServerRequestCompleted(String result_type, String result_msg);
}
