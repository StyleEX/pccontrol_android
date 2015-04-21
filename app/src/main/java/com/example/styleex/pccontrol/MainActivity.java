package com.example.styleex.pccontrol;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import org.json.JSONArray;

import java.io.*;
import java.net.*;


public class MainActivity extends ActionBarActivity {
    private Socket mSocket;

    public void setSocket(Socket mSocket) {
        this.mSocket = mSocket;
        findViewById(R.id.seekBar).setEnabled(mSocket != null);

        SeekBar.OnSeekBarChangeListener listener = null;
        if (mSocket != null) {
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
        if (mSocket == null) {
            new ConnectTask().execute(((EditText) findViewById(R.id.ip)).getText().toString());
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
        try {
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            setSocket(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        doConnect();

    }

    public void connect(View v) {
        doConnect();
    }

    private class ConnectTask extends android.os.AsyncTask<String, Void, Socket> {
        @Override
        protected Socket doInBackground(String... ip) {
            try {
                return new Socket(ip[0], 9999);
            } catch (Exception e) {
                // TODO: Maybe server program not started?
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Socket socket) {
            super.onPostExecute(socket);
            setSocket(socket);
        }
    }

    private class ChangeVolumeTask extends android.os.AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... volume) {
            try {
                org.json.JSONArray arr = new JSONArray();
                arr.put(Integer.toString(volume[0]));

                org.json.JSONObject json = new org.json.JSONObject();
                json.put("name", "volume");
                json.put("args", arr);

                DataOutputStream outToServer = new DataOutputStream(mSocket.getOutputStream());
                outToServer.writeBytes(json.toString() + '\n');
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result) {
                setSocket(null);
            }
        }
    }
}
