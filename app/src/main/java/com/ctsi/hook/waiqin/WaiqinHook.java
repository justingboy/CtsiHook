package com.ctsi.hook.waiqin;

import android.util.Log;
import android.widget.EditText;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by wanglin on 2018/5/14.
 */

public class WaiqinHook {

    public static void hook(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

        if ("com.ctsi.android.mts.client".equals(loadPackageParam.packageName)) {
            XposedBridge.log("进入外勤助手Hook");
            Log.i("TAG_CTSI", "进入外勤助手Hook");
            XposedHelpers.findAndHookMethod("com.ctsi.android.mts.client.ztest.accountTest.Activity_AccountTest",
                    loadPackageParam.classLoader, "getAppEnvironment", new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            XposedBridge.log("beforeHookedMethod");
                            Log.i("TAG_CTSI", "beforeHookedMethod");
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            param.setResult("Hook注外勤助手了");
                            XposedBridge.log("afterHookedMethod" + param.getResult());
                            Log.i("TAG_CTSI", "afterHookedMethod" + param.getResult());
                        }
                    });
            hookLogin(loadPackageParam);
        }
    }

    private static void hookLogin(XC_LoadPackage.LoadPackageParam loadPackageParam) {
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
                        } catch (Exception ex) {
                            Log.i("TAG_CTSI", "ex = " + ex.getLocalizedMessage());
                        }


                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Log.i("TAG_CTSI", "外勤助手登录");
                    }
                });
    }

}