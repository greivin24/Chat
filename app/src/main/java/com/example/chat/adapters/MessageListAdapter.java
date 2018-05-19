package com.example.chat.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.chat.R;
import com.example.chat.models.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter  extends ArrayAdapter<Message> {

    public MessageListAdapter(Context contexto, ArrayList<Message> mensajes) {
        super(contexto, R.layout.item_mensaje_enviado, mensajes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message mensaje = getItem(position);
        if(mensaje.isSalida()) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mensaje_enviado, parent, false);
        }else {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mensaje_recibido, parent, false);
        }
        TextView txv_mensaje = convertView.findViewById(R.id.txv_mensaje);
        txv_mensaje.setText(mensaje.getTexto());
        return convertView;
    }
}
