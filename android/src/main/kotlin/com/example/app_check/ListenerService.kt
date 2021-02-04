package com.example.app_check

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel


class ListenerService : Service() {
  private var argument: ArrayList<String>? = ArrayList()
  private var flutterEngine: FlutterEngine? = null
  private var channel: MethodChannel? = null
  lateinit var handler: Handler

  private val binder: IBinder = LocalBinder()

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onCreate() {
    super.onCreate()

    Log.i(TAG, "开始监听");

    flutterEngine = FlutterEngine(this)
    flutterEngine!!.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
    channel = MethodChannel(flutterEngine!!.dartExecutor, "com.example.app_check")

    channel!!.invokeMethod("callbackOff", null)

    handler = Handler(Looper.getMainLooper())
  }

  @SuppressLint("HandlerLeak")
  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    try {
      val extras = intent.extras
      if (extras != null) {
        argument?.clear()
        argument?.add(applicationContext.packageName)
        channel!!.invokeMethod("callbackOff", null)
        extras.getStringArrayList("argument")?.let { argument?.addAll(it) }
        Log.i(TAG, argument.toString())
      } else {
        Log.i(TAG, "没有参数")
      }
    } catch(e: Exception) {}

    flag = true

    object : Thread() {
      @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
      override fun run() {
        super.run()
        while(flag) {
          synchronized(ListenerService::class.java) {
            var topApp =  RunningTaskUtil(applicationContext).getTopRunningTasks()?.packageName
            if(argument!=null){
              var bingoFlag = true
              for(item in argument!!){
                if(topApp==item){
                  handler.post {
                    Log.i(TAG, "callbackOff")
                    channel!!.invokeMethod("callbackOff", null)
                  }
                  bingoFlag = false
                  break
                }
              }
              if(bingoFlag){
//                var message = Message()
//                message.what = 1
//                handler.sendMessage(message)
                handler.post {
                  Log.i(TAG, "callbackOn")
                  channel!!.invokeMethod("callbackOn", null)
                }
              }
            }
            SystemClock.sleep(500)
          }
        }
      }
    }.start()

    return START_STICKY
  }

  override fun onDestroy() {
    flag = false
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? {
    return binder
  }

  override fun onRebind(intent: Intent?) {
    super.onRebind(intent)
  }

  override fun onUnbind(intent: Intent?): Boolean {
    return false
  }

  companion object {
    private const val TAG = "ListenerService"
    private var flag = true
  }

  inner class LocalBinder : Binder() {
    val service: ListenerService
      get() = this@ListenerService
  }
}

