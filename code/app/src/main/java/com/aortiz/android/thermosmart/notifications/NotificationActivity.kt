package com.aortiz.android.thermosmart.notifications

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavArgument
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navArgs
import androidx.navigation.navArgument
import com.aortiz.android.thermosmart.R
import timber.log.Timber

class NotificationActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_THERMOSTAT_ID = "EXTRA_THERMOSTAT_ID"
        fun newIntent(context: Context, thermostatId: String): Intent {
            val intent = Intent(context, NotificationActivity::class.java)
            intent.putExtra(EXTRA_THERMOSTAT_ID, thermostatId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        val myNavHostFragment: NavHostFragment = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment
        val inflater = myNavHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.notification_nav_graph)
        intent.extras?.let { b ->
            val id = b.getString(EXTRA_THERMOSTAT_ID, "")
            graph.addArgument("thermostatId", NavArgument.Builder().setDefaultValue(id).build())
        }
        myNavHostFragment.navController.graph = graph
    }
}