package com.binozoworks.arduinoandroidcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    WifiConfiguration currentConfig;
    WifiManager.LocalOnlyHotspotReservation hotspotReservation;

    public final String TAG = "Arduino Controller";
    ServerSocket server;
    final int PORT = 8140;

    //UI

    private LinearLayout linearLayout_controller, linearLayout_console;
    private Button button_start;
    private TextView textView_status, textView_ssid, textView_passwort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init_views();

    }

    void init_views(){
        linearLayout_controller = findViewById(R.id.linearlayout_arduino_controller);
        textView_status = findViewById(R.id.textview_status);
        textView_ssid = findViewById(R.id.textview_ssid);
        textView_passwort = findViewById(R.id.textview_passwort);
        linearLayout_console = findViewById(R.id.linearlayout_console);
        button_start = findViewById(R.id.button_start);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnHotspot();
            }
        });
    }

    public void turnOnHotspot() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        linearLayout_controller.setVisibility(View.VISIBLE);
        button_start.setVisibility(View.GONE);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                hotspotReservation = reservation;
                currentConfig = hotspotReservation.getWifiConfiguration();

                textView_ssid.setText("SSID: " + currentConfig.SSID);
                textView_passwort.setText("Passwort: " + currentConfig.preSharedKey);
                textView_status.setText("Status: System läuft");

                Log.v("DANG", "THE PASSWORD IS: "
                        + currentConfig.preSharedKey
                        + " \n SSID is : "
                        + currentConfig.SSID);

                wait_for_arduino();

            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.v("DANG", "Local Hotspot Stopped");
                textView_status.setText("Status: Hotspot wurde gestoppt.");
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Log.v("DANG", "Local Hotspot failed to start");
                textView_status.setText("Status: Hotspot konnte nicht starten: " + reason);
            }
        }, new Handler());
    }

    private void wait_for_arduino(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                write_to_console("Warte auf Arduino...");
                write_to_console("Auf IP " + Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress()) + " und port " + PORT);

                try{
                    server = new ServerSocket();
                    server.bind(new InetSocketAddress(PORT)); //sehr wichtig. Wenn fehlt, funktioniert nicht!

                    while(true){
                        try{
                            Socket client = server.accept();
                            DataInputStream input = new DataInputStream(client.getInputStream());
                            String client_nachricht = input.readUTF();
                            write_to_console("Nachricht von Client: " + client_nachricht);
                            DataOutputStream output = new DataOutputStream(client.getOutputStream());
                            output.writeUTF("Verbindung erfolgreich!");
                            client.close();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }


                }catch (Exception e){
                    e.printStackTrace();
                    write_to_console("Fehler: ");
                    write_to_console(e.getMessage());
                    write_to_console(e.getCause().toString());
                }

            }
        }).start();
    }

    private void write_to_console(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = new TextView(MainActivity.this);
                textView.setText(text);
                linearLayout_console.addView(textView);
            }
        });
    }
}