# DragScaleRoundImageView

圆角图片控件，支持圆角边框的同时可以放大并且拖动图片位置

#Usage

#XML:
        <yongcan.dragscaleroundimageview.DragScaleRoundImageView
                android:id="@+id/iv"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_margin="10dp"
                android:src="@drawable/bg"/>
        
DragScaleRoundImageView iv = (DragScaleRoundImageView) findViewById(R.id.iv);

iv.setBorderRadius(20);//设置圆角

#演示
![image](https://github.com/goushengLi/DragScaleRoundImageView/blob/master/MyApplication/app/src/main/res/drawable/show.gif)
