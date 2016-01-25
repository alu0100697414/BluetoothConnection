package com.example.jose.bluetoothconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
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

    private String nombre_dispositivo = "LENNY";
    private String direccion_dispositivo = "D8:3C:69:E0:26:FE";
    private int distancia_limite = 10;

    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)) {

                double px = -54; // Valor rssi a un metro de distancia
                double rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                double distance = getDistance(rssi, px);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

                // Si encuentra el móvil del agresor y supera la distancia limite, vibra
                if(name.equals(getNombre_dispositivo()) && distance < getDistancia_limite()){
                    // Parseamos el resultaandroid:gravity="center"do para que muestre dos decimales
                    DecimalFormat df = new DecimalFormat("#.##");
                    String rdistance = df.format(distance);

                    TextView rssi_msg = (TextView) findViewById(R.id.res_busqueda);
                    rssi_msg.setText("¡PELIGRO!\nSe ha superado la distancia límite. El agresor se encuentra a una distancia aproximada de:");

                    TextView res_dist = (TextView) findViewById(R.id.res_distancia);
                    res_dist.setText(rdistance + "m");

                    Toast.makeText(Principal.this, "Búsqueda finalizada.", Toast.LENGTH_SHORT).show();

                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    // Vibrate for 500 milliseconds
                    v.vibrate(800);
                }

                // Si se encuentra el dispositivo del agresor, se avisa a la victima
                else if(name.equals(getNombre_dispositivo())){
                    // Parseamos el resultado para que muestre dos decimales
                    DecimalFormat df = new DecimalFormat("#.##");
                    String rdistance = df.format(distance);

                    TextView rssi_msg = (TextView) findViewById(R.id.res_busqueda);
                    rssi_msg.setText("Fuera de la distancia de peligro.");

                    TextView res_dist = (TextView) findViewById(R.id.res_distancia);
                    res_dist.setText(rdistance + "m");

                    Toast.makeText(Principal.this, "Búsqueda finalizada.", Toast.LENGTH_SHORT).show();
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_principal);
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

        // Si está desactivado el Bluetooth, enviamos mensaje para activarlo
        if (!BTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        // Coge los dispositivos emparejados
        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();

        // Si hay dispositivos emparejados
        if (pairedDevices.size() > 0) {

            String[] mArrayAdapter = new String[1];
            int i = 0;

            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {

                if(device.getName().equals(getNombre_dispositivo())) {
                    // Añadimos el nombre y la dirección para mostrarlo luego por el listview
                    mArrayAdapter[i] = ("Nombre: " + device.getName() + "\n" + "Dirección: " + device.getAddress());
                    i++;
                }
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

        //delay in ms
        int DELAY = 12000;

        // Si cuando se acaba la búsqueda, no lo encontró, no hay peligro
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView rssi_msg = (TextView) findViewById(R.id.res_busqueda);

                if(rssi_msg.getText().equals("Buscando...")){
                    rssi_msg.setText("NO HAY PELIGRO");
                    Toast.makeText(Principal.this, "Búsqueda finalizada.", Toast.LENGTH_SHORT).show();
                }
            }
        }, DELAY);
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

    // Destructor para cuando se cierre el programa
    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    // Devuelve la distancia aproximada en metros entre dos dispositivos
    double getDistance(double rssi, double txPower) {
        // El 4 es el valor de n y si no hay obstáculos de por medio se usa el valor 2
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 4));
    }

    String getNombre_dispositivo(){
        return nombre_dispositivo;
    }

    String getDireccion_dispositivo(){
        return direccion_dispositivo;
    }

    int getDistancia_limite(){
        return distancia_limite;
    }
}
