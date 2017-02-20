package codewrencher.gifit.objects.simple;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Gene on 3/30/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class ParsedResponse {
    private LinkedHashMap <String, String>              strings;
    private LinkedHashMap <String, ArrayList <String>>  arrays;
    private ArrayList <LinkedHashMap <String, String>>  objects;

    public ParsedResponse() {
        strings = new LinkedHashMap<>();
        arrays = new LinkedHashMap<>();
        objects = new ArrayList<>();
    }
    public LinkedHashMap <String, String> getStrings() {
        return this.strings;
    }
    public LinkedHashMap <String, ArrayList <String>>  getArrays() {
        return this.arrays;
    }
    public ArrayList <LinkedHashMap <String, String>> getObjects() {
        return this.objects;
    }

    public void setStrings( LinkedHashMap <String, String>  strings) {
        this.strings = strings;
    }
    public void  setArrays( LinkedHashMap <String, ArrayList <String>> arrays) {
        this.arrays = arrays;
    }
    public void setObjects( ArrayList <LinkedHashMap <String, String>> objects ) {
        this.objects = objects;
    }
}
