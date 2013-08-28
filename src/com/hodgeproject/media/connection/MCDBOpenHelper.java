package com.hodgeproject.media.connection;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * OpenHelper for the MediaController Database
 * Currently holds data for the Favorite Server List
 * @author BMCJ
 *
 */
public class MCDBOpenHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "MEDIACONTROLLER_DB"; 
    public static final String TABLE_NAME = "FAVORITES_TABLE";
    public static final int VERSION = 1;
    
    public static final String KEY_ID = "_id";
    public static final String SNAME = "S_NAME";
    public static final String SADDR = "S_ADDR";
    public static final String SPSWD = "S_PSWD"; //TODO: make sure this is hashed SHA-256
    private static final String SCRIPT = "create table " + TABLE_NAME + " ("
            + KEY_ID + " integer primary key autoincrement, " + SNAME
            + " text not null, " + SADDR + " text not null, "+ SPSWD + " text);";

	public MCDBOpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(SCRIPT);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("drop table " + TABLE_NAME);
		onCreate(db);
	}

}
