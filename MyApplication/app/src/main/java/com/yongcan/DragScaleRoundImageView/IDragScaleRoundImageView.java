package com.yongcan.DragScaleRoundImageView;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

/**
 * DragScaleRoundImageView中包含的主要方法
 * <p/>
 * Created by Administrator on 2016/3/15 0015.
 */
public interface IDragScaleRoundImageView {


    /**
     * 在放大状态下,如果我们缩小的时候检查图片是否跟边缘脱离
     *
     * @param matrix 当前图片的Matrix
     * @return 经过调整之后的Matrix
     */
    Matrix getCloseToBound(Matrix matrix);

    /**
     * 两个手指缩放的时候,检查图片是否已经到了限制
     *
     * @param matrix 当前图片的Matrix
     * @return 返回true表示当前还是合理缩放的范围, 返回false表示已经到达最大(小)缩放范围
     */
    boolean checkViewForZoom(Matrix matrix);

    /**
     * 在单指拖动的情况下,检测图片是否已经拖动到了边缘
     *
     * @param matrix 当前图片的Matrix
     * @return 返回true表示当前还是可拖动范围, 返回false表示已经到达边缘, 不可以再拖动
     */
    boolean checkBound(Matrix matrix);

    /**
     * 获取两个手指的中点
     *
     * @param point 用来存储的点的位置的PointF对象
     * @param event 屏幕的手势事件
     */
    void midPoint(PointF point, MotionEvent event);

    /**
     * 计算两个手指间的距离
     *
     * @param event 屏幕的手势事件
     * @return 手指之间的距离
     */
    float spacing(MotionEvent event);

    /**
     * 初始化画笔的Shader
     */
    void initShader();

    /**
     * 将ImageView的背景Drawable转化为Bitmap
     *
     * @param drawable View的背景Drawable
     * @return 转化的Bitmap
     */
    Bitmap drawableToBitamp(Drawable drawable);

    /**
     * 设置圆角的大小
     *
     * @param radius 圆角的单位(px)
     */
    void setBorderRadius(int radius);
}
