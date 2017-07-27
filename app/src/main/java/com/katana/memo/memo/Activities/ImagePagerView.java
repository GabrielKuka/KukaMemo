package com.katana.memo.memo.Activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.katana.memo.memo.Adapters.FragmentPagerAdapter;
import com.katana.memo.memo.Models.Images;
import com.katana.memo.memo.R;
import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

import java.util.ArrayList;

public class ImagePagerView extends FragmentActivity implements
        OnClickListener, OnPageChangeListener {

    private Button btnImagePrevious, btnImageNext;
    private int position = 0, totalImage;
    private ViewPager viewPage;
    private ArrayList<Drawable> itemData;
    private FragmentPagerAdapter adapter;
    private Images image;
    private Storage storage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_imageview_page);

        storage = SimpleStorage.getInternalStorage(this);

        viewPage = (ViewPager) findViewById(R.id.viewPager);
        btnImagePrevious = (Button) findViewById(R.id.btnImagePrevious);
        btnImageNext = (Button) findViewById(R.id.btnImageNext);

        ArrayList<String> imagesList = getIntent().getStringArrayListExtra("images");

        ArrayList<Drawable> images = new ArrayList<>();

        for (String path : imagesList) {
            images.add(Drawable.createFromPath(storage.getFile("Images", path).getPath()));
        }

        image = new Images(images);
        itemData = image.getImageItem();
        totalImage = itemData.size();

        position = getIntent().getIntExtra("currentImage", 0);

        setPage(position);

        adapter = new FragmentPagerAdapter(getSupportFragmentManager(),
                itemData);
        viewPage.setAdapter(adapter);
        viewPage.setOnPageChangeListener(ImagePagerView.this);
        viewPage.setCurrentItem(position);

        btnImagePrevious.setOnClickListener(this);
        btnImageNext.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == btnImagePrevious) {
            position--;
            viewPage.setCurrentItem(position);
        } else if (v == btnImageNext) {
            position++;
            viewPage.setCurrentItem(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int position) {
        this.position = position;
        setPage(position);
    }

    private void setPage(int page) {
        if (page == 0 && totalImage == 1) {
            btnImageNext.setVisibility(View.INVISIBLE);
            btnImagePrevious.setVisibility(View.INVISIBLE);
        } else if (page == 0 && totalImage > 0) {
            btnImageNext.setVisibility(View.VISIBLE);
            btnImagePrevious.setVisibility(View.INVISIBLE);
        } else if (page == totalImage - 1 && totalImage > 0) {
            btnImageNext.setVisibility(View.INVISIBLE);
            btnImagePrevious.setVisibility(View.VISIBLE);
        } else {
            btnImageNext.setVisibility(View.VISIBLE);
            btnImagePrevious.setVisibility(View.VISIBLE);
        }
    }


}
