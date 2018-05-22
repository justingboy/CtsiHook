package com.ctsi.hook.waiqin;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by wanglin on 2018/5/14.
 */

public class WaiqinHook {

    private static Context mContext;

    public static void hook(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Log.i("TAG_CTSI", "进入外勤助手Hook--AAAAAAAAAAAA");
        if ("com.ctsi.android.mts.client".equals(loadPackageParam.packageName)) {
            XposedBridge.log("进入外勤助手Hook");
            Log.i("TAG_CTSI", "进入外勤助手Hook");
            XposedHelpers.findAndHookMethod("com.ctsi.android.mts.client.ztest.accountTest.Activity_AccountTest",
                    loadPackageParam.classLoader, "getAppEnvironment", new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            param.setResult("Hook注外勤助手了 before");
                            XposedBridge.log("beforeHookedMethod");
                            Log.i("TAG_CTSI", "beforeHookedMethod");
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            param.setResult("Hook注外勤助手了 after");
                            XposedBridge.log("afterHookedMethod" + param.getResult());
                            Log.i("TAG_CTSI", "afterHookedMethod" + param.getResult());
                        }
                    });
//            hookLogin(loadPackageParam);
//            hookMethodReplacement(loadPackageParam);
        }
    }

    private static void hookLogin(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod("com.ctsi.android.mts.client.ztest.accountTest.Activity_AccountTest",
                loadPackageParam.classLoader, "onClick", new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        try {
                            Log.i("TAG_CTSI", "login --->beforeHookedMethod");
                            Field mEditNumble = param.thisObject.getClass().getDeclaredField("editNumble");
                            Log.i("TAG_CTSI", "mEditNumble = " + mEditNumble.getName());
                            mEditNumble.setAccessible(true);
                            EditText editText = (EditText) mEditNumble.get(param.thisObject);
                            Log.i("TAG_CTSI", "mdn = " + editText.getText().toString());
                            editText.setText("13370170836");
                            mEditNumble.set(param.thisObject, editText);
                            mContext = editText.getContext();
                        } catch (Exception ex) {
                            Log.i("TAG_CTSI", "ex = " + ex.getLocalizedMessage());
                        }

                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Log.i("TAG_CTSI", "外勤助手登录");
                        Toast.makeText(mContext, "外勤助手登录成功", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private static void hookMethodReplacement(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod("com.ctsi.android.mts.client.ztest.accountTest.Activity_AccountTest",
                loadPackageParam.classLoader, "onClick", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                        Log.i("TAG_CTSI", "thisObject = " + methodHookParam.thisObject);
                        return null;//返回null ,则是屏蔽掉该方法的调用

                        //下面这个表示返回的原始的方法
                        // return XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);
                    }
                });
    }


    //hook住View的OnClickListener
    private static void hookOnClickListener(View view) {
        try {
            // 得到 View 的 ListenerInfo 对象
            Method getListenerInfo = View.class.getDeclaredMethod("getListenerInfo");
            getListenerInfo.setAccessible(true);
            Object listenerInfo = getListenerInfo.invoke(view);
            // 得到 原始的 OnClickListener 对象
            Class<?> listenerInfoClz = Class.forName("android.view.View$ListenerInfo");
            Field mOnClickListener = listenerInfoClz.getDeclaredField("mOnClickListener");
            mOnClickListener.setAccessible(true);
            View.OnClickListener originOnClickListener = (View.OnClickListener) mOnClickListener.get(listenerInfo);
            // 用自定义的 OnClickListener 替换原始的 OnClickListener
            View.OnClickListener hookedOnClickListener = new HookedOnClickListener(originOnClickListener);
            //通过属性设置来改变的原来的对象
            mOnClickListener.set(listenerInfo, hookedOnClickListener);

        } catch (Exception e) {
            Log.i("TAG_CTSI", "ex = " + e.getLocalizedMessage());
        }

    }

    private static class HookedOnClickListener implements View.OnClickListener {

        View.OnClickListener mOriginOnClickListener;

        public HookedOnClickListener(View.OnClickListener originOnClickListener) {
            this.mOriginOnClickListener = originOnClickListener;
        }

        @Override
        public void onClick(View v) {
            if (mOriginOnClickListener != null) {
                mOriginOnClickListener.onClick(v);
            }
        }
    }

}