package id.web.proditipolines.amop.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.web.proditipolines.amop.R;
import id.web.proditipolines.amop.base.AppController;
import id.web.proditipolines.amop.util.Helper;

import static id.web.proditipolines.amop.util.AppConstans.TAG_ID;
import static id.web.proditipolines.amop.util.AppConstans.TAG_MESSAGE;
import static id.web.proditipolines.amop.util.AppConstans.TAG_PASSWORD;
import static id.web.proditipolines.amop.util.AppConstans.TAG_SUCCESS;
import static id.web.proditipolines.amop.util.AppConstans.TAG_USERNAME;
import static id.web.proditipolines.amop.util.Server.URL_LOGIN;

public class LoginActivity extends AppCompatActivity {

    ProgressDialog pDialog;
    Button btnLogin;
    EditText txtUsername, txtPassword;
    Helper help;

    int success;
    ConnectivityManager conMgr;

    private static final String TAG = LoginActivity.class.getSimpleName();


    String tag_json_obj = "json_obj_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        help = new Helper(getApplicationContext());

        conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr != null) {
            if (conMgr.getActiveNetworkInfo() == null || !conMgr.getActiveNetworkInfo().isAvailable() || !conMgr.getActiveNetworkInfo().isConnected()) {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        }


        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String username = txtUsername.getText().toString();
                String password = txtPassword.getText().toString();

                // mengecek kolom yang kosong
                if (username.trim().length() > 0 && password.trim().length() > 0) {
                    if (conMgr.getActiveNetworkInfo() != null
                            && conMgr.getActiveNetworkInfo().isAvailable()
                            && conMgr.getActiveNetworkInfo().isConnected()) {
                        checkLogin(username, password);
                    } else {
                        Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(), "Kolom tidak boleh kosong", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void checkLogin(final String username, final String password) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Login Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);

                    // Check for error node in json
                    if (success == 1) {
                        String id = jObj.getString(TAG_ID);
                        String username = jObj.getString(TAG_USERNAME);
                        String password = jObj.getString(TAG_PASSWORD);

                        Log.e("Successfully Login!", jObj.toString());

                        Toast.makeText(getApplicationContext(),
                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();


                        // Memanggil main activity
                        Intent i = new Intent(getApplicationContext(),
                                MainActivity.class);
                        i.putExtra(TAG_ID, id);
                        i.putExtra(TAG_USERNAME, username);
                        finish();
                        startActivity(i);
                        help.createLoginSession(id, username, password);

                    } else {
                        Toast.makeText(getApplicationContext(),
                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

                hideDialog();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put(TAG_USERNAME, username);
                params.put(TAG_PASSWORD, password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
