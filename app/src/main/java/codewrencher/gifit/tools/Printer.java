package codewrencher.gifit.tools;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Gene on 4/16/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class Printer {

    public static void printBreak() {
        Log.d("----------", "-----------------------------------");
    }
    public static void printBreak2() {
        Log.d("=========", "=====================================");
    }
    public static void printBreak3() {
        Log.d(">>>>>>>>>", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
    public static void printArray( ArrayList<String> array_list, String tag ) {
        Log.d("PRINTING ARRAY " + tag, ">>>>>>>>>>>>>>>>>>>>");

        if (array_list == null) {
            Log.d("ARRAY " + tag, "Is Null");
        } else {
            for (String item : array_list) {
                Log.d("ARRAY ITEM", item);
            }
        }
    }
    public static void printObjectList( ArrayList <LinkedHashMap<String, String>> object_list, String tag ) {
        Log.d("PRINTING OBJECT ARRAY " + tag, ">>>>>>>>>>>>>>>>>>>>");

        if (object_list == null) {
            Log.d("OBJECT LIST " + tag, "Is Null");
        } else {
            for ( LinkedHashMap <String, String> object : object_list) {

                String row = "";
                for ( String key : object.keySet() ) {
                    row += ( " [" + key + " : " );
                    row += object.get( key );
                    row += "] ";
                }

                Log.d("Object", row);
            }
        }
    }
    public static void printHashMap( HashMap<String, String> hash_map, String tag ) {
        Log.d("PRINTING HASH MAP " + tag, ">>>>>>>>>>>>>>>>>>>>");

        if (hash_map == null) {
            Log.d("HASH MAP " + tag, "Is Null");
        } else {
            for (String key : hash_map.keySet()) {
                Log.d("HASH MAP ITEM", key + " : " + hash_map.get(key));
            }
        }
    }
    public static void printKeys( HashMap<String, String> hash_map, String tag ) {
        Log.d("PRINTING KEYS " + tag, ">>>>>>>>>>>>>>>>>>>>");

        if (hash_map == null) {
            Log.d("HASH MAP FOR KEYS " + tag, "Is Null");
        } else {
            for (String key : hash_map.keySet()) {
                Log.d("HASH MAP KEY", key);
            }
        }
    }
}
