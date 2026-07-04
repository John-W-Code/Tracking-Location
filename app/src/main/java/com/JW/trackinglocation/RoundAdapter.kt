package com.JW.trackinglocation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class RoundAdapter(private var rounds: List<RoundData>) : RecyclerView.Adapter<RoundAdapter.RoundViewHolder>() {

    class RoundViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val number: TextView = view.findViewById(R.id.round_number)
        val time: TextView = view.findViewById(R.id.round_time)
        val distance: TextView = view.findViewById(R.id.round_distance)
        val speed: TextView = view.findViewById(R.id.round_speed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoundViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_round, parent, false)
        return RoundViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoundViewHolder, position: Int) {
        val round = rounds[position]
        holder.number.text = round.number.toString()
        holder.time.text = String.format("%02d:%02d", round.time.toMinutesPart(), round.time.toSecondsPart())
        holder.distance.text = String.format(Locale("nl", "NL"), "%,.0f m", round.distance)
        holder.speed.text = String.format(Locale("nl", "NL"), "%,.2f", round.speed)
    }

    override fun getItemCount() = rounds.size

    fun updateData(newRounds: List<RoundData>) {
        rounds = newRounds
        notifyDataSetChanged()
    }
}