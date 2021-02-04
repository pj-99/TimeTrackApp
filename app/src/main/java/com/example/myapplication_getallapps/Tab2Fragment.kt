package com.example.myapplication_getallapps


import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Process.myUid
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.fragment_tab1.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Tab2Fragment() : Fragment() {

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


    @RequiresApi(Build.VERSION_CODES.O)
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
            var newList : ArrayList<String> =  getNewCategoryList(startCal, nowCal,activity!!)
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
            var newList : ArrayList<String> =  getNewCategoryList(startCal, nowCal,activity!!)
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
                var category:String = "None";
                val packageInfo = list?.get(i)
                //system app?
                val appName =
                    activity?.packageManager?.let { packageInfo?.applicationInfo?.loadLabel(it).toString() }
                val packageName :String = packageInfo?.packageName.toString()
                Log.e("app List$i", "$packageName ,appName:$appName")

                val url = "https://play.google.com/store/apps/details?id=" + packageName

                //val category = getCategory(url, activity!!)
               // Log.e("category",category);

                var pref: SharedPreferences? =
                    context?.getSharedPreferences("CATEGORY", MODE_PRIVATE)
                if (pref?.contains(appName)!!){
                    category = pref.getString(appName,"None").toString()
                    Log.i("category", "app: $appName ,category:$category")
                }
                else{
                    Log.i("Get category","app: $appName ,category:$category")
                    MyTask(activity!!, url,appName).execute()
                   // val ret = MyTask().execute(url,appName)

                }


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
        var categorySumMap:MutableMap<String,Long> = mutableMapOf()
        var categoryMap: MutableMap<String,MutableMap<String, Long>> = mutableMapOf()
        for ((k, v) in resultMap) {

            var str = k+": " +((v / 1000) / 60).toString() + " mins "+ (v/1000 %60).toString() +"secs"
            var pref: SharedPreferences? =
                context?.getSharedPreferences("CATEGORY", MODE_PRIVATE)
            var category = pref?.getString(k,"NOTFOUND").toString()
            if(!categorySumMap.contains(category)) categorySumMap[category] = v
            else categorySumMap[category]?.plus(v)?.let { categorySumMap.put(category, it) }
            //str = pref?.getString(k,"NOTFOUND").toString() +": "+ str
            //Log.i("str",str)
            //textStr.add(str)
        }
        for((k,v)in categorySumMap){
            var str = k+": " +((v / 1000) / 60).toString() + " mins "+ (v/1000 %60).toString() +"secs"
            Log.i("str",str)
            textStr.add(str)
        }
        arrayAdapter = ArrayAdapter(activity!!, R.layout.support_simple_spinner_dropdown_item, textStr as List<*>)
        listView.adapter = arrayAdapter


    }


    private open class MyTask(
        val activity: FragmentActivity,
        val url: String,
        val appName: String?
    ) : AsyncTask<Void?, Void?, String?>() {
        override fun doInBackground(vararg params: Void?): String?{
            //appName = params[1].toString()
            //if(params.isNotEmpty())  Log.i("async", params[0])
            //else Log.i("async","its null")
            var doc: Document
            try {
                Log.i("async","try")
                doc  =  Jsoup.connect(url.toString()).get()
                var title = doc.title()
                val link =doc.select("a[itemprop=genre]");
                if(link!=null)  {
                    Log.i("async","link: "+ link.text())
                    var pref =  activity?.getSharedPreferences("CATEGORY", MODE_PRIVATE)
                    pref?.edit()
                        ?.putString(appName,link.text())
                        ?.apply()
                    Log.i("category-new", "app: $appName ,category:${link.text()}")
                    return link.text();
                }
                var pref =  activity?.getSharedPreferences("CATEGORY", MODE_PRIVATE)
                pref?.edit()
                    ?.putString(appName,"UNDEFINED")
                    ?.apply()
                return "UNDEFINED"
            } catch (e: IOException) {
                Log.i("async","catch")
                e.printStackTrace()
                var pref =  activity?.getSharedPreferences("CATEGORY", MODE_PRIVATE)
                pref?.edit()
                    ?.putString(appName,"UNDEFINED")
                    ?.apply()
                return "UNDEFINED";
            }
        }



    }


}





fun isOnline(context:Context): Boolean {
    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
    return networkInfo?.isConnected == true
}



fun getNewCategoryList(start_cal:Calendar, cal: Calendar, context:Context): ArrayList<String> {
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
    var categorySumMap:MutableMap<String,Long> = mutableMapOf()
    var categoryMap: MutableMap<String,MutableMap<String, Long>> = mutableMapOf()
    for ((k, v) in resultMap) {

        var str = k+": " +((v / 1000) / 60).toString() + " mins "+ (v/1000 %60).toString() +"secs"
        var pref: SharedPreferences? =
            context?.getSharedPreferences("CATEGORY", MODE_PRIVATE)
        var category = pref?.getString(k,"NOTFOUND").toString()
        if(!categorySumMap.contains(category)) categorySumMap[category] = v
        else categorySumMap[category]?.plus(v)?.let { categorySumMap.put(category, it) }
        //str = pref?.getString(k,"NOTFOUND").toString() +": "+ str
        //Log.i("str",str)
        //textStr.add(str)
    }
    for((k,v)in categorySumMap){
        var str = k+": " +((v / 1000) / 60).toString() + " mins "+ (v/1000 %60).toString() +"secs"
        Log.i("str",str)
        textStr.add(str)
    }
    return textStr

}