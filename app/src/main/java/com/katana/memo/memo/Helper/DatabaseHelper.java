package com.katana.memo.memo.Helper;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.katana.memo.memo.Models.MemoModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 18;

    // Database Name
    private static final String DATABASE_NAME = "user_notes";

    // Table names
    private static final String TABLE_MEMO = "memo";


    // Memo Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_BODY = "body";
    private static final String KEY_FAVORITE = "favorite";
    private static final String KEY_WIDGET = "widget_id";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_AUDIO = "audio";
    private static final String KEY_DATE = "timestamp";
    private static final String KEY_LOCATION = "location";


    // SQL queries
    private static final String CREATE_MEMO_TABLE = "CREATE TABLE " + TABLE_MEMO + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_TITLE + " TEXT,"
            + KEY_FAVORITE + " INTEGER,"
            + KEY_WIDGET + " INTEGER,"
            + KEY_IMAGE + " TEXT,"
            + KEY_AUDIO + " TEXT,"
            + KEY_DATE + " TEXT,"
            + KEY_LOCATION + " TEXT,"
            + KEY_BODY + " TEXT );";

    private static final String DROP_MEMO_TABLE = "DROP TABLE IF EXISTS " + TABLE_MEMO;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MEMO_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL(DROP_MEMO_TABLE);
        // Create tables again
        onCreate(db);
    }


    /**
     * Storing memo details in database
     */
    public void addMemo(String title, String body, int memoId, ArrayList<String> imagePaths, ArrayList<String> audioPaths, String location) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, title);
        values.put(KEY_BODY, body);
        values.put(KEY_ID, memoId);
        values.put(KEY_FAVORITE, 0);
        values.put(KEY_WIDGET, -1);
        values.put(KEY_DATE, getDateTime());

        if (location.length() > 0) {
            values.put(KEY_LOCATION, location);
        } else {
            values.put(KEY_LOCATION, "No location");
        }

        if (imagePaths.size() > 0) {
            String[] allPaths = imagePaths.toArray(new String[0]);

            StringBuilder builder = new StringBuilder();
            for (String item : allPaths) {
                builder.append(",").append(item);
            }

            String paths = builder.deleteCharAt(0).toString();
            values.put(KEY_IMAGE, paths);
        }

        if (audioPaths.size() > 0) {
            String[] allPaths = audioPaths.toArray(new String[0]);

            StringBuilder builder = new StringBuilder();
            for (String item : allPaths) {
                builder.append(",").append(item);
            }

            String paths = builder.deleteCharAt(0).toString();
            values.put(KEY_AUDIO, paths);

        }

        db.insert(TABLE_MEMO, null, values);
        db.close();
    }


    public void changeMemo(String title, String body, int Id, ArrayList<String> imagePaths, ArrayList<String> audioPaths, String location) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(KEY_TITLE, title);
        cv.put(KEY_BODY, body);
        cv.put(KEY_ID, Id);

        if (location.length() > 0) {
            cv.put(KEY_LOCATION, location);
        } else {
            cv.put(KEY_LOCATION, "No location");
        }

        if (imagePaths.size() == 0) {
            cv.put(KEY_IMAGE, "No images found");
        } else {

            String[] allPaths = imagePaths.toArray(new String[0]);

            StringBuilder builder = new StringBuilder();
            for (String item : allPaths) {
                builder.append(",").append(item);
            }

            String paths = builder.deleteCharAt(0).toString();
            cv.put(KEY_IMAGE, paths);
        }

        if (audioPaths.size() == 0) {
            cv.put(KEY_AUDIO, "No audios found");
        } else {
            String[] allPaths = audioPaths.toArray(new String[0]);

            StringBuilder builder = new StringBuilder();

            for (String item : allPaths) {
                builder.append(",").append(item);
            }

            String paths = builder.deleteCharAt(0).toString();

            cv.put(KEY_AUDIO, paths);

        }

        try {
            db.update(TABLE_MEMO, cv, KEY_ID + "=?", new String[]{"" + Id});
        } catch (SQLException e) {
            e.printStackTrace();
        }

        db.close();

    }

    public ArrayList<String> getMemoTitles() {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + KEY_TITLE + " FROM " + TABLE_MEMO;

        Cursor c = db.rawQuery(query, null);

        ArrayList<String> titles = new ArrayList<>();

        c.moveToFirst();

        for (int i = 0; i < c.getCount(); i++) {
            titles.add(c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_TITLE))));
            Log.d("Titles", c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_TITLE))));
            c.moveToNext();
        }

        c.close();
        db.close();
        return titles;

    }


    /**
     * Get specific memo from database
     */

    public int getMemoIdFromTitle(String title) {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + KEY_ID + " FROM " + TABLE_MEMO + " WHERE " + KEY_TITLE + "=\"" + title + "\";";
        Cursor c = db.rawQuery(query, null);

        if (c.getCount() > 0 && c.moveToFirst()) {
            return c.getInt(c.getColumnIndexOrThrow(String.valueOf(KEY_ID)));
        } else {
            return 0;
        }

    }

    public String getSpecificMemoDate(int Id) {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + KEY_DATE + " FROM " + TABLE_MEMO + " WHERE " + KEY_ID + " = " + Id + ";";
        Cursor c = db.rawQuery(query, null);

        if (c.getCount() > 0 && c.moveToFirst()) {
            return c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_DATE)));
        } else {
            db.close();
            return "";
        }

    }

    public String getSpecificMemoTitle(int Id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + KEY_TITLE + " FROM " + TABLE_MEMO + " WHERE " + KEY_ID + " = \"" + Id + "\";";
        Cursor c = db.rawQuery(query, null);

        if (c.getCount() > 0 && c.moveToFirst()) {

            return c.getString(c.getColumnIndex(String.valueOf(KEY_TITLE)));
        } else {
            c.close();
            return "Title not found!";
        }

    }

    public String getSpecificMemoBody(int Id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + KEY_BODY + " FROM " + TABLE_MEMO + " WHERE " + KEY_ID + " = \"" + Id + "\";";
        Cursor c = db.rawQuery(query, null);
        if (c.getCount() > 0 && c.moveToFirst()) {
            db.close();
            return c.getString(c.getColumnIndex(String.valueOf(KEY_BODY)));
        } else {
            db.close();
            c.close();
            return "Body not found!";
        }
    }

    public ArrayList<String> getSpecificImagePaths(int Id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + KEY_IMAGE + " FROM " + TABLE_MEMO + " WHERE " + KEY_ID + " = " + Id + ";";

        Cursor c = db.rawQuery(query, null);
        if (c.getCount() > 0 && c.moveToFirst() && c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_IMAGE))) != null) {
            String[] paths = c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_IMAGE))).split(",");
            db.close();
            return new ArrayList<>(Arrays.asList(paths));
        } else {
            ArrayList<String> notFound = new ArrayList<>();
            notFound.add("No images found");
            db.close();
            c.close();
            return notFound;
        }

    }

    public ArrayList<String> getSpecificAudioPaths(int Id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + KEY_AUDIO + " FROM " + TABLE_MEMO + " WHERE " + KEY_ID + " = " + Id + ";";
        Cursor c = db.rawQuery(query, null);

        if (c.getCount() > 0 && c.moveToFirst() && c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_AUDIO))) != null) {
            String[] paths = c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_AUDIO))).split(",");
            db.close();

            return new ArrayList<>(Arrays.asList(paths));
        } else {
            ArrayList<String> notFound = new ArrayList<>();

            notFound.add("No audios found");
            c.close();
            db.close();
            return notFound;
        }
    }

    public String getSpecificLocation(int Id) {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + KEY_LOCATION + " FROM " + TABLE_MEMO + " WHERE " + KEY_ID + " = " + Id + ";";

        Cursor c = db.rawQuery(query, null);

        if (c.getCount() > 0 && c.moveToFirst() && !c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_LOCATION))).equals("No location")) {

            String location = c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_LOCATION)));

            db.close();

            return location;
        } else {
            db.close();
            return "No location found";
        }

    }

    public int getTheAmountOfMemos() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_MEMO, null);
        int number = c.getCount();
        if (number > 0) {
            return number;
        } else {
            c.close();
            return 0;
        }
    }


    /**
     * Recreate database Delete all tables and create them again
     */
    public void deleteMemos() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL(DROP_MEMO_TABLE);
            db.execSQL(CREATE_MEMO_TABLE);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        db.close();
    }

    public void deleteSpecificMemo(int Id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int prevAmountOfMemos = this.getTheAmountOfMemos();

        try {
            db.delete(TABLE_MEMO, KEY_ID + "=?", new String[]{"" + Id});
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (Id != prevAmountOfMemos || this.getTheAmountOfMemos() != 0) {
                ContentValues contentValues = new ContentValues();

//                for (int i = (Id + 1); i < ((this.getTheAmountOfMemos() - (Id - 1)) + (Id + 1)); i++) {
//                    contentValues.put(KEY_ID, (i - 1));
//                    try {
//                        db.update(TABLE_MEMO, contentValues, KEY_ID + " =? ", new String[]{"" + i});
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    }
//                }
                if (Id != 1) {
                    for (int i = (Id + 1); i < ((this.getTheAmountOfMemos() - (Id - 1)) + (Id + 1)); i++) {
                        contentValues.put(KEY_ID, (i - 1));
                        try {
                            db.update(TABLE_MEMO, contentValues, KEY_ID + " =? ", new String[]{"" + i});
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    for (int i = 2; i <= prevAmountOfMemos; i++) {
                        contentValues.put(KEY_ID, (i - 1));
                        try {
                            db.update(TABLE_MEMO, contentValues, KEY_ID + " =? ", new String[]{"" + i});
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }

    }

    public boolean checkMemoForPhoto(int Id) {

        boolean hasPhoto = false;

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + KEY_IMAGE + " FROM " + TABLE_MEMO + " WHERE " + KEY_ID + " = " + Id;

        Cursor c = db.rawQuery(query, null);

        if (c.getCount() > 0 && c.moveToFirst() && c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_IMAGE))) != null) {
            String imgName = c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_IMAGE)));

            String[] paths = imgName.split(",");

            for (String path : paths) {
                if (!hasPhoto) {
                    hasPhoto = path.startsWith("KukaMemo");
                }
            }

        }

        c.close();


        return hasPhoto;

    }

    public boolean checkMemoForDrawing(int Id) {
        boolean hasDrawing = false;

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + KEY_IMAGE + " FROM " + TABLE_MEMO + " WHERE " + KEY_ID + " = " + Id + ";";

        Cursor c = db.rawQuery(query, null);

        if (c.getCount() > 0 && c.moveToFirst() && c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_IMAGE))) != null) {
            String drawingName = c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_IMAGE)));

            String[] paths = drawingName.split(",");

            for (String path : paths) {
                if (!hasDrawing) {
                    hasDrawing = path.startsWith("noteImage");
                }
            }


        }


        c.close();


        return hasDrawing;
    }

    public boolean checkMemoForAddedImages(int Id) {
        boolean hasAddedImages = false;
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + KEY_IMAGE + " FROM " + TABLE_MEMO + " WHERE " + KEY_ID + " = " + Id;

        Cursor c = db.rawQuery(query, null);

        if (c.getCount() > 0 && c.moveToFirst() && c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_IMAGE))) != null) {
            String addedImageName = c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_IMAGE)));

            String paths[] = addedImageName.split(",");

            for (String path : paths) {
                if (!hasAddedImages) {
                    hasAddedImages = path.endsWith("memoAddedImage");
                }
            }
        }

        c.close();


        return hasAddedImages;
    }

    public boolean checkMemoForAudios(int Id) {
        boolean hasAudio = false;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + KEY_AUDIO + " FROM " + TABLE_MEMO + " WHERE " + KEY_ID + " = " + Id + ";";

        Cursor c = db.rawQuery(query, null);

        if (c.getCount() > 0 && c.moveToFirst() && c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_AUDIO))) != null) {
            String addedAudioName = c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_AUDIO)));

            String paths[] = addedAudioName.split(",");

            for (String path : paths) {
                if (!hasAudio) {
                    hasAudio = path.startsWith("MemoAudioRecord");
                }
            }
        }

        return hasAudio;
    }

    // Widget methods

    public void setKeyWidget(int Id, int widgetId) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.execSQL("UPDATE " + TABLE_MEMO + " SET " + KEY_WIDGET + " = " + widgetId + " WHERE " + KEY_ID + " = " + Id);
            Log.d("WidgetSts", "Added");
        } catch (SQLException e) {
            e.printStackTrace();
            Log.d("WidgetSts", "Failed to add");
        }
        db.close();
    }

    public void removeKeyWidget(int widgetId) {
        SQLiteDatabase db = getWritableDatabase();
        int defWidgetId = -1;

        String query = "SELECT " + KEY_WIDGET + " FROM " + TABLE_MEMO + " WHERE " + KEY_WIDGET + " = " + widgetId;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        if (c.getCount() > 0 && c.getInt(c.getColumnIndexOrThrow(String.valueOf(KEY_WIDGET))) != -1) {
            try {
                db.execSQL("UPDATE " + TABLE_MEMO + " SET " + KEY_WIDGET + " = " + defWidgetId + " WHERE " + KEY_WIDGET + " = " + widgetId);
                Log.d("WidgetSts", "Removed");
            } catch (SQLException e) {
                e.printStackTrace();
                Log.d("WidgetSts", "Failed to remove");
            }

        }

        c.close();
        db.close();
    }

    public int getSpecificWidgetId(int Id) {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + KEY_WIDGET + " FROM " + TABLE_MEMO + " WHERE " + KEY_ID + " = " + Id;

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        if (c.getInt(c.getColumnIndexOrThrow(String.valueOf(KEY_WIDGET))) != -1) {
            return c.getInt(c.getColumnIndexOrThrow(String.valueOf(KEY_WIDGET)));
        } else {
            c.close();
            db.close();
            return -1;
        }
    }

    // Favorite notes methods

    public void favoriteMemo(int Id) {
        SQLiteDatabase db = getWritableDatabase();

        if (!checkIfFavorite(Id)) {
            try {
                db.execSQL("UPDATE " + TABLE_MEMO + " SET " + KEY_FAVORITE + " = 1 WHERE " + KEY_ID + " = " + Id);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try {
                db.execSQL("UPDATE " + TABLE_MEMO + " SET " + KEY_FAVORITE + " = 0 WHERE " + KEY_ID + " = " + Id);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }

    public boolean checkIfFavorite(int Id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " + KEY_FAVORITE + " FROM " + TABLE_MEMO + " WHERE " + KEY_ID + " = " + Id, null);
        c.moveToFirst();
        return c.getInt(c.getColumnIndexOrThrow(String.valueOf(KEY_FAVORITE))) != 0;
    }


    private int getTheAmountOfFavoriteMemos() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_MEMO + " WHERE " + KEY_FAVORITE + " = 1", null);
        int number = c.getCount();
        if (number > 0) {
            return number;
        } else {
            return 0;
        }
    }

    public ArrayList<String> getFavoriteMemoTitles() {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + KEY_TITLE + " FROM " + TABLE_MEMO + " WHERE " + KEY_FAVORITE + " = 1;";
        Cursor c = db.rawQuery(query, null);
        ArrayList<String> list = new ArrayList<>(c.getCount());

        if (c.getCount() > 0 && c.moveToFirst()) {
            do {
                list.add(c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_TITLE))));
            } while (c.moveToNext());

        }

        c.close();


        return list;
    }

    public ArrayList<MemoModel> getFavoriteMemos() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_MEMO + " WHERE " + KEY_FAVORITE + " = 1;", null);
        ArrayList<MemoModel> list = new ArrayList<>(getTheAmountOfFavoriteMemos());
        if (c.getCount() > 0 && c.moveToFirst()) {

            do {

                String title = c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_TITLE)));
                String body = c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_BODY)));
                int id = c.getInt(c.getColumnIndexOrThrow(String.valueOf(KEY_ID)));
                list.add(new MemoModel(title, body, id));

            } while (c.moveToNext());

        }

        c.close();


        return list;
    }

    public int getFavoriteMemoId(String title) {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + KEY_ID + " FROM " + TABLE_MEMO + " WHERE " + KEY_FAVORITE + " = 1 AND " + KEY_TITLE + " =\"" + title + "\";";
        Cursor c = db.rawQuery(query, null);

        c.moveToFirst();

        return c.getInt(c.getColumnIndexOrThrow(String.valueOf(KEY_ID)));


    }

    public String getFavoriteMemoLocation(String title) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + KEY_LOCATION + " FROM " + TABLE_MEMO + " WHERE " + KEY_LOCATION + " = 1 AND " + KEY_TITLE + " = \"" + title + "\";";

        Cursor c = db.rawQuery(query, null);

        if (c.getCount() > 0 && c.moveToFirst() && !c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_LOCATION))).equals("No location")) {

            String location = c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_LOCATION)));

            return location;

        } else {
            return "No location";
        }


    }

    public String getFavoriteMemoBody(String title) {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + KEY_BODY + " FROM " + TABLE_MEMO + " WHERE " + KEY_FAVORITE + " = 1 AND " + KEY_TITLE + " =\"" + title + "\";";
        Cursor c = db.rawQuery(query, null);

        c.moveToFirst();

        return c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_BODY)));


    }

    public String getFavoriteMemoDate(String title) {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + KEY_DATE + " FROM " + TABLE_MEMO + " WHERE " + KEY_FAVORITE + " = 1 AND " + KEY_TITLE + " =\"" + title + "\";";
        Cursor c = db.rawQuery(query, null);

        c.moveToFirst();

        return c.getString(c.getColumnIndexOrThrow(String.valueOf(KEY_DATE)));

    }

    public void clearFavoriteMemos() {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put(KEY_FAVORITE, 0);
        try {
            db.update(TABLE_MEMO, cv, KEY_FAVORITE + " = " + 1, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}