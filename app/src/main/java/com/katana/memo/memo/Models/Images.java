package com.katana.memo.memo.Models;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.HashMap;


public class Images {


    private HashMap<Integer, Drawable> imageList;

    public Images(ArrayList<Drawable> images) {

        imageList = new HashMap<>();

        for (int i = 0; i < images.size(); i++) {
            imageList.put(i, images.get(i));
        }

    }

    public ArrayList<Drawable> getImageItem() {

        ArrayList<Drawable> drawables = new ArrayList<>();

        for (int i = 0; i < imageList.size(); i++) {
            drawables.add(imageList.get(i));
        }

        return drawables;

    }
}
