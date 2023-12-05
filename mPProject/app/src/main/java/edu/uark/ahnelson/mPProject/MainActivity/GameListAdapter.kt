package edu.uark.ahnelson.mPProject.MainActivity

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.uark.ahnelson.mPProject.Model.Game
import edu.uark.ahnelson.mPProject.R

class GameListAdapter(val itemClicked: (id: Int) -> Unit)
    : ListAdapter<Game, GameListAdapter.GameViewHolder>(GameComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        return GameViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val current = getItem(position)

        // Bind info to the MainActivity recyclerView and set up onClickListener
        current.id?.let {
            holder.bind(current.title,current.system,current.rating)
        }
        holder.itemView.tag = current.id
        holder.itemView.setOnClickListener {
            val itemId = it.tag
            Log.d("GameListAdapter", "Item Clicked: $itemId")
            itemClicked(it.tag as Int)
        }
    }
    //TODO make starbar usable from recyclerview
    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gameTitleView: TextView = itemView.findViewById(R.id.tvTitle)
        private val gameSystemView: TextView = itemView.findViewById(R.id.tvSystem)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBarShow)

        //
        fun bind(title: String?, system: String?, score: Float?) {
            gameTitleView.text = title
            gameSystemView.text = system
            if (score != null) {
                ratingBar.rating = score
                }
            ratingBar.setIsIndicator(true)
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