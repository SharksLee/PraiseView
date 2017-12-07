package com.example.administrator.praiseview;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import java.lang.reflect.Method;


/**
 * 屏幕相关 包含屏幕信息及关于屏幕的一些计算<br>
 */
public class DisplayUtil {
    private static float fontScale = -1.0f;
    private static int width;
    private static int height;
    private static float density;

    /**
     * 初始化工具类
     * <p>
     * activity
     */
    public static void init(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        fontScale = dm.scaledDensity;
        width = dm.widthPixels;
        height = dm.heightPixels;
        density = dm.density;
    }

    /**
     * 获取屏幕宽度
     *
     * @return 单位px
     */
    public static int getScreenWidth() {

        return width;
    }

    /**
     * 获取屏幕高度
     *
     * @return 单位px
     */
    public static int getScreenHeight() {

        return height;
    }


    /**
     * 同density 但是会基于用户 再系统设置中改变字体大小 而改变
     *
     * @return 单位px
     */
    public static float getFontScale() {
        return fontScale;
    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(float dpValue) {
        return (int) (dpValue * getDensity() + 0.5f);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(double dpValue) {
        return (int) (dpValue * getDensity() + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(float pxValue) {
        return (int) (pxValue / getDensity() + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return sp
     */
    public static int px2sp(float pxValue) {
        return (int) (pxValue / getFontScale() + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return px
     */
    public static int sp2px(float spValue) {
        return (int) (spValue * getFontScale() + 0.5f);
    }

    /**
     * 获得屏幕密度
     */
    public static float getDensity() {

        return density;
    }

    /**
     * 沉浸式窗体，隐藏status bar和 navigation bar
     *
     * @param context
     */
    public static void forceImmersiveWindow(Activity context) {
        WindowManager.LayoutParams params = context.getWindow().getAttributes();

        params.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
        ;

        context.getWindow().setAttributes(params);
    }

    /**
     * 隐藏状态栏
     *
     * @param window
     */
    public static void hideStatusBar(Window window) {
        if (window == null) return;

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 沉浸式窗体，隐藏status bar和 navigation bar
     * 4.4以下不隐藏导航栏，因为点击事件会呼唤出导航栏，导致点击事件和预想的不一样
     *
     * @param context
     */
    public static void immersiveWindow(Activity context) {
        WindowManager.LayoutParams params = context.getWindow().getAttributes();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            params.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN;
        } else {
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        context.getWindow().setAttributes(params);
    }


    /**
     * 获得相对于360，640的最小縮放比例 比例大小和密度有关
     *
     * @return 最小缩放比例
     */
    public static float getMinScale() {
        return getMinScale(360f, 640f);
    }

    /**
     * 获得相对于dstWidth，dstHeight的最小縮放比例 比例大小和密度有关
     *
     * @param dstWidth  目标宽度
     * @param dstHeight 目标高度
     * @return 最小缩放比例
     */
    public static float getMinScale(float dstWidth, float dstHeight) {
        float scaleWidth = getScreenWidth() / (dstWidth * getDensity());
        float scaleHeight = getScreenHeight() / (dstHeight * getDensity());
        return Math.min(scaleWidth, scaleHeight);
    }

    /**
     * 获取虚拟功能键高度
     */
    public static int getVirtualBarHeight(Context context) {
        int vh = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vh;
    }

}