package com.example.bricklist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Switch

class Options : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)
        val sw  = findViewById(R.id.activeSwitch) as Switch
        sw.setOnCheckedChangeListener(object:CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView:CompoundButton, isChecked:Boolean){
                if (isChecked){
                    MainActivity.active = true
                }
                else{
                    MainActivity.active = false
                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val refresh = Intent(this,MainActivity::class.java)
        startActivity(refresh)
        this.finish()
    }
}
