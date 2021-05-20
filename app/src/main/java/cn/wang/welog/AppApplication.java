package cn.wang.welog;

import android.app.Application;

import cn.wang.log.core.LogCenter;
import cn.wang.log.core.WeLog;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/5/12
 */
public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        WeLog.init(new LogCenter.Builder(this));
    }
}
