package com.crazyma.flexboxlayoutsample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var adapter: FlexboxAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {

        val list = mutableListOf<String>().apply {
            for (i in 0..30) {
                (Math.random() * 10000).toInt().toString().run {
                    this@apply.add(this)
                }
            }
        }

        adapter = FlexboxAdapter(this).apply {
            this.list = list
        }

        val manager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.CENTER
        }

        recyclerView.itemAnimator = SlideInDownAnimator()
        recyclerView.layoutManager = manager
        recyclerView.adapter = adapter


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

    fun buttonClicked(v: View) {
        when (v.id) {

            R.id.addButton -> {

                adapter.insertItem((Math.random() * 10).toInt(), listOf("AAA","BBBBBB","CCCCCCCCC"))
            }

            R.id.deleteButton -> {

            }
        }
    }
}
