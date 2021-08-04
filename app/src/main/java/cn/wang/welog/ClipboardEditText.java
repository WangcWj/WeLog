package cn.wang.welog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * Created to :
 *
 * @author Wang
 * @date 2021/7/8
 */
public class ClipboardEditText extends AppCompatEditText {

    public static final int paste = 16908322;

    public ClipboardEditText(@NonNull Context context) {
        super(context);
    }

    public ClipboardEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ClipboardEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        Log.e("Wang", "ClipboardEditText.onTextContextMenuItem." + id);
        if (id == paste) {
            ClipboardManager systemService = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (null != systemService && systemService.hasPrimaryClip()) {
                ClipData.Item itemAt = systemService.getPrimaryClip().getItemAt(0);
                if (null != itemAt) {
                    String s = itemAt.getText().toString();
                    Log.e("Wang", "ClipboardEditText.value." + s);
                }
            }
            return true;
        }
        return super.onTextContextMenuItem(id);
    }
}
