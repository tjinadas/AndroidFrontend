package com.goofood.gofood.com.goofood.gofood.androidsql;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Thanuj on 5/14/2016.
 */
public class CustomerDatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "CustomerAuth.db";
    public static final String TABLE_NAME = "Auth";
    public static final String COLUMN_NAME_TOKEN= "token";

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAME_TOKEN + " TEXT)";

    public static final String SQL_UPGRADE_TABLE = "DROP TABLE IF EXISTS" + TABLE_NAME ;

    public CustomerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the database to contain the data for the projects
        db.execSQL(SQL_CREATE_TABLE);
        //initializeData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Logs that the database is being upgraded
        db.execSQL(SQL_UPGRADE_TABLE);
        onCreate(db);
        Log.i(CustomerDatabaseHelper.class.getSimpleName(),
                "Upgrading database from version " + oldVersion + " to " + newVersion);
    }


   public boolean insertCustomerData(String jwt){
       SQLiteDatabase db = this.getWritableDatabase();
       ContentValues contentValues = new ContentValues();
       contentValues.put(COLUMN_NAME_TOKEN,jwt);
       long result = db.insert(TABLE_NAME,null,contentValues);
       if(result == -1)
            return false;
       else
           return true;
   }



}
