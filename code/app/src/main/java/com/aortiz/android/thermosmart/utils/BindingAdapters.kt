package com.aortiz.android.thermosmart.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.domain.Thermostat
import com.aortiz.android.thermosmart.repository.ThermostatRepository
import com.aortiz.android.thermosmart.thermostat.list.ThermostatAdapter
import com.squareup.picasso.Picasso
import org.koin.core.KoinComponent
import org.koin.core.inject

object BindingAdapters {

    private object ThermostatRepositoryInstance : KoinComponent {
        val repository : ThermostatRepository by inject()
    }

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

    @BindingAdapter("imageUrl")
    @JvmStatic
    fun setImageUrl(imageView: ImageView, url: String?) {
        if (url != null) {
            Picasso.get()
                .load(url)
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image)
                .into(imageView)
        }
    }

    @BindingAdapter("tempText")
    @JvmStatic
    fun setTempText(textView: TextView, celsiusValue : Double) {
        val farEnabled = ThermostatRepositoryInstance.repository.getShowInFahrenheitConfig()
        val units = if (farEnabled) "ºF" else "ºC"
        val value = if (farEnabled) celsiusValue+32 else celsiusValue
        textView.text = "${value.format(2)} $units"
    }
    @BindingAdapter("tempText")
    @JvmStatic
    fun setTempText(textView: TextView, celsiusValueLiveData : LiveData<Double>) {
        celsiusValueLiveData.value?.let {
            setTempText(textView, it)
        }
    }
    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    @BindingAdapter("goneIfNotNull")
    @JvmStatic
    fun goneIfNotNull(view: View, it: Any?) {
        view.visibility = if (it != null) View.GONE else View.VISIBLE
    }

    @BindingAdapter("goneIfNull")
    @JvmStatic
    fun goneIfNull(view: View, it: Any?) {
        view.visibility = if (it == null) View.GONE else View.VISIBLE
    }


    @BindingAdapter("thermostatStatusImage")
    @JvmStatic
    fun thermostatStatusImage(imageView: ImageView, active: Boolean) {
        if (active) {
            imageView.setImageResource(R.drawable.ic_power_on)
        } else {
            imageView.setImageResource(R.drawable.ic_power_off)
        }
    }

}