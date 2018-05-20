package com.example.chat.controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.UUID;

public class ServerClass extends Thread {
    private final BluetoothServerSocket serverSocket;
    private BluetoothAdapter bluetoothAdapter;
    private static final String APP_NAME = "BluetoothChatApp";

    private SendReceive sendReceive;

    public ServerClass(BluetoothAdapter adapter, UUID MY_UUID, SendReceive sendR) {
        BluetoothServerSocket tmp = null;
        bluetoothAdapter = adapter;
        this.sendReceive = sendR;
        try {
            tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        serverSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket= null;
        while (socket == null) {
            try {
                Message message = Message.obtain();
                message.what= MainActivity.CONNECTING;
                MainActivity.handler.sendMessage(message);
                socket = serverSocket.accept();
            } catch (IOException e) {
                Message message = Message.obtain();
                message.what= MainActivity.CONNECTION_FAILED;
                MainActivity.handler.sendMessage(message);
            }

            if(socket!=null){
                Message message = Message.obtain();
                message.what= MainActivity.CONNECTED;
                MainActivity.handler.sendMessage(message);
                sendReceive = new SendReceive(socket);
                sendReceive.start();

                break;
            }
        }
    }

    public void cancel() {
        try {
            serverSocket.close();
        } catch (IOException e) {
        }
    }
}
