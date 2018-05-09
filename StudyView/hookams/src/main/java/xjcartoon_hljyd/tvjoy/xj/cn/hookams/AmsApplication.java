package xjcartoon_hljyd.tvjoy.xj.cn.hookams;

import android.app.Application;

/**
 * Created by hp on 2018/5/8.
 */

public class AmsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HookAmsUtil hookAmsUtil = new HookAmsUtil(this, ProxyActivity.class);
        hookAmsUtil.hookSystemHandler();
        hookAmsUtil.hookPackageManager();
        hookAmsUtil.hookAms();



    }
}
