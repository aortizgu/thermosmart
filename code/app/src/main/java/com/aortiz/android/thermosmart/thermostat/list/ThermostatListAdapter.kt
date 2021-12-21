package com.aortiz.android.thermosmart.thermostat.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aortiz.android.thermosmart.databinding.ThermostatItemBinding
import com.aortiz.android.thermosmart.domain.Thermostat

class ThermostatAdapter(private val clickListener: ThermostatClickListener) :
    ListAdapter<Thermostat,
            ThermostatAdapter.ThermostatViewHolder>(ThermostatDiffCallback()) {

    override fun onBindViewHolder(holder: ThermostatViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(clickListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThermostatViewHolder {
        return ThermostatViewHolder.from(parent)
    }

    class ThermostatViewHolder private constructor(val binding: ThermostatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: ThermostatClickListener, item: Thermostat) {
            binding.item = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ThermostatViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ThermostatItemBinding.inflate(layoutInflater, parent, false)

                return ThermostatViewHolder(binding)
            }
        }
    }
}

class ThermostatDiffCallback : DiffUtil.ItemCallback<Thermostat>() {
    override fun areItemsTheSame(oldItem: Thermostat, newItem: Thermostat): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Thermostat, newItem: Thermostat): Boolean {
        return oldItem == newItem
    }
}

class ThermostatClickListener(val clickListener: (thermostat: Thermostat) -> Unit) {
    fun onClick(thermostat: Thermostat) = clickListener(thermostat)
}

