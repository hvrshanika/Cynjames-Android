package au.com.cynjames.login;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import au.com.cynjames.Utils.GenericMethods;
import au.com.cynjames.cjtv10.R;
import au.com.cynjames.communication.HTTPHandler;
import au.com.cynjames.communication.ResponseListener;
import au.com.cynjames.models.User;
import au.com.cynjames.models.Vehicles;
import au.com.cynjames.models.Vehicles.Vehicle;

public class LoginActivity extends Activity {
    Gson gson;
    Spinner vehicleList;
    Context context;
    EditText username;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        context = this;
        gson = new Gson();
        if(GenericMethods.isConnectedToInternet(this)) {
            loadVehicleList();
        }
        else{
            GenericMethods.showNoInternetDialog(context);
        }
    }

    private void init(){
        username = (EditText) findViewById(R.id.login_txt_username);
        password = (EditText) findViewById(R.id.login_txt_password);
        password.setTypeface(username.getTypeface());
        vehicleList = (Spinner) findViewById(R.id.login_vehicle_list);

        Button loginBtn = (Button) findViewById(R.id.login_btn_login);
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
            sendLoginRequest(username.getText().toString(), password.getText().toString(), (Vehicle) vehicleList.getSelectedItem());
        } else {
            GenericMethods.showToast(context, "Please select Vehicle id");
        }
    }

    private void vehicleListClicked(){

    }

    private void sendLoginRequest(String userName, String password, Vehicle vehicle) {
        RequestParams params = new RequestParams();
        params.add("username", userName);
        params.add("password", password);
        HTTPHandler.post("login.php", params,  new HTTPHandler.ResponseManager(new LoginChecker(vehicle), context));
    }

    private void loadVehicleList(){
        HTTPHandler.post("vehicles.php", null, new HTTPHandler.ResponseManager(new VehicleListLoader(), context));
    }

    public class VehicleListLoader implements ResponseListener {

        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            ArrayList<Vehicle> vehicleArrayList = ((Vehicles) gson.fromJson(jSONObject.toString(), Vehicles.class)).getVehicles();
            vehicleArrayList.add(0, new Vehicles().getAVehicle("Vehicle no", "-1"));
            vehicleList.setAdapter(new ArrayAdapter<>(context,R.layout.spinner_item,vehicleArrayList));
        }
    }

    public class LoginChecker implements ResponseListener {
        Vehicle vehicle;

        public LoginChecker(Vehicle vehicle) {
            this.vehicle = vehicle;
        }

        @Override
        public void onSuccess(JSONObject jSONObject) throws JSONException {
            GenericMethods.showToast(context,jSONObject.getString("message"));
            if (jSONObject.getInt("success") == 1) {
                User user = (User) gson.fromJson(jSONObject.toString(), User.class);
                user.setVehicle(vehicle);
                return;
            }
        }
    }

}
