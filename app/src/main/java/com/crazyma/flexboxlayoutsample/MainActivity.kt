package com.crazyma.flexboxlayoutsample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        insertItem()
    }

    private fun insertItem() {
        for (i in 0..16) {
            (Math.random() * 10000).toInt().toString().run {
                (LayoutInflater.from(this@MainActivity)
                    .inflate(R.layout.view_flex_item, flexboxLayout, false) as TextView).apply {
                    text = this@run
                    isSelected = false
                    setOnClickListener {

                    }
                }.apply {
                    flexboxLayout.addView(this)
                }
            }
        }
    }

    fun buttonClicked(v: View){

    }
}
