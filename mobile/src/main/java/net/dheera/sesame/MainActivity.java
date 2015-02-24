package net.dheera.sesame;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    private static String TAG = "Sesame/MainActivity";
    private static final int SESAME_OPEN = 175;
    private static final int SESAME_CLOSE = 5;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((SeekBar)findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    setSesame(progress * 170 / 1000 + 5);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            setSesame(SESAME_OPEN);
        } catch(IOException e) {
            e.printStackTrace();
        }
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

    void findBT() {
        Log.d(TAG, "findBT()");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            Log.d(TAG, "No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                Log.d(TAG, device.getName());
                if(device.getName().equals("HC-05")) {
                    mmDevice = device;
                    Log.d(TAG, "HC-05 Device Found");
                    break;
                }
            }
        }
    }

    void openBT() throws IOException {
        Log.d(TAG, "openBT()");
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard //SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmInputStream = mmSocket.getInputStream();
        mmOutputStream = mmSocket.getOutputStream();
    }

    void setSesame(int position) throws IOException {
        String msg = String.format("write %d\n", position);

        try {
            if(mmDevice==null) { findBT(); }
            if(mmSocket==null || !mmSocket.isConnected()) { openBT(); }
            mmOutputStream.write(msg.getBytes());
            Log.d(TAG, "Data Sent");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    void closeBT() throws IOException {
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }

    public void ButtonOpen_onClick(View v) {
        try {
            setSesame(SESAME_OPEN);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    public void ButtonClose_onClick(View v) {
        try {
            setSesame(SESAME_CLOSE);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
