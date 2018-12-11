package com.crazyma.flexboxlayoutsample

import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.util.Log

class CustomAnimator: DefaultItemAnimator() {

    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
        return super.animateAdd(holder)
    }

//    override fun animateAddImpl(holder: RecyclerView.ViewHolder?) {
//        super.animateAddImpl(holder)
//        Log.d("badu","" )
//    }
}