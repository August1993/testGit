package xjcartoon_hljyd.tvjoy.xj.cn.studyview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hp on 2018/4/10.
 */

public class ClockView extends View {
    SimpleDateFormat mSimpleDateFormat;
    private Paint mPaint;
    private float second_degree = 0;
    private float min_degree = 0;
    private float hour_degree = 0;
    private Paint textPaint;
    private Timer mTimer = new Timer();
    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            second_degree = second_degree + 6;
            min_degree = (min_degree + 0.1f);
            hour_degree = (hour_degree + 1.0f / 120);
            if (second_degree > 360) {
                second_degree = 0;
            }
            if (min_degree == 360) {
                min_degree = 0;
            }
            if (hour_degree == 360) {
                hour_degree = 0;
            }
            postInvalidate();
        }
    };
    private int mBoder_color;


    public void setTime() {
        Calendar calendar = Calendar.getInstance();  //获取当前时间，作为图标的名字
        String hour = calendar.get(Calendar.HOUR_OF_DAY) + "";
        String minute = calendar.get(Calendar.MINUTE) + "";
        String second = calendar.get(Calendar.SECOND) + "";
        int t = (Integer.valueOf(hour)) * 60 * 60 + Integer.valueOf(minute) * 60 + Integer.valueOf(second);
        hour_degree = (1.0f / 120) * t;
        min_degree = (float) (0.1 * t);
        second_degree = 6 * t;
    }

    public void start() {
        mTimer.schedule(mTimerTask, 0, 1000);
    }

    public ClockView(Context context) {
        this(context, null);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        /*--------------------------------------------------------------------------*/
        textPaint = new Paint();
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(25);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(5);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockView);
        mBoder_color = typedArray.getColor(R.styleable.ClockView_borderColor_wzy, Color.BLACK);
        /*--------------------------------------------------------------------------*/
        setTime();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int r = (getWidth() > getHeight() ? getHeight() : getWidth()) / 3;
        canvas.save();
        mPaint.setColor(mBoder_color);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, r, mPaint);
        mPaint.setColor(Color.BLACK);
        canvas.translate(getWidth() / 2, getHeight() / 2);
        for (int i = 0; i < 360; i++) {
            if (i % 30 == 0) {
                canvas.drawLine(r - 25, 0, r, 0, mPaint);
            } else if (i % 6 == 0) {
                canvas.drawLine(r - 14, 0, r, 0, mPaint);
            } else {
                canvas.drawLine(r - 9, 0, r, 0, mPaint);
            }
            canvas.rotate(1);
        }
        textPaint.setStrokeWidth(2);
        textPaint.setColor(Color.BLACK);
        for (int i = 0; i < 12; i++) {
                drawNumer(canvas, i * 30,i==0?""+12:""+i, textPaint, r);
        }
        canvas.restore();
        canvas.drawPoint(getWidth() / 2, getHeight() / 2, textPaint);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(2);
        textPaint.setColor(Color.RED);
        drawPointer(canvas, 0, 0, 0, -190, second_degree, textPaint);
        textPaint.setColor(Color.BLACK);
        textPaint.setStrokeWidth(4);
        drawPointer(canvas, 0, 0, 0, -130, min_degree, textPaint);
        textPaint.setStrokeWidth(7);
        textPaint.setColor(Color.BLACK);
        drawPointer(canvas, 0, 0, 0, -90, hour_degree, textPaint);
    }

    private void drawPointer(Canvas canvas, int startX, int startY, int endX, int endY, float degree, Paint textPaint) {
        canvas.save();
        canvas.translate(getWidth() / 2, getHeight() / 2);
        canvas.rotate(degree);
        canvas.drawLine(startX, startY, endX, endY, textPaint);
        canvas.restore();
    }

    public void drawNumer(Canvas canvas, int degree, String text, Paint paint, int r) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
//      旋转canvas
        canvas.rotate(degree);
//      平移canvas(之所以不用r-50),是因为canvas坐标系默认上面为负数
        canvas.translate(0, 50 - r);
//      摆正canvas
        canvas.rotate(-degree);
        canvas.drawText(text, -rect.width() / 2, rect.height() / 2, paint);
        canvas.rotate(degree);
        canvas.translate(0, r - 50);
        canvas.rotate(-degree);
    }
}
