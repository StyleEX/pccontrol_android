package com.example.styleex.pccontrol;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.client.*;
import java.util.*;

import java.io.*;
import java.net.*;


public class MainActivity extends ActionBarActivity {
    private JSONRPC2Session mSession;

    public void setSession(JSONRPC2Session mSession) {
        this.mSession = mSession;
        findViewById(R.id.seekBar).setEnabled(mSession != null);

        SeekBar.OnSeekBarChangeListener listener = null;
        if (mSession != null) {
            listener = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    changeVolume(((SeekBar)findViewById(R.id.seekBar)).getProgress());
                }
            };
        }

        ((SeekBar)findViewById(R.id.seekBar)).setOnSeekBarChangeListener(listener);
    }

    private void doConnect() {
        try {
            String hostname = ((EditText) findViewById(R.id.ip)).getText().toString();
            URL url = new URL(String.format("http://%s:10000/rpc", hostname));

            setSession(new JSONRPC2Session(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void changeVolume(Integer volume) {
        new ChangeVolumeTask().execute(volume);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.seekBar).setEnabled(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setSession(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        doConnect();

    }

    public void connect(View v) {
        doConnect();
    }

    public void shutdown(View v) {
        String time = ((EditText) findViewById(R.id.shutdownTime)).getText().toString();
        new ShutdownTask().execute(Integer.parseInt(time));
    }

    protected JSONRPC2Response rpc_request(final String method,
                          final Map <String,Object> namedParams,
                          final Object id) {
        try {
            JSONRPC2Request request = new JSONRPC2Request(method, namedParams, 0);
            Log.d("Request", request.toString());

            JSONRPC2Response response = mSession.send(request);
            Log.d("response", response.toString());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private class ChangeVolumeTask extends android.os.AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... volume) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("Level", volume[0]);

            JSONRPC2Response response = rpc_request("VolumeService.SetLevel", params, 0);
            if ( response == null ) {
                Log.e("RPC Error", "Null response");
            }
            return volume[0];
        }

        @Override
        protected void onPostExecute(Integer result) {
            ((TextView)findViewById(R.id.curVolumeTextView)).setText(String.format("%d%%", result));
        }
    }

    private class ShutdownTask extends android.os.AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... time) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("Minutes", time[0]);

            JSONRPC2Response response = rpc_request("SystemService.Shutdown", params, 0);
            if ( response == null ) {
                Log.e("RPC Error", "Null response");
            }
            return time[0];
        }
    }
}
