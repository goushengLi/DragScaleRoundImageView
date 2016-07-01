package yongcan.dragscaleroundimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by Administrator on 2016/3/15 0015.
 */
public class DragScaleRoundImageView extends ImageView implements IDragScaleRoundImageView, DragScaleRoundImageViewCallBack {

    /**
     * 当前DragScaleRoundImageView的拖动状态形式
     */
    static final int NONE = 0;//闲置状态
    static final int DRAG = 1;//单指拖动的状态
    static final int ZOOM = 2;//双指缩放状态

    //当前DragScaleRoundImageView的拖动状态,默认为闲置状态
    int dragState = NONE;

    //ImageView圆角的大小,默认为25PX
    private int BorderRadius = 25;

    //用来缩放ImageView背景图的Matrix,也是整个ImageView绘制过程中唯一有效的Matrix
    private Matrix mMatrix;

    //这个是用来临时存储的Matrix,因为大多数变化不能直接在原始Matrix(mMatrix)上修改,所以要创建一个临时的可供改变的Matrix
    private Matrix savedMatrix;

    //用于绘制ImageView背景图的Paint
    private Paint mBitmapPaint;

    //手势监听器,方便我们实现一些复杂的手势监听
    private GestureDetectorCompat mDetector;

    public DragScaleRoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMatrix = new Matrix();
        savedMatrix = new Matrix();
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);//开启抗锯齿效果
        mDetector = new GestureDetectorCompat(context, new DragGestureListener(this));

    }

    //ImageView的背景图片
    private Bitmap bgBitmap;

    //用于绘制背景图的着色器
    private BitmapShader mBitmapShader;

    //初始化时用于放大背景图片的比例
    private float scaleX;
    private float scaleY;

    @Override
    public void initShader() {

        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        bgBitmap = drawableToBitamp(drawable);
        mBitmapShader = new BitmapShader(bgBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        int bitmapW = bgBitmap.getWidth();
        int bitmapH = bgBitmap.getHeight();

        if ((bitmapW * 1.0f / bitmapH * 1.0f) > (viewW * 1.0f / viewH * 1.0f)) {//需要拉伸(缩小)图片的宽
            scaleY = viewH * 1.0f / bitmapH * 1.0f;
            scaleX = scaleY;
        } else {
            scaleX = viewW * 1.0f / bitmapW * 1.0f;
            scaleY = scaleX;
        }

        mMatrix.setScale(scaleX, scaleY);
        float p[] = new float[9];
        mMatrix.getValues(p);
        MIN_SCALE = p[0];
        // 设置变换矩阵
        mBitmapShader.setLocalMatrix(mMatrix);
        // 设置shader
        mBitmapPaint.setShader(mBitmapShader);
    }

    //ACTION_DOWN时记录按下的点
    PointF prevPoint = new PointF();

    //ACTION_POINTER_DOWN时两个手指的中点
    PointF midPoint = new PointF();

    //用来记录两点间的距离
    float distance = 0f;

    /**
     * 滑动的方向的状态
     */
    private int VERTICAL = 0;
    private int HORIZONTAL = 1;

    //记录滑动的方向
    private int dragDirection;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //同时将event交给手势识别器去处理
        mDetector.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:

                prevPoint.set(event.getX(), event.getY());
                savedMatrix.set(mMatrix);//手指按下的时候存储一份原图的Matrix
                dragState = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:

                distance = spacing(event);

                if (distance > 10f) {// 如果连续两点距离大于10，则判定为多点模式
                    midPoint(midPoint, event);
                    dragState = ZOOM;
                }
                break;

            case MotionEvent.ACTION_MOVE:

                switch (dragState) {

                    case DRAG:

                        if ((Math.abs(event.getX() - prevPoint.x) - Math.abs(event.getY() - prevPoint.y)) > 0) {//证明是水平滑动
                            dragDirection = HORIZONTAL;
                        } else {
                            dragDirection = VERTICAL;
                        }

                        //手指滑动的距离,除以3是为了减缓滑动的效果
                        float disX = (event.getX() - prevPoint.x) / 3;
                        float disY = (event.getY() - prevPoint.y) / 3;
                        savedMatrix.postTranslate(disX, disY);
                        if (checkBound(savedMatrix)) {

                            if (dragDirection == HORIZONTAL) {
                                mMatrix.postTranslate(disX, 0);
                                // 设置变换矩阵
                                mBitmapShader.setLocalMatrix(mMatrix);
                                // 设置shader
                                mBitmapPaint.setShader(mBitmapShader);
                                invalidate();
                            } else if (dragDirection == VERTICAL) {
                                mMatrix.postTranslate(0, disY);
                                // 设置变换矩阵
                                mBitmapShader.setLocalMatrix(mMatrix);
                                // 设置shader
                                mBitmapPaint.setShader(mBitmapShader);
                                invalidate();
                            }

                        }
                        break;

                    case ZOOM:

                        float newDistance = spacing(event);//根据新的距离来判断是否有缩放

                        if (newDistance > 10f) {//如果两点间的距离大于10才判断为缩放行为

                            savedMatrix.set(mMatrix);
                            /**
                             * 临时复制一份savedMatrix,因为我们在下一步将要进入checkViewForZoom方法去判断是否已经到了缩放
                             * 的临界点,那么如果是到达了临界点,那么我们是要返回false的,但是这个时候savedMatrix就已经发生了
                             * 变化,所以我们就可以把它通过zoomTemporaryMatrix还原回去
                             */
                            zoomTemporaryMatrix.set(savedMatrix);
                            float tScale = newDistance / distance;
                            savedMatrix.postScale(tScale, tScale, midPoint.x, midPoint.y);

                            if (checkViewForZoom(savedMatrix)) {

                                mMatrix.postScale(tScale, tScale, midPoint.x, midPoint.y);
                                //边缘判定
                                Matrix m = getCloseToBound(mMatrix);
                                mMatrix.set(m);
                                // 设置变换矩阵
                                mBitmapShader.setLocalMatrix(mMatrix);
                                // 设置shader
                                mBitmapPaint.setShader(mBitmapShader);
                                invalidate();
                            }
                        }

                        break;

                }

                break;

        }

        return true;
    }

    /**
     * 图片的状态
     */
    private final int SMALL = 1;
    private final int BIG = 2;

    //当前图片所处的状态(大方面)
    int doubleTapState = SMALL;

    @Override
    public void onDoubleTap() {

        switch (doubleTapState) {

            case SMALL://当前的状态是小,那么双击就放大
                mMatrix.setScale(MAX_SCALE, MAX_SCALE);
                // 设置变换矩阵
                mBitmapShader.setLocalMatrix(mMatrix);
                // 设置shader
                mBitmapPaint.setShader(mBitmapShader);
                invalidate();
                doubleTapState = BIG;
                break;
            case BIG://当前的状态是大,那么双击就缩小

                mMatrix.setScale(scaleX, scaleY);
                // 设置变换矩阵
                mBitmapShader.setLocalMatrix(mMatrix);
                // 设置shader
                mBitmapPaint.setShader(mBitmapShader);
                invalidate();
                doubleTapState = SMALL;
                break;
        }
    }

    @Override
    public void onLongPress() {

    }

    //缩放时临时存储的Matrix
    private Matrix zoomTemporaryMatrix = new Matrix();

    //DragScaleRoundImageView展示的区域,也就是圆角矩形
    private RectF mRoundRect;

    private int viewW;
    private int viewH;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewW = w;
        viewH = h;
        mRoundRect = new RectF(0, 0, w, h);
        initShader();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (getDrawable() == null) {
            return;
        }
        canvas.drawRoundRect(mRoundRect, BorderRadius, BorderRadius, mBitmapPaint);

    }

    @Override
    public Matrix getCloseToBound(Matrix matrix) {
        float deltaX = 0, deltaY = 0;

        //新建一个Matrix用于存放,避免当前ImageView的Matrix
        Matrix m = new Matrix();
        m.set(matrix);
        //通过mRoundRect和当前的ImageView的Matrix来计算,计算出经过缩放后的bitmap所占的Rect有多大
        RectF rect = new RectF(0, 0, bgBitmap.getWidth(), bgBitmap.getHeight());//这里同样深度复制,不要修改到原来的rect
        m.mapRect(rect);

        if (rect.left > 0) {

            deltaX = -rect.left;
        }

        if (rect.right < mRoundRect.right) {

            deltaX = mRoundRect.right - rect.right;
        }

        if (rect.top > 0) {

            deltaY = -rect.top;
        }

        if (rect.bottom < mRoundRect.height()) {

            deltaY = mRoundRect.height() - rect.bottom;
        }
        matrix.postTranslate(deltaX, deltaY);
        return matrix;
    }

    /**
     * 最小缩放比例
     */
    private float MIN_SCALE;

    /**
     * 最大缩放比例
     */
    private final float MAX_SCALE = 5f;

    @Override
    public boolean checkViewForZoom(Matrix matrix) {

        float p[] = new float[9];
        savedMatrix.getValues(p);
        if (p[0] <= MIN_SCALE) {

            savedMatrix.set(zoomTemporaryMatrix);
            return false;
        }
        if (p[0] > MAX_SCALE) {

            savedMatrix.set(zoomTemporaryMatrix);
            return false;
        }

        return true;//还在可拖动范围
    }

    @Override
    public boolean checkBound(Matrix matrix) {

        //新建一个Matrix用于存放,避免当前ImageView的Matrix
        Matrix m = new Matrix();
        m.set(matrix);
        //通过mRoundRect和当前的ImageView的Matrix来计算,计算出经过缩放后的bitmap所占的Rect有多大
        RectF rect = new RectF(0, 0, bgBitmap.getWidth(), bgBitmap.getHeight());//这里同样深度复制,不要修改到原来的rect
        m.mapRect(rect);

        //那么这个时候rect的宽高应该就是跟我们缩放过后的bitmap相对应了
        //其实这时候事情已经开始变得简单了,我们可以根据Rect.top,left,bottom,right来进行边缘判断了
        if (dragDirection == HORIZONTAL) {
            if (rect.left > 0) {
                return false;
            }
            if (rect.right < mRoundRect.right) {
                return false;
            }
        }
        if (dragDirection == VERTICAL) {

            if (rect.top > 0) {//证明拖动已经到达了边界,那么这个时候由于我们是边缘拉伸,模仿玩图的话这个时候是要开启拖动的
                return false;
            }

            if (rect.bottom < mRoundRect.height()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void midPoint(PointF point, MotionEvent event) {

        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    @Override
    public float spacing(MotionEvent event) {

        float x = 0;
        float y = 0;
        try {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return (float) Math.floor(x * x + y * y);
    }


    @Override
    public Bitmap drawableToBitamp(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 设置圆角的大小
     *
     * @param radius
     */
    @Override
    public void setBorderRadius(int radius) {
        this.BorderRadius = radius;
    }


}
