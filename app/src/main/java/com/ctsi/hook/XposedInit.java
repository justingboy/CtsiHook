package com.ctsi.hook;

import com.ctsi.hook.waiqin.WaiqinHook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


/**
 * Created by wanglin on 2018/5/14.
 */

public class XposedInit implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(LoadPackageParam loadPackageParam) throws Throwable {
        WaiqinHook.hook(loadPackageParam);
    }
}
