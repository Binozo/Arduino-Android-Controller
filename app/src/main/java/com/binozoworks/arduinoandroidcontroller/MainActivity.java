package com.binozoworks.arduinoandroidcontroller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {


    //General
    public final String TAG = "Arduino Controller";

    //Kommunikation zwischen Arduino und Android
    NetworkingHelper networkingHelper;

    //UI
    private LinearLayout linearLayout_logs, linearLayout_control;
    private Button button_verbinden, button_led_an, button_led_aus;
    private EditText editText_ip;

    //WriteLog
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_views();
        register_button_commands();
    }

    void register_button_commands(){
        button_led_aus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                networkingHelper.send_command("led aus");
            }
        });
        button_led_an.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                networkingHelper.send_command("led an");
            }
        });
    }

    void verbinde_mit_arduino(){
        if(editText_ip.getText().toString().isEmpty()){
            editText_ip.setError("Trage bitte eine IP ein");
            return;
        }

        String arduinoIP = editText_ip.getText().toString();

        networkingHelper = new NetworkingHelper(arduinoIP);
        networkingHelper.connect(new ArduinoEvents() {
            @Override
            public void OnConnectError(String message) {
                writeLog("Fehler beim Verbinden mit Arduino " + arduinoIP + ": " + message);
            }

            @Override
            public void OnConnect() {
                writeLog("Erfolgreich mit Arduino " + arduinoIP + " verbunden! Befehle können jetzt gesendet werden!");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        linearLayout_control.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void OnDisconnect() {
                writeLog("Von Arduino " + arduinoIP + " getrennt.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        linearLayout_control.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void OnServerMessage(String message) {
                writeLog("Nachricht von Server: " + message);
            }
        });



    }

    void writeLog(String message){
        String time = formatter.format(new Date(System.currentTimeMillis()));
        TextView textView = new TextView(this);
        textView.setText("[" + time + "] " + message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearLayout_logs.addView(textView);
            }
        });
    }

    void init_views(){
        linearLayout_logs = findViewById(R.id.linearlayout_logs);
        button_verbinden = findViewById(R.id.button_verbinden);
        linearLayout_control = findViewById(R.id.linearlayout_control);
        linearLayout_control.setVisibility(View.GONE);
        editText_ip = findViewById(R.id.edittext_ip);
        button_verbinden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verbinde_mit_arduino();
            }
        });
        button_led_an = findViewById(R.id.button_led_an);
        button_led_aus = findViewById(R.id.button_led_aus);
    }
}