package xjcartoon_hljyd.tvjoy.xj.cn.hookams;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class ProxyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proxy);
        Log.d("HookAmsUtil", "onCreate: "+getIntent().getParcelableExtra("oldintent"));
    }
}
