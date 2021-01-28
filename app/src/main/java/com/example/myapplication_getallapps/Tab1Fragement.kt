package com.example.myapplication_getallapps



import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process.myUid
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_tab1.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Tab1Fragment() : Fragment() {

    lateinit var listView: ListView
    var arrayAdapter: ArrayAdapter<*>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        super.onCreate(savedInstanceState)
        val view: View = inflater!!.inflate(R.layout.fragment_tab1, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val date = getCurrentDateTime()
        val dateInString = date.toString("yyyy/MM/dd")
        today?.text = dateInString
        listView = listViewer

        var MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1
        val appOps = activity?.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
            myUid(), activity?.packageName)
        val granted = mode == AppOpsManager.MODE_ALLOWED
        if (AppOpsManager.MODE_ALLOWED != mode){
            startActivityForResult(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
        }


        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        val lastDateTime = cal.time.time
        Log.e("convert date",lastDateTime.toString() )

        lastDay.setOnClickListener {
            val nowCal = Calendar.getInstance()
            val startCal = Calendar.getInstance()
            val sdf =
                SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)

            nowCal.time = sdf.parse(today.text as String) // all done
            nowCal.add(Calendar.DATE,-1)
            today.text = nowCal.time.toString("yyyy/MM/dd")
            startCal.time = sdf.parse(today.text as String)
            startCal.add(Calendar.DATE,-1)
            Log.e("nowDay", nowCal.time.toString("yyyy/MM/dd"))
            Log.e("endtDay", startCal.time.toString("yyyy/MM/dd"))
            var newList : ArrayList<String> =  getNewList(startCal, nowCal,activity!!)
            arrayAdapter = ArrayAdapter(activity!!, R.layout.support_simple_spinner_dropdown_item, newList as List<*>)
            listView.adapter = arrayAdapter
        }

        nextDay.setOnClickListener {
            val nowCal = Calendar.getInstance()
            val startCal = Calendar.getInstance()
            val sdf =
                SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)

            nowCal.time = sdf.parse(today.text as String) // all done
            nowCal.add(Calendar.DATE,+1)
            today.text = nowCal.time.toString("yyyy/MM/dd")
            startCal.time = sdf.parse(today.text as String)
            startCal.add(Calendar.DATE,-1)
            Log.e("nowDay", nowCal.time.toString("yyyy/MM/dd"))
            Log.e("endtDay", startCal.time.toString("yyyy/MM/dd"))
            var newList : ArrayList<String> =  getNewList(startCal, nowCal,activity!!)
            arrayAdapter = ArrayAdapter(activity!!, R.layout.support_simple_spinner_dropdown_item, newList as List<*>)
            listView.adapter = arrayAdapter
        }



        val time = System.currentTimeMillis()
        val mUsageStatsManager  = activity?.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val lUsageStatsMap: Map<String, UsageStats> = mUsageStatsManager.queryAndAggregateUsageStats(lastDateTime, time)



        val list = activity?.packageManager?.getInstalledPackages(0)
        var myMap= mutableMapOf<String,Long>();
        var resultMap:Map<String,Long> = mapOf<String,Long>();
        if (list != null) {
            for (i in list.indices) {
                val packageInfo = list?.get(i)
                //system app?
                val appName =
                    activity?.packageManager?.let { packageInfo?.applicationInfo?.loadLabel(it).toString() }
                val packageName :String = packageInfo?.packageName.toString()
                //Log.e("System app List$i", "$packageName ,appName:$appName")
                val totalTimeUsageInMillis = lUsageStatsMap[packageName]?.totalTimeInForeground
                //Log.i("totalTimeUsage", totalTimeUsageInMillis.toString());
                if (totalTimeUsageInMillis!=null) {
                    if (totalTimeUsageInMillis > 0) {
                        //val totalTimeUsageInMillis = (lUsageStatsMap[packageName] ?: error("")).totalTimeInForeground
                        //Log.i(
                         //   "Total Time Usage",
                         //   "app:" + appName + ", totalTimeUsage: " + ((totalTimeUsageInMillis / 1000) / 60).toString() + " minutes "+ (totalTimeUsageInMillis/1000 %60).toString() +"seconds"
                        //)
                        if(appName!=null)  myMap[appName] = totalTimeUsageInMillis;
                    }
                }

                resultMap = myMap.toList().sortedByDescending { (_, value) -> value}.toMap()

            }
        }
        var textStr = ArrayList<String>();
        for ((k, v) in resultMap) {

            val str = k+": " +((v / 1000) / 60).toString() + " mins "+ (v/1000 %60).toString() +"secs"
            //Log.i("Sorted Map",str)
            textStr.add(str)
        }
        arrayAdapter = ArrayAdapter(activity!!, R.layout.support_simple_spinner_dropdown_item, textStr as List<*>)
        listView.adapter = arrayAdapter


    }

}
fun getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}
fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}



fun getNewList(start_cal:Calendar, cal: Calendar, context:Context): ArrayList<String> {
    //Log.e("last Date time date", "$start_cal $cal")
    val lastDateTime = start_cal.time.time

    val time = cal.time.time
   // Log.e("lastDateTime time", "$lastDateTime $time")

    val mUsageStatsManager  = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val lUsageStatsMap: Map<String, UsageStats> = mUsageStatsManager.queryAndAggregateUsageStats(lastDateTime, time)

    val list = context?.packageManager?.getInstalledPackages(0)
    var myMap= mutableMapOf<String,Long>();
    var resultMap:Map<String,Long> = mapOf<String,Long>();
    if (list != null) {
        for (i in list.indices) {
            val packageInfo = list?.get(i)
            //system app?
            val appName =
                context?.packageManager?.let { packageInfo?.applicationInfo?.loadLabel(it).toString() }
            val packageName :String = packageInfo?.packageName.toString()
            //Log.e("System app List$i", "$packageName ,appName:$appName")
            val totalTimeUsageInMillis = lUsageStatsMap[packageName]?.totalTimeInForeground
            //Log.i("totalTimeUsage", totalTimeUsageInMillis.toString());
            if (totalTimeUsageInMillis!=null) {
                if (totalTimeUsageInMillis > 0) {
                    //val totalTimeUsageInMillis = (lUsageStatsMap[packageName] ?: error("")).totalTimeInForeground
                    //Log.i(
                    //    "Total Time Usage",
                    //    "app:" + appName + ", totalTimeUsage: " + ((totalTimeUsageInMillis / 1000) / 60).toString() + " minutes "+ (totalTimeUsageInMillis/1000 %60).toString() +"seconds"
                    //)
                    if(appName!=null)  myMap[appName] = totalTimeUsageInMillis;
                }
            }

            resultMap = myMap.toList().sortedByDescending { (_, value) -> value}.toMap()

        }
    }
    var textStr = ArrayList<String>();
    for ((k, v) in resultMap) {

        val str = k+": " +((v / 1000) / 60).toString() + " mins "+ (v/1000 %60).toString() +"secs"
        //Log.i("Sorted Map",str)
        textStr.add(str)
    }
    return textStr

}