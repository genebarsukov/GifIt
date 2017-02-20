package codewrencher.gifit.objects.complex.gif_chain;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Gene on 4/17/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 */
public class User extends UpdatableDataObject {

    public static final String ID_FIELD_NAME = "user_id";
    public static final String TABLE = "DEF_USER";
    public static final String DATA_PATH = "GetTableData";

    private String user_type;
    private String user_chain_action;
    private String first_name;
    private String last_name;
    private String phone_number;
    private String email;
    private String password;
    private String registered_on;
    private String updated_on;

    /***********************************************************************************************
     * Constructor
     * Initiate a complete User by parsing a complete Json object
     * @param user_object JSONObject: Json object representing all user data
     */
    public User( JSONObject user_object ) throws JSONException {
        super( user_object.getString( ID_FIELD_NAME ), ID_FIELD_NAME, TABLE, DATA_PATH );

        this.user_type = user_object.getString("user_type");
        this.user_chain_action = user_object.getString("user_chain_action");
        this.name = user_object.getString("user_name");
        this.first_name = user_object.getString("first_name");
        this.last_name = user_object.getString("last_name");
        this.phone_number = user_object.getString("phone_number");
        this.email = user_object.getString("email");
        this.password = user_object.getString("password");
        this.registered_on = user_object.getString("registered_on");
        this.updated_on = user_object.getString("updated_on");
    }

    public String getUserType() {
        return this.user_type;
    }
    public String getFirstName() {
        return this.first_name;
    }
    public String getLastName() {
        return this.last_name;
    }

}
