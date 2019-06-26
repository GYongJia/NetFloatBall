package com.example.netfloatball;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFloatBall();    // 1.悬浮球模块

    }

    /**
     *
     * @param context 传进来要从当前哪个活动开启服务
     */
    private void addWatchingService(Context context){
        Intent intent = new Intent(context,GetNetSpeedService.class);
        startService(intent);
    }

    /**
     * 初始化悬浮窗权限，允许应用出现在其他窗口之上
     */
    private void initFloatBall(){

        //检查权限是否开启
        if(Settings.canDrawOverlays(MainActivity.this)){
            addWatchingService(MainActivity.this);
            finish();
        }else{
            Toast.makeText(MainActivity.this,"请先打开权限",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent,1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            //判断回调情况
            case 1:
                if(!Settings.canDrawOverlays(MainActivity.this)){
                    Toast.makeText(MainActivity.this,"获取权限失败",Toast.LENGTH_SHORT);
                    finish();
                }else{
                    initFloatBall();
                }
        }
    }

}
