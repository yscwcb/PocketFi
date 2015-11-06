package android.ch.com.pocketfidatausage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private String tag = "MainActivity";
    private HashMap<String, Double> dataInfoMap = new HashMap<String, Double>();
    private NetworkThread nt = new NetworkThread();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nt.execute();
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.refresh_btn: {
                nt.execute();
            }
        }
    }

    public void onBackPressed() {
        // TODO Auto-generated method stub
        // super.onBackPressed(); //지워야 실행됨

        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setMessage("정말 종료하시겠습니까?");
        d.setPositiveButton("예", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // process전체 종료
                NetworkConnection.getInstance().release();
                finish();
            }
        });
        d.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        d.show();
    }

    public class NetworkThread extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... args) {
            NetworkConnection networkConnection = NetworkConnection.getInstance();
            return networkConnection.getDataUsageInfo();
        }

        protected void onPostExecute(JSONObject jsonObject) {
            parseDataUsageInfo(jsonObject);

            TextView rxText = (TextView)findViewById(R.id.rx_total);
            TextView txText = (TextView)findViewById(R.id.tx_total);
            TextView totalDataText = (TextView)findViewById(R.id.total_data);

            rxText.setText(getReadable(dataInfoMap.get("Total_RX")));
            txText.setText(getReadable(dataInfoMap.get("Total_TX")));
            totalDataText.setText(getReadable(dataInfoMap.get("Total_Usage")));
        }

        public String getReadable(Double data) {
            String readableData = null;

            if(data< 1024)                  readableData = String.format("%.2f", data) +"Bytes";
            else if(data < 1024*1024)       readableData = String.format("%.2f", data/1024)+"KB";
            else if(data < 1024*1024*1024)  readableData = String.format("%.2f", data/(1024*1024))+"MB";
            else                            readableData = String.format("%.2f", data/(1024*1024*1024))+"GB";

            return readableData;
        }
        public void parseDataUsageInfo(JSONObject jsonObject)  {
            try {
                JSONObject lteJson = jsonObject.getJSONObject("lte").getJSONObject("statistics");
                Log.e(tag, lteJson.toString());
                dataInfoMap.put("Total_TX", Double.parseDouble(lteJson.getString("TOTAL_TX_BYTES")));
                dataInfoMap.put("Total_RX", Double.parseDouble(lteJson.getString("TOTAL_RX_BYTES")));
                double total = dataInfoMap.get("Total_TX") + dataInfoMap.get("Total_RX");
                dataInfoMap.put("Total_Usage", total);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
