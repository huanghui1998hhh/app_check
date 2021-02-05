package com.example.app_check

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/** AppCheckPlugin */
class AppCheckPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var context: Context
  private lateinit var activity: Activity

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "app_check")
    channel.setMethodCallHandler(this)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity;
  }

  override fun onDetachedFromActivityForConfigChanges() {}

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}

  override fun onDetachedFromActivity() {}



  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when(call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "appCheck" -> {
        result.success(getPackages())
      }
      "openAppWithPackageName" -> {
        startAppIntent(call.arguments<String>())
      }
      "getTopApp" -> {
        result.success(RunningTaskUtil(context).getTopRunningTasks()?.packageName)
      }
      "checkAppPermission" -> {
        result.success(checkAppPermission())
      }
      "startListen" -> {
        result.success(true)
        val intent = Intent(context, ListenerService::class.java)
        intent.putExtra("argument", call.arguments as ArrayList<String>)
        context.startService(intent)
      }
      "endListen" -> {
        result.success(false)
        val intent = Intent(context, ListenerService::class.java)
        context.stopService(intent)
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  private fun checkAppPermission(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (!Settings.canDrawOverlays(context)) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
          intent.data = Uri.fromParts("package", context.packageName, null)
        }
        activity.startActivityForResult(intent, MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS)
      } else {
        return true
      }
    }
    return false
  }

  private fun getPackages() :ArrayList<String> {
    val packages: List<PackageInfo> = context.packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES)
    val list = ArrayList<String>()
    for(i in packages.indices) {
      val packageInfo = packages[i]
      if(isSystemApp(packageInfo)) {
        // AppInfo 自定义类，包含应用信息
//        val appInfo = AppInfo()
//        appInfo.setAppName(
//          packageInfo.applicationInfo.loadLabel(context.packageManager).toString()) //获取应用名称
//        appInfo.setPackageName(packageInfo.packageName) //获取应用包名，可用于卸载和启动应用
//        appInfo.setVersionName(packageInfo.versionName) //获取应用版本名
//        appInfo.setVersionCode(packageInfo.versionCode) //获取应用版本号
//        appInfo.setAppIcon(packageInfo.applicationInfo.loadIcon(context.packageManager)) //获取应用图标
      } else {
        list.add(packageInfo.packageName)
      }
    }
    return list
  }

  private fun isSystemApp(pi: PackageInfo): Boolean {
    val isSysApp = pi.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 1
    val isSysUpd = pi.applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 1
    return isSysApp || isSysUpd
  }

  private fun startAppIntent(packageName: String?) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName!!)
    intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    val pendingIntent = PendingIntent.getActivity(context.applicationContext, 0, intent, 0)
    try {
      pendingIntent.send()
    } catch(e: PendingIntent.CanceledException) {
      e.printStackTrace()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  companion object{
    var MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1101
  }
}
