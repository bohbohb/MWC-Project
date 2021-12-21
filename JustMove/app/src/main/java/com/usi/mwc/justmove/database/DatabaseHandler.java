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

    private static final int DATABASE_VERSION = 7;
    private static final String DATABASE_NAME = "JustMove";
    private static final String TRAVEL_TABLE = "t_travels";
    private static final String POINTS_TABLE = "t_points";

    private static final String KEY_ID = "idTravel";
    private static final String KEY_NAME = "name";
    private static final String KEY_DISTANCE = "distance";
    private static final String KEY_TIME = "time";
    private static final String KEY_DATE = "dateTravel";
    private static final String KEY_DATE_START = "dateTravelStart";
    private static final String KEY_STEPS = "numberOfSteps";
    private static final String KEY_PUBLIBIKE = "publibike";

    private static final String KEY_ID_POINT = "idPoint";
    private static final String KEY_LAT_POINT = "latitude";
    private static final String KEY_LON_POINT = "longitude";
    private static final String KEY_NEXT_POINT = "idNextPoint";
    private static final String KEY_ID_TRAVEL_POINT = "idTravel";
    
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Method that creates the table Travel and Point.
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTravelTable = ("CREATE TABLE " + TRAVEL_TABLE + "("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_NAME + " TEXT, "
                + KEY_DISTANCE + " REAL, "
                + KEY_TIME + " TEXT, "
                + KEY_DATE + " TEXT, "
                + KEY_DATE_START + " TEXT, "
                + KEY_STEPS + " INTEGER, "
                + KEY_PUBLIBIKE + " INTEGER)");
        String createPointsTable = ("CREATE TABLE " + POINTS_TABLE + "("
                + KEY_ID_POINT + " INTEGER PRIMARY KEY, "
                + KEY_LAT_POINT + " REAL, "
                + KEY_LON_POINT + " REAL, "
                + KEY_NEXT_POINT + " INT, "
                + KEY_ID_TRAVEL_POINT + " INT, "
                + "FOREIGN KEY(idTravel) REFERENCES t_travels(idTravels))");
        db.execSQL("PRAGMA foreign_keys = ON;");
        db.execSQL(createTravelTable);
        db.execSQL(createPointsTable);
    }

    /**
     * Will upgrade the Database when the version is changed.
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TRAVEL_TABLE));
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", POINTS_TABLE));
        onCreate(db);
    }

    /**
     * Will downgrade from an old version to the new one.
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Allows adding a new Travel to the Table Travel.
     * @param travel
     * @return
     */
    public Long insertNewTravel(TravelModel travel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NAME, travel.getName());
        contentValues.put(KEY_DISTANCE, travel.getDistance());
        contentValues.put(KEY_TIME, travel.getTime());
        contentValues.put(KEY_DATE, travel.getDateTravel());
        contentValues.put(KEY_DATE_START, travel.getDateStartTravel());

        Long success = db.insert(TRAVEL_TABLE, null, contentValues);
        db.close();
        return success;
    }

    /**
     * Allows adding a new Point to the Points Travel.
     * @param p
     * @return
     */
    public Long insertNewPoint(PointModel p) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NEXT_POINT, p.getIdNextPoint());
        contentValues.put(KEY_LAT_POINT, p.getLat());
        contentValues.put(KEY_LON_POINT, p.getLon());
        contentValues.put(KEY_ID_TRAVEL_POINT, p.getIdTravel());

        Long success = db.insert(POINTS_TABLE, null, contentValues);
        db.close();
        return success;
    }

    /**
     * Allows retrieving the Arraylist of Points from the table Points.
     * @param idTravel
     * @return
     */
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
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LAT_POINT));
                double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LON_POINT));
                int idNextPoint = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NEXT_POINT));
                int idTravelNew = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID_TRAVEL_POINT));

                PointModel p = new PointModel(id, lat, lon, idNextPoint, idTravelNew);
                markersList.add(p);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return markersList;
    }

    /**
     * Allows retrieving the Arraylist of Travels from the table Travel.
     * @return
     */
    public ArrayList<TravelModel> getTravels() {
        ArrayList<TravelModel> travelsList = new ArrayList<>();
        String qry = String.format("SELECT * FROM %s WHERE %s NOT LIKE ''", TRAVEL_TABLE, KEY_NAME);
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
                Double distance = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_DISTANCE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME));
                String dateTravel = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE));
                String dateStartTravel = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE_START));
                int steps = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STEPS));
                int publibike = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PUBLIBIKE));

                ArrayList<PointModel> points = getTravelPoints(id);
                TravelModel t = new TravelModel(id, name, distance, time, dateTravel, dateStartTravel, points, steps, publibike);
                travelsList.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return travelsList;
    }

    /**
     * returns the First Point element in the table Point.
     * @param idTravel
     * @return
     */
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

    /**
     * returns the travel list
     * @return
     */
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
                double distance = Double.parseDouble(String.format("%.2f", cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_DISTANCE)) / 1000.0));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TIME));
                String dateTravel = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE));
                String dateStartTravel = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE_START));
                int steps = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STEPS));
                int publibike = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PUBLIBIKE));

                ArrayList<PointModel> points = getTravelPoints(id);
                TravelModel t = new TravelModel(id, name, distance, time, dateTravel, dateStartTravel, points, steps, publibike);
                travelsList.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return travelsList;
    }

    /**
     * add the travel to the Travel table.
     * @param travel
     * @return
     */
    public Integer updateTravel(TravelModel travel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_NAME, travel.getName());
        contentValues.put(KEY_DISTANCE, travel.getDistance());
        contentValues.put(KEY_TIME, travel.getTime());
        contentValues.put(KEY_DATE, travel.getDateTravel());
        contentValues.put(KEY_DATE_START, travel.getDateStartTravel());
        contentValues.put(KEY_PUBLIBIKE, travel.getPublibike());
        contentValues.put(KEY_STEPS, travel.getNbSteps());

        Integer success = db.update(TRAVEL_TABLE, contentValues, KEY_ID + "=" + travel.getId(), null);

        db.close();
        return success;
    }

    /**
     * delete the travel from Travel table.the
     * @param travel
     */
    public void deleteTravel(TravelModel travel) {
        SQLiteDatabase db = this.getWritableDatabase();
        int success = db.delete(TRAVEL_TABLE, KEY_ID + "=" + travel.getId(), null);
        db.close();
    }

}
