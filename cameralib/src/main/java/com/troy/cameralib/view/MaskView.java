package com.troy.cameralib.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.troy.cameralib.R;


/**
 * Author: Troy
 * Date: 2017/8/30
 * Email: 810196673@qq.com
 * Des: MaskView
 */
public class MaskView extends ImageView {
    private static final String TAG = MaskView.class.getSimpleName();
    private Paint mLinePaint;
    private Paint mAreaPaint;
    private Rect mCenterRect = null;

    private int width; //当前CameraView的宽度
    private int height; //当前CameraView的高度

    private static final int DEFAULT_LINE_COLOR = 0xFFFFFFFF;
    private static final int DEFAULT_LINE_STROKE = 1;
    private static final int DEFAULT_LINE_ALPHA = 30;
    private static final int DEFAULT_AREA_BG_COLOR = 0xFF000000;
    private static final int DEFAULT_AREA_BG_ALPHA = 120;

    private int lineColor;
    private int lineStroke;
    private int lineAlpha;
    private int areaBgColor;
    private int areaBgAlpha;
    private Paint mBiankuangPaint;


    public MaskView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public MaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public MaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.cameView_MaskView, defStyleAttr, 0);
        lineColor = a.getColor(R.styleable.cameView_MaskView_maskview_line_color, DEFAULT_LINE_COLOR);
        lineStroke = a.getDimensionPixelOffset(R.styleable.cameView_MaskView_maskview_line_stroke, DEFAULT_LINE_STROKE);
        lineAlpha = a.getInt(R.styleable.cameView_MaskView_maskview_line_alpha, DEFAULT_LINE_ALPHA);
        areaBgColor = a.getColor(R.styleable.cameView_MaskView_maskview_area_bg_color, DEFAULT_AREA_BG_COLOR);
        areaBgAlpha = a.getInt(R.styleable.cameView_MaskView_maskview_area_bg_alpha, DEFAULT_AREA_BG_ALPHA);

        initPaint();
    }


    private void initPaint() {
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(lineColor);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(lineStroke);
        mLinePaint.setAlpha(lineAlpha);

//        //边框的画笔
//        mBiankuangPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mBiankuangPaint.setColor(lineColor);
//        mBiankuangPaint.setStyle(Paint.Style.STROKE);
//        mBiankuangPaint.setStrokeWidth(lineStroke);
//        mBiankuangPaint.setAlpha(lineAlpha);

        //绘制四周区域
        mAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAreaPaint.setColor(areaBgColor);
        mAreaPaint.setStyle(Paint.Style.FILL);
        mAreaPaint.setAlpha(areaBgAlpha);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mCenterRect == null) {
            return;
        }
        //绘制四周阴影区域
        canvas.drawRect(0, 0, width, mCenterRect.top, mAreaPaint);//top
        canvas.drawRect(0, mCenterRect.top, mCenterRect.left - 1, mCenterRect.bottom + 1, mAreaPaint);//left
        canvas.drawRect(0, mCenterRect.bottom + 1, width, height, mAreaPaint); // bottom
        canvas.drawRect(mCenterRect.right + 1, mCenterRect.top, width, mCenterRect.bottom + 1, mAreaPaint);

        //绘制拍照的透明区域
        canvas.drawRect(mCenterRect, mLinePaint);

        super.onDraw(canvas);
    }

    public void setCenterRect(Rect r) {
        this.mCenterRect = r;
        postInvalidate();
    }

}

