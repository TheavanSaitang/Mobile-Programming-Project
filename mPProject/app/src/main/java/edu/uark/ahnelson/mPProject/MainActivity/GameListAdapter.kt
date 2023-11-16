package edu.uark.ahnelson.mPProject.MainActivity

import android.icu.text.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.uark.ahnelson.mPProject.Model.Game
import edu.uark.ahnelson.mPProject.R
import java.util.Calendar

class GameListAdapter(val itemClicked: (id: Int) -> Unit)
    : ListAdapter<Game, GameListAdapter.GameViewHolder>(GameComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        return GameViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val current = getItem(position)

        // Bind info to the MainActivity recyclerView and set up onClickListener
        current.id?.let {
            holder.bind(it,current.title,current.system)
        }
        holder.itemView.tag = current.id
        holder.itemView.setOnClickListener {
            val itemId = it.tag
            Log.d("GameListAdapter","Item Clicked: " + itemId)
            itemClicked(it.tag as Int)
        }
    }

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameTitleView: TextView = itemView.findViewById(R.id.tvTitle)
        private val gameSystemView:TextView = itemView.findViewById(R.id.tvSystem)

        //
        fun bind(id:Int, title: String?, system: String?) {
            gameTitleView.text = title
            gameSystemView.text = system
            }
        companion object {
            fun create(parent: ViewGroup): GameViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return GameViewHolder(view)
            }
        }
    }

    class GameComparator : DiffUtil.ItemCallback<Game>() {
        override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem.title == newItem.title
        }
    }
}