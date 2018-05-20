package com.example.chat.controllers;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Handler;

public class SendReceive extends Thread{
    private final BluetoothSocket bluetoothSocket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public SendReceive(BluetoothSocket socket) {
        bluetoothSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputStream = tmpIn;
        outputStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream
        while (true) {
            try {
                // Read from the InputStream
                bytes = inputStream.read(buffer);

                // Send the obtained bytes to the UI Activity
                MainActivity.handler.obtainMessage(MainActivity.LEER, bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // write to OutputStream
    public void write(byte[] buffer) {
        try {
            outputStream.write(buffer);
            MainActivity.handler.obtainMessage(MainActivity.ESCRIBIR, -1, -1, buffer).sendToTarget();
        } catch (IOException e) {
        }
    }

    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
