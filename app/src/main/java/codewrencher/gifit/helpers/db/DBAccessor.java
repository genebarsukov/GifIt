package codewrencher.gifit.helpers.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import codewrencher.gifit.tools.Printer;

/**
 * Created by Gene on 9/22/2015.
 */
public class DBAccessor {

    protected SQLiteDatabase db;
    protected DEF_REGISTERED_CONTACT def_registered_contact;

    public DBAccessor(Context context) {
        def_registered_contact = new DEF_REGISTERED_CONTACT(context);
    }

    /*************************************************************************************************************************************************************************
     * SELECT :                                                                                                                                                              *
     * ---------------------------------------------------------------------------------------------                                                                         *
     * Cursor cursor = SQLiteDatabase.query( String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy )       *
     *                                                            ( null = all columns )                                                                                     *
     * Cursor cursor = SQLiteDatabase.rawQuery(String sql, String[] selectionArgs)                                                                                           *
     * INSERT :                                                                                                                                                              *
     * ---------------------------------------------------------------------------------------------                                                                         *
     *	insert(String table, String nullColumnHack, ContentValues values)                                                                                                    *
     *                            ( Specify name of column into which a null value will be inserted in case all insert values are null, so as not to break it )              *
     * 	insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues, int conflictAlgorithm)                                                        *
     * GENERAL :	                                                                             ( CONFLICT_REPLACE, CONFLICT_IGNORE )                                       *
     * ---------------------------------------------------------------------------------------------                                                                         *
     * execSQL(String sql)                                                                                                                                                   *
     *************************************************************************************************************************************************************************/

    /**
     * MUST be called before using database
     * Opens the database connection
     * @throws SQLException
     */
    public void open() throws SQLException {
        db = def_registered_contact.getWritableDatabase();
        def_registered_contact.onCreate( db );
    }

    /**
     * DON't FORGET to close the database connection when done using it
     */
    public void close() {
        def_registered_contact.close();
    }

    /**
     * Refreshed the table when the app is upgraded
     * Calls the table object onUpgrade method which drops and recreates the table
     */
    public void onUpgrade() {
        def_registered_contact.onUpgrade(db, 1, 2);
    }

    /**
     * Insert a record into the specified table by passing a key - value hash map object
     * @param table String:             the table to query
     * @param record LinkedHashMap:     the hash map key - value record to insert
     */
    public void insertRecord( String table, LinkedHashMap <String, String> record ) {
        LinkedHashMap <String, String> record_copy = (LinkedHashMap) record.clone();

        record_copy = this.cleanRecordFields( table, record_copy );

        ContentValues insert_values = new ContentValues();

        for ( String record_key : record_copy.keySet() ) {
            insert_values.put( record_key, record.get( record_key ) );
        }
        db.insertWithOnConflict(table, null, insert_values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Get a single record from the database by specifying the id field name and value
     * @param table String:             the table to query
     * @param id_field_name String:     the name of the id field (could be any field)
     * @param id_field_value String:    the value of the above named field
     * @return  LinkedHashMap:      Returns a hash map with all the row key - value pairs
     */
    public LinkedHashMap <String, String> getRecordById( String table, String id_field_name, String id_field_value ) {
        LinkedHashMap record = new LinkedHashMap();

        Cursor cursor = db.query( table, null, id_field_name + " = ? ", new String[]{ id_field_value }, null, null, "first_name ASC");

        cursor.moveToFirst();
        record = cursorToRow( cursor );
        cursor.close();

        return record;
    }

    /**
     * Gets all records from the specified table
     * @param table String:             the table to query
     * @return   ArrayList:         returns a list of row objects
     */
    public ArrayList <LinkedHashMap <String, String>> getAllRecords( String table ) {
        ArrayList records = new ArrayList();

        Cursor cursor = db.query(table, null, null, null, null, null, "first_name ASC");

        cursor.moveToFirst();

        while (! cursor.isAfterLast()) {


            records.add( cursorToRow( cursor ) );
            cursor.moveToNext();
        }
        cursor.close();

        Printer.printBreak();
        Printer.printObjectList(records, "DB RECORDS");
        return records;
    }

    /**
     * Extracts all of the column names and associated values from the Cursor at a given row position
     * Returns a row object hash map
     * @param cursor  Cursor:     The SQL cursor from the result set
     * @return  LinkedHashMap:      Returns a hash map with all the row key - value pairs
     */
    public LinkedHashMap <String, String> cursorToRow ( Cursor cursor ) {
        LinkedHashMap row = new LinkedHashMap();

        int column_index = 0;
        for ( String column_name : cursor.getColumnNames() ) {

            row.put( column_name, cursor.getString( column_index ) );
            column_index ++;
        }
        return row;
    }

    /**
     * Removes keys from the insert object that are not present in the insert table
     * @param table String:             the table to query
     * @param record LinkedHashMap      the record object to clean
     * @return LinkedHashMap cleaned object
     */
    private LinkedHashMap <String, String> cleanRecordFields( String table, LinkedHashMap <String, String> record ) {

        // get all table fields in a String array
        Cursor cursor = db.query(table, null, null, null, null, null, null);
        String[] column_names = cursor.getColumnNames();

        // transform String array to HashMap
        LinkedHashMap <String, String> column_obj = new LinkedHashMap<>();
        for ( String column_name : column_names ) {
            column_obj.put( column_name, column_name );
        }

        // iterate over the record object keys and create a list if keys not present in the table, since we cannot iterate and delete concurrently
        ArrayList <String> keys_to_remove = new ArrayList<>();
        for ( String key : record.keySet() ) {

            if ( !column_obj.containsKey( key ) ) {
                keys_to_remove.add(key);
            }
        }

        // delete keys from record object that are not present in the column object
        for ( String key : keys_to_remove ) {
            record.remove( key );
        }

        return record;
    }
}