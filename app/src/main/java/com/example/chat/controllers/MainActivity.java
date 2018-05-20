package com.example.chat.controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chat.R;
import com.example.chat.adapters.DeviceListAdapter;
import com.example.chat.adapters.MessageListAdapter;
import com.example.chat.models.Backgrounds;
import com.example.chat.utilities.ArrayListDevices;
import com.example.chat.utilities.ArrayListMessages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private static Boolean IS_SERVER = false;
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    public static final int LISTENING = 1;
    public static final int CONNECTING = 2;
    public static final int CONNECTED = 3;
    public static final int CONNECTION_FAILED = 4;
    public static final int LEER = 5;
    public static final int ESCRIBIR = 6;

    BluetoothAdapter bluetoothAdapter;
    static BluetoothDevice temDevice;

    LinearLayout linearLayoutChat, linearLayoutDevice, linearLayoutSend;
    ListView listViewChat, listViewDevices;
    MenuItem menuItem;

    //---- RadioButton
    RadioButton radioButtonServer;
    static TextView status;
    //----ImageView del chat y del search
    ImageView imageViewSend, imageViewChat, imageViewSearch, imageViewEdit;

    //---EidtText
    EditText editTextMessage;
    //------------------ ArrayListDevices Y ArrayListMessages Y SUS ADAPTERS
    static ArrayListMessages arrayListMessages;
    static MessageListAdapter messageListAdapter;
    ArrayListDevices arrayListDevices;
    DeviceListAdapter deviceListAdapter;

    //---opcion3
    Backgrounds backgrounds;
    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //----Inicializacion de clases
        arrayListDevices = new ArrayListDevices();
        arrayListMessages = new ArrayListMessages();
        backgrounds = new Backgrounds((ConstraintLayout)findViewById(R.id.container));
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        findViewByIdElemets();

        //----Enlaze de los listViewDevices y listViewMensages
        messageListAdapter = new MessageListAdapter(getApplicationContext(),arrayListMessages.getArrayListMessages());
        listViewChat.setAdapter(messageListAdapter);

        deviceListAdapter = new DeviceListAdapter(getApplicationContext(), R.layout.item_device, arrayListDevices.getArrayListDevices());
        listViewDevices.setAdapter(deviceListAdapter);

        setOnClickListenerElements();

    }

    private void setOnClickListenerElements() {
        radioButtonServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverClass = new ServerClass(bluetoothAdapter,MY_UUID);
                if(!IS_SERVER){
                    serverClass.start();
                    IS_SERVER = true;
                }else{
                    serverClass.cancel();
                    radioButtonServer.setChecked(false);
                    IS_SERVER = false;
                }
            }
        });

        imageViewSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editTextMessage.getText().toString().equals("") && status.getText().equals("Conectado")) {
                    byte[] send = editTextMessage.getText().toString().getBytes();
                    if(!IS_SERVER){
                       clientClass.getSendReceive().write(send);
                    }else{
                        serverClass.getSendReceive().write(send);
                    }

                    editTextMessage.setText("");
                }
            }
        });

        imageViewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatBluetooth();
            }
        });

        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBluetooth();
            }
        });

        imageViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgrounds.nextBackgrounds();
            }
        });
    }

    private void findViewByIdElemets() {

        //----- Radio Button castin
        radioButtonServer = (RadioButton)findViewById(R.id.radioButtonServidor);
        radioButtonServer.setChecked(false);
        //-----ImageView casting
        imageViewSend = (ImageView) findViewById(R.id.imageViewSend);
        imageViewChat = (ImageView) findViewById(R.id.imageViewChat);
        imageViewSearch = (ImageView) findViewById(R.id.imageViewSearch);
        imageViewEdit = (ImageView) findViewById(R.id.imageViewEdit);
        //---EditText casting
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        //----LinearLayout de los tab
        linearLayoutChat = (LinearLayout) findViewById(R.id.layout_chat);
        linearLayoutDevice = (LinearLayout) findViewById(R.id.layout_device);
        linearLayoutSend = (LinearLayout) findViewById(R.id.linearLayoutSend);

        //----ListView del devices y los de chat
        listViewChat = (ListView) findViewById(R.id.list_item_mensaje);
        listViewDevices = (ListView) findViewById(R.id.list_item_divices);

        //----TextView status
        status = (TextView) findViewById(R.id.txv_estado);
    }

    private AdapterView.OnItemClickListener deviceClick = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            bluetoothAdapter.cancelDiscovery();
            temDevice = arrayListDevices.getArrayListDevicesById(position);
            clientClass = new ClientClass(arrayListDevices.getArrayListDevicesById(position),MY_UUID);
            clientClass.start();
            status.setText("Connecting");
            chatBluetooth();
        }
    };

    public void chatBluetooth(){
        bluetoothAdapter.cancelDiscovery();
        linearLayoutDevice.setVisibility(View.GONE);
        linearLayoutChat.setVisibility(View.VISIBLE);
        linearLayoutSend.setVisibility(View.VISIBLE);
    }

    public void searchBluetooth(){
        arrayListMessages.clearArrayListMessage();
        linearLayoutChat.setVisibility(View.GONE);
        linearLayoutSend.setVisibility(View.GONE);
        linearLayoutDevice.setVisibility(View.VISIBLE);
        arrayListDevices.clearArrayListDevices();
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(broadcastReceiverDiscover, intentFilter);
            intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver(broadcastReceiverDiscover, intentFilter);
        }
        if(!bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.startDiscovery();
            //Registramos el evento de cuando se descubre un dispositivo
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(broadcastReceiverDiscover, intentFilter);
            //Registramos el evento de cuando finaliza la busqueda de dispositivos
            intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver(broadcastReceiverDiscover, intentFilter);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        linearLayoutDevice.setVisibility(View.GONE);
        listViewDevices.setOnItemClickListener(deviceClick);
        if (bluetoothAdapter == null) {
            finish();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiverDiscover);
        bluetoothAdapter.disable();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        menuItem = item;
        switch (item.getItemId()) {
            case R.id.men_visible:
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
                startActivity(discoverableIntent);

                IntentFilter intentFilter = new IntentFilter(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                registerReceiver(broadcastReceiverDiscover,intentFilter);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private final BroadcastReceiver broadcastReceiverDiscover = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String accion = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(accion)) {
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                arrayListDevices.insertInArrayListDevices(device);
                deviceListAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(accion)) {
                Toast.makeText(getApplicationContext(),"Tiempo de Busqueda Agotado",Toast.LENGTH_SHORT).show(); // termino la busqueda de devices
            }
            if (accion.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        menuItem.setIcon(R.drawable.ic_visibility_black_24dp);
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Toast.makeText(getApplicationContext(),"Tiempo de Visibilidad de Dispositivo Agotado",Toast.LENGTH_SHORT).show();
                        menuItem.setIcon(R.drawable.ic_visibility_off_black_24dp);
                        break;
                }

            }
        }
    };


    public static Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case LISTENING:
                    status.setText("Esparando..");
                    break;
                case CONNECTING:
                    status.setText("Conectando..");
                    break;
                case CONNECTED:
                    status.setText("Conectado");
                    break;
                case CONNECTION_FAILED:
                    status.setText("Conexion Fallida");
                    break;
                case ESCRIBIR:
                    //status.setText(temDevice.getName());
                    byte[] recibido = (byte[]) msg.obj;
                    String rMessage = new String(recibido);
                    arrayListMessages.insertInArrayListMessage(new com.example.chat.models.Message(rMessage,true));
                    messageListAdapter.notifyDataSetChanged();
                    break;
                case LEER:
                    //status.setText(temDevice.getName());
                    byte[] enviado = (byte[]) msg.obj;
                    String sMessage = new String(enviado, 0, msg.arg1);
                    arrayListMessages.insertInArrayListMessage(new com.example.chat.models.Message(sMessage,false));
                    messageListAdapter.notifyDataSetChanged();
                    break;
            }
            return true;
        }
    });


}
