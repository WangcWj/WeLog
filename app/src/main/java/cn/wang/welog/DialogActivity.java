package cn.wang.welog;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;


public class DialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        ParentFocus.onWindowFocusChanged("");
    }
}