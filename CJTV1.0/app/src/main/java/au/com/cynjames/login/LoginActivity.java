package au.com.cynjames.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import au.com.cynjames.CJT;
import au.com.cynjames.mainView.MainActivity;
import au.com.cynjames.utils.GenericMethods;
import au.com.cynjames.cjtv10.R;
import au.com.cynjames.communication.HTTPHandler;
import au.com.cynjames.communication.ResponseListener;
import au.com.cynjames.models.User;
import au.com.cynjames.models.Vehicles;
import au.com.cynjames.models.Vehicles.Vehicle;
import au.com.cynjames.utils.SQLiteHelper;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static au.com.cynjames.utils.SQLiteHelper.DATABASE_VERSION;

public class LoginActivity extends AppCompatActivity {
    Gson gson;
    Spinner vehicleList;
    Context context;
    EditText username;
    EditText password;
    Editor prefsEditor;
    SQLiteHelper db;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

//        Intent pmIntent = new Intent();
//        String packageName = getPackageName();
//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
//                pmIntent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//                pmIntent.setData(Uri.parse("package:" + packageName));
//                startActivity(pmIntent);
//            }
//        }

        //Check user avail and Move to Main
        SharedPreferences mPrefs = getApplicationContext().getSharedPreferences("AppData" + DATABASE_VERSION, 0);
        String jsonUser = mPrefs.getString("User", "");
        String jsonVehicle = mPrefs.getString("Vehicle", "");
        if(!(jsonUser.equals("") || jsonUser == null || jsonVehicle.equals("") || jsonVehicle == null)){
            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
        }

        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {

            finish();
            return;
        }
        setContentView(R.layout.activity_login);
        init();
        gson = new Gson();
        if(GenericMethods.isConnectedToInternet(this)) {
            loadVehicleList();
        }
        else{
            GenericMethods.showNoInternetDialog(context);
        }
        prefsEditor = mPrefs.edit();
        db = new SQLiteHelper(this);
        //FirebaseCrash.report(new Exception("My first Android non-fatal error"));
    }

    private void init(){
        username = (EditText) findViewById(R.id.login_txt_username);
        password = (EditText) findViewById(R.id.login_txt_password);
        password.setTypeface(username.getTypeface());
        vehicleList = (Spinner) findViewById(R.id.login_vehicle_list);
        vehicleList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                ((TextView) view).setTextColor(Color.WHITE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        Button loginBtn = (Button) findViewById(R.id.login_btn_login);
        loginBtn.setTypeface(loginBtn.getTypeface(), Typeface.BOLD);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButtonClicked();
            }
        });

    }

    private void loginButtonClicked(){
        if (username.getText().toString().isEmpty()) {
            GenericMethods.showToast(context, "Please enter your username");
        } else if (password.getText().toString().isEmpty()) {
            GenericMethods.showToast(context, "Please enter your password");
        } else if (vehicleList.getSelectedItemPosition() != 0) {
            if(GenericMethods.isConnectedToInternet(this)) {
                sendLoginRequest(username.getText().toString(), password.getText().toString(), (Vehicle) vehicleList.getSelectedItem());
            }
            else{
                GenericMethods.showNoInternetDialog(context);
            }
        } else {
            GenericMethods.showToast(context, "Please select Vehicle id");
        }
    }

    private void sendLoginRequest(String userName, String password, Vehicle vehicle) {
        RequestParams params = new RequestParams();
        params.add("username", userName);
        params.add("password", password);
        HTTPHandler.post("cjt-login.php", params,  new HTTPHandler.ResponseManager(new LoginChecker(vehicle), context, "Please Wait..."));
    }

    private void loadVehicleList(){
        HTTPHandler.post("cjt-vehicles.php", null, new HTTPHandler.ResponseManager(new VehicleListLoader(), context, "Please Wait..."));
    }

    public class VehicleListLoader implements ResponseListener {

        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            ArrayList<Vehicle> vehicleArrayList = gson.fromJson(jSONObject.toString(), Vehicles.class).getVehicles();
            vehicleArrayList.add(0, new Vehicles().getAVehicle("Vehicle no", "-1"));
            vehicleList.setAdapter(new ArrayAdapter<>(context,R.layout.spinner_item,vehicleArrayList));
            vehicleList.setSelection(0, true);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        ((CJT)this.getApplication()).stopActivityTransitionTimer();
    }

    public class LoginChecker implements ResponseListener {
        Vehicle vehicle;

        public LoginChecker(Vehicle vehicle) {
            this.vehicle = vehicle;
        }

        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            if (jSONObject.getInt("success") == 1) {
                User user = gson.fromJson(jSONObject.toString(), User.class);
                user.setVehicle(vehicle);
                db.clearTable("user");
                db.addUser(user);
                String jsonUser = gson.toJson(user.getUserid());
                String jsonVehicle = gson.toJson(vehicle, Vehicle.class);
                prefsEditor.putString("User", jsonUser);
                prefsEditor.putString("Vehicle", jsonVehicle);
                prefsEditor.commit();
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
            else{
                GenericMethods.showToast(context,jSONObject.getString("message"));
            }
        }
    }

}
