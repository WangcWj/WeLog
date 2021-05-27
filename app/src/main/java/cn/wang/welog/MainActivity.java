package cn.wang.welog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import cn.wang.log.core.WeLog;
import cn.wang.log.loginterceptors.IOFileInterceptor;

public class MainActivity extends AppCompatActivity {

    View notice;
    IOFileInterceptor IOFileInterceptor = new IOFileInterceptor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppApplication.count +=1;
        notice = findViewById(R.id.notice);
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
        long l = System.currentTimeMillis();
        for (int j = 0; j < 1000; j++) {
            WeLog.dw("MainActivity onCreate "+AppApplication.count);
        }
        Log.e("cc.wang","MainActivity.onCreate."+(System.currentTimeMillis() - l));


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
    }
}