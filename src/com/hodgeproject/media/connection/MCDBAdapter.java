package com.hodgeproject.media.connection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Adapter for the Media Controller Database
 * Currently accesses data for the Favorite Server List
 * @author BMCJ
 *
 */
public class MCDBAdapter {
	SQLiteDatabase database_ob;
	MCDBOpenHelper openHelper_ob;
	Context context;
	
	public MCDBAdapter(Context c){
		context = c;
	}
	
	public MCDBAdapter openToRead(){
		openHelper_ob = new MCDBOpenHelper(context, MCDBOpenHelper.DATABASE_NAME, null, MCDBOpenHelper.VERSION);
		database_ob = openHelper_ob.getReadableDatabase();
		return this;
	}
	
	public MCDBAdapter openToWrite(){
		openHelper_ob = new MCDBOpenHelper(context, MCDBOpenHelper.DATABASE_NAME, null, MCDBOpenHelper.VERSION);
        database_ob = openHelper_ob.getWritableDatabase();
		return this;
	}
	
    public void close() {
        database_ob.close();
    }
    
    /**
     * Create a new Entry in the Table
     * @param sname
     * @param saddr
     * @param spswd
     * @return
     */
    public long insertEntry(String sname, String saddr, String spswd) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MCDBOpenHelper.SNAME, sname);
        contentValues.put(MCDBOpenHelper.SADDR, saddr);
        
        if(spswd != null) contentValues.put(MCDBOpenHelper.SPSWD, spswd);
        
        openToWrite();
        long val = database_ob.insert(MCDBOpenHelper.TABLE_NAME, null, contentValues);
        close();
        return val;
    }
    
    /**
     * Query every Entry in the table
     * @return
     */
    public Cursor queryAll(){
    	String[] cols = { MCDBOpenHelper.KEY_ID, MCDBOpenHelper.SNAME, MCDBOpenHelper.SADDR, MCDBOpenHelper.SPSWD };
        openToWrite();
        Cursor c = database_ob.query(MCDBOpenHelper.TABLE_NAME, cols, null, null, null, null, null);
    	return c;
    }
    
    /**
     * Query a given Entry (by ID) from Table
     * @param rowId
     * @return
     */
    public Cursor queryEntry(int rowId){
    	String[] cols = { MCDBOpenHelper.KEY_ID, MCDBOpenHelper.SNAME, MCDBOpenHelper.SADDR, MCDBOpenHelper.SPSWD };
        openToWrite();
        Cursor c = database_ob.query(MCDBOpenHelper.TABLE_NAME, cols, MCDBOpenHelper.KEY_ID + "=" + rowId, null, null, null, null);
    	return c;
    }
    
    /**
     * Update a given Entry (by ID) in Table
     * @param rowId
     * @param sname
     * @param saddr
     * @param spswd
     * @return
     */
    public long updateEntry(int rowId, String sname, String saddr, String spswd){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MCDBOpenHelper.SNAME, sname);
        contentValues.put(MCDBOpenHelper.SADDR, saddr);
        if(spswd != null) contentValues.put(MCDBOpenHelper.SPSWD, spswd);
        
        openToWrite();
        long val = database_ob.update(MCDBOpenHelper.TABLE_NAME, contentValues, MCDBOpenHelper.KEY_ID + "=" + rowId, null);
        close();
        return val;
    }
    
    /**
     * Delete a given Entry (by ID) from the table
     * @param rowId
     * @return
     */
    public int deleteEntry(int rowId) {
        openToWrite();
        int val = database_ob.delete(MCDBOpenHelper.TABLE_NAME, MCDBOpenHelper.KEY_ID + "=" + rowId, null);
        close();
        return val;
    }
}
