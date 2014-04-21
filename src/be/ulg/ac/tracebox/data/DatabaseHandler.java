package be.ulg.ac.tracebox.data;

import android.database.Cursor;

import java.util.Random;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
	 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "tracebox_database";
 
    // Table names
    private static final String TABLE_DESTINATIONS = "destinations";
    private static final String TABLE_PROBES = "probes";
    private static final String TABLE_ROUTERS = "routers";
    private static final String TABLE_PACKETMODIFICATIONS = "packetmodifications";
    private static final String TABLE_LOGS = "logs";
 
    private static final String KEY_ID = "id";

    // Destinations Table Columns names
    private static final String KEY_NAME = "name";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_CUSTOM_DESTINATION = "custom_destination";

    // Logs Table Columns names
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_DATE = "date";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

 // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

    	// Create database for destinations
        String CREATE_DESTINATIONS_TABLE = "CREATE TABLE " + TABLE_DESTINATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_ADDRESS + " TEXT,"
                + KEY_CUSTOM_DESTINATION + " INT" +  ")";

        db.execSQL(CREATE_DESTINATIONS_TABLE);

        // Logs
        String CREATE_LOGS_TABLE = "CREATE TABLE " + TABLE_LOGS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_MESSAGE + " TEXT,"
                + KEY_DATE + " TEXT" +  ")";

        db.execSQL(CREATE_LOGS_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    	// Drop older destinations table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DESTINATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
 
        // Create tables again
        onCreate(db);
    }

    //---------- DESTINATIONS ------------
    // Adding new destination
    public void addDestination(Destination d) {
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, d.getName()); // Name
        values.put(KEY_ADDRESS, d.getAddress()); // Name
        if (d.isDestinationCustom())
        	values.put(KEY_CUSTOM_DESTINATION, 1); // Custom dest
        else
        	values.put(KEY_CUSTOM_DESTINATION, 2); // Not custom dest

        // Inserting Row
        db.insert(TABLE_DESTINATIONS, null, values);
        db.close(); // Closing database connection
    }

    // Get all destinations
    public Vector<Destination> getAllDestinations()
    {
        Vector<Destination> destinations = new Vector<Destination>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DESTINATIONS;
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (((android.database.Cursor) cursor).moveToFirst()) {
            do {
            	Destination currentDestination;

            	if (cursor.getInt(3) == 1)
            		currentDestination = new Destination(cursor.getString(1), cursor.getString(2), true);
            	else
            		currentDestination = new Destination(cursor.getString(1), cursor.getString(2), false);
            		
                destinations.add(currentDestination);
            } while (cursor.moveToNext());
        }
     
        db.close(); // Closing database connection
        return destinations;
    }

    public int getNumberOfDestinations()
    {
    	String countQuery = "SELECT * FROM " + TABLE_DESTINATIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
 
        return cursor.getCount();
    }
    // Get a destination by id
    public Destination getDestination(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_DESTINATIONS, new String[] { KEY_ID,
                KEY_NAME, KEY_ADDRESS, KEY_CUSTOM_DESTINATION }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        Destination dest = new Destination(cursor.getString(1), cursor.getString(2));
        dest.setId(Integer.parseInt(cursor.getString(0)));
        cursor.close();

        return dest;
    }

    // Get random destination
    public Destination getRandomDestination()
    {
    	Random rand = new Random();
	    int num = rand.nextInt(500) + 1;
		return this.getDestination(num);
    }

    // Delete non custom destinations
    public void deleteNonCustomDestinations()
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.delete(TABLE_DESTINATIONS, KEY_CUSTOM_DESTINATION + "!=1", null);
    	db.close(); // Closing database connection
    }

    // Delete all destinations
    public void deleteAllDestinations()
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.delete(TABLE_DESTINATIONS, null, null);
    	db.close(); // Closing database connection
    }
    
    //---------- LOGS ------------
    // Adding new log
    public void addLog(Log l) {
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGE, l.getMessage()); // Name
        values.put(KEY_DATE, l.getDate().toString()); // Date

        // Inserting Row
        db.insert(TABLE_LOGS, null, values);
        db.close(); // Closing database connection
    }

    // Get all destinations
    public Vector<Log> getAllLogs()
    {
        Vector<Log> logs = new Vector<Log>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LOGS;
     
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
     
        // looping through all rows and adding to list
        if (((android.database.Cursor) cursor).moveToFirst()) {
            do {
            	Log currentLog;

            	currentLog = new Log(cursor.getString(1), cursor.getString(2));
            	logs.add(currentLog);
            } while (cursor.moveToNext());
        }
     
        db.close(); // Closing database connection
        return logs;
    }

    // Delete all destinations
    public void deleteAllLogs()
    {
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.delete(TABLE_LOGS, null, null);
    	db.close(); // Closing database connection
    }
}
