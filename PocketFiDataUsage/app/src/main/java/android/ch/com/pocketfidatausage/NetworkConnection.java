package android.ch.com.pocketfidatausage;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by Changhan on 2015-11-05.
 */
public class NetworkConnection {
    private String tag = "NetworkConnection";
    private final String serverUrl = "http://192.168.1.1/cgi-bin/webctl.cgi";

    public String mNetworkUsername;
    public String mNetworkPassword;
    java.net.CookieManager cookieManager = new java.net.CookieManager();
    private HttpURLConnection conn;

    private static final NetworkConnection networkConnection = new NetworkConnection();

    public NetworkConnection() {
        mNetworkUsername = null;
        mNetworkPassword = null;
        CookieHandler.setDefault(cookieManager);
    }

    public static NetworkConnection getInstance() {
        return networkConnection;
    }

    public void setData(String username, String password) {
        mNetworkUsername = username;
        mNetworkPassword = password;
    }

    public JSONObject loginAuth() {
        String msg = null;
        try {
            msg = "act=login&ac="+ URLEncoder.encode(networkConnection.mNetworkUsername, "UTF-8")+"&passwd="+URLEncoder.encode(networkConnection.mNetworkPassword,"UTF-8")+"&login=login";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        JSONObject result = request(serverUrl, msg);
        CookieStore cookieStore = cookieManager.getCookieStore();
        List<HttpCookie> cookies = cookieStore.getCookies();
        return result;
    }

    public JSONObject getDataUsageInfo() {
        String msg;
        msg = "act=lte_statistics;limit_data_conf";
        JSONObject result = request(serverUrl, msg);

        return result;
    }

    public void release() {
        conn.disconnect();
    }

    public JSONObject request(String urlName, String msg) {
        URL url = null;

        OutputStream os = null;

        try {
            url = new URL(urlName);
            conn = (HttpURLConnection)url.openConnection();

            if(conn != null) {
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Connection", "close");

                conn.setDoInput(true);
                conn.setDoOutput(true);
                os = conn.getOutputStream();
                os.write(msg.getBytes());
                os.flush();

                int resCode = conn.getResponseCode();
                if(resCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in =
                            new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    JSONObject jsonMsg = new JSONObject(response.toString());
                    in.close();

                    return jsonMsg;
                }
                Log.e(tag, "HTTP Fail");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
