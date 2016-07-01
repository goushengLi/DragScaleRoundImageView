package com.example.administrator.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import yongcan.dragscaleroundimageview.DragScaleRoundImageView;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DragScaleRoundImageView iv = (DragScaleRoundImageView) findViewById(R.id.iv);
        iv.setBorderRadius(20);//设置圆角
    }


}
