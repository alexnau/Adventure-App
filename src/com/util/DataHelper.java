package com.util;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DataHelper {
	//private static final String LOG_TAG = "DataHelper";

	private static final String DATABASE_NAME = "sc2ranks.db";
	private static final int DATABASE_VERSION = 6;
	
	private static final String CREATE_CHARACTERS = "CREATE TABLE " + Table.CHARACTERS + "(" + 
		Column.PRIMARY_KEY + " INTEGER PRIMARY KEY, " + 
		Column.NAME + " TEXT, " + 
		Column.PORTRAIT_BASE_ID + " INT, " + 
		Column.PORTRAIT_ID + " INT, " + 
		Column.DATE + " TEXT)";

	private static final String CREATE_TEAM_VIEW_HISTORY = "CREATE TABLE " + Table.TEAM_VIEW_HISTORY + "(" + 
		Column.PRIMARY_KEY + " INTEGER PRIMARY KEY, " + 
		Column.TEAM_ID + " INT)";
	
	private static final String CREATE_CACHED_CHARACTER  = "CREATE TABLE " + Table.CACHED_CHARACTER + "(" + 
		Column.PRIMARY_KEY + " INTEGER PRIMARY KEY, " + 
		Column.CHARACTER_DATA + " TEXT, " + 
		Column.DATE + " TEXT)";
	
	private static final String CREATE_CACHED_TEAM = "CREATE TABLE " + Table.CACHED_TEAM + "(" + 
		Column.PRIMARY_KEY + " INTEGER PRIMARY KEY, " + 
		Column.TEAM_DATA + " TEXT, " +
		Column.COUNT + " INTEGER DEFAULT 1)";

	public static class Column implements BaseColumns {
		public static final String CHARACTER_DATA = "character_data";
		public static final String COUNT = "count";
		public static final String DATE = "date";
		public static final String NAME = "name";
		public static final String PHONEID = "phoneID";
		public static final String PORTRAIT = "portrait";
		public static final String PORTRAIT_BASE_ID = "portrait_base_id";
		public static final String PORTRAIT_ID = "portrait_id";
		public static final String PRIMARY_KEY = "id";
		public static final String TEAM_DATA = "team_data";
		public static final String TEAM_ID = "team_id";
	}
	
	public static class Table implements BaseColumns {
		public static final String CACHED_CHARACTER = "cached_characters_table";
		public static final String CACHED_TEAM = "cached_team";
		public static final String CHARACTERS = "characters_table";
		public static final String PHONEID = "phoneid_table";
		public static final String TEAM_VIEW_HISTORY = "team_view_history_table";
	}

	private Context context;
	private SQLiteDatabase db;

	public DataHelper(Context context) {
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
	}
	
	public void close() {
		
		db.close();
	}
	
	public boolean contains(String table, int id) {
		boolean r;
		
		db.beginTransaction();
		try {
			r = db.query(table, new String[] { Column.PRIMARY_KEY }, Column.PRIMARY_KEY + " = ?", new String[] { String.valueOf(id) }, null, null, null).getCount() > 0;
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		return r;
	}
	
	/**
	 * Delete a single row with the given id
	 * @param table - the table to delete from
	 * @param id - the primary key of the row to delete
	 */
	public void delete(String table, int id) {
		db.beginTransaction();
		try {
			db.delete(table, Column.PRIMARY_KEY + " = ?", new String[] { String.valueOf(id) });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Deletes the specified table
	 * 
	 * @param table
	 *            - the table to delete
	 */
	public void deleteAll(String table) {
		this.db.delete(table, null, null);
	}

	/**
	 * Inserts the contents of values into the specified table
	 * 
	 * @param table
	 *            - the table to insert into
	 * @param values
	 *            - the values to insert
	 * @return - -1 if failure, the id if successful
	 * @throws SQLException
	 */
	public long insert(String table, ContentValues values) {
		long r = 0;

		db.beginTransaction();
		try {
			r = db.insert(table, Column.NAME, values);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			return -1;
		} finally {
			db.endTransaction();
		}

		return r;
	}
	
	/**
	 * Searches the specified table
	 * 
	 * @param table
	 *            - the table name to compile the query against
	 * @param columns
	 *            - a list of which columns to return. Passing null will return all columns, which is discouraged to prevent reading data from storage that isn't going to be used.
	 * @param selection
	 *            - a filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given table.
	 * @param selectionArgs
	 *            - you may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
	 * @param groupBy
	 *            - a filter declaring how to group rows, formatted as an SQL GROUP BY clause (excluding the GROUP BY itself). Passing null will cause the rows to not be grouped.
	 * @param having
	 *            - a filter declare which row groups to include in the cursor, if row groupings is being used, formatted as an SQL HAVING clause (excluding the HAVING itself). Passing null will cause all row groups to be included, and is required when grouping is not being used.
	 * @param orderBy
	 *            - How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort order, which may be unordered.
	 * @return - the Cursor to the returned tuples
	 * @throws SQLException
	 */
	public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
		Cursor c = null;

		db.beginTransaction();
		try {
			c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		return c;
	}

	/**
	 * Selects all the given columns
	 * 
	 * @param table
	 *            - the table to query
	 * @param columns
	 *            - the columns to return
	 * @param orderBy
	 *            - orderBy clause to tell how to order the results
	 * @return - an ArrayList of String[] objects containing the requested
	 *         columns
	 */
	public List<String[]> selectAll(String table, String[] columns, String orderBy) {

		List<String[]> list = new ArrayList<String[]>();
		Cursor cursor = db.query(table, columns, null, null, null, null, orderBy);
		if (cursor.moveToFirst()) {
			do {
				String[] a = new String[cursor.getColumnCount()];
				for (int i = 0; i < cursor.getColumnCount(); i++)
					a[i] = cursor.getString(i);
				list.add(a);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}
	
	/**
	 * Update an entry in the given table based on the id
	 * @param table - the table to update
	 * @param columns - the columns to update
	 * @param id - the primary key of the row to update
	 */
	public void update(String table, ContentValues columns, int id) {
		db.beginTransaction();
		try {
			db.update(table, columns, Column.PRIMARY_KEY + " = ?", new String[] { String.valueOf(id) });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private static class OpenHelper extends SQLiteOpenHelper {

		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_CHARACTERS);
			db.execSQL(CREATE_TEAM_VIEW_HISTORY);
			db.execSQL(CREATE_CACHED_CHARACTER);
			db.execSQL(CREATE_CACHED_TEAM);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			switch (oldVersion) {
			case 0:
			case 1:
				db.execSQL("DROP TABLE IF EXISTS " + Table.CHARACTERS);
				onCreate(db);
			case 2:
			case 3:
				db.execSQL("DROP TABLE IF EXISTS " + Table.PHONEID);
			case 4:
				db.execSQL(CREATE_TEAM_VIEW_HISTORY);
			case 5:
				db.execSQL(CREATE_CACHED_CHARACTER);
				db.execSQL(CREATE_CACHED_TEAM);
			}
		}
	}
}