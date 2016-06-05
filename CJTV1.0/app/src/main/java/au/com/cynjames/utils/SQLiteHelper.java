package au.com.cynjames.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import au.com.cynjames.models.ConceptBooking;
import au.com.cynjames.models.ConceptBookingLog;
import au.com.cynjames.models.DriverStatus;
import au.com.cynjames.models.User;

/**
 * Created by eleos on 5/16/2016.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 9;
    // Database Name
    private static final String DATABASE_NAME = "CJTdb";
    String[] USERCOLUMNS = {"userid","userFirstName","userLastName","userRole","driverId","userArriveConcept","userArriveClient"};

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE user (userid INTEGER PRIMARY KEY NOT NULL, userFirstName TEXT, userLastName TEXT, userRole TEXT, driverId INTEGER, userArriveConcept TEXT, userArriveClient TEXT )";
        String createJobsTable = "CREATE TABLE conceptBooking (conceptBookingId INTEGER PRIMARY KEY NOT NULL, conceptBookingOrderNo TEXT, conceptBookingBarcode TEXT, conceptBookingDeliverySuburb TEXT, conceptBookingClientName TEXT, conceptBookingDeliveryAddress TEXT, specialNotes TEXT, conceptBookingTime TEXT, conceptBookingTimeFor TEXT, conceptBookingPallets INTEGER, conceptBookingParcels INTEGER, conceptBookingStatus INTEGER, conceptBookingTailLift INTEGER, conceptBookingHandUnload INTEGER, conceptBookingUrgent INTEGER, conceptBookingPickupDate TEXT,conceptPickupSignature TEXT, conceptPickupName TEXT, arrivedConcept TEXT, conceptDeliveryDate TEXT,arrivedClient TEXT, conceptDeliverySignature TEXT, conceptDeliveryName TEXT)";
        String createDriverStatusTable = "CREATE TABLE driverStatus (id INTEGER PRIMARY KEY AUTOINCREMENT, driverStatusDate TEXT, driverStatusTime TEXT, driverStatusDescription TEXT, driverStatusLongitude DOUBLE, driverStatusLatitude DOUBLE, driverStatus_driverId INTEGER, driverStatus_vehicleId TEXT)";
        String createconceptBookingLogTable = "CREATE TABLE conceptBookingLog (id INTEGER PRIMARY KEY AUTOINCREMENT, conceptBookingLog_bookingId INTEGER, conceptBookingLogOrderNo TEXT, conceptBookingLogBarcode TEXT, conceptBookingLogUserId INTEGER, conceptBookingLogComments TEXT, conceptBookingLogDate TEXT, conceptBookingLogStatus INTEGER, hasDeparted INTEGER)";
        db.execSQL(createUserTable);
        db.execSQL(createJobsTable);
        db.execSQL(createDriverStatusTable);
        db.execSQL(createconceptBookingLogTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS conceptBooking");
        db.execSQL("DROP TABLE IF EXISTS driverStatus");
        db.execSQL("DROP TABLE IF EXISTS conceptBookingLog");
        this.onCreate(db);
    }

    public void addUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userid", user.getUserid());
        values.put("userFirstName", user.getUserFirstName());
        values.put("userLastName", user.getUserLastName());
        values.put("userRole", user.getUserRole());
        values.put("driverId", user.getDriverId());
        values.put("userArriveConcept", user.getUserArriveConcept());
        values.put("userArriveClient", user.getUserArriveClient());


        db.insert("user", null, values);

        db.close();
    }

    public void updateUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        //values.put("userid", user.getUserid());
        values.put("userFirstName", user.getUserFirstName());
        values.put("userLastName", user.getUserLastName());
        values.put("userRole", user.getUserRole());
        values.put("driverId", user.getDriverId());
        values.put("userArriveConcept", user.getUserArriveConcept());
        values.put("userArriveClient", user.getUserArriveClient());


        db.update("user", values, "userid"+" = ?",new String[] { String.valueOf(user.getUserid()) });

        db.close();
    }

    public User getUser(int id){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("user", USERCOLUMNS, " userid = ?", new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        User user = new User();
        user.setUserid(Integer.parseInt(cursor.getString(0)));
        user.setUserFirstName(cursor.getString(1));
        user.setUserLastName(cursor.getString(2));
        user.setUserRole(cursor.getString(3));
        user.setDriverId(Integer.parseInt(cursor.getString(4)));
        user.setUserArriveConcept(cursor.getString(5));
        user.setUserArriveClient(cursor.getString(6));

        Log.d("getUser("+id+")", "" + user.getUserid() + " " + user.getUserFirstName());

        return user;
    }

    public void clearTable(String tableName){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from "+ tableName);
    }

    public void addJob(ConceptBooking job){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("conceptBookingId", job.getId());
        values.put("conceptBookingOrderNo", job.getOrderno());
        values.put("conceptBookingBarcode", job.getBarcode());
        values.put("conceptBookingDeliverySuburb", job.getConceptBookingDeliverySuburb());
        values.put("conceptBookingClientName", job.getClient());
        values.put("conceptBookingDeliveryAddress", job.getAddress());
        values.put("specialNotes", job.getSpecialNotes());
        values.put("conceptBookingTime", job.getDate());
        values.put("conceptBookingTimeFor", job.getConceptBookingTimeFor());
        values.put("conceptBookingPallets", job.getPallets());
        values.put("conceptBookingParcels", job.getParcels());
        values.put("conceptBookingStatus", job.getConceptBookingStatus());
        values.put("conceptBookingTailLift", job.getConceptBookingTailLift());
        values.put("conceptBookingHandUnload", job.getConceptBookingHandUnload());
        values.put("conceptBookingUrgent", job.getConceptBookingUrgent());
        values.put("conceptBookingPickupDate", job.getConceptBookingPickupDate());
        values.put("conceptPickupSignature", job.getConceptPickupSignature());
        values.put("conceptPickupName", job.getConceptPickupName());
        values.put("arrivedConcept", job.getArrivedConcept());
        values.put("conceptDeliveryDate", job.getConceptDeliveryDate());
        values.put("arrivedClient", job.getArrivedClient());
        values.put("conceptDeliverySignature", job.getConceptDeliverySignature());
        values.put("conceptDeliveryName", job.getConceptDeliveryName());

        db.insert("conceptBooking", null, values);

        db.close();
    }

    public void updateJob(ConceptBooking job){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
//        values.put("conceptBookingId", job.getId());
        values.put("conceptBookingOrderNo", job.getOrderno());
        values.put("conceptBookingBarcode", job.getBarcode());
        values.put("conceptBookingDeliverySuburb", job.getConceptBookingDeliverySuburb());
        values.put("conceptBookingClientName", job.getClient());
        values.put("conceptBookingDeliveryAddress", job.getAddress());
        values.put("specialNotes", job.getSpecialNotes());
        values.put("conceptBookingTime", job.getDate());
        values.put("conceptBookingTimeFor", job.getConceptBookingTimeFor());
        values.put("conceptBookingPallets", job.getPallets());
        values.put("conceptBookingParcels", job.getParcels());
        values.put("conceptBookingStatus", job.getConceptBookingStatus());
        values.put("conceptBookingTailLift", job.getConceptBookingTailLift());
        values.put("conceptBookingHandUnload", job.getConceptBookingHandUnload());
        values.put("conceptBookingUrgent", job.getConceptBookingUrgent());
        values.put("conceptBookingPickupDate", job.getConceptBookingPickupDate());
        values.put("conceptPickupSignature", job.getConceptPickupSignature());
        values.put("conceptPickupName", job.getConceptPickupName());
        values.put("arrivedConcept", job.getArrivedConcept());
        values.put("conceptDeliveryDate", job.getConceptDeliveryDate());
        values.put("arrivedClient", job.getArrivedClient());
        values.put("conceptDeliverySignature", job.getConceptDeliverySignature());
        values.put("conceptDeliveryName", job.getConceptDeliveryName());


        db.update("conceptBooking", values, "conceptBookingId"+" = ?",new String[] { String.valueOf(job.getId()) });

        db.close();
    }

    public List<ConceptBooking> getAllJobs() {
        List<ConceptBooking> jobs = new LinkedList<ConceptBooking>();

        String query = "SELECT * FROM conceptBooking";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        ConceptBooking job = null;
        if (cursor.moveToFirst()) {
            do {
                job = new ConceptBooking();
                job.setId(Integer.parseInt(cursor.getString(0)));
                job.setDate(cursor.getString(7));
                job.setClient(cursor.getString(4));

                jobs.add(job);
            } while (cursor.moveToNext());
        }
        for(ConceptBooking jobb: jobs) {
            Log.d("getAllJobs()", jobb.getClient());
        }
        return jobs;
    }

    public List<ConceptBooking> getPendingJobs(){
        List<ConceptBooking> jobs = new LinkedList<ConceptBooking>();

        String query = "SELECT * FROM conceptBooking where conceptBookingStatus in (2,7)";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        ConceptBooking job = null;
        if (cursor.moveToFirst()) {
            do {
                job = new ConceptBooking();
                job.setId(Integer.parseInt(cursor.getString(0)));
                job.setOrderno(cursor.getString(1));
                job.setBarcode(cursor.getString(2));
                job.setConceptBookingDeliverySuburb(cursor.getString(3));
                job.setClient(cursor.getString(4));
                job.setAddress(cursor.getString(5));
                job.setSpecialNotes(cursor.getString(6));
                job.setDate(cursor.getString(7));
                job.setConceptBookingTimeFor(cursor.getString(8));
                job.setPallets(Integer.parseInt(cursor.getString(9)));
                job.setParcels(Integer.parseInt(cursor.getString(10)));
                job.setConceptBookingStatus(Integer.parseInt(cursor.getString(11)));
                job.setConceptBookingTailLift(Integer.parseInt(cursor.getString(12)));
                job.setConceptBookingHandUnload(Integer.parseInt(cursor.getString(13)));
                job.setConceptBookingUrgent(Integer.parseInt(cursor.getString(14)));
                job.setConceptBookingPickupDate(cursor.getString(15));
                job.setConceptPickupSignature(cursor.getString(16));
                job.setConceptPickupName(cursor.getString(17));
                job.setArrivedConcept(cursor.getString(18));
                job.setConceptDeliveryDate(cursor.getString(19));
                job.setArrivedClient((cursor.getString(20)));
                job.setConceptDeliverySignature(cursor.getString(21));
                job.setConceptDeliveryName(cursor.getString(22));

                jobs.add(job);
            } while (cursor.moveToNext());
        }
        for(ConceptBooking jobb: jobs) {
            Log.d("getPendingJobs()", String.valueOf(jobb.getConceptBookingStatus()));
        }
        return jobs;
    }

    public List<ConceptBooking> getPendingJobsWithStatus(String status){
        List<ConceptBooking> jobs = new LinkedList<ConceptBooking>();

        String query = "SELECT * FROM conceptBooking where conceptBookingStatus = " + status + "";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        ConceptBooking job = null;
        if (cursor.moveToFirst()) {
            do {
                job = new ConceptBooking();
                job.setId(Integer.parseInt(cursor.getString(0)));
                job.setOrderno(cursor.getString(1));
                job.setBarcode(cursor.getString(2));
                job.setConceptBookingDeliverySuburb(cursor.getString(3));
                job.setClient(cursor.getString(4));
                job.setAddress(cursor.getString(5));
                job.setSpecialNotes(cursor.getString(6));
                job.setDate(cursor.getString(7));
                job.setConceptBookingTimeFor(cursor.getString(8));
                job.setPallets(Integer.parseInt(cursor.getString(9)));
                job.setParcels(Integer.parseInt(cursor.getString(10)));
                job.setConceptBookingStatus(Integer.parseInt(cursor.getString(11)));
                job.setConceptBookingTailLift(Integer.parseInt(cursor.getString(12)));
                job.setConceptBookingHandUnload(Integer.parseInt(cursor.getString(13)));
                job.setConceptBookingUrgent(Integer.parseInt(cursor.getString(14)));
                job.setConceptBookingPickupDate(cursor.getString(15));
                job.setConceptPickupSignature(cursor.getString(16));
                job.setConceptPickupName(cursor.getString(17));
                job.setArrivedConcept(cursor.getString(18));
                job.setConceptDeliveryDate(cursor.getString(19));
                job.setArrivedClient((cursor.getString(20)));
                job.setConceptDeliverySignature(cursor.getString(21));
                job.setConceptDeliveryName(cursor.getString(22));

                jobs.add(job);
            } while (cursor.moveToNext());
        }
        for(ConceptBooking jobb: jobs) {
            Log.d("getPendingJobs()", String.valueOf(jobb.getConceptBookingStatus()));
        }
        return jobs;
    }

    public List<ConceptBooking> getReadyJobs(){
        List<ConceptBooking> jobs = new LinkedList<ConceptBooking>();

        String query = "SELECT * FROM conceptBooking where conceptBookingStatus in (8,9)";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        ConceptBooking job = null;
        if (cursor.moveToFirst()) {
            do {
                job = new ConceptBooking();
                job.setId(Integer.parseInt(cursor.getString(0)));
                job.setOrderno(cursor.getString(1));
                job.setBarcode(cursor.getString(2));
                job.setConceptBookingDeliverySuburb(cursor.getString(3));
                job.setClient(cursor.getString(4));
                job.setAddress(cursor.getString(5));
                job.setSpecialNotes(cursor.getString(6));
                job.setDate(cursor.getString(7));
                job.setConceptBookingTimeFor(cursor.getString(8));
                job.setPallets(Integer.parseInt(cursor.getString(9)));
                job.setParcels(Integer.parseInt(cursor.getString(10)));
                job.setConceptBookingStatus(Integer.parseInt(cursor.getString(11)));
                job.setConceptBookingTailLift(Integer.parseInt(cursor.getString(12)));
                job.setConceptBookingHandUnload(Integer.parseInt(cursor.getString(13)));
                job.setConceptBookingUrgent(Integer.parseInt(cursor.getString(14)));
                job.setConceptBookingPickupDate(cursor.getString(15));
                job.setConceptPickupSignature(cursor.getString(16));
                job.setConceptPickupName(cursor.getString(17));
                job.setArrivedConcept(cursor.getString(18));
                job.setConceptDeliveryDate(cursor.getString(19));
                job.setArrivedClient((cursor.getString(20)));
                job.setConceptDeliverySignature(cursor.getString(21));
                job.setConceptDeliveryName(cursor.getString(22));

                jobs.add(job);
            } while (cursor.moveToNext());
        }
        for(ConceptBooking jobb: jobs) {
            Log.d("getReadyJobs()", String.valueOf(jobb.getConceptBookingStatus()));
        }
        return jobs;
    }

    public void clearConcept(int id) {
        String idString = String.valueOf(id);
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from conceptBooking WHERE conceptBookingId = " + idString);
    }


    public void addDriverStatus(DriverStatus status){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("driverStatusDate", status.getDriverStatusDate());
        values.put("driverStatusTime", status.getDriverStatusTime());
        values.put("driverStatusDescription", status.getDriverStatusDescription());
        values.put("driverStatusLongitude", status.getDriverStatusLongitude());
        values.put("driverStatusLatitude", status.getDriverStatusLatitude());
        values.put("driverStatus_driverId", status.getDriverStatus_driverId());
        values.put("driverStatus_vehicleId", status.getDriverStatus_vehicleId());

        db.insert("driverStatus", null, values);

        db.close();
    }

    public List<DriverStatus> getAllStatus() {
        List<DriverStatus> jobs = new LinkedList<DriverStatus>();

        String query = "SELECT * FROM driverStatus";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        DriverStatus job = null;
        if (cursor.moveToFirst()) {
            do {
                job = new DriverStatus();
                job.setDriverStatusDate(cursor.getString(1));
                job.setDriverStatusTime(cursor.getString(2));
                job.setDriverStatusDescription(cursor.getString(3));
                job.setDriverStatusLongitude(Double.parseDouble(cursor.getString(4)));
                job.setDriverStatusLatitude(Double.parseDouble(cursor.getString(5)));
                job.setDriverStatus_driverId(Integer.parseInt(cursor.getString(6)));
                job.setDriverStatus_vehicleId(cursor.getString(7));

                jobs.add(job);
            } while (cursor.moveToNext());
        }
        for(DriverStatus jobb: jobs) {
            Log.d("getAllStatus()", String.valueOf(jobb.getDriverStatusLongitude()));
        }
        return jobs;
    }

    public void addLog(ConceptBookingLog log){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("conceptBookingLog_bookingId", log.getConceptBookingLog_bookingId());
        values.put("conceptBookingLogOrderNo", log.getConceptBookingLogOrderNo());
        values.put("conceptBookingLogBarcode", log.getConceptBookingLogBarcode());
        values.put("conceptBookingLogUserId", log.getConceptBookingLogUserId());
        values.put("conceptBookingLogComments", log.getConceptBookingLogComments());
        values.put("conceptBookingLogDate", log.getConceptBookingLogDate());
        values.put("conceptBookingLogStatus", log.getConceptBookingLogStatus());
        values.put("hasDeparted", log.getHasDeparted());

        db.insert("conceptBookingLog", null, values);

        db.close();
    }

    public List<ConceptBookingLog> getAllLogs() {
        List<ConceptBookingLog> jobs = new LinkedList<ConceptBookingLog>();

        String query = "SELECT * FROM conceptBookingLog";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        ConceptBookingLog job = null;
        if (cursor.moveToFirst()) {
            do {
                job = new ConceptBookingLog();
                job.setConceptBookingLog_bookingId(Integer.parseInt(cursor.getString(1)));
                job.setConceptBookingLogOrderNo(cursor.getString(2));
                job.setConceptBookingLogBarcode(cursor.getString(3));
                job.setConceptBookingLogUserId(Integer.parseInt(cursor.getString(4)));
                job.setConceptBookingLogComments(cursor.getString(5));
                job.setConceptBookingLogDate(cursor.getString(6));
                job.setConceptBookingLogStatus(Integer.parseInt(cursor.getString(7)));
                job.setHasDeparted(Integer.parseInt(cursor.getString(8)));

                jobs.add(job);
            } while (cursor.moveToNext());
        }
        for(ConceptBookingLog jobb: jobs) {
            Log.d("getAllStatus()", String.valueOf(jobb.getConceptBookingLogDate()));
        }
        return jobs;
    }
}
