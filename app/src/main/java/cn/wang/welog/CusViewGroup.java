package cn.wang.welog;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/6/4
 */
public class CusViewGroup extends FrameLayout {

    public CusViewGroup(Context context) {
        super(context);
    }

    public CusViewGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CusViewGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.e("FrameMetrics","CusViewTwo.onDraw.");
    }

    float targetX;


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean interceptor = false;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                targetX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                float distance = Math.abs(x - targetX);
                if(distance > 8){
                    Log.e(" Wang", "CusViewGroup.onInterceptTouchEvent.拦截");
                     interceptor = true;
                }
                targetX = x;
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        if(interceptor) {
            return true;
        }else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }


        return true;
    }
}
