package com.example.bricklist

import android.accounts.AccountManager.get
import android.app.Activity
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

class MyListAdapter(context:Activity,imgid:Array<Int>,name:Array<String>,color_name:Array<String>,numbers:Array<String>, idIs:Array<Int>,codes:Array<String>,colorCodes:Array<Int>,itemIds:Array<String>) :ArrayAdapter<String>(context, R.layout.mylist,name){
    private val context: Activity
    private val imgid:Array<Int>
    private val name:Array<String>
    private val color_name:Array<String>
    private val numbers:Array<String>
    private val idBase:Array<Int>
    private val codesBase:Array<String>
    private val colorCodes:Array<Int>
    private val itemIds:Array<String>

    init{
        this.context = context
        this.imgid = imgid
        this.name = name
        this.color_name = color_name
        this.numbers = numbers
        this.idBase = idIs
        this.codesBase = codes
        this.colorCodes = colorCodes
        this.itemIds = itemIds
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getLayoutInflater()
        val rowView = inflater.inflate(R.layout.mylist,null,true)
        val nameItem = rowView.findViewById(R.id.name) as TextView
        val imageView = rowView.findViewById(R.id.icon) as ImageView
        val colorNameView = rowView.findViewById(R.id.color_name) as TextView
        val numbersView = rowView.findViewById(R.id.numbers) as TextView
        nameItem.setText(name[position])
        //imageView.setImageResource(imgid[position])
        //imageView.setImageBitmap(bitmapBase[position])

        Picasso.with(context).load("http://img.bricklink.com/P/"+colorCodes[position].toString()+"/"+itemIds[position]+".gif").into(imageView,object: Callback{
            override fun onSuccess() {
                Log.d(TAG, "success")

            }

            override fun onError() {
                Picasso.with(context).load("https://www.bricklink.com/PL/"+itemIds[position]+".jpg").into(imageView)
            }
        })



        colorNameView.setText(color_name[position])
        numbersView.setText(numbers[position])
        val buttonPlus = rowView.findViewById(R.id.plus) as Button
        val buttonMinus:Button = rowView.findViewById(R.id.minus) as Button
        val changedLayout = rowView.findViewById(R.id.coloringLayout) as LinearLayout
        var firstNum= Character.getNumericValue(numbers[position].first())
        var lastNum = Character.getNumericValue(numbers[position].last())
        if(firstNum==lastNum) changedLayout.setBackgroundColor(Color.GREEN)
        buttonPlus.setOnClickListener {
            var firstNum= Character.getNumericValue(numbers[position].first())
            var lastNum = Character.getNumericValue(numbers[position].last())
            var choosedId = idBase[position]
            if(firstNum>=lastNum) {
                changedLayout.setBackgroundColor(Color.GREEN)
            }
            else{
                firstNum+=1
                val edText = firstNum.toString()+numbers[position].substring(1)
                numbers[position] = edText
                numbersView.setText(edText)
                var a =MainActivity.Companion.database?.execSQL("Update InventoriesParts " +
                        "Set QuantityInStore = $firstNum WHERE id=$choosedId")

                if(firstNum==lastNum){
                    changedLayout.setBackgroundColor(Color.GREEN)
                }
            }

        }
        buttonMinus.setOnClickListener {
            var firstNum= Character.getNumericValue(numbers[position].first())
            var lastNum = Character.getNumericValue(numbers[position].last())
            var choosedId = idBase[position]
            if(firstNum!=0){
                firstNum--
                val edText = firstNum.toString()+numbers[position].substring(1)
                numbersView.setText(edText)
                numbers[position] = edText
                var a =MainActivity.Companion.database?.execSQL("Update InventoriesParts " +
                        "Set QuantityInStore = $firstNum WHERE id=$choosedId")
                if(firstNum==0){
                    changedLayout.setBackgroundColor(Color.WHITE)
                }
            }
        }


        return rowView
    }



}