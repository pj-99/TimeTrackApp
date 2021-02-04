package com.example.myapplication_getallapps;

public class GetYesterday {


}




/*


/*public class MainActivity extends AppCompatActivity {

    public final static String GOOGLE_URL = "https://play.google.com/store/apps/details?id=";
    public static final String ERROR = "error";



    private class FetchCategoryTask extends AsyncTask<Void, Void, Void> {

        private final String TAG = FetchCategoryTask.class.getSimpleName();
        private PackageManager pm;
        private ActivityUtil mActivityUtil;

        @Override
        protected Void doInBackground(Void... errors) {
            String category;
            pm = getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            Iterator<ApplicationInfo> iterator = packages.iterator();
            while (iterator.hasNext()) {
                ApplicationInfo packageInfo = iterator.next();
                String query_url = GOOGLE_URL + packageInfo.packageName;
                Log.i(TAG, query_url);
                category = getCategory(query_url);
                // store category or do something else
            }
            return null;
        }


        private String getCategory(String query_url) {
            boolean network = mActivityUtil.isNetworkAvailable();
            if (!network) {
                //manage connectivity lost
                return ERROR;
            } else {
                try {
                    Document doc = Jsoup.connect(query_url).get();
                    Element link = doc.select("span[itemprop=genre]").first();
                    return link.text();
                } catch (Exception e) {
                    return ERROR;
                }
            }
        }
    }
}


<?xml version="1.0" encoding="utf-8"?>


<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:padding="4dp"
    tools:context=".MainActivity">
    <Button
        android:id="@+id/lastDay"
        android:text="前一天"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
    <Button
        android:text="後一天"
        android:layout_width="wrap_content"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        android:layout_height="wrap_content"
        android:id="@+id/nextDay"/>

    <TextView
        android:id="@+id/today"
        android:text="today"
        android:textSize="25dp"
        app:layout_constraintBottom_toBottomOf="@id/lastDay"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        />
    <TextView
        android:id="@+id/level"
        app:layout_constraintTop_toBottomOf="@id/today"
        app:layout_constraintBottom_toTopOf="@id/listView"
        android:text="App-level"
        android:textSize="20dp"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>

    <ListView
        android:id="@+id/listView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/level">

    </ListView>


</androidx.constraintlayout.widget.ConstraintLayout>
* */

/*
package com.example.myapplication_getallapps

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    lateinit var listView: ListView
    var arrayAdapter: ArrayAdapter<*>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "TimeTrackApp"
        listView = findViewById(R.id.listView)

        val dateTextView: TextView = findViewById(R.id.today)
        val date = getCurrentDateTime()
        val dateInString = date.toString("yyyy/MM/dd")
        dateTextView.text = dateInString


        var MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), packageName)
        val granted = mode == AppOpsManager.MODE_ALLOWED
        if (AppOpsManager.MODE_ALLOWED != mode){
            startActivityForResult(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
        }


        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        val lastDateTime = cal.time.time
        Log.e("convert date",lastDateTime.toString() )



        val time = System.currentTimeMillis()
        val mUsageStatsManager  = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val lUsageStatsMap: Map<String, UsageStats> = mUsageStatsManager.queryAndAggregateUsageStats(lastDateTime, time)



        val list = packageManager.getInstalledPackages(0)
        var myMap= mutableMapOf<String,Long>();
        var resultMap:Map<String,Long> = mapOf<String,Long>();
        for (i in list.indices) {
            val packageInfo = list[i]
                //system app?
            val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
            val packageName :String = packageInfo.packageName.toString()
            Log.e("System app List$i", "$packageName ,appName:$appName")
            val totalTimeUsageInMillis = lUsageStatsMap[packageName]?.totalTimeInForeground
            Log.i("totalTimeUsage", totalTimeUsageInMillis.toString());
            if (totalTimeUsageInMillis!=null) {
                if (totalTimeUsageInMillis > 0) {
                    //val totalTimeUsageInMillis = (lUsageStatsMap[packageName] ?: error("")).totalTimeInForeground
                    Log.i(
                        "Total Time Usage",
                        "app:" + appName + ", totalTimeUsage: " + ((totalTimeUsageInMillis / 1000) / 60).toString() + " minutes "+ (totalTimeUsageInMillis/1000 %60).toString() +"seconds"
                    )
                    myMap[appName] = totalTimeUsageInMillis;
                }
            }

            resultMap = myMap.toList().sortedByDescending { (_, value) -> value}.toMap()

        }
        var textStr = ArrayList<String>();
        for ((k, v) in resultMap) {

            val str = k+": " +((v / 1000) / 60).toString() + " mins "+ (v/1000 %60).toString() +"secs"
            Log.i("Sorted Map",str)
            textStr.add(str)
        }
        arrayAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, textStr as List<*>)
        listView.adapter = arrayAdapter
    }
    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }
    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

}
 */

