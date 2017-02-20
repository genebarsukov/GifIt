package codewrencher.gifit.helpers;

import android.app.Application;
import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import codewrencher.gifit.objects.simple.ParsedResponse;

public class JsonParser extends Application {


    // ---------------------------------------------------------------------------------------------
    public ParsedResponse parseJson(InputStream json_obj) throws IOException {
        JsonReader json_reader = new JsonReader(new InputStreamReader(json_obj, "UTF-8"));
        try {
            return parseResponse(json_reader);
        }
        finally {
            json_reader.close();
        }
    }
    public ParsedResponse parseResponse(JsonReader json_reader) {
        ParsedResponse parsed_response = new ParsedResponse();
        try {
            JsonToken initial_peek = json_reader.peek();

            if (initial_peek == JsonToken.BEGIN_ARRAY) {
                parsed_response = parseArray( json_reader, parsed_response, "");
            }
            else {
                json_reader.beginObject();

                while (json_reader.hasNext()) {
                    String name = "";
                    JsonToken peek = json_reader.peek();

                    if (peek == JsonToken.NAME) {
                        name = json_reader.nextName();

                        JsonToken peek_again = json_reader.peek();
                        if (peek_again == JsonToken.STRING) {

                            String string = json_reader.nextString();
                            parsed_response.getStrings().put(name, string);

                        } else if (peek_again == JsonToken.BEGIN_ARRAY) {
                            parsed_response = parseArray(json_reader, parsed_response, name);
                        } else {
                            json_reader.skipValue();
                        }
                    } else if (peek == JsonToken.STRING) {

                        String string = json_reader.nextString();
                        parsed_response.getStrings().put(name, string);

                    } else {
                        json_reader.skipValue();
                    }
                }
                json_reader.endObject();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return parsed_response;
    }
    public ParsedResponse parseArray(JsonReader json_reader, ParsedResponse parsed_response, String previous_name) {
        ArrayList <LinkedHashMap <String, String>> array_objects = new ArrayList<>();
        ArrayList <String> array_strings = new ArrayList<>();

        try {
            json_reader.beginArray();
            while (json_reader.hasNext()) {
                String name = "";
                JsonToken peek = json_reader.peek();

                if (peek == JsonToken.NAME) {
                    name = json_reader.nextName();
                   // json_reader.endArray();

                } else if (peek == JsonToken.STRING) {          // parse an array of strings
                    String string = json_reader.nextString();

                    array_strings.add(string);

                } else if (peek == JsonToken.BEGIN_OBJECT) {    // parse an array of objects
                    array_objects.add( parseObject(json_reader) );
                } else {
                    json_reader.skipValue();
                }
            }
            json_reader.endArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        parsed_response.getArrays().put( previous_name, array_strings );
        parsed_response.setObjects( array_objects );

        return parsed_response;
    }
    public LinkedHashMap<String, String> parseObject(JsonReader json_reader) {
        LinkedHashMap <String, String> parsed_object = new LinkedHashMap<>();

        try {
            json_reader.beginObject();
            while (json_reader.hasNext()) {
                String name = "";
                JsonToken peek = json_reader.peek();

                if (peek == JsonToken.NAME) {
                    name = json_reader.nextName();

                    JsonToken peek_again = json_reader.peek();
                    if (peek_again == JsonToken.STRING) {
                        String string = json_reader.nextString();
                        parsed_object.put(name, string);

                    } else {
                        json_reader.skipValue();
                    }
                } else if (peek == JsonToken.STRING) {
                    String string = json_reader.nextString();
                    parsed_object.put(name, string);

                } else if (peek == JsonToken.BEGIN_ARRAY) {
                    json_reader.skipValue();
                } else {
                    json_reader.skipValue();
                }
            }
            json_reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parsed_object;
    }
    // ---------------------------------------------------------------------------------------------
    public LinkedHashMap<String, String> parseFlatObject(JsonReader json_reader) throws IOException {
        LinkedHashMap <String, String> status_map = new LinkedHashMap<>();
        try {
            json_reader.beginObject();
            while (json_reader.hasNext()) {
                String name = "";
                JsonToken peek = json_reader.peek();

                if (peek == JsonToken.NAME) {

                    name = json_reader.nextName();
                    JsonToken peek_again = json_reader.peek();

                    if (peek_again == JsonToken.STRING) {

                        String value = json_reader.nextString();

                        status_map.put(name, value);
                    } else {
                        json_reader.skipValue();
                    }
                } else {
                    json_reader.skipValue();
                }
            }
            json_reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status_map;
    }
    // ---------------------------------------------------------------------------------------------
    public void parseResponse(JsonReader json_reader, String parsing_action) throws IOException {
        LinkedHashMap self_map = new LinkedHashMap<String, String>();
        LinkedHashMap transactions_map = new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>();
        LinkedHashMap postings_map = new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>();
        try {
            json_reader.beginObject();

            while (json_reader.hasNext()) {

                JsonToken peek = json_reader.peek();
                if (peek == JsonToken.NULL) {
                    json_reader.skipValue();
                } else {
                    try {
                        String data_field_name = json_reader.nextName();

                        if (data_field_name.equals("self")) {
                            self_map = parseSelf(json_reader);
                        }
                        else if (data_field_name.equals("transactions")) {
                            transactions_map = parseTransactions(json_reader);
                        }
                        else if (data_field_name.equals("postings")) {
                            postings_map = parsePostings(json_reader);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            json_reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------------------------------------
    public LinkedHashMap parseSelf(JsonReader json_reader) throws IOException {
        LinkedHashMap self_map = new LinkedHashMap<String, String>();
        try {
            json_reader.beginObject();
            while (json_reader.hasNext()) {
                String data_field_name = json_reader.nextName();
                JsonToken peek = json_reader.peek();
                if (peek == JsonToken.NULL) {
                    self_map.put(data_field_name, null);
                    json_reader.skipValue();
                } else {
                    String data_field_value = json_reader.nextString();
                    self_map.put(data_field_name, data_field_value);
                }
            }
            json_reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return self_map;
    }
    // ---------------------------------------------------------------------------------------------
    public LinkedHashMap parseTransactions(JsonReader json_reader) throws IOException {
        LinkedHashMap transaction_id_map = new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>();
        try {
            json_reader.beginObject();
            while (json_reader.hasNext()) {

                JsonToken peek = json_reader.peek();
                if (peek == JsonToken.NULL) {
                    json_reader.skipValue();
                } else {
                    try {
                        String next_transaction_id = json_reader.nextName();
                        LinkedHashMap<String, LinkedHashMap<String, String>> transaction_map = parseDoubleTierStringMap(json_reader);
                        transaction_id_map.put(next_transaction_id, transaction_map);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            json_reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transaction_id_map;
    }
    // ---------------------------------------------------------------------------------------------
    public LinkedHashMap parsePostings(JsonReader json_reader) throws IOException {
        LinkedHashMap posting_id_map = new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>>();
        try {
            json_reader.beginObject();
            while (json_reader.hasNext()) {

                JsonToken peek = json_reader.peek();
                if (peek == JsonToken.NULL) {
                    json_reader.skipValue();
                } else {
                    try {
                        String next_posting_id = json_reader.nextName();
                        LinkedHashMap<String, LinkedHashMap<String, String>> posting_map = parseDoubleTierStringMap(json_reader);
                        posting_id_map.put(next_posting_id, posting_map);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            json_reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return posting_id_map;
    }
    // ---------------------------------------------------------------------------------------------

    public LinkedHashMap parseDoubleTierStringMap(JsonReader json_reader) throws IOException {
        LinkedHashMap double_tier_map = new LinkedHashMap<String, LinkedHashMap<String, String>>();
        try {
            json_reader.beginObject();
            while (json_reader.hasNext()) {
                String field_name = json_reader.nextName();
                JsonToken peek = json_reader.peek();
                if (peek == JsonToken.NULL) {
                    json_reader.skipValue();
                } else {
                    LinkedHashMap<String, String> single_tier_map = parseSingleTierStringMap(json_reader);
                    double_tier_map.put(field_name, single_tier_map);
                }
            }
            json_reader.endObject();
        } catch (IOException e) {
                e.printStackTrace();
        }
        return double_tier_map;
    }
    // ---------------------------------------------------------------------------------------------
    public LinkedHashMap parseSingleTierStringMap(JsonReader json_reader) throws IOException {
        LinkedHashMap single_tier_map = new LinkedHashMap<String,String>();
        try {
        json_reader.beginObject();
        while (json_reader.hasNext()) {
            String field_name = json_reader.nextName();
            JsonToken peek = json_reader.peek();
            if (peek == JsonToken.NULL) {
                json_reader.skipValue();
            } else {
                String field_value = json_reader.nextString();
                single_tier_map.put(field_name, field_value);
            }
        }
        json_reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return single_tier_map;
    }

}
