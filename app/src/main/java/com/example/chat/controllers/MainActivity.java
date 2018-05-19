package com.example.chat.controllers;

import android.Manifest;
import android.app.AutomaticZenRule;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chat.R;
import com.example.chat.adapters.DeviceListAdapter;
import com.example.chat.adapters.MessageListAdapter;
import com.example.chat.utilities.ArrayListDevices;
import com.example.chat.utilities.ArrayListMessages;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PrincipalActivity";
    public static final int ESTADO_CONEXION = 1;
    public static final int LEER_MENSAJE = 2;
    public static final int ESCRIBIR_MENSAJE = 3;
    public static final int DISPOSITIVO_CONECTADO = 4;
    public static final int DISPOSITIVO_ENLAZADO = 12;
    public static BluetoothDevice dispositivoEnlazado;

    public static Boolean VISIBLE = false;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice diviceConectado;
    ChatController chatController;

    LinearLayout linearLayoutChat, linearLayoutDevice;
    ListView listViewChat, listViewDevices;
    MenuItem menuItem;

    TextView status, conect;
    //----ImageView del chat y del search
    ImageView imageViewChat, imageViewSearch;

    //------------------ ArrayListDevices Y ArrayListMessages Y SUS ADAPTERS
    ArrayListMessages arrayListMessages;
    MessageListAdapter messageListAdapter;
    ArrayListDevices arrayListDevices;
    DeviceListAdapter deviceListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //----Inicializacion de clases
        arrayListDevices = new ArrayListDevices();
        arrayListMessages = new ArrayListMessages();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //-----ImageView casting
        imageViewChat = (ImageView) findViewById(R.id.imageViewChat);
        imageViewSearch = (ImageView) findViewById(R.id.imageViewSearch);

        //----LinearLayout de los tab
        linearLayoutChat = (LinearLayout) findViewById(R.id.layout_chat);
        linearLayoutDevice = (LinearLayout) findViewById(R.id.layout_device);

        //----ListView del devices y los de chat
        listViewChat = (ListView) findViewById(R.id.list_item_mensaje);
        listViewDevices = (ListView) findViewById(R.id.list_item_divices);

        //----TextView conect y status
        conect = (TextView) findViewById(R.id.txv_conectado);
        status = (TextView) findViewById(R.id.txv_estado);

        //----Enlaze de los listViewDevices y listViewMensages
        messageListAdapter = new MessageListAdapter(getApplicationContext(),arrayListMessages.getArrayListMessages());
        listViewChat.setAdapter(messageListAdapter);

        deviceListAdapter = new DeviceListAdapter(getApplicationContext(), R.layout.item_device, arrayListDevices.getArrayListDevices());
        listViewDevices.setAdapter(deviceListAdapter);


        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiverBonded, filter);

        // eventos click de chat y search
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

    private AdapterView.OnItemClickListener deviceClick = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            //first cancel discovery because its very memory intensive.
            bluetoothAdapter.cancelDiscovery();
            String deviceName = arrayListDevices.getArrayListDevicesById(position).getName();
            String deviceAddress = arrayListDevices.getArrayListDevicesById(position).getAddress();

            Toast.makeText(getApplicationContext(), "nombre: "+deviceName+" mac: "+deviceAddress, Toast.LENGTH_SHORT).show();

            //create the bond.
            //NOTE: Requires API 17+? I think this is JellyBean
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
                Log.d(TAG, "Trying to pair with " + deviceName);
                arrayListDevices.getArrayListDevicesById(position).createBond();
            }

            if(arrayListDevices.getArrayListDevicesById(position).getBondState() == DISPOSITIVO_ENLAZADO){
                dispositivoEnlazado = arrayListDevices.getArrayListDevicesById(position);
                Log.d(TAG, "enlazado con: " + dispositivoEnlazado.getName());
                chatBluetooth();
            }
        }
    };

    public void chatBluetooth(){
        //Si el controlador del chat se encuentra desconectado entonces lo iniciamos.
        if (chatController != null && chatController.getState() == ChatController.STATE_NONE) {
            chatController.start();
        }
        //Si ya tenemos el dispositivo con el que vamos a enlazar entonces nos conectamos
        if(dispositivoEnlazado != null) {
            conect.setText("Conectado con: "+dispositivoEnlazado.getName());
            BluetoothDevice dispositivo = bluetoothAdapter.getRemoteDevice(dispositivoEnlazado.getAddress());
            chatController.connect(dispositivo);
        }
        bluetoothAdapter.cancelDiscovery();
        linearLayoutDevice.setVisibility(View.GONE);
        linearLayoutChat.setVisibility(View.VISIBLE);
    }

    public void searchBluetooth(){
        linearLayoutChat.setVisibility(View.GONE);
        linearLayoutDevice.setVisibility(View.VISIBLE);
        Log.d(TAG, "searchDevices: Looking for unpaired devices.");
        arrayListDevices.clearArrayListDevices();
        if(bluetoothAdapter.isDiscovering()){
            checkBTPermissions();
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
            checkBTPermissions();
            bluetoothAdapter.startDiscovery();
            //Registramos el evento de cuando se descubre un dispositivo
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(broadcastReceiverDiscover, intentFilter);
            //Registramos el evento de cuando finaliza la busqueda de dispositivos
            intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver(broadcastReceiverDiscover, intentFilter);
        }
    }


    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
                permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
                if (permissionCheck != 0) {

                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
                }
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
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
        } else {
            //Si el Bluetooth está activado entonces se crea un nuevo el controlador del chat
            if (chatController == null) {
                chatController = new ChatController(handler);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiverDiscover);
        unregisterReceiver(broadcastReceiverBonded);
        bluetoothAdapter.disable();
        if (chatController != null) {
            chatController.stop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Cuando se inicia el Activity se llama a esta
        // función para crear el menú de acuerdo al recurso
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Captura de eventos de acuerdo al item seleccionado en el menú
        // este item es definido de acuerdo al android:id en el menu.xml
        menuItem = item;
        switch (item.getItemId()) {
            case R.id.men_visible:
                if(item.getIcon().equals(R.drawable.ic_visibility_off_black_24dp)){
                    item.setIcon(R.drawable.ic_visibility_black_24dp);
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3);
                    startActivity(discoverableIntent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private final BroadcastReceiver broadcastReceiverDiscover = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Obtenemos la acción que ejecuta el Bluethooth
            String accion = intent.getAction();
            //Si se encontró un nuevo dispositvo
            if (BluetoothDevice.ACTION_FOUND.equals(accion)) {
                //Obtenemos el dispostivo encontrado
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                arrayListDevices.insertInArrayListDevices(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                deviceListAdapter.notifyDataSetChanged();
                //Si finalizó la busqueda de dispositivos
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(accion)) {
                Toast.makeText(getApplicationContext(),"ACTION_DISCOVERY_FINISHED",Toast.LENGTH_SHORT).show(); // termino la busqueda de devices
                //Si hay cambio en el modo de exploración bluetooth
            } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(accion)) {
                //Obtenemos el modo de escaneo en el que se encuentra nuestro dispositivo
                int modo = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                //Si el dispositivo se encuentra visible
                if (modo == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Toast.makeText(getApplicationContext(),"VISIBLE",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),"NO ES VISIBLE",Toast.LENGTH_SHORT).show();
                    menuItem.setIcon(R.drawable.ic_visibility_off_black_24dp);
                }
            }
        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver broadcastReceiverBonded = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Obtenemos la acción que ejecuta el Bluethooth
            String accion = intent.getAction();
            //Si hay un cambio en el estado de enlace de un dispositivo
            if(accion.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Si el enlace está listo
                if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(getBaseContext(), "Dispositivo vinculado", Toast.LENGTH_SHORT).show();
                    //Nos redirigiimos al chat
                   // ChatActivity.dispositivoEnlazado = dispositivoEnlazado;
                    //finish();
                }
                //Si se está creando un enlace
                if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(getBaseContext(), "Vinculando dispositivo...", Toast.LENGTH_SHORT).show();
                }
                //Si se rompio el enlace
                if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Toast.makeText(getBaseContext(), "Dispositivo desvinculado", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };



    //Con Handler podemos enviar y recibir mensajes asociados al hilo ChatController
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case ESTADO_CONEXION:
                    //Si el dispositivo se encuentra conectado entonces mostramos el nombre del dispositivo con el que se encuentra enlazado
                    if(msg.arg1 == ChatController.STATE_CONNECTED) {
                        conect.setText(diviceConectado.getName());
                        //Si el dispositivo se encuentra creando conexión entonces mostramos que se está conectando
                    }else if(msg.arg1 == ChatController.STATE_CONNECTING) {
                        conect.setText("Conectando...");
                        //Si el dispositivo se encuentra desconectado entonces indicamos que no hay vínculo
                    }else if(msg.arg1 == ChatController.STATE_NONE) {
                        conect.setText("No vinculado");
                    }
                    break;
                case ESCRIBIR_MENSAJE:
                    //Obtenemos el mensaje enviado para poderlo imprimir en pantalla
                    byte[] mensajeEnviado = (byte[]) msg.obj;
                    //Convertimos el mensaje enviado a String
                    String s_mensajeEnviado = new String(mensajeEnviado);
                    //Creamos un nuevo mensaje de salida para mostrarlo en el chat
                    arrayListMessages.insertInArrayListMessage(new com.example.chat.models.Message(s_mensajeEnviado,true));
                    //Actualizamos el adaptador de mensajería para que el mensaje aparezca en pantalla
                    messageListAdapter.notifyDataSetChanged();
                    //Reproducimos el sonido para el mensaje
                    //sonidoMensaje.start();
                    break;
                case LEER_MENSAJE:
                    //Obtenemos el mensaje recibido para poderlo imprimir en pantalla
                    byte[] mensajeRecibido = (byte[]) msg.obj;
                    //Convertimos el mensaje enviado a String.// msg.arg1 es el tamaño del mensaje recibido y se usa para crear el String con
                    //ese tamaño ya que msg.obj es de un tamaño de 1024 byte y hay que descartar los datos basura
                    String readMessage = new String(mensajeRecibido, 0, msg.arg1);
                    //Creamos un nuevo mensaje de entrada para mostrarlo en el chat
                    arrayListMessages.insertInArrayListMessage(new com.example.chat.models.Message(readMessage,false));
                    //Actualizamos el adaptador de mensajería para que el mensaje aparezca en pantalla
                    messageListAdapter.notifyDataSetChanged();
                    //Reproducimos el sonido para el mensaje
                    //sonidoMensaje.start();
                    break;
                case DISPOSITIVO_CONECTADO:
                    //Obtenemos el dispositivo conectado al nuestro
                    diviceConectado = msg.getData().getParcelable("dispositivoConectado");
                    break;
            }
            return false;
        }
    });

}
