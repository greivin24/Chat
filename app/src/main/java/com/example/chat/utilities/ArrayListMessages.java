package com.example.chat.utilities;

import com.example.chat.models.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ArrayListMessages {

    private ArrayList<Message> arrayListMessages;

    public ArrayListMessages() {
        arrayListMessages = new ArrayList<>();
    }

    public ArrayList<Message> getArrayListMessages() {
        ArrayList<Message> tem = arrayListMessages;
        Collections.reverse(tem);
        return tem;
    }

    public void insertInArrayListMessage(Message message){
            arrayListMessages.add(message);
    }

    public void clearArrayListMessage(){
        this.arrayListMessages.clear();
    }

    public Message getArrayListMessageById(int position){
        return arrayListMessages.get(position);
    }
}
