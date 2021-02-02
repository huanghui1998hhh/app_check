package com.example.app_check

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*


class RunningTaskUtil(context: Context) {
  private val TAG = "RunningTaskUtil"
  private lateinit var mUsageStatsManager: UsageStatsManager
  private var topComponentName: ComponentName? = null

  init {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      mUsageStatsManager = context.applicationContext
          .getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  fun getTopRunningTasks(): ComponentName? {
    return getTopRunningTasksByEvent()

  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  private fun getTopRunningTasksByEvent(): ComponentName? {
    val time = System.currentTimeMillis()
    val usageEvents: UsageEvents = mUsageStatsManager.queryEvents(time - 60 * 60 * 1000, time)
    var out: UsageEvents.Event
    val map: TreeMap<Long?, UsageEvents.Event?> = TreeMap()
    if (usageEvents != null) {
      while (usageEvents.hasNextEvent()) {
        out = UsageEvents.Event() //这里一定要初始化，不然getNextEvent会报空指针
        if (usageEvents.getNextEvent(out)) {
          if (out != null) {
            map[out.timeStamp] = out
          } else {
            Log.e(TAG, " out is NULL")
          }
        } else {
          Log.e(TAG, " usageEvents is unavailable")
        }
      }
      if (!map.isEmpty()) {
        //将keyset颠倒过来，让最近的排列在上面
        val keySets = map.navigableKeySet()
        val iterator: Iterator<*> = keySets.descendingIterator()
        while (iterator.hasNext()) {
          val event = map[iterator.next()]
          if (event!!.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
            //当遇到有app移动到前台时，就更新topComponentName
            topComponentName = ComponentName(event.packageName, "")
            break
          }
        }
      }
    } else {
      Log.e(TAG, " usageEvents is null")
    }
    return topComponentName
  }
}

