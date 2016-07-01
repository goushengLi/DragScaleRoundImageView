package yongcan.dragscaleroundimageview;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by Administrator on 2016/3/16 0016.
 */
public class DragGestureListener extends GestureDetector.SimpleOnGestureListener {

    DragScaleRoundImageViewCallBack callBack;

    public DragGestureListener(DragScaleRoundImageViewCallBack callBack) {

        this.callBack = callBack;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {//双击发生时的通知

        callBack.onDoubleTap();
        return true;
    }


    @Override
    public void onLongPress(MotionEvent e) {//长按时的通知事件
        super.onLongPress(e);
        callBack.onLongPress();
    }


}
