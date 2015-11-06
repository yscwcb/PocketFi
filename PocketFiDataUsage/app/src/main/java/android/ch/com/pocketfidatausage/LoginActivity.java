package android.ch.com.pocketfidatausage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Network;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;

import static android.ch.com.pocketfidatausage.NetworkConnection.*;

/**
 * Created by Changhan on 2015-10-30.
 */
public class LoginActivity extends AppCompatActivity {
    EditText mUserNameEdit = null;
    EditText mPasswordEdit = null;
    CheckBox saveCheck = null;
    SharedPreferences setting;
    private static String tag = "PocketPiDataUsage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserNameEdit = (EditText) findViewById(R.id.user_name_edit);
        // 에디트 암호입력 처리
        mPasswordEdit = (EditText) findViewById(R.id.password);
        mPasswordEdit.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        PasswordTransformationMethod pass = new PasswordTransformationMethod();
        mPasswordEdit.setTransformationMethod(pass);

        // 패스워드 저장 확인
        saveCheck = (CheckBox) findViewById(R.id.checkbox);
        setting = getSharedPreferences("setting", 0);
        if (setting.getBoolean("checkbox", false)) {
            Log.e(tag, "onClick password check");
            String savedPassword = setting.getString("password", null);
            saveCheck.setChecked(setting.getBoolean("checkbox", false));
            mPasswordEdit.setText(savedPassword);
        }
    }

    // 패스워드 저장 확인
    public void savePreferences(String password) {
        CheckBox saveCheck=(CheckBox)findViewById(R.id.checkbox);
        SharedPreferences.Editor editor = null;
        if(saveCheck.isChecked()) {
            setting = getSharedPreferences("setting", 0);
            editor = setting.edit();
            editor.putString("password", password);
            editor.putBoolean("checkbox", saveCheck.isChecked());
            editor.apply();
        } else {
            setting = getSharedPreferences("setting", 0);
            editor = setting.edit();
            editor.clear();
            editor.apply();
            Log.e(tag, "deleting password");
        }
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.login_btn: {
                String username = null;
                String password = null;
                username = mUserNameEdit.getText().toString();
                password = mPasswordEdit.getText().toString();
                if(saveCheck.isChecked()) {
                    savePreferences(password);
                }
                String usernameBase64 = getBase64endcode(username);
                String passwordBase64 = getBase64endcode(password);
                NetworkThread nt = new NetworkThread();
                nt.execute(usernameBase64, passwordBase64);
                break;
            }
            /*
            case R.id.checkbox: {
                String userPassword = null;
                userPassword = mPasswordEdit.getText().toString();
                savePreferences(userPassword);
            }
            */
        }
    }

    public String getBase64endcode(String content) {
        return Base64.encodeToString(content.getBytes(), 0);
    }

    public class NetworkThread extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... args) {
            NetworkConnection networkConnection = NetworkConnection.getInstance();
            networkConnection.setData(args[0], args[1]);

            return networkConnection.loginAuth();
        }

        protected void onPostExecute(JSONObject jsonObject) {
            if(jsonObject != null) {
                try {
                    if (jsonObject.getString("RESULT").equals("SUCCESS")) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        CheckBox saveCheck=(CheckBox)findViewById(R.id.checkbox);
                        if(saveCheck.isChecked()) {
                            SharedPreferences.Editor editor = null;
                            setting = getSharedPreferences("setting", 0);
                            editor = setting.edit();
                            editor.clear();
                            editor.apply();
                            saveCheck.setChecked(false);
                        }
                        Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else
                Toast.makeText(LoginActivity.this, "포켓파이에 접속하지 않았습니다", Toast.LENGTH_LONG).show();
        }
    }
}
