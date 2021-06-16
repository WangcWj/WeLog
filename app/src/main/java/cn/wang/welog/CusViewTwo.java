package cn.wang.welog;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/6/4
 */
public class CusViewTwo extends AppCompatTextView {

    public CusViewTwo(Context context) {
        super(context);
    }

    public CusViewTwo(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CusViewTwo(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e("cc.wang","CusViewTwo.onMeasure.");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.e("cc.wang","CusViewTwo.onDraw.");
    }
}
