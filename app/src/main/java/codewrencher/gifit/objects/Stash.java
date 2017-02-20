package codewrencher.gifit.objects;

import android.app.Application;

import java.util.LinkedHashMap;

/**
 * Created by Gene on 10/31/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 *
 * A simple box for storing things
 * Useful when you dont know whether or not your Fragment ot Activity will be destroyed
 * Also its nice to have a specialized class for this instead of just storing things willy nilly
 *
 * We organize this by Application -> Activity -> Fragment -> Helper -> Object
 * I need to store a gifchain, a gif, and a bunch of images or layouts
 */
public class Stash extends Application {

    private static String tag = "Stash";

    LinkedHashMap<String, Object> stash;

    /**
     * Constructor - Initialize the stash container
     */
    public Stash() {
        stash = new LinkedHashMap<>();
    }

    /**
     * Store an object by its unique key
     * @param key Unique object key - remember it
     * @param object The object you want to store - varying types
     */
    public void store(String key, Object object) {
        stash.put(key, object);
    }

    /**
     * Retrieve and object from the stash
     * @param key The key under which an object is stored
     */
    public Object get(String key) {
        return stash.get(key);
    }

    /**
     * Remove and object from the stash
     * @param key The key under which the object is stored
     */
    public void remove(String key) {
        stash.remove(key);
    }
}
