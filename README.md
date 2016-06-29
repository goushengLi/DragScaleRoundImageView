# DragScaleRoundImageView

圆角图片控件，支持圆角边框的同时可以放大并且拖动图片位置

#Usage

<com.yongcan.DragScaleRoundImageView.DragScaleRoundImageView
        android:id="@+id/iv"
        android:layout_width="250dp"
        android:layout_height="550dp"
        android:layout_margin="10dp"
        android:src="@drawable/bg"/>
        
DragScaleRoundImageView iv = (DragScaleRoundImageView) findViewById(R.id.iv);
        iv.setBorderRadius(20);//设置圆角
