package com.aortiz.android.thermosmart.utils

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.aortiz.android.thermosmart.domain.Thermostat
import com.aortiz.android.thermosmart.thermostat.list.ThermostatAdapter


object BindingAdapters {

    @BindingAdapter("listData")
    @JvmStatic
    fun bindRecyclerView(recyclerView: RecyclerView, data: LiveData<List<Thermostat>>?) {
        val adapter = recyclerView.adapter as ThermostatAdapter
        data?.value.let {
            adapter.submitList(it)
        }
    }

    /**
     * Use this binding adapter to show and hide the views using boolean variables
     */
    @BindingAdapter("android:fadeVisible")
    @JvmStatic
    fun setFadeVisible(view: View, visible: Boolean? = true) {
        if (view.tag == null) {
            view.tag = true
            view.visibility = if (visible == true) View.VISIBLE else View.GONE
        } else {
            view.animate().cancel()
            if (visible == true) {
                if (view.visibility == View.GONE)
                    view.fadeIn()
            } else {
                if (view.visibility == View.VISIBLE)
                    view.fadeOut()
            }
        }
    }

}