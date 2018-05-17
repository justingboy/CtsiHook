## 一、[什么是Xposed 框架？](https://www.csdn.net/article/2015-08-14/2825462)

* Xposed框架是一款可以在不修改APK的情况下影响程序运行（修改系统）的框架服务；
* 通过替换/system/bin/app_process程序控制zygote进程，使得app_process在启动过程中会加载XposedBridge.jar这个jar包，从而完成对Zygote进程及其创建的Dalvik虚拟机的劫持；
* Xposed框架是基于一个Android的本地服务应用XposedInstaller，与一个提供API 的jar文件来完成的；
* Xposed 框架是彻底开源的，Cydia Substrate 不是彻底开源的；
* [xposed框架的运行与否和系统是否root没有任何关系！不root也是运行的！](http://www.xposed.pro/list/201-1-1.html)


## 二、类型Xposed 框架有哪些？

* Xposed
* Cydia Substrate
* Fradia


## 三、安装XposedInstaller

* 下载地址：http://repo.xposed.info/module/de.robv.android.xposed.installer
* 安装好后进入XposedInstaller应用程序，会出现需要激活框架的界面，点击“安装/更新”就能完成框架的激活了；
* 部分设备如果不支持直接写入的话，可以选择“安装方式”，修改为在Recovery模式下自动安装即可。
* 因为安装时会存在需要Root权限，安装后会启动Xposed的app_process，所以安装过程中会存在设备多次重新启动。


## 四、下载XposedBridgeApi-54.jar

* [下载XposedBridgeApi-54.jar地址](https://forum.xda-developers.com/xposed/xposed-api-changelog-developer-news-t2714067)
* 下载完毕后需要将Xposed Library复制到lib目录（注意是lib目录，不是Android提供的libs目录,放在libs中可能出错）
* 然后将这个jar包添加到Build PATH中;


## 五、Hook 实现登录劫持

* Hook操作主要就是使用到了Xposed中的两个比较重要的方法：
```
1. handleLoadPackage获取包加载时候的回调并拿到其对应的classLoader;
2. findAndHookMethod对指定类的方法进行Hook;
```
* handleLoadPackage :
```
public class XposedInit implements IXposedHookLoadPackage {

	private final String TAG = "Xposed";

	public Class<?> clazz = null;

	public void log(String s){
		Log.d(TAG, s);
		XposedBridge.log(s);
	}

	@Override
	public void handleLoadPackage(final LoadPackageParam loadPackageParam){
		WeixinHook.hook(loadPackageParam);
	}

}
```
* findAndHookMethod :
```
private static void hookAppid(ClassLoader loader) throws Throwable{

		clazz = loader.loadClass("com.tencent.mm.plugin.appbrand.page.u");
		if(clazz != null){
			XposedHelpers.findAndHookMethod(clazz, "aeN", new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					super.afterHookedMethod(param);
					try{
						String path = (String)param.getResult();
						if(!path.contains("preload")){
							Field mAppIdF = param.thisObject.getClass().getDeclaredField("mAppId");
							mAppIdF.setAccessible(true);
							String appid = (String)mAppIdF.get(param.thisObject);
							if(!TextUtils.isEmpty(appid)){
								wxAppid = appid;
							}
							Log.i("jw", "path:"+path+",appid:"+appid);
							String[] strAry = path.split(appid);
							String[] strAry1 = strAry[1].split("/");
							Log.i("jw", strAry1[0]+","+strAry1[1]);
							if(!TextUtils.isEmpty(strAry1[1])){
								wxAppVersion = strAry1[1];
							}
						}
					}catch(Exception e){
						Log.i("jw", "reflect err:"+Log.getStackTraceString(e));
					}
				}
			});
		}

	}
```

## 六、使用Xposed进行Hook几个步骤

1. 在AndroidManifest.xml文件中配置插件名称与Api版本号：
```
<application
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme" >
        <!-- 模块是否可用 -->
        <meta-data
            android:name="xposedmodule"
            android:value="true" />
        <!-- 模块描述 -->
        <meta-data
            android:name="xposeddescription"
            android:value="一个登陆劫持的样例" />
        <!-- 最低版本号 -->
        <meta-data
            android:name="xposedminversion"
            android:value="30" />
</application>
```
2.  新建一个入口类并继承并实现IXposedHookLoadPackage接口：
```
public class Main implements IXposedHookLoadPackage {

    /**
     * 包加载时候的回调
     */
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        // 将包名不是 com.example.login 的应用剔除掉
        if (!lpparam.packageName.equals("com.example.login"))
            return;
        XposedBridge.log("Loaded app: " + lpparam.packageName);
    }
}
```
3. 声明主入口路径:
```
需要在assets文件夹中新建一个xposed_init的文件，并在其中声明主入口类。如这里我们的主入口类为com.example.loginhook.Main
```
4. 使用findAndHookMethod方法Hook劫持登陆信息:
```
XposedHelpers.findAndHookMethod(clazz, "aeN", new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					super.afterHookedMethod(param);
					try{
						String path = (String)param.getResult();
						if(!path.contains("preload")){
							Field mAppIdF = param.thisObject.getClass().getDeclaredField("mAppId");
							mAppIdF.setAccessible(true);
							String appid = (String)mAppIdF.get(param.thisObject);
							if(!TextUtils.isEmpty(appid)){
								wxAppid = appid;
							}
							Log.i("jw", "path:"+path+",appid:"+appid);
							String[] strAry = path.split(appid);
							String[] strAry1 = strAry[1].split("/");
							Log.i("jw", strAry1[0]+","+strAry1[1]);
							if(!TextUtils.isEmpty(strAry1[1])){
								wxAppVersion = strAry1[1];
							}
						}
					}catch(Exception e){
						Log.i("jw", "reflect err:"+Log.getStackTraceString(e));
					}
				}
			});
```
5. 在XposedInstaller中启动我们自定义的模块:
```
1. 编译后安装在Android设备上的模块应用程序不会立即的生效，我们需要在XpasedInstaller模块选项中勾选待启用的模块才能让其正常的生效;
2. 重启Android设备，进入XposedInstaller查看日志模块，因为我们之前使用的是XposedBridge.log方法打印log，所以log都会显示在此处;
```


## 六、Xposed 提供的其他方法

* 屏蔽掉原始方法的调用：
```
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
```
* 支持免重启生效修改的hook代码(**第一次生效需要重启**)：
```
1. HookLoader (具体看该类的代码实现与配置)
2. private final String handleHookClass = XposedInit.class.getName();（真正hook逻辑处理类）
```

* 支持多个模块同时生效：
```
public class XposedInit implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(LoadPackageParam loadPackageParam) throws Throwable {
        WaiqinHook.hook(loadPackageParam);//外勤助手
        WxapkgHook.hook(loadPackageParam);//微信小程序
    }
}
```


## 七、参考资料
* [Xposed框架的安装](https://www.jianshu.com/p/21800fc795e6)
* [VAExposed更新至0.3.5，无需root直接使用Xposed模块](https://www.52pojie.cn/thread-675975-1-1.html)
* [ Android插件化系列第（一）篇---Hook技术之Activity的启动过程的拦截](https://blog.csdn.net/u013263323/article/details/54946604)
* [理解 Android Hook 技术以及简单实战](https://www.jianshu.com/p/4f6d20076922)
* [Xposed模块开发,免重启改进方案](https://blog.csdn.net/u011956004/article/details/78612502)