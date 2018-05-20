package com.example.chat.models;

import android.support.constraint.ConstraintLayout;

import com.example.chat.R;

public class Backgrounds {
    private int BACKGROUND;
    ConstraintLayout mainLayout;

    public Backgrounds(ConstraintLayout constraintLayout) {
        this.BACKGROUND = 1;
        this.mainLayout = constraintLayout;
    }

    public void nextBackgrounds(){
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
                mainLayout.setBackgroundResource(R.drawable.fondo4);
                BACKGROUND = 4;
                break;
            case 4:
                mainLayout.setBackgroundResource(R.drawable.fondo5);
                BACKGROUND = 5;
                break;
            case 5:
                mainLayout.setBackgroundResource(R.drawable.fondo1);
                BACKGROUND = 1;
                break;
        }
    }
}
