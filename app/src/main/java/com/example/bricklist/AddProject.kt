package com.example.bricklist

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_project.*
import kotlinx.android.synthetic.main.mylist.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class AddProject : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)
    }
    lateinit var nameProject: String
    lateinit var xml:String
    val possibleXMLS =  arrayOf("615","70403","10179","361","10258","384","555")
    fun onAcceptButtonClick(v:View){
         nameProject = projectInput.text.toString();
         xml = xmlNum.text.toString();

        if(nameProject == ""){
            Toast.makeText(this@AddProject, "Wrong or empty name", Toast.LENGTH_SHORT).show()

        }
        else{
            if(xml == "" || xml !in possibleXMLS) {
                Toast.makeText(this@AddProject, "Wrong xml name", Toast.LENGTH_SHORT).show()
            }
            else{
                //read XML
                //val xmlDoc = readXML(xml)
                downloadData()
                //readXML(xml);
                //save XML to database
                //die inside
                //add to project list
                //MainActivity.items.add(nameProject);
                MainActivity.recentlyChoosed = nameProject
                onBackPressed()
            }
        }


    }
    override fun onBackPressed() {
        super.onBackPressed()
        val refresh = Intent(this,MainActivity::class.java)
        startActivity(refresh)
        this.finish()
    }
    fun downloadData(){
        val cd = XmlDownloader()
        cd.execute()
    }
    private inner class XmlDownloader: AsyncTask<String,Int,String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            loadData()


        }


        @SuppressLint("Recycle")
        fun loadData(){
            val filename = nameProject+".xml"
            val path = filesDir
            val inDir = File(path,"XML")
            if (inDir.exists()){
                val file = File(inDir,filename)
                if (file.exists()){
                    val xmlDoc:Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
                    xmlDoc.documentElement.normalize()
                    var reseID:Cursor = MainActivity.Companion.database!!.rawQuery("Select" +
                            " COALESCE(MAX(ID),0)+1 as id FROM Inventories", null)
                    reseID.moveToFirst()
                    var active = 1
                    var inventoryID = reseID.getString(reseID.getColumnIndex("id")).toInt()
                    var date = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    val formatted = date.format(formatter)
                    MainActivity.Companion.database?.execSQL("Insert into Inventories(id,name,active,LastAccessed)" +
                            " VALUES($inventoryID,'$nameProject',$active,$formatted)")
                    val items: NodeList = xmlDoc.getElementsByTagName("ITEM")
                    for (i in 0..items.length-1){
                        val itemNode: Node = items.item(i)
                        val elem = itemNode as Element
                        val children = elem.childNodes
                        var itemID:String = ""
                        var quantityInSet:Int? = 0
                        var quantityInStore:Int? = 0
                        var color:Int? = null
                        //var extra:String? = null
                        var alternate:String? = ""
                        var itemType:String? = null
                        for (j in 0..children.length-1) {
                            val node = children.item(j)
                            when (node.nodeName){
                                "ITEMTYPE" -> {itemType = node.textContent}
                                "ITEMID" -> {itemID = node.textContent}
                                "QTY" -> {quantityInSet = node.textContent.toInt()}
                                "COLOR" -> {color = node.textContent.toInt()}
                                //"extra" -> {extra = node.textContent}
                                "ALTERNATE" -> {alternate = node.textContent}

                            }
                        }
                        if(alternate=="N"){
                            var realItemCur = MainActivity.Companion.database!!.rawQuery("Select" +
                                    " id from Parts where Code='$itemID'",null)

                            if(realItemCur!=null && realItemCur.count>0) {
                                realItemCur.moveToFirst()
                                var realItemID = realItemCur.getString(realItemCur.getColumnIndex("id")).toInt()
                                // znajdujemy TYPE_ID

                                var res: Cursor = MainActivity.Companion.database!!.rawQuery(
                                    "select" +
                                            " id from ItemTypes where code='$itemType'", null
                                )
                                res.moveToFirst()
                                var realItemType: Int =
                                    res.getString(res.getColumnIndex("id")).toInt()
                                var ressid: Cursor = MainActivity.Companion.database!!.rawQuery(
                                    "select" +
                                            " COALESCE(MAX(ID),0)+1 AS id FROM InventoriesParts",
                                    null
                                )
                                ressid.moveToFirst()
                                var realID = ressid.getString(res.getColumnIndex("id")).toInt()
                                MainActivity.Companion.database?.execSQL(
                                    "Insert into InventoriesParts" +
                                            "(id,InventoryID,TypeID,ItemID,QuantityInSet,QuantityInStore,ColorID" +
                                            ") VALUES($realID,$inventoryID,$realItemType,$realItemID," +
                                            "$quantityInSet,$quantityInStore,$color)"
                                )
                            }






                        }




                    }
                }
            }
        }

        override fun doInBackground(vararg p0: String?): String {
            try {
                val url = URL("http://fcds.cs.put.poznan.pl/MyWeb/BL/"+xml+".xml")
                val connection = url.openConnection()
                connection.connect()
                val lengthOfFile = connection.contentLength
                val isStream = url.openStream()
                val testDirectory = File("$filesDir/XML")
                if (!testDirectory.exists()) testDirectory.mkdir()
                val fos = FileOutputStream("$testDirectory/"+nameProject+".xml")
                val data = ByteArray(1024)
                var count = 0
                var total: Long = 0
                var progress = 0
                count = isStream.read(data)
                while (count != -1) {
                    total += count.toLong()
                    val progress_temp = total.toInt() * 100 / lengthOfFile
                    if (progress_temp % 10 == 0 && progress != progress_temp) {
                        progress = progress_temp
                    }
                    fos.write(data, 0, count)
                    count = isStream.read(data)
                }
                isStream.close()
                fos.close()
            } catch (e: MalformedURLException) {
                return "Malformed URL"
            } catch (e: FileNotFoundException) {
                return "File not found"
            } catch (e: IOException) {
                return "IO Exception"
            }
            return "success"
            //val inDir = File(filesDir,"XML")

            //val file = File(inDir, "test.xml")

            //val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            //xmlDoc.documentElement.normalize()
            //return xmlDoc

        }
    }



}
