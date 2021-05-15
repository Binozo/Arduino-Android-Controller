package com.binozoworks.arduinoandroidcontroller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkingHelper {

    //TCP
    Socket server;
    final int PORT = 8140;
    final String arduinoIP;
    DataInputStream in;
    DataOutputStream out;

    public NetworkingHelper(String arduinoIP){
        this.arduinoIP = arduinoIP;
    }

    public void connect(ArduinoEvents events){

        new Thread(new Runnable() { //wenn das nicht in einem Thread ist, kommt eine NetworkOnMainThread Exception
            @Override
            public void run() {
                try {
                    server = new Socket(arduinoIP, 8140 );
                } catch (IOException e) {
                    e.printStackTrace();

                    events.OnConnectError("Fehler beim Verbinden: " + e.getMessage() + "\nCause: " + e.getCause());
                    return;
                }

                try{
                    in = new DataInputStream(server.getInputStream());
                    out = new DataOutputStream(server.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                    events.OnConnectError("Fehler beim öffnen des Datenstreams: " + e.getMessage() + "\nCause: " + e.getCause());
                    return;
                }

                try {
                    //Ware auf bestätigungsnachricht
                    String message = in.readUTF();
                    events.OnServerMessage(message);
                    if(message.equals("ok")){
                        events.OnConnect();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    events.OnConnectError("Fehler im Datenstreams: " + e.getMessage() + "\nCause: " + e.getCause());
                    return;
                }


            }
        }).start();
    }

    public void send_command(String command){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    out.writeUTF(command);
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        }).start();
    }



}

interface ArduinoEvents{
    void OnConnectError(String message);
    void OnConnect();
    void OnDisconnect();
    void OnServerMessage(String message);
}
