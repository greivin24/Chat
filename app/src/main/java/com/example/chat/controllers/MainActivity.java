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
import com.example.chat.utilities.ArrayListDevices;
import com.example.chat.utilities.ArrayListMessages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PrincipalActivity";
    private static Boolean IS_SERVER = false;
    private static int BACKGROUND = 1;

    private static final String APP_NAME = "BluetoothChatApp";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    public static final int LISTENING = 1;
    public static final int CONNECTING = 2;
    public static final int CONNECTED = 3;
    public static final int CONNECTION_FAILED = 4;
    public static final int LEER = 5;
    public static final int ESCRIBIR = 6;

    BluetoothAdapter bluetoothAdapter;

    LinearLayout linearLayoutChat, linearLayoutDevice;
    ListView listViewChat, listViewDevices;
    MenuItem menuItem;

    //---- RadioButton
    RadioButton radioButtonServer;
    TextView status;
    //----ImageView del chat y del search
    ImageView imageViewSend, imageViewChat, imageViewSearch;

    //---EidtText
    EditText editTextMessage;
    //------------------ ArrayListDevices Y ArrayListMessages Y SUS ADAPTERS
    ArrayListMessages arrayListMessages;
    MessageListAdapter messageListAdapter;
    ArrayListDevices arrayListDevices;
    DeviceListAdapter deviceListAdapter;

    //---opcion3
    SendReceive sendReceive;

    // contra
    ConstraintLayout mainLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //----Inicializacion de clases
        arrayListDevices = new ArrayListDevices();
        arrayListMessages = new ArrayListMessages();

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
                ServerClass serverClass = new ServerClass();
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
                if (!editTextMessage.getText().toString().equals("")) {
                    byte[] send = editTextMessage.getText().toString().getBytes();
                    sendReceive.write(send);
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
    }

    private void findViewByIdElemets() {

        //---mainLayouta
        mainLayout = (ConstraintLayout) findViewById(R.id.container);
        //----- Radio Button castin
        radioButtonServer = (RadioButton)findViewById(R.id.radioButtonServidor);
        radioButtonServer.setChecked(false);
        //-----ImageView casting
        imageViewSend = (ImageView) findViewById(R.id.imageViewSend);
        imageViewChat = (ImageView) findViewById(R.id.imageViewChat);
        imageViewSearch = (ImageView) findViewById(R.id.imageViewSearch);

        //---EditText casting
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        //----LinearLayout de los tab
        linearLayoutChat = (LinearLayout) findViewById(R.id.layout_chat);
        linearLayoutDevice = (LinearLayout) findViewById(R.id.layout_device);

        //----ListView del devices y los de chat
        listViewChat = (ListView) findViewById(R.id.list_item_mensaje);
        listViewDevices = (ListView) findViewById(R.id.list_item_divices);

        //----TextView status
        status = (TextView) findViewById(R.id.txv_estado);
    }

    private AdapterView.OnItemClickListener deviceClick = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            bluetoothAdapter.cancelDiscovery();
            ClientClass clientClass = new ClientClass(arrayListDevices.getArrayListDevicesById(position));
            clientClass.start();
            status.setText("Connecting");
            chatBluetooth();
        }
    };

    public void chatBluetooth(){
        bluetoothAdapter.cancelDiscovery();
        linearLayoutDevice.setVisibility(View.GONE);
        linearLayoutChat.setVisibility(View.VISIBLE);
    }

    public void searchBluetooth(){
        arrayListMessages.clearArrayListMessage();
        linearLayoutChat.setVisibility(View.GONE);
        linearLayoutDevice.setVisibility(View.VISIBLE);
        Log.d(TAG, "searchDevices: Looking for unpaired devices.");
        arrayListDevices.clearArrayListDevices();
        if(bluetoothAdapter.isDiscovering()){
            Log.d(TAG, "searchDevices: Canceling discovery.");
            bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();
            //Registramos el evento de cuando se descubre un dispositivo
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(broadcastReceiverDiscover, intentFilter);
            //Registramos el evento de cuando finaliza la busqueda de dispositivos
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


    private Handler handler = new Handler(new Handler.Callback() {
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
                    status.setText("Vinculado");
                    byte[] recibido = (byte[]) msg.obj;
                    String rMessage = new String(recibido);
                    arrayListMessages.insertInArrayListMessage(new com.example.chat.models.Message(rMessage,true));
                    messageListAdapter.notifyDataSetChanged();
                    break;
                case LEER:
                    status.setText("Vinculado");
                    byte[] enviado = (byte[]) msg.obj;
                    String sMessage = new String(enviado, 0, msg.arg1);
                    arrayListMessages.insertInArrayListMessage(new com.example.chat.models.Message(sMessage,false));
                    messageListAdapter.notifyDataSetChanged();
                    break;
            }
            return true;
        }
    });

    public void editBackground(View view){
        switch (BACKGROUND){
            case 1:
                mainLayout.setBackgroundResource(R.drawable.fondo2);
                BACKGROUND = 2;
                break;
            case 2:
                mainLayout.setBackgroundResource(R.drawable.fondo3);
                BACKGROUND = 3;
                break;
            case 3:
                mainLayout.setBackgroundResource(R.drawable.fondo1);
                BACKGROUND = 1;
                break;
        }
    }


    private class ServerClass extends Thread {
        private final BluetoothServerSocket serverSocket;

        public ServerClass() {
            BluetoothServerSocket tmp = null;
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
                    message.what= CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Message message = Message.obtain();
                    message.what= CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if(socket!=null){
                    Message message = Message.obtain();
                    message.what= CONNECTED;
                    handler.sendMessage(message);
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

    private class ClientClass extends  Thread{
        private BluetoothDevice bluetoothDevice;
        private  BluetoothSocket bluetoothSocket;

        public ClientClass(BluetoothDevice device){
            bluetoothDevice = device;
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
                message.what= CONNECTED;
                handler.sendMessage(message);
                sendReceive = new SendReceive(bluetoothSocket);
                sendReceive.start();
            }catch (IOException e){
                Message message = Message.obtain();
                message.what= CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendReceive extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket){
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
                    handler.obtainMessage(LEER, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // write to OutputStream
        public void write(byte[] buffer) {
            try {
                outputStream.write(buffer);
                handler.obtainMessage(ESCRIBIR, -1, -1, buffer).sendToTarget();
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
}
