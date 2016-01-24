package com.example.jose.bluetoothconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.UUID;

public class Principal extends AppCompatActivity {

    private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (!BTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();

        // If there are paired devices
        if (pairedDevices.size() > 0) {

            String[] mArrayAdapter = new String[pairedDevices.size()];
            int i = 0;

            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mArrayAdapter[i] = (device.getName() + "\n" + device.getAddress());

                i++;
            }

            // Listamos todos los dispositivos emparejados con el nuestro
            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mArrayAdapter);

            ListView listView = (ListView) findViewById(R.id.list_view);
            listView.setAdapter(itemsAdapter);
        }

        // Comprobamos si el móvil ya está buscando dispositivos
        if (BTAdapter.isDiscovering()) {
            BTAdapter.cancelDiscovery();
        }

        // Buscamos nuevos dispositivos
        BTAdapter.startDiscovery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
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

    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                int px = -54; // Valor rssi a un metro de distancia
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                double distance = getDistance(rssi, px);

                DecimalFormat df = new DecimalFormat("#.##");
                String rdistance = df.format(distance);

                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                TextView rssi_msg = (TextView) findViewById(R.id.res_busqueda);
                rssi_msg.setText(rssi_msg.getText() + name + ": " + rssi + " dBm -- Distancia aproximada: " + rdistance + "\n");
            }

            Toast.makeText(Principal.this, "Búsqueda finalizada.", Toast.LENGTH_SHORT).show();
        }
    };

    // Devuelve la distancia aproximada en metros entre dos dispositivos
    double getDistance(int rssi, int txPower) {
        // El 4 es el valor de n y si no hay obstáculos de por medio se usa el valor 2
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 4));
    }
}
