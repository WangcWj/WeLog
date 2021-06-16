package cn.wang.welog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.wang.monitor.fps.core.AppMonitor;
import com.wang.monitor.startup.ViewServer;

import java.util.HashMap;
import java.util.Map;

import cn.wang.log.core.WeLog;
import cn.wang.log.loginterceptors.IOFileInterceptor;

public class MainActivity extends AppCompatActivity {

    View notice;
    IOFileInterceptor IOFileInterceptor = new IOFileInterceptor();

    private long createActivityTime;

    Map<String,Boolean> r = new HashMap<>(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        createActivityTime = System.currentTimeMillis();
        r.put(getClass().getSimpleName(),true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppMonitor.getInstance().onStart();


        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();




        //手机型号
        Log.e("cc.wang","MainActivity.onCreate.型号 "+android.os.Build.MODEL);
        //手机厂商
        Log.e("cc.wang","MainActivity.onCreate.厂商 "+android.os.Build.BRAND);


        View viewById = findViewById(R.id.block);
        notice = findViewById(R.id.cus);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notice.setVisibility(View.VISIBLE);
            }
        });
        /*
         * 1.采用Builder方式来建造。
         * 2.日志的处理应该采用责任链的模式，可以仿照OkHttp。因为日志信息可能需要要经过很多的步骤，
         * 比如说日志格式化、系统打印日志、日志加密、日志写入文件等，这样的话还是采用责任链模式来说比较解耦，
         * 另外如果要替换某个步骤的实现也很简单。
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         * */
        int i = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (i == -1) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
            return;
        }

        //WeLog.dw("MainActivity");
        //NIO  184  54 45
        //IO  160 89 72
        WeLog.dw("MainActivity onCreate " + AppApplication.count);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Boolean aBoolean = r.get(getClass().getSimpleName());
        if(aBoolean != null && aBoolean){
            r.remove(getClass().getSimpleName());
            Log.e("Displayed","MainActivity.onWindowFocusChanged: start time "+(System.currentTimeMillis() - createActivityTime));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // WeLog.dw("MainActivity  onStop");
    }

    @Override
    protected void onDestroy() {
        //WeLog.dw("MainActivity  onDestroy");
        super.onDestroy();
        ViewServer.get(this).removeWindow(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewServer.get(this).setFocusedWindow(this);
    }
}