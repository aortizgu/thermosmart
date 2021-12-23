package com.aortiz.android.thermosmart.notifications

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.aortiz.android.thermosmart.R
import com.aortiz.android.thermosmart.databinding.ActivityDetailNotificationBinding
import com.aortiz.android.thermosmart.domain.Thermostat
import com.aortiz.android.thermosmart.thermostat.detail.ThermostatDetailViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DetailNotificationActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_thermostat = "EXTRA_thermostat"
        fun newIntent(context: Context, thermostat: Thermostat): Intent {
            val intent = Intent(context, DetailNotificationActivity::class.java)
            intent.putExtra(EXTRA_thermostat, thermostat)
            return intent
        }
    }

    private val viewModel: ThermostatDetailViewModel by viewModel { parametersOf(thermostatId) }
    private lateinit var binding: ActivityDetailNotificationBinding
    private lateinit var thermostatId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_detail_notification
        )
        val thermostat = intent.extras?.get(EXTRA_thermostat) as Thermostat
        thermostatId = thermostat.id!!
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }
}