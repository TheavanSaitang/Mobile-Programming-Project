package edu.uark.ahnelson.mPProject.MainActivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import edu.uark.ahnelson.mPProject.R
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager


class SteamFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        val root = inflater.inflate(R.layout.fragment_steam_login, container, false)
        val buttonCancel = root.findViewById<Button>(R.id.btnCancel)
        buttonCancel.setOnClickListener{
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }
        //TODO MAKE SUBMIT ACTUALLY SUBMIT TO SOMEWHERE
        val buttonSubmit = root.findViewById<Button>(R.id.btnSubmit)
        buttonSubmit.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }
        return root
    }




}