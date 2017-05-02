package au.com.cynjames.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import au.com.cynjames.models.AdhocDimensions;
import au.com.cynjames.models.ConceptBooking;
import au.com.cynjames.models.ConceptBookingLog;
import au.com.cynjames.models.DriverStatus;
import au.com.cynjames.models.User;

/**
 * Created by eleos on 5/16/2016.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 17;
    // Database Name
    private static final String DATABASE_NAME = "CJTdb";
    String[] USERCOLUMNS = {"userid","userFirstName","userLastName","userRole","driverId","userArriveConcept","userArriveClient"};

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE user (userid INTEGER PRIMARY KEY NOT NULL, userFirstName TEXT, userLastName TEXT, userRole TEXT, driverId INTEGER, userArriveConcept TEXT, userArriveClient TEXT )";
        String createJobsTable = "CREATE TABLE conceptBooking (conceptBookingId INTEGER PRIMARY KEY NOT NULL, conceptBookingOrderNo TEXT, conceptBookingBarcode TEXT, conceptBookingDeliverySuburb TEXT, conceptBookingClientName TEXT, conceptBookingDeliveryAddress TEXT, specialNotes TEXT, conceptBookingTime TEXT, conceptBookingTimeFor TEXT, conceptBookingPallets INTEGER, conceptBookingParcels INTEGER, conceptBookingStatus INTEGER, conceptBookingTailLift INTEGER, conceptBookingHandUnload INTEGER, conceptBookingUrgent INTEGER, conceptBookingPickupDate TEXT,conceptPickupSignature TEXT, conceptPickupName TEXT, arrivedConcept TEXT, conceptDeliveryDate TEXT,arrivedClient TEXT, conceptDeliverySignature TEXT, conceptDeliveryName TEXT, deliveryImages TEXT, pickupImages TEXT)";
        String createDriverStatusTable = "CREATE TABLE driverStatus (id INTEGER PRIMARY KEY AUTOINCREMENT, driverStatusDate TEXT, driverStatusTime TEXT, driverStatusDescription TEXT, driverStatusLongitude DOUBLE, driverStatusLatitude DOUBLE, driverStatus_driverId INTEGER, driverStatus_vehicleId TEXT)";
        String createconceptBookingLogTable = "CREATE TABLE conceptBookingLog (id INTEGER PRIMARY KEY AUTOINCREMENT, conceptBookingLog_bookingId INTEGER, conceptBookingLogOrderNo TEXT, conceptBookingLogBarcode TEXT, conceptBookingLogUserId INTEGER, conceptBookingLogComments TEXT, conceptBookingLogDate TEXT, conceptBookingLogStatus INTEGER, hasDeparted INTEGER)";
        String createAdhocJobsTable = "CREATE TABLE adhocBooking (conceptBookingId INTEGER PRIMARY KEY NOT NULL, conceptBookingOrderNo TEXT, conceptBookingBarcode TEXT, conceptBookingDeliverySuburb TEXT, conceptBookingClientName TEXT, conceptBookingDeliveryAddress TEXT, specialNotes TEXT, conceptBookingTime TEXT, conceptBookingTimeFor TEXT, conceptBookingPallets INTEGER, conceptBookingParcels INTEGER, conceptBookingStatus INTEGER, conceptBookingTailLift INTEGER, conceptBookingHandUnload INTEGER, conceptBookingUrgent INTEGER, conceptBookingPickupDate TEXT,conceptPickupSignature TEXT, conceptPickupName TEXT, arrivedConcept TEXT, conceptDeliveryDate TEXT,arrivedClient TEXT, conceptDeliverySignature TEXT, conceptDeliveryName TEXT, deliveryImages TEXT, pickupImages TEXT, conceptBookingPickupAddress TEXT, conceptBookingPickupSuburb TEXT, conceptClientsName TEXT, conceptBookingPickupClientName TEXT, customerType INTEGER, no_pallets INTEGER, no_parcels INTEGER)";
        String createAdhocBookingLogTable = "CREATE TABLE adhocBookingLog (id INTEGER PRIMARY KEY AUTOINCREMENT, conceptBookingLog_bookingId INTEGER, conceptBookingLogOrderNo TEXT, conceptBookingLogBarcode TEXT, conceptBookingLogUserId INTEGER, conceptBookingLogComments TEXT, conceptBookingLogDate TEXT, conceptBookingLogStatus INTEGER, hasDeparted INTEGER)";
        String createAdhocDimensionsTable = "CREATE TABLE adhocDimensions (id INTEGER PRIMARY KEY AUTOINCREMENT, qty INTEGER, height REAL, width REAL, length REAL, weight REAL, clientId INTEGER, bookingId INTEGER, vehicle TEXT, rate REAL)";
        db.execSQL(createUserTable);
        db.execSQL(createJobsTable);
        db.execSQL(createDriverStatusTable);
        db.execSQL(createconceptBookingLogTable);
        db.execSQL(createAdhocJobsTable);
        db.execSQL(createAdhocBookingLogTable);
        db.execSQL(createAdhocDimensionsTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS conceptBooking");
        db.execSQL("DROP TABLE IF EXISTS driverStatus");
        db.execSQL("DROP TABLE IF EXISTS conceptBookingLog");
        db.execSQL("DROP TABLE IF EXISTS adhocBooking");
        db.execSQL("DROP TABLE IF EXISTS adhocBookingLog");
        db.execSQL("DROP TABLE IF EXISTS adhocDimensions");
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
        db.close();
        return user;
    }

    public void clearTable(String tableName){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from "+ tableName);
        db.close();
    }

    public void addJob(ConceptBooking job, boolean isConcept){
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

        String table = "";
        if(isConcept){
            table = "conceptBooking";
        }
        else{
            table = "adhocBooking";
            values.put("conceptBookingPickupAddress", job.getConceptBookingPickupAddress());
            values.put("conceptBookingPickupSuburb", job.getConceptBookingPickupSuburb());
            values.put("conceptClientsName", job.getConceptClientsName());
            values.put("conceptBookingPickupClientName", job.getConceptBookingPickupClientName());
            values.put("customerType", job.getCustomerType());
            values.put("no_pallets", job.getNo_pallets());
            values.put("no_parcels", job.getNo_parcels());
        }
        db.insert(table, null, values);

        db.close();
    }

    public void updateJob(ConceptBooking job, boolean isConcept){
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
        values.put("deliveryImages", job.getDeliveryImages());
        values.put("pickupImages", job.getPickupImages());

        String table = "";
        if(isConcept){
            table = "conceptBooking";
        }
        else{
            table = "adhocBooking";
            values.put("conceptBookingPickupAddress", job.getConceptBookingPickupAddress());
            values.put("conceptBookingPickupSuburb", job.getConceptBookingPickupSuburb());
            values.put("conceptClientsName", job.getConceptClientsName());
            values.put("conceptBookingPickupClientName", job.getConceptBookingPickupClientName());
            values.put("customerType", job.getCustomerType());
            values.put("no_pallets", job.getNo_pallets());
            values.put("no_parcels", job.getNo_parcels());
        }
        db.update(table, values, "conceptBookingId"+" = ?",new String[] { String.valueOf(job.getId()) });

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
        db.close();
        return jobs;
    }

    public boolean jobExist(int id, boolean isConcept){
        boolean exist;

        String table = "";
        if(isConcept){
            table = "conceptBooking";
        }
        else{
            table = "adhocBooking";
        }

        String query = "SELECT COUNT(conceptBookingId) FROM " + table + " WHERE conceptBookingId =" + id +"";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                count = cursor.getInt(0);
            } while (cursor.moveToNext());
        }
        exist = count != 0;
        db.close();
        return exist;
    }

    public List<ConceptBooking> getPendingJobs(boolean isConcept){
        List<ConceptBooking> jobs = new LinkedList<ConceptBooking>();

        String table;
        if(isConcept){
            table = "conceptBooking";
        }
        else{
            table = "adhocBooking";
        }

        String query = "SELECT * FROM "+ table +" where conceptBookingStatus in (2,7)";

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
                job.setDeliveryImages(cursor.getString(23));
                job.setPickupImages(cursor.getString(24));

                if(!isConcept){
                    job.setConceptBookingPickupAddress(cursor.getString(25));
                    job.setConceptBookingPickupSuburb(cursor.getString(26));
                    job.setConceptClientsName(cursor.getString(27));
                    job.setConceptBookingPickupClientName(cursor.getString(28));
                    job.setCustomerType(cursor.getInt(29));
                    job.setNo_pallets(cursor.getInt(30));
                    job.setNo_parcels(cursor.getInt(31));
                }

                jobs.add(job);
            } while (cursor.moveToNext());
        }
        for(ConceptBooking jobb: jobs) {
            Log.d("getPendingJobs()", String.valueOf(jobb.getConceptBookingStatus()));
        }
        db.close();
        return jobs;
    }

    public List<ConceptBooking> getPendingJobsWithStatus(String status, boolean isConcept){
        List<ConceptBooking> jobs = new LinkedList<ConceptBooking>();

        String table = "";
        if(isConcept){
            table = "conceptBooking";
        }
        else{
            table = "adhocBooking";
        }
        String query = "SELECT * FROM "+ table +" where conceptBookingStatus = " + status + "";

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
                job.setDeliveryImages(cursor.getString(23));
                job.setPickupImages(cursor.getString(24));

                if(!isConcept){
                    job.setConceptBookingPickupAddress(cursor.getString(25));
                    job.setConceptBookingPickupSuburb(cursor.getString(26));
                    job.setConceptClientsName(cursor.getString(27));
                    job.setConceptBookingPickupClientName(cursor.getString(28));
                    job.setCustomerType(cursor.getInt(29));
                    job.setNo_pallets(cursor.getInt(30));
                    job.setNo_parcels(cursor.getInt(31));
                }

                jobs.add(job);
            } while (cursor.moveToNext());
        }
        for(ConceptBooking jobb: jobs) {
            Log.d("getPendingJobs()", String.valueOf(jobb.getConceptBookingStatus()));
        }
        db.close();
        return jobs;
    }

    public List<ConceptBooking> getReadyJobs(boolean isConcept){
        List<ConceptBooking> jobs = new LinkedList<ConceptBooking>();

        String query;
        if(isConcept){
            query = "SELECT * FROM conceptBooking where conceptBookingStatus in (8,9)";
        }
        else{
            query = "SELECT * FROM adhocBooking where conceptBookingStatus in (8,9)";
        }

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
                job.setDeliveryImages(cursor.getString(23));
                job.setPickupImages(cursor.getString(24));

                if(!isConcept){
                    job.setConceptBookingPickupAddress(cursor.getString(25));
                    job.setConceptBookingPickupSuburb(cursor.getString(26));
                    job.setConceptClientsName(cursor.getString(27));
                    job.setConceptBookingPickupClientName(cursor.getString(28));
                    job.setCustomerType(cursor.getInt(29));
                    job.setNo_pallets(cursor.getInt(30));
                    job.setNo_parcels(cursor.getInt(31));
                }

                jobs.add(job);
            } while (cursor.moveToNext());
        }
        for(ConceptBooking jobb: jobs) {
            Log.d("getReadyJobs()", String.valueOf(jobb.getConceptBookingStatus()));
        }
        db.close();
        return jobs;
    }

    public void clearConcept(int id, boolean isConcept) {
        String table = "";
        if(isConcept){
            table = "conceptBooking";
        }
        else{
            table = "adhocBooking";
        }
        String idString = String.valueOf(id);
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from "+ table +" WHERE conceptBookingId = " + idString);
        db.close();
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
        db.close();
        return jobs;
    }

    public void addLog(ConceptBookingLog log, boolean isConcept){
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

        String table = "";
        if(isConcept){
            table = "conceptBookingLog";
        }
        else{
            table = "adhocBookingLog";
        }
        db.insert(table, null, values);

        db.close();
    }

    public List<ConceptBookingLog> getAllLogs(boolean isConcept) {
        List<ConceptBookingLog> jobs = new LinkedList<ConceptBookingLog>();

        String table = "";
        if(isConcept){
            table = "conceptBookingLog";
        }
        else{
            table = "adhocBookingLog";
        }
        String query = "SELECT * FROM " + table;

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
        db.close();
        return jobs;
    }

    public void addDimension(AdhocDimensions dimen){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", dimen.getId());
        values.put("qty", dimen.getQty());
        values.put("height", dimen.getHeight());
        values.put("width", dimen.getWidth());
        values.put("length", dimen.getLength());
        values.put("weight", dimen.getWeight());
        values.put("rate", dimen.getRate());
        values.put("clientId", dimen.getClientId());
        values.put("bookingId", dimen.getBookingId());
        values.put("vehicle", dimen.getVehicle());

        String table = "adhocDimensions";
        db.insert(table, null, values);

        db.close();
    }

    public List<AdhocDimensions> getDimenForJob(int id){
        List<AdhocDimensions> jobs = new LinkedList<AdhocDimensions>();

        String query = "SELECT * FROM adhocDimensions WHERE bookingId = " + id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        AdhocDimensions job = null;
        if (cursor.moveToFirst()) {
            do {
                job = new AdhocDimensions();
                job.setQty(Integer.parseInt(cursor.getString(1)));
                job.setHeight(Float.parseFloat(cursor.getString(2)));
                job.setWidth(Float.parseFloat(cursor.getString(3)));
                job.setLength(Float.parseFloat(cursor.getString(4)));
                job.setWeight(Float.parseFloat(cursor.getString(5)));
                job.setClientId(Integer.parseInt(cursor.getString(6)));
                job.setBookingId(Integer.parseInt(cursor.getString(7)));
                job.setVehicle(cursor.getString(8));
                job.setRate(Float.parseFloat(cursor.getString(9)));

                jobs.add(job);
            } while (cursor.moveToNext());
        }
        db.close();
        return jobs;
    }
}
