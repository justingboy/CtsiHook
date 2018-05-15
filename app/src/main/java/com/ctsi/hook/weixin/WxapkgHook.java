package com.ctsi.hook.weixin;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by wanglin on 2018/5/14.
 */

public class WxapkgHook {

    public static Class<?> clazz = null;
    public static String wxAppid = null;
    public static String wxAppVersion = null;
    public static Context wxContext;


    public static void hook(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Log.i("TAG_CTSI", "微信--start");
        if ("com.tencent.mm".equals(loadPackageParam.packageName)) {
            Log.i("TAG_CTSI", "开始Hook 微信 " + loadPackageParam.packageName);
            ClassLoader loader = loadPackageParam.classLoader;
            try {
                hookAddItem(loader);
                hookClickItem(loader);
                hookAppid(loader);
            } catch (Throwable throwable) {
                Log.i("TAG_CTSI", "hook微信失败：" + throwable.getLocalizedMessage());
            }
        }
    }

    private static void hookAppid(ClassLoader loader) throws Throwable {

        clazz = loader.loadClass("com.tencent.mm.plugin.appbrand.page.u");
        if (clazz != null) {
            XposedHelpers.findAndHookMethod(clazz, "aeN", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    try {
                        String path = (String) param.getResult();
                        if (!path.contains("preload")) {
                            Field mAppIdF = param.thisObject.getClass().getDeclaredField("mAppId");
                            mAppIdF.setAccessible(true);
                            String appid = (String) mAppIdF.get(param.thisObject);
                            if (!TextUtils.isEmpty(appid)) {
                                wxAppid = appid;
                            }
                            Log.i("jw", "path:" + path + ",appid:" + appid);
                            String[] strAry = path.split(appid);
                            String[] strAry1 = strAry[1].split("/");
                            Log.i("jw", strAry1[0] + "," + strAry1[1]);
                            if (!TextUtils.isEmpty(strAry1[1])) {
                                wxAppVersion = strAry1[1];
                            }
                        }
                    } catch (Exception e) {
                        Log.i("jw", "reflect err:" + Log.getStackTraceString(e));
                    }
                }
            });
        }

    }

    private static void hookAddItem(final ClassLoader loader) throws Throwable {
        Log.i("TAG_CTSI", "hookAddItem start");
        clazz = loader.loadClass("com.tencent.mm.ui.widget.g$b");
        Class<?> paramClazz = loader.loadClass("com.tencent.mm.ui.widget.g");
        if (clazz != null && paramClazz != null) {
            XposedHelpers.findAndHookMethod(paramClazz, "ca", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    try {
                        if (!TextUtils.isEmpty(wxAppid) && !TextUtils.isEmpty(wxAppVersion)) {
                            //hook这个ca方法一定要记得保存wx全局的context，后面有很大用途
                            wxContext = (Context) param.args[0];
                            //反射获取类的qWf变量对象
                            Field qWfF = param.thisObject.getClass().getDeclaredField("qWf");
                            qWfF.setAccessible(true);
                            Object qWfObj = qWfF.get(param.thisObject);
                            //然后在反射获取菜单类的内部菜单子项列表对象
                            Field xcZF = qWfObj.getClass().getField("xcZ");
                            @SuppressWarnings("unchecked")
                            List<MenuItem> xcZObj = (List<MenuItem>) xcZF.get(qWfObj);

                            //下面就开始添加菜单了，这里的小程序appid和版本号后面再说，可以先忽略
                            String menuItem1Str = "小程序的Appid：" + wxAppid + "\n小程序版本号：" + wxAppVersion;
                            MenuItem menuItem1 = createItem(loader, menuItem1Str, 0);

                            String menuItem2Str = "小程序包路径：/data/data/com.tencent.mm/MicroMsg/4d0238658a35658e7bc9597a2de4d49e/appbrand/pkg/" +
                                    "_" + wxAppid.hashCode() + "_" + wxAppVersion + ".wxapkg";
                            MenuItem menuItem2 = createItem(loader, menuItem2Str, 0);

                            String menuItem3Str = "点击解析小程序包源码";
                            MenuItem menuItem3 = createItem(loader, menuItem3Str, 0);

                            String menuItem4Str = "点击外勤助手";
                            MenuItem menuItem4 = createItem(loader, menuItem4Str, 0);

                            xcZObj.add(menuItem1);
                            xcZObj.add(menuItem2);
                            xcZObj.add(menuItem3);
                            xcZObj.add(menuItem4);
                        }
                    } catch (Exception e) {
                        Log.i("TAG_CTSI", "添加Item 失败");
                        Log.i("TAG_CTSI", "reflect err:" + Log.getStackTraceString(e));
                    }
                }
            });
        }
    }

    private static void hookClickItem(final ClassLoader loader) throws Throwable {
        clazz = loader.loadClass("com.tencent.mm.ui.widget.g$1");
        if (clazz != null) {
            XposedHelpers.findAndHookMethod(clazz, "onItemClick", AdapterView.class, View.class, int.class, long.class, new XC_MethodHook() {
                @SuppressLint("SdCardPath")
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    int pos = (int) param.args[2];
                    if (pos == 0) {
                        boolean isSucc = copyClipboard(wxAppid);
                        if (isSucc) {
                            Toast.makeText(wxContext, "小程序appid成功复制到剪切板中", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(wxContext, "小程序appid复制失败", Toast.LENGTH_LONG).show();
                        }
                    } else if (pos == 1) {
                        if (TextUtils.isEmpty(wxAppid) || TextUtils.isEmpty(wxAppVersion)) {
                            Toast.makeText(wxContext, "小程序appid复制失败", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String content = "/data/data/com.tencent.mm/MicroMsg/4d0238658a35658e7bc9597a2de4d49e/appbrand/pkg/" +
                                "_" + wxAppid.hashCode() + "_" + wxAppVersion + ".wxapkg";
                        boolean isSucc = copyClipboard(content);
                        if (isSucc) {
                            Toast.makeText(wxContext, "小程序包路径成功复制到剪切板中", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(wxContext, "小程序包路径复制失败", Toast.LENGTH_LONG).show();
                        }
                    } else if (pos == 2) {
                        if (wxContext != null) {
                            Toast.makeText(wxContext, "开始解析，解析后的目录为:/sdcard/fourbrother/" + wxAppid + "_" + wxAppVersion + "/", Toast.LENGTH_LONG).show();
                            startParseWxpkg();
                        }
                    } else if (pos == 3) {
                        if (wxContext != null) {
                            Intent intent = new Intent();
                            ComponentName componentName = new ComponentName("com.ctsi.android.mts.client", "com.ctsi.android.mts.client.ztest.accountTest.Activity_AccountTest");
                            intent.setComponent(componentName);
                            wxContext.startActivity(intent);
                            Log.i("TAG_CTSI", "跳转外勤助手！");
                        }
                    }
                }
            });
        }
    }

    private static MenuItem createItem(ClassLoader loader, String title, int index) {
        try {
            //子菜单最终的类是o,所以反射，然后获取菜单中的变量title，就是文本信息
            Class<?> oClazz = loader.loadClass("com.tencent.mm.ui.base.o");
            Constructor<?> constructor = oClazz.getConstructor(int.class, int.class);
            MenuItem oObj = (MenuItem) constructor.newInstance(index, 0);
            oObj.setTitle(title);
//            Field titleF = oObj.getClass().getDeclaredField("title");
//            titleF.setAccessible(true);
//            titleF.set(oObj, title);
            return oObj;
        } catch (Exception e) {
            return null;
        }
    }

    private static void startParseWxpkg() {
        if (wxContext == null) {
            return;
        }
        if (TextUtils.isEmpty(wxAppid)) {
            return;
        }
        if (TextUtils.isEmpty(wxAppVersion)) {
            return;
        }

        new AsyncTask<Void, Void, Boolean>() {

            @SuppressLint("SdCardPath")
            @Override
            protected Boolean doInBackground(Void... params) {
                String srcPath = wxContext.getFilesDir().getParentFile().getAbsolutePath() + "/MicroMsg/4d0238658a35658e7bc9597a2de4d49e/appbrand/pkg/";
                srcPath = srcPath + "_" + wxAppid.hashCode() + "_" + wxAppVersion + ".wxapkg";
                String desPath = "/sdcard/fourbrother/" + "_" + wxAppid.hashCode() + "_" + wxAppVersion + ".wxapkg";
                File desFile = new File(desPath);
                if (!desFile.getParentFile().exists()) {
                    desFile.getParentFile().mkdirs();
                }
                boolean isSucc = copyFile(srcPath, desPath);
                if (!isSucc) {
                    return false;
                }
                try {
                    Main.main(new String[]{desPath});
                    return true;
                } catch (Exception e) {
                    Log.i("jw", "parse wxapkg err:" + Log.getStackTraceString(e));
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (wxContext != null) {
                    Toast.makeText(wxContext, result ? "解析成功" : "解析失败", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private static boolean copyFile(String srcPath, String desPath) {
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            return false;
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(desPath);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            return true;
        } catch (Exception e) {
            Log.i("jw", "copy file err:" + Log.getStackTraceString(e));
            return false;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private static boolean copyClipboard(String content) {
        if (wxContext == null || TextUtils.isEmpty(content)) {
            return false;
        }
        try {
            ClipboardManager clipboardManager = (ClipboardManager) wxContext.getSystemService(Context.CLIPBOARD_SERVICE);
            //创建ClipData对象
            ClipData clipData = ClipData.newPlainText("wx", content);
            //添加ClipData对象到剪切板中
            clipboardManager.setPrimaryClip(clipData);
            return true;
        } catch (Exception e) {
            Log.i("jw", "copy clip err:" + Log.getStackTraceString(e));
            return false;
        }
    }


}
