package android.ch.com.pocketfidatausage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

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
            Log.e(tag, "saving password");
            setting = getSharedPreferences("setting", 0);
            editor = setting.edit();
            editor.putString("password", password);
            editor.putBoolean("checkbox", saveCheck.isChecked());
            editor.apply();
        } else {
            Log.e(tag, "deleting password");
        }
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.login_btn: {
                String userName = null;
                String userPassword = null;
                userName = mUserNameEdit.getText().toString();
                userPassword = mPasswordEdit.getText().toString();
                if(saveCheck.isChecked()) {
                    savePreferences(userPassword);
                }
                sendRequest(userName, userPassword);
                break;
            }
            case R.id.checkbox: {
                String userPassword = null;
                userPassword = mPasswordEdit.getText().toString();
                savePreferences(userPassword);
            }
        }
    }

    private void sendRequest(String username, String password) {
        boolean succeed=false;

        NetworkThread nt = new NetworkThread(username, password);
        nt.start();

        try {
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(nt.flagChange())
            succeed = true;

        if(succeed) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        else
             Toast.makeText(this, "로그인 실패", Toast.LENGTH_LONG).show();
    }

    class NetworkThread extends Thread {
        public String mThreadUsername = null;
        public String mThreadPassword = null;
        protected boolean mFlag = false;

        public NetworkThread(String username, String password) {
            mThreadUsername = username;
            mThreadPassword = password;
        }
        public void run() {
            URL url = null;
            HttpURLConnection conn = null;
            StringBuilder output = new StringBuilder();
            String serverUrl = "http://192.168.1.1/cgi-bin/webctl.cgi";

            try {
                url = new URL(serverUrl);
                conn = (HttpURLConnection)url.openConnection();
                if(conn != null) {
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    int resCode = conn.getResponseCode();

                    if(resCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        String line = null;
                        while (true) {
                            line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            output.append(line + "\n");
                        }
                        mFlag = true;
                        reader.close();
                        conn.disconnect();
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean flagChange() {
            return mFlag;
        }
    }
}
