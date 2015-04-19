package com.example.styleex.pccontrol;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.net.*;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void connect(View v) {

        SeekBar seek = (SeekBar)findViewById(R.id.seekBar);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                new ChangeVolumeTask().execute();
            }
        });
    }

    private class ChangeVolumeTask extends android.os.AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls) {
            EditText ipEdit = (EditText)findViewById(R.id.ip);
            SeekBar seek = (SeekBar)findViewById(R.id.seekBar);

            try {
                org.json.JSONObject json = new org.json.JSONObject();
                org.json.JSONArray arr = new JSONArray();
                arr.put(Integer.toString(seek.getProgress()));

                json.put("name", "volume");
                json.put("args", arr);

                InetAddress serverAddr = InetAddress.getByName(ipEdit.getText().toString());
                Socket clientSocket = new Socket(serverAddr, 9999);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                outToServer.writeBytes(json.toString());
                clientSocket.close();
            } catch (Exception e) {
                e.fillInStackTrace();
            }
            return true;
        }
    }
}
