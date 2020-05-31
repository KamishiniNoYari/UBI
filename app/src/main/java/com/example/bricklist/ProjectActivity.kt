package com.example.bricklist

import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.MimeTypeFilter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_project.*
import org.w3c.dom.Element
import java.io.File
import java.lang.Exception
import java.lang.System.load
import java.net.URI
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.properties.Delegates

class ProjectActivity : AppCompatActivity() {
    lateinit var list: ListView
    var names:MutableList<String> = ArrayList()
    var colors:MutableList<String> = ArrayList()
    var numbers:MutableList<String> = ArrayList()
    var images:MutableList<Int> = ArrayList()
    var itemIDs:MutableList<Int> = ArrayList()
    var codes:MutableList<String> = ArrayList()
    var colorCodes:MutableList<Int> = ArrayList()
    var itemCodes:MutableList<String> = ArrayList()
    var projId by Delegates.notNull<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)
        //znajdz id projektu
        var idCur:Cursor = MainActivity.Companion.database!!.rawQuery("Select id" +
                " from Inventories Where Name='${MainActivity.choosedProjectName}'",null)
        idCur.moveToFirst()
        projId = idCur.getString(idCur.getColumnIndex("id")).toInt()
        var itemCur:Cursor = MainActivity.Companion.database!!.rawQuery("Select *" +
                " from InventoriesParts Where InventoryID=$projId",null)
        itemCur.moveToFirst()

        var itemCode:String
        var itemName:String
        var itemColorName:String
        var numberInStore:Int
        var numberInProject:Int



        while(!itemCur.isAfterLast()){
            var itemID = itemCur.getString(itemCur.getColumnIndex("ItemID")).toInt()
            var itemIDcurr = MainActivity.Companion.database!!.rawQuery("Select " +
                    "Code, Name from Parts Where id=$itemID",null)
            itemIDcurr.moveToFirst()
            itemCode = itemIDcurr.getString(itemIDcurr.getColumnIndex("Code"))
            itemName = itemIDcurr.getString(itemIDcurr.getColumnIndex("Name"))
            numberInStore = itemCur.getString(itemCur.getColumnIndex("QuantityInSet")).toInt()
            numberInProject = itemCur.getString(itemCur.getColumnIndex("QuantityInStore")).toInt()
            //szukamy koloru
            var colorCode = itemCur.getString(itemCur.getColumnIndex("ColorID")).toInt()
            var itemColorCur = MainActivity.Companion.database!!.rawQuery("Select" +
                    " Name from Colors Where Code=$colorCode",null)
            itemColorCur.moveToFirst()
            itemColorName=itemColorCur.getString(itemColorCur.getColumnIndex("Name"))

            var selectedCodes = MainActivity.Companion.database!!.rawQuery("Select " +
                    "Code from Codes Where ItemID=$itemID And ColorID=$colorCode",null)

            if((selectedCodes != null) && (selectedCodes.count > 0)){
                selectedCodes.moveToFirst()
                val selCode = selectedCodes.getString(selectedCodes.getColumnIndex("Code"))
                codes.add(selCode)
            }
            else{
                codes.add(itemCode)
            }
            colorCodes.add(colorCode)
            itemCodes.add(itemCode)




            //bitmaps.add(resizedMap)

            names.add(itemName)
            colors.add(itemColorName+" ["+itemCode+"]")
            numbers.add(numberInProject.toString()+" of "+numberInStore.toString())
            images.add(R.drawable.nophoto)
            itemIDs.add(itemCur.getString(itemCur.getColumnIndex("id")).toInt())

            itemCur.moveToNext()

        }
        var adapter = MyListAdapter(
            this,
            images.toTypedArray(),
            names.toTypedArray(),
            colors.toTypedArray(),
            numbers.toTypedArray(),
            itemIDs.toTypedArray(),
            codes.toTypedArray(),
            colorCodes.toTypedArray(),
            itemCodes.toTypedArray()

        )
        list = this.findViewById(R.id.list)
        list.adapter=adapter


    }
    fun onSaveXML(v:View){
        val docBuilder:DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = docBuilder.newDocument()
        val rootElement: Element = doc.createElement("INVENTORY")
        var cursorElements = MainActivity.database!!.rawQuery("Select * from InventoriesParts " +
                "Where InventoryID=$projId",null)
        cursorElements.moveToFirst()
        while(!cursorElements.isAfterLast())
        {
            val item = doc.createElement("ITEM")
            val itemTypeDoc = doc.createElement("ITEMTYPE")
            val itemTypeID = cursorElements.getString(cursorElements.getColumnIndex("TypeID")).toInt()
            val itemTypeCursor = MainActivity.database!!.rawQuery("Select Code from ItemTypes Where" +
                    " id=$itemTypeID",null)
            itemTypeCursor.moveToFirst()
            val itemType = itemTypeCursor.getString(itemTypeCursor.getColumnIndex("Code"))
            itemTypeDoc.appendChild(doc.createTextNode(itemType))
            item.appendChild(itemTypeDoc)
            val itemIdDoc = doc.createElement("ITEMID")
            val itemID = cursorElements.getString(cursorElements.getColumnIndex("ItemID")).toInt()
            val itemCodeCur = MainActivity.database!!.rawQuery("Select Code from Parts" +
                    " Where id=$itemID",null)
            itemCodeCur.moveToFirst()
            val itemCode = itemCodeCur.getString(itemCodeCur.getColumnIndex("Code"))
            itemIdDoc.appendChild(doc.createTextNode(itemCode))
            item.appendChild(itemIdDoc)
            val colorDoc = doc.createElement("COLOR")
            val colorID = cursorElements.getString(cursorElements.getColumnIndex("ColorID"))
            colorDoc.appendChild(doc.createTextNode(colorID))
            item.appendChild(colorDoc)
            val qtyfilledDoc = doc.createElement("QTYFILLED")
            val qtyfilled =  cursorElements.getString(cursorElements.getColumnIndex("QuantityInSet")).toInt() -
                    cursorElements.getString(cursorElements.getColumnIndex("QuantityInStore")).toInt()

            if(qtyfilled > 0){
                qtyfilledDoc.appendChild(doc.createTextNode(qtyfilled.toString()))
                item.appendChild(qtyfilledDoc)
                rootElement.appendChild(item)
            }
            cursorElements.moveToNext()
        }
        doc.appendChild(rootElement)
        val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2")
        val path=this.filesDir
        val outDir = File(path,"Output")
        outDir.mkdir()
        val file = File(outDir,MainActivity.choosedProjectName+".xml")

        transformer.transform(DOMSource(doc),StreamResult(file))
        MainActivity.Companion.database?.execSQL("UPDATE Inventories "+
        "SET Active=0 Where id=$projId")
        Toast.makeText(this@ProjectActivity, "Done", Toast.LENGTH_SHORT).show()

    }

}
