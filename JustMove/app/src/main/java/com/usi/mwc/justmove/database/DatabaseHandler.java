package com.usi.mwc.justmove.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

import com.usi.mwc.justmove.model.InterestPointModel;
import com.usi.mwc.justmove.model.PointModel;
import com.usi.mwc.justmove.model.TravelModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "JustMove";
    private static final String TRAVEL_TABLE = "t_travels";
    private static final String MARKERS_TABLE = "t_markers";
    private static final String POINTS_TABLE = "t_points";

    private static final String KEY_ID = "idTravel";
    private static final String KEY_NAME = "name";
    private static final String KEY_COMMENT = "comment";
    private static final String KEY_DISTANCE = "distance";
    private static final String KEY_TIME = "time";
    private static final String KEY_DATE = "dateTravel";

    private static final String KEY_ID_MARKER = "idMarker";
    private static final String KEY_NAME_MARKER = "name";
    private static final String KEY_LAT_MARKER = "latitude";
    private static final String KEY_LON_MARKER = "longitude";
    private static final String KEY_ID_TRAVEL_MARKER = "idTravel";

    private static final String KEY_ID_POINT = "idPoint";
    private static final String KEY_LAT_POINT = "latitude";
    private static final String KEY_LON_POINT = "longitude";
    private static final String KEY_NEXT_POINT = "idNextPoint";
    private static final String KEY_ID_TRAVEL_POINT = "idTravel";
    
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createMarkersTable = ("CREATE TABLE " + MARKERS_TABLE + "("
                + KEY_ID_MARKER + " INTEGER PRIMARY KEY, "
                + KEY_LAT_MARKER + " REAL, "
                + KEY_LON_MARKER + " REAL, "
                + KEY_NAME_MARKER + " TEXT, "
                + KEY_ID_TRAVEL_MARKER + " INT, "
                + "FOREIGN KEY(idTravel) REFERENCES t_travels(idTravels))");
        String createTravelTable = ("CREATE TABLE " + TRAVEL_TABLE + "("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_NAME + " TEXT, "
                + KEY_DISTANCE + " REAL, "
                + KEY_TIME + " TEXT, "
                + KEY_DATE + " TEXT, "
                + KEY_COMMENT + " TEXT)");
        String createPointsTable = ("CREATE TABLE " + POINTS_TABLE + "("
                + KEY_ID_POINT + " INTEGER PRIMARY KEY, "
                + KEY_LAT_POINT + " REAL, "
                + KEY_LON_POINT + " REAL, "
                + KEY_NEXT_POINT + " INT, "
                + KEY_ID_TRAVEL_POINT + " INT, "
                + "FOREIGN KEY(idTravel) REFERENCES t_travels(idTravels))");
        db.execSQL("PRAGMA foreign_keys = ON;");
        db.execSQL(createTravelTable);
        db.execSQL(createMarkersTable);
        db.execSQL(createPointsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TRAVEL_TABLE));
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", MARKERS_TABLE));
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", POINTS_TABLE));
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public Long insertNewTravel(TravelModel travel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME, travel.getName());
        contentValues.put(KEY_COMMENT, travel.getComment());
        contentValues.put(KEY_DISTANCE, travel.getDistance());
        contentValues.put(KEY_TIME, travel.getTime());
        contentValues.put(KEY_DATE, travel.getDateTravel());

        Long success = db.insert(TRAVEL_TABLE, null, contentValues);
        db.close();
        return success;
    }

    public Long insertNewInterestPoint(InterestPointModel p) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME_MARKER, p.getName());
        contentValues.put(KEY_LAT_MARKER, p.getLat());
        contentValues.put(KEY_LON_MARKER, p.getLon());
        contentValues.put(KEY_ID_TRAVEL_MARKER, p.getIdTravel());

        Long success = db.insert(MARKERS_TABLE, null, contentValues);
        db.close();
        return success;
    }

    public Long insertNewPoint(PointModel p) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NEXT_POINT, p.getIdNextPoint());
        contentValues.put(KEY_LAT_MARKER, p.getLat());
        contentValues.put(KEY_LON_MARKER, p.getLon());
        contentValues.put(KEY_ID_TRAVEL_POINT, p.getIdTravel());

        Long success = db.insert(POINTS_TABLE, null, contentValues);
        db.close();
        return success;
    }

    public ArrayList<InterestPointModel> getInterestPointsForTravel(Integer idTravel) {
        ArrayList<InterestPointModel> markersList = new ArrayList<>();
        String qry = String.format("SELECT * FROM %s WHERE idTravel = %d", MARKERS_TABLE, idTravel);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;

        try {
            cursor = db.rawQuery(qry, null);
        } catch (SQLiteException e) {
            db.execSQL(qry);
            return new ArrayList<>();
        }

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LAT_MARKER));
                double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LON_MARKER));
                int idTravelNew = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID_TRAVEL_MARKER));

                InterestPointModel m = new InterestPointModel(id, name, lat, lon, idTravelNew);
                markersList.add(m);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return markersList;
    }

    public ArrayList<PointModel> getTravelPoints(Integer idTravel) {
        ArrayList<PointModel> markersList = new ArrayList<>();
        String qry = String.format("SELECT * FROM %s WHERE idTravel = %d ORDER BY idNextPoint ASC", POINTS_TABLE, idTravel);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;

        try {
            cursor = db.rawQuery(qry, null);
        } catch (SQLiteException e) {
            db.execSQL(qry);
            return new ArrayList<>();
        }

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LAT_MARKER));
                double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LON_MARKER));
                int idNextPoint = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NEXT_POINT));
                int idTravelNew = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID_TRAVEL_MARKER));

                PointModel p = new PointModel(id, lat, lon, idNextPoint, idTravelNew);
                markersList.add(p);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return markersList;
    }

    public ArrayList<TravelModel> getTravels() {
        ArrayList<TravelModel> travelsList = new ArrayList<>();
        String qry = String.format("SELECT * FROM %s WHERE $KEY_NAME NOT LIKE ''", TRAVEL_TABLE);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;

        try {
            cursor = db.rawQuery(qry, null);
        } catch (SQLiteException e) {
            db.execSQL(qry);
            return new ArrayList<>();
        }

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME));
                String comment = cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMMENT));
                Double distance = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_DISTANCE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME));
                String dateTravel = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE));

                ArrayList<PointModel> points = getTravelPoints(id);
                TravelModel t = new TravelModel(id, name, comment, distance, time, dateTravel, points);
                travelsList.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return travelsList;
    }

    public PointModel getFirstPointTravel(Integer idTravel) {
        String qry = String.format("SELECT * FROM %s WHERE %s = %d AND %s = 0", POINTS_TABLE, KEY_ID_TRAVEL_POINT, idTravel, KEY_NEXT_POINT);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;

        try {
            cursor = db.rawQuery(qry, null);
        } catch (SQLiteException e) {
            db.execSQL(qry);
            return null;
        }

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID_POINT));
            double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LAT_POINT));
            double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LON_POINT));
            int idNextPoint = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NEXT_POINT));
            int idTravelNew = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID_TRAVEL_POINT));

            return new PointModel(id, lat, lon, idNextPoint, idTravelNew);
        }
        cursor.close();
        db.close();
        return null;
    }

    public ArrayList<TravelModel> getEmptyTravel() {
        ArrayList<TravelModel> travelsList = new ArrayList<>();
        String qry = String.format("SELECT * FROM %s WHERE %s = ''", TRAVEL_TABLE, KEY_NAME);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;

        try {
            cursor = db.rawQuery(qry, null);
        } catch (SQLiteException e) {
            db.execSQL(qry);
            return null;
        }

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME));
                String comment = cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMMENT));
                double distance = Double.parseDouble(String.format("%.2f", cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_DISTANCE)) / 1000.0));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME));
                String dateTravel = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE));

                ArrayList<PointModel> points = getTravelPoints(id);
                TravelModel t = new TravelModel(id, name, comment, distance, time, dateTravel, points);
                travelsList.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return travelsList;
    }

    public Integer updateTravel(TravelModel travel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_NAME, travel.getName());
        contentValues.put(KEY_COMMENT, travel.getComment());
        contentValues.put(KEY_DISTANCE, travel.getDistance());
        contentValues.put(KEY_TIME, travel.getTime());
        contentValues.put(KEY_DATE, travel.getDateTravel());

        Integer success = db.update(TRAVEL_TABLE, contentValues, KEY_ID + "=" + travel.getId(), null);

        db.close();
        return success;
    }

    public void deleteTravel(TravelModel travel) {
        SQLiteDatabase db = this.getWritableDatabase();
        int success = db.delete(TRAVEL_TABLE, KEY_ID + "=" + travel.getId(), null);
        db.close();
    }

}
