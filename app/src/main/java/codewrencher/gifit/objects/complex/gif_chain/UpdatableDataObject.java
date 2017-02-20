package codewrencher.gifit.objects.complex.gif_chain;

/**
 * Created by Gene on 4/17/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */

import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import codewrencher.gifit.helpers.Damen;
import codewrencher.gifit.helpers.JsonParser;
import codewrencher.gifit.helpers.async.interfaces.ActionCompletedListener;
import codewrencher.gifit.helpers.async.interfaces.ServerFileListener;
import codewrencher.gifit.helpers.async.interfaces.ServerResultsListener;
import codewrencher.gifit.helpers.async.server_connectors.ServerConnector;
import codewrencher.gifit.objects.simple.ParsedResponse;
import codewrencher.gifit.ui.MainActivity;
import codewrencher.gifit.ui.fragments.BaseFragment;

import static java.lang.String.format;

/***************************************************************************************************
 * An Object dependent on back-end database data                                                   *
 * Has functionality to populate and update itself                                                 *
 * And to create and update dependent database data on the back end                                *
 **************************************************************************************************/

public class UpdatableDataObject implements ServerResultsListener,
                                            ServerFileListener {

    protected static final String DATABASE = "gifit";
    protected static final String BASE_SERVER_URL = "http://www.codewrencher.com/gifit";
    public static final String APP_STORAGE_DIRECTORY = "GifIt";
    public static final String APP_FILE_PREFIX = "gifit_";
    protected static final String BASE_SERVER_FILE_DIRECTORY = "shared_gifs";
    protected static final String BASE_LOCAL_FILE_DIRECTORY = "shared_gifs";

    protected View rendered_view;
    protected String id;
    protected String name;
    protected String date_created;
    protected String date_updated;

    protected String id_field_name;
    protected String table;
    protected String data_path;   /** The server endpoint (path) on which the object is dependent for data*/
    protected String download_file_path;

    protected boolean data_loaded;
    protected boolean file_loaded;
    protected boolean image_loaded;

    protected boolean data_sent;
    protected boolean file_sent;

    protected MainActivity activity;
    protected BaseFragment fragment;
    protected UpdatableDataObject parent_object;

    protected String server_action;
    protected ActionCompletedListener action_completed_listener;
    protected Damen damen;

    /***********************************************************************************************
     * Default Constructor for creating new objects
     */
    public UpdatableDataObject() {}

    /***********************************************************************************************
     * Constructor
     * @param id int:               Object id corresponding to the id in the db table
     * @param id_field_name String: The name of the id field in the table
     * @param table Sting:          The name of the db table to get fata from
     * @param data_path String:     The server path to get and send data to and from
     */
    public UpdatableDataObject( String id, String id_field_name, String table, String data_path ) {

        this.id = id;
        this.id_field_name = id_field_name;
        this.table = table;
        this.data_path = data_path;
    }

    /**---------------------------------------------------------------------------------------------
     * Register Listener
     */
    /***********************************************************************************************
     * Register the data requesting class as a listener
     * Many actions require getting data from the server
     * @param action_completed_listener ActionCompletedListener: notifies when the action is completed
     */
    public void registerListener( ActionCompletedListener action_completed_listener ) {
        this.action_completed_listener = action_completed_listener;
    }

    /**---------------------------------------------------------------------------------------------
     * Getters
     */
    /**
     * Get Stuff
     */
    public String getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }
    public String getIdFieldName() {
        return this.id_field_name;
    }
    public String getTable() {
        return this.table;
    }
    public String getDataPath() {
        return this.data_path;
    }
    public UpdatableDataObject getParentObject() {
        return this.parent_object;
    }
    public String getDownloadFilePath() {
        return this.download_file_path;
    }
    public BaseFragment getFragment() {
        return this.fragment;
    }

    /**---------------------------------------------------------------------------------------------
     * Setters
     */
    /**
     * Set Stuff
     */
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setIdFieldName(String id_field_name) {
        this.id_field_name = id_field_name;
    }
    public void setTable(String table) {
        this.table = table;
    }
    public void setDataPath(String data_path) {
        this.data_path = data_path;
    }

    public void setActivity( MainActivity activity ) {
        this.activity = activity;
    }
    public void setParentObject( UpdatableDataObject parent_object ) {
        this.parent_object = parent_object;
    }
    public void setDownloadFilePath(String download_file_path) {
        this.download_file_path = download_file_path;
    }
    public void setFragment( BaseFragment fragment ) {
        this.fragment = fragment;
        this.damen = fragment.damen;
    }

    protected File appStorageDirectoryExists() {
        File media_storage_dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), APP_STORAGE_DIRECTORY);
        if (! media_storage_dir.exists()){
            if (! media_storage_dir.mkdirs()){
                Log.d(APP_STORAGE_DIRECTORY, "failed to create directory");
                return null;
            }
        }
        return media_storage_dir;
    }

    public void setDateCreated( String date_created ) {
        this.date_created = date_created;
    }
    public void setDateUpdated( String date_updated ) {
        this.date_updated = date_updated;
    }

    /***********************************************************************************************
     * Sets self object properies from a flat key - value hash map
     * @param properties LinkedHashMap: a hash map of values whose keys must correspond to existing
     * class variables
     */
    protected void setPropertiesFromHashMap( LinkedHashMap <String, String> properties ) {

        for( Field field : getClass().getDeclaredFields() ) {

            if ( properties.get( field.getName() ) != null ) {

                field.setAccessible( true );
                try {
                    field.set( this, properties.get( field.getName() ) );
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    /***********************************************************************************************
     * Contacts the server, gets object data, and populates its own properties
     */
    public void createSelf() {
        this.server_action = "get_self";
        this.initiateServerConnection( this.buildServerParamString( "get" ) );
    }

    /***********************************************************************************************
     * Creates a database record based on existing properties
     */
    public void createDependentData() {
        this.server_action = "create_record";
        this.initiateServerConnection( this.buildServerParamString( "create" ) );
    }

    /***********************************************************************************************
     * Contacts the server, gets data, and updates its properties based on retrieved data
     */
    public void updateSelf() {
        this.server_action = "update_self";
        this.initiateServerConnection( this.buildServerParamString( "get" ) );
    }

    /***********************************************************************************************
     * Contacts the server and updates its corresponding database record
     */
    public void updateDependentData() {
        this.server_action = "update_record";
        this.initiateServerConnection( this.buildServerParamString( "update" ) );
    }

    /***********************************************************************************************
     * Builds a default server parameter string based on defined properties
     */
    protected String buildServerParamString( String data_action ) {

        String param_string = format( "data_action=%s" +
                                      "&database=%s" +
                                      "&query_format=def" +
                                      "&data_format=row" +
                                      "&table=%s" +
                                      "&field_name=%s" +
                                      "&field_value=%s", data_action,
                                                         DATABASE,
                                                         this.table,
                                                         this.id_field_name,
                                                         this.id );

        return param_string;
    }
    /***********************************************************************************************
     * Builds a custom server parameter string based on specified parameters
     * Type of query to perform, tables to use, conditions, data format to return
     */
    protected String buildServerParamString( String data_action, LinkedHashMap params ) {

        String custom_param_string = this.createParamStringFromObject( params );

        String param_string = format( "data_action=%s" +
                                      "&database=%s +" +
                                      "&%s", data_action,
                                             DATABASE,
                                             custom_param_string );

        return param_string;
    }

    /***********************************************************************************************
     * Builds a server param string from a key-value pair object using keys as names for the values
     * @param params LinkedHashMap:  a key-value pair flat hash map
     * @return  String:     Formatter server param string
     */
    protected String createParamStringFromObject ( LinkedHashMap <String, String> params ) {
        String param_string = "";

        int index = 0;
        for ( String key : params.keySet() ) {

            param_string += ( key + "=" + params.get(key) );

            if ( index < params.size() ) {
                param_string += "&";
            }
        }
        return param_string;
    }

    /***********************************************************************************************
     * Goes to the server to get / send data
     * @param param_string String:  The parameters to send to the server
     */
    protected void initiateServerConnection( String param_string ) {
        ServerConnector server_connector = new ServerConnector();
        server_connector.registerListener(this);
        server_connector.execute( BASE_SERVER_URL + "/" + this.data_path, param_string );
    }


    /***********************************************************************************************
     * Checks for, creates, and returns the local storage directory
     * @param target_dir_path String:   the name of the top level directory you desire
     * @return String:                  the full path of the created or existing directory
     */
    protected String getLocalDirectory ( String target_dir_path ) {

        File target_dir = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES ),
                                    MainActivity.APP_STORAGE_DIRECTORY +
                                            "/" + BASE_LOCAL_FILE_DIRECTORY +
                                            "/" + target_dir_path );
        if (! target_dir.exists()){
            if (! target_dir.mkdirs()){

                return null;
            }
        }
        return target_dir.getPath();
    }

    /***********************************************************************************************
     * Parse the server response and store the result in an custom object
     * Returned object contains various data structures depending on the structure of the Json
     * @param result_string String:     Json encoded string returned from the server
     * @return ParsedResponse:          contains results from parsing Json
     */
    protected ParsedResponse parseJsonResponse ( String result_string ) {
        ParsedResponse parsed_response = null;

        InputStream result_stream = new ByteArrayInputStream( result_string.getBytes() );
        try {
            JsonParser json_parser = new JsonParser();
            parsed_response = json_parser.parseJson(result_stream);

            return parsed_response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parsed_response;
    }
    /***********************************************************************************************
     * ServerResultsListener notifications: Handling text data
     ***********************************************************************************************/
    @Override
    public void onServerResultReturned(String result) {
        ParsedResponse parsed_response = this.parseJsonResponse( result );

        switch ( this.server_action ) {

            case "get_self":
                this.setPropertiesFromHashMap(parsed_response.getStrings());
                break;

            case "create_record":
                this.setPropertiesFromHashMap(parsed_response.getStrings());
                break;

            case "update_self":
                this.setPropertiesFromHashMap(parsed_response.getStrings());
                break;

            case "update_record":
                this.setPropertiesFromHashMap(parsed_response.getStrings());
                break;
        }
    }

    @Override
    public void onServerRequestCompleted(String result_type, String result_msg) {

    }

    /***********************************************************************************************
     * ServerFileListener notifications: Handling files
     ***********************************************************************************************/
    @Override
    public void onFileUploaded(String message) {

    }

    @Override
    public void onFileDownloaded(String message) {

    }

    @Override
    public void onServerError(String message) {

    }


}
