package com.example.netfloatball;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GetNetSpeedService extends Service {

    private double total_Rdata = TrafficStats.getTotalRxBytes();
    private double total_Tdata = TrafficStats.getTotalTxBytes();
    private Handler handler ;
    private final int SECOND = 1; //设置每隔1秒刷新下载速度
    private final int TOTAL_SPEED = 1;  //下载和上传速度的message

    private WindowManager.LayoutParams layoutParams;
    private WindowManager manager;
    private View floatview;
    private TextView downloadText;
    private TextView uploadText;
    private RelativeLayout floatingball;

    public GetNetSpeedService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //重写onCreate方法来初始化我们的悬浮窗
    @Override
    public void onCreate() {
        super.onCreate();

        createBall(); //创建悬浮球
        initThread(); //创建线程监测网速
        initHandler(); //创建handler接子收线程发来的数据
    }

    /**
     * 创建悬浮球
     */
    private void createBall(){
        manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();


        floatview = LayoutInflater.from(this).inflate(R.layout.floatball,null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.format = PixelFormat.TRANSPARENT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        manager.addView(floatview,layoutParams);

        downloadText = (TextView)floatview.findViewById(R.id.download);
        uploadText = (TextView)floatview.findViewById(R.id.upload);
        floatingball = (RelativeLayout)floatview.findViewById(R.id.floationgball);
        floatingball.setOnTouchListener(new FloatingOnTouchListener());  //设置拖动悬浮球的监听器
    }

    private void initThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(SECOND * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg = new Message();
                    msg.what = TOTAL_SPEED;
                    msg.obj = getTotalSpeed();
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case TOTAL_SPEED:
                        displayTotalSpeed(msg);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * 悬浮球拖动监听器，主要负责用户拖动悬浮球时的一些逻辑
     */
    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    manager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    /**
     * 获取上传和下载速度
     * @return 返回一个大小为2的数组，包含了上传和下载速度
     */
    private double[] getTotalSpeed() {

        double downloadSpeed = TrafficStats.getTotalRxBytes() - total_Rdata;
        total_Rdata = TrafficStats.getTotalRxBytes();

        double uploadSpeed = TrafficStats.getTotalTxBytes() - total_Tdata;
        total_Tdata = TrafficStats.getTotalTxBytes();

        return new double[]{downloadSpeed/SECOND,uploadSpeed/SECOND};
    }

    /**
     * 在悬浮窗上显示速度
     * @param msg 子线程中传来的message在此函数中解析
     */
    private void displayTotalSpeed(Message msg){
        double speed[] = (double[])msg.obj;
        double downSpeed = speed[0];
        double upSpeed = speed[1];

        uploadText.setText(speedFormat(upSpeed));
        downloadText.setText(speedFormat(downSpeed));
    }

    /**
     * 格式化速度
     * @param speed 此参数为上传或下载速度
     * @return 返回格式化好的速度
     */
    private String speedFormat(double speed){
        if(speed < 1024){
            return (String.format("%4.2f",speed/1024) + " K/s");
        }else if(speed < 1024*10){
            return (String.format("%4.2f",speed/1024) + " K/s");
        }else if(speed < 1024*100){
            return (String.format("%4.1f",speed/1024) + " K/s");
        }else if(speed < 1024*1024){
            return (String.format("%4.0f",speed/1024) + " K/s");
        }else if(speed < 1024*1024*10){
            return (String.format("%4.2f",speed/1024/1024) + " M/s");
        }else{
            return (String.format("%4.1f",speed/1024/1024) + " M/s");
        }
    }

    //重写onDestroy方法当服务关闭时关闭悬浮窗
    @Override
    public void onDestroy() {
        manager.removeView(floatview);
        super.onDestroy();
    }

}
