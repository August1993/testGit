package xjcartoon_hljyd.tvjoy.xj.cn.hookams;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * Created by hp on 2018/5/8.
 */

public class HookAmsUtil {
    private static final String TAG = "HookAmsUtil";
    Context mContext;
    Class<?> mProxyActivity;

    public HookAmsUtil(Context context, Class<?> proxyActivity) {
        mContext = context;
        mProxyActivity = proxyActivity;
    }

    public void hookAms() {
        try {
            Log.d(TAG, "hookAms: 开始hook");
            Class<?> activityManagerNativeClss = Class.forName("android.app.ActivityManagerNative");
            Field gDefault = activityManagerNativeClss.getDeclaredField("gDefault");
            gDefault.setAccessible(true);
            Object defaultValue = gDefault.get(null);
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstance = singletonClass.getDeclaredField("mInstance");
            mInstance.setAccessible(true);
            //到这里拿到activityManager对象
            Object activityManager = mInstance.get(defaultValue);
            //代理的activityManager,用这个来替换上面的activityManager
            Class<?> iActivityManagerIntercept = Class.forName("android.app.IActivityManager");
            AMSInvocationHandler amsInvocationHandler = new AMSInvocationHandler(activityManager);
            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{iActivityManagerIntercept}, amsInvocationHandler);
            mInstance.set(defaultValue, proxy);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "hookAms: " + e);
        }
    }


    public void hookSystemHandler() {
        try {
            Log.d(TAG, "hookSystemHandler: hookSystemHandler>>>>>>>>");
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            //获取主线程对象
            Object activityThread = currentActivityThreadMethod.invoke(null);
            //获取mh对象
            Field mH = activityThreadClass.getDeclaredField("mH");
            mH.setAccessible(true);
            Handler handler = (Handler) mH.get(activityThread);
            Field mCallback = Handler.class.getDeclaredField("mCallback");
            mCallback.setAccessible(true);
            Log.d(TAG, "hookSystemHandler: 拦截");
            mCallback.set(handler, new ActivityThreadCallback(handler));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "hookSystemHandler: " + e);
        }
    }

    private class ActivityThreadCallback implements Handler.Callback {
        private Handler mHandler;

        public ActivityThreadCallback(Handler handler) {
            mHandler = handler;
        }

        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: 拦截systemHandler");
            if (msg.what == 100) {
                Log.d(TAG, "HookAms: lauchActivity");
                try {
                    handleLauchActivity(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mHandler.handleMessage(msg);
            return true;
        }

        public void handleLauchActivity(Message msg) throws Exception {
            Object obj = msg.obj;
            Field intentField = obj.getClass().getDeclaredField("intent");
            intentField.setAccessible(true);
            Intent proxyIntent = (Intent) intentField.get(obj);
            Intent realIntent = proxyIntent.getParcelableExtra("oldintent");
            Log.d(TAG, "handleLauchActivity: "+realIntent.getComponent());
            if (realIntent != null) {
                proxyIntent.setComponent(realIntent.getComponent());
            }


        }
    }







    public void hookPackageManager() {
        //需要hook ActivityThread
        try {
            //获取ActivityThread的成员变量 sCurrentActivityThread
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);
            Object iPackageManagerObj = sPackageManagerField.get(null);
            Class<?> iPackageManagerClass = Class.forName("android.content.pm.IPackageManager");
            InterceptPackageManagerHandler interceptInvocationHandler = new InterceptPackageManagerHandler(iPackageManagerObj);
            Object iPackageManagerObjProxy = Proxy.newProxyInstance(mContext.getClassLoader(), new Class[]{iPackageManagerClass}, interceptInvocationHandler);
            sPackageManagerField.set(null, iPackageManagerObjProxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class InterceptPackageManagerHandler implements InvocationHandler {
        Object originalObject;

        public InterceptPackageManagerHandler(Object originalObject) {
            this.originalObject = originalObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Log.d(TAG,  "method:" + method.getName() + " called with args:" + Arrays.toString(args));
            if ("getActivityInfo".equals(method.getName())) {
                return new ActivityInfo();
            }
            return method.invoke(originalObject, args);
        }
    }








    class AMSInvocationHandler implements InvocationHandler {
        private Object iActivityManager;

        public AMSInvocationHandler(Object iActivityManager) {
            this.iActivityManager = iActivityManager;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("startActivity".equals(method.getName())) {
                Log.d(TAG, "invoke: 拦截 startActivity");
                Intent intent = null;
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Intent) {
                        intent = (Intent) arg;
                        index = i;
                        break;
                    }
                }
                //创建一个代理intent,代理intent启动的就是proxyActivity;
                Intent proxyIntent = new Intent();
                ComponentName componentName = new ComponentName(mContext, mProxyActivity);
                proxyIntent.putExtra("oldintent", intent);
                proxyIntent.setComponent(componentName);
                args[index] = proxyIntent;
            }
            return method.invoke(iActivityManager, args);
        }
    }
}
