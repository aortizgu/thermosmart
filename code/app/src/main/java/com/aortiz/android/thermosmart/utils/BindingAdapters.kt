package com.aortiz.android.thermosmart.utils

import android.content.Context
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object BindingAdapters {

    private object ThermostatRepositoryInstance : KoinComponent {
        val repository: ThermostatRepository by inject()
        val context: Context by inject()
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
    fun setTempText(textView: TextView, celsiusValue: Double) {
        val farEnabled = ThermostatRepositoryInstance.repository.getShowInFahrenheitConfig()
        val units = if (farEnabled) "ºF" else "ºC"
        val value = if (farEnabled) celsiusValue + 32 else celsiusValue
        textView.text = ThermostatRepositoryInstance.context.resources.getString(R.string.temp_placeholder, value.format(2), units)
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    @BindingAdapter("goneIfNotNull")
    @JvmStatic
    fun goneIfNotNull(view: View, it: Any?) {
        view.visibility = if (it != null && it != "null") View.GONE else View.VISIBLE
    }

    @BindingAdapter("goneIfNull")
    @JvmStatic
    fun goneIfNull(view: View, it: Any?) {
        view.visibility = if (it == null) View.GONE else View.VISIBLE
    }

    @BindingAdapter("goneIfTrue")
    @JvmStatic
    fun goneIfTrue(view: View, it: Boolean) {
        view.visibility = if (it) View.GONE else View.VISIBLE
    }

    @BindingAdapter("thermostatStatusImage")
    @JvmStatic
    fun thermostatStatusImage(imageView: ImageView, thermostat: LiveData<Thermostat>) {
        thermostat.value?.configuration?.boiler?.automaticActivationEnabled?.let { automatic ->
            thermostat.value?.status?.outputs?.boiler?.let { active ->
                if (!automatic) {
                    imageView.setImageResource(R.drawable.ic_power_disabled)
                } else if (active) {
                    imageView.setImageResource(R.drawable.ic_power_on)
                } else {
                    imageView.setImageResource(R.drawable.ic_power_off)
                }
            }
        }
    }

    @BindingAdapter("wateringStatusImage")
    @JvmStatic
    fun wateringStatusImage(imageView: ImageView, thermostat: LiveData<Thermostat>) {
        thermostat.value?.configuration?.watering?.automaticActivationEnabled?.let { automatic ->
            thermostat.value?.status?.outputs?.watering?.let { active ->
                if (active) {
                    imageView.setImageResource(R.drawable.ic_power_on)
                } else if (!automatic) {
                    imageView.setImageResource(R.drawable.ic_power_disabled)
                } else {
                    imageView.setImageResource(R.drawable.ic_power_off)
                }
            }
        }
    }

    @BindingAdapter("wateringFreqText")
    @JvmStatic
    fun wateringFreqText(textView: TextView, value: Int) {
        val values =
            ThermostatRepositoryInstance.context.resources.getStringArray(R.array.wateringFreq)
        val valueOffset = value - 1
        textView.text =
            if (valueOffset >= 0 && valueOffset < values.size) values[valueOffset] else value.toString()
    }

    @BindingAdapter("wateringDurationText")
    @JvmStatic
    fun wateringDurationText(textView: TextView, value: Int) {
        val values =
            ThermostatRepositoryInstance.context.resources.getStringArray(R.array.wateringDuration)
        val valueOffset = value - 1
        textView.text =
            if (valueOffset >= 0 && valueOffset < values.size) values[valueOffset] else value.toString()
    }

    @BindingAdapter("wateringTimeText")
    @JvmStatic
    fun wateringTimeText(textView: TextView, value: Int) {
        val values =
            ThermostatRepositoryInstance.context.resources.getStringArray(R.array.wateringTime)
        textView.text = if (value >= 0 && value < values.size) values[value] else value.toString()
    }

    @BindingAdapter("epochToLocalDate")
    @JvmStatic
    fun epochToLocalDate(textView: TextView, value: Int) {
        val dt = Instant.ofEpochSecond(value.toLong())
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        textView.text = dt.format(DateTimeFormatter.ofPattern(" KK:mm a EEE d LLL"))
    }

    @BindingAdapter("goneIfNot")
    @JvmStatic
    fun goneIfNot(textView: TextView, value: Boolean) {
        textView.visibility = if (value) View.VISIBLE else View.GONE
    }

}
