package yongcan.dragscaleroundimageview;

/**
 * 用来沟通DragScaleRoundImageView和手势适配器
 * Created by Administrator on 2016/3/16 0016.
 */
public interface DragScaleRoundImageViewCallBack {

    /**
     * 当用户双击View的时候触发的事件
     */
    void onDoubleTap();

    /**
     * 当用户长按的时候触发的事件
     */
    void onLongPress();
}
