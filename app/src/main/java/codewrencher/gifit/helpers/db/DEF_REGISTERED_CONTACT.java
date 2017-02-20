package codewrencher.gifit.helpers.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Gene on 9/22/2015.
 */

public class DEF_REGISTERED_CONTACT extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "DEF_REGISTERED_CONTACT";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "gifit";

    private static final String CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " +
                    TABLE_NAME + "(" +
                    "user_id INTEGER PRIMARY KEY, " +
                    "user_name TEXT, " +
                    "first_name TEXT, " +
                    "last_name TEXT, " +
                    "phone_number TEXT, " +
                    "email TEXT, " +
                    "registered_on DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_on DATETIME DEFAULT CURRENT_TIMESTAMP " +
                    "); " +
                "CREATE INDEX email_index ON " + TABLE_NAME + " ( email ); " +
                "CREATE INDEX phone_index ON " + TABLE_NAME + " ( phone ); ";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public DEF_REGISTERED_CONTACT(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.beginTransaction();

        try {
            db.execSQL( CREATE_TABLE );
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(DROP_TABLE);
        onCreate(db);
    }
}