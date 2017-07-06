package kau.easystudio.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jay lee on 2016-04-28.
 */


public class ProjectsDBManager extends SQLiteOpenHelper {
    static final String DB_PROJECTS = "Projects.db";
    static final String TABLE_PROJECTS = "Projects";
    static final int DB_VERSION = 1;

    Context mContext = null;
    SQLiteDatabase database;
    private static ProjectsDBManager mDbManager = null; //Singleton pattern 사용

    public static ProjectsDBManager getInstance(Context context) {
        if (mDbManager == null) {
            mDbManager = new ProjectsDBManager(context,
                    DB_PROJECTS,
                    null,
                    DB_VERSION);
        }

        return mDbManager;
    }

    private ProjectsDBManager(Context context, String dbName, CursorFactory factory, int version) {
        super(context, dbName, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PROJECTS +
                "(" + "project_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "project_name TEXT," +
                "project_type INTEGER);");
        database = db;
    }

    public void makeNewProcessTable(String clip_table_name, Integer project_id) {
        database.execSQL("CREATE TABLE IF NOT EXISTS " + clip_table_name +project_id.toString()+
                "(" + "clip_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "clip_sequence INTEGER, " +
                "media_address TEXT," +
                "clip_start_time DOUBLE," +
                "clip_end_time DOUBLE);");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {
    }

    public long insert(ContentValues addRowValue, String table_name) {
        return getWritableDatabase().insert(table_name,
                null,
                addRowValue);
    }

    public int insertAll(ContentValues[] values, String table_name) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        for (ContentValues contentValues : values) {
            db.insert(table_name, null, contentValues);
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        return values.length;
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return getReadableDatabase().query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public int update(ContentValues updateRowValue, String whereClause, String[] whereArgs, String table_name) {
        return getWritableDatabase().update(table_name, updateRowValue, whereClause, whereArgs);
    }

    public int delete(String whereClause, String[] whereArgs, String table_name) {
        return getWritableDatabase().delete(table_name, whereClause, whereArgs);
    }
}