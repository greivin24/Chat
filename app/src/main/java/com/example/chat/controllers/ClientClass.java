package com.example.chat.controllers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.UUID;

public class ClientClass extends  Thread{
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private UUID MY_UUID;
    private SendReceive sendReceive;


    public ClientClass(BluetoothDevice device, UUID uuid, SendReceive SendR){
        bluetoothDevice = device;
        this.MY_UUID = uuid;
        this.sendReceive = SendR;
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        try {
            bluetoothSocket.connect();
            Message message = Message.obtain();
            message.what= MainActivity.CONNECTED;
            MainActivity.handler.sendMessage(message);
            sendReceive = new SendReceive(bluetoothSocket);
            sendReceive.start();
        }catch (IOException e){
            Message message = Message.obtain();
            message.what= MainActivity.CONNECTION_FAILED;
            MainActivity.handler.sendMessage(message);
        }
    }
}
