package com.example.bricklist

import AssetDatabaseOpenHelper
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*

class MainActivity : AppCompatActivity() {

    companion object {
        var items:MutableList<String> = ArrayList()
        var database:SQLiteDatabase? = null;
        var link:String = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"
        lateinit var choosedProjectName:String
        var recentlyChoosed = ""
        var active = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //read dataBase
        val dbHelper = AssetDatabaseOpenHelper(applicationContext)

        database = dbHelper.openDatabase()
        //var result:Array<String> = emptyArray()
        // dbHelper.copyDataBase()
        //dataBase.rawQuery("Select * from Parts where Code=3001",result)
        lateinit var rawQuery:Cursor
        if(active == false) {
             rawQuery = database!!.rawQuery("Select * from Inventories Where Active = 1", null)
        }
        else{
             rawQuery = database!!.rawQuery("Select * from Inventories",null)
        }
        rawQuery.moveToFirst()
        items = ArrayList()
        while(!rawQuery.isAfterLast())
        {
            items.add(rawQuery.getString(rawQuery.getColumnIndex("Name")))
            rawQuery.moveToNext()
        }
        if(recentlyChoosed!=""){
            items.add(recentlyChoosed)
        }

        //create list view
        var mListView = findViewById<ListView>(R.id.listView)
        //adapter
        var adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items)
        //set adapter
        mListView.adapter=adapter
        //List items click
        mListView.setOnItemClickListener{ parent: AdapterView<*>, view:View, position:Int, id:Long ->
            Toast.makeText(this@MainActivity, items[position], Toast.LENGTH_SHORT).show()
            choosedProjectName = items[position]
            val intent  = Intent(this,ProjectActivity::class.java)
            startActivity(intent)


    }
}
    fun onAddProject(v:View){
        val intent = Intent(this,AddProject::class.java)
        startActivity(intent)

    }
    fun onOptions(v:View){
        val intent = Intent(this,Options::class.java)
        startActivity(intent)
    }
}
