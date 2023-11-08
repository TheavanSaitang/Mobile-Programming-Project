package com.example.mpproject.viewmodel


import android.icu.text.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mpproject.R
import com.example.mpproject.model.Game

class GameListAdapter(val gameClicked:(game:Game)->Unit, val clickedCheckbox:(game:Game,isChecked:Boolean)->Unit): ListAdapter<Game, GameListAdapter.GameViewHolder>(GamesComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        return GameViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val current = getItem(position)
        // TODO: Implement the bind from GameViewHolder.bind, call it with game parameters
        // holder.bind(current.title,,current,clickedCheckbox)

        holder.itemView.tag= current
        holder.itemView.setOnClickListener{
            gameClicked(holder.itemView.tag as Game)
        }
    }

    // TODO: Bind the game data to tile here: GameViewHolder.bind.
    // Left the code for task list for reference
    /* class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tileTitleView: TextView = itemView.findViewById(R.id.tileTitle)
        private val tileDateView: TextView = itemView.findViewById(R.id.tileDate)
        private val tileCompleteView: CheckBox = itemView.findViewById(R.id.tileComplete)


        // This is where the game data gets bound to it's tile.
        // We can customize the data between the database and presentation
        fun bind(text: String?,dueDate:Long?, game:Game?, clickedCheckbox:(game:Game, isChecked:Boolean)->Unit ) {
            // Parse the date long into string, no content if the long is 0 or null.
            val dateString = if(dueDate?.toInt() != 0) {
                java.text.DateFormat.getDateTimeInstance(
                    DateFormat.DEFAULT,
                    DateFormat.SHORT
                ).format(dueDate)
            }
            else {
                ""
            }

            // Bind all relevant game values to appropriate XML elements
            tileTitleView.text = text
            tileDateView.text = dateString

            tileCompleteView.tag = game
            tileCompleteView.setOnClickListener{
                clickedCheckbox(tileCompleteView.tag as Game, tileCompleteView.isChecked)
            }
            tileCompleteView.isChecked = game?.done ?: false

        }
        companion object {
            fun create(parent: ViewGroup): GameViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return GameViewHolder(view)
            }
        }
    } */

    class GamesComparator : DiffUtil.ItemCallback<Game>() {
        override fun areItemsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: Game, newItem: Game): Boolean {
            return oldItem.title == newItem.title
        }
    }
}
