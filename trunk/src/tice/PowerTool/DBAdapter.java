package tice.PowerTool;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {

	public static final String KEY_LABEL = "label";
	public static final String KEY_PACKAGENAME = "package";
	public static final String KEY_NAME = "name";
	public static final String KEY_HOUR = "Hour";
	public static final String KEY_MINUTE = "Minute";
	public static final String KEY_ROWID = "_id";

	private static final String TAG = "DBAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_CREATE_TIMES = "create table times (_id integer primary key autoincrement, "
			+ "Hour integer not null, Minute integer not null);";
	private static final String DATABASE_CREATE_APPS = "create table apps (_id integer primary key autoincrement, "
		+ "name string not null, label string not null, package string not null);";

	
	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE_TIMES = "times";
	private static final String DATABASE_TABLE_APPS = "apps";
	private static final int DATABASE_VERSION = 3;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(DATABASE_CREATE_TIMES);
			db.execSQL(DATABASE_CREATE_APPS);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS times");
			db.execSQL("DROP TABLE IF EXISTS apps");
			onCreate(db);
		}
	}

	public DBAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public DBAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public long createTime(int hour, int minute) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_HOUR, hour);
		initialValues.put(KEY_MINUTE, minute);
		return mDb.insert(DATABASE_TABLE_TIMES, null, initialValues);
	}
	
	public boolean deleteTime(long rowId) {
		return mDb.delete(DATABASE_TABLE_TIMES, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	public Cursor fetchTime(long rowId) throws SQLException {

		Cursor mCursor =

		mDb.query(true, DATABASE_TABLE_TIMES, new String[] { KEY_ROWID, KEY_HOUR,
				KEY_MINUTE }, KEY_ROWID + "=" + rowId, null, null, null, null,
				null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	public Cursor fetchAllTimes() {
		return mDb.query(DATABASE_TABLE_TIMES, new String[] { KEY_ROWID, KEY_HOUR,
				KEY_MINUTE }, null, null, null, null, null);
	}

	public boolean updateTime(long rowId, int hour, int minute) {
		ContentValues args = new ContentValues();
		args.put(KEY_HOUR, hour);
		args.put(KEY_MINUTE, minute);

		return mDb.update(DATABASE_TABLE_TIMES, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	public long createAppName(String label, String packagename, String name) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_LABEL, label);
		initialValues.put(KEY_PACKAGENAME, packagename);
		initialValues.put(KEY_NAME, name);
		return mDb.insert(DATABASE_TABLE_APPS, null, initialValues);
	}
	
	public Cursor fetchAllAppNames() {
		return mDb.query(DATABASE_TABLE_APPS, new String[] { KEY_LABEL, KEY_PACKAGENAME, KEY_NAME }, null, null, null, null, null);
	}
	
	public Cursor fetchAppName(long rowId) throws SQLException {

		Cursor mCursor =

		mDb.query(true, DATABASE_TABLE_APPS, new String[] { KEY_ROWID, KEY_LABEL, KEY_PACKAGENAME, KEY_NAME }, KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}	
	
	public boolean findAppName(String name) throws SQLException {

		boolean ret = false;
		Cursor mCursor =

		mDb.query(true, DATABASE_TABLE_APPS, new String[] { KEY_NAME }, KEY_NAME + "='" + name + "'", null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		
		if(mCursor.getCount() > 0){
			ret = true;
		}
		
		if (mCursor != null) mCursor.close();
		return ret;

	}	
	
	public void DeleteAppNames() throws SQLException {
		mDb.delete(DATABASE_TABLE_APPS, null, null);
	}		
}