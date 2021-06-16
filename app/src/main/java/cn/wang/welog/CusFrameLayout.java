package cn.wang.welog;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/6/16
 */
public class CusFrameLayout extends FrameLayout {
    public CusFrameLayout(@NonNull Context context) {
        super(context);
    }

    public CusFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CusFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e("cc.wang","CusFrameLayout.onMeasure.");
    }
}
