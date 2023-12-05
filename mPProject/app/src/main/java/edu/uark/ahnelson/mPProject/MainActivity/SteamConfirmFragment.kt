package edu.uark.ahnelson.mPProject.MainActivity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import edu.uark.ahnelson.mPProject.R
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE

class SteamConfirmFragment : Fragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_steam_confirm, container, false)
        val parentActivity = activity as MainActivity
        val buttonNo = root.findViewById<Button>(R.id.btnNo)
        val buttonYes = root.findViewById<Button>(R.id.btnYes)
        val titleConfirm: TextView = root.findViewById(R.id.titleConfirm)
        var exit = false
        if(parentActivity.gameListViewModel.playerTitle.value == "") {
            titleConfirm.text = "INVALID USER ID"
            buttonYes.visibility = View.GONE
            buttonNo.text = "Okay"
        }
        else
        {
            titleConfirm.text = parentActivity.gameListViewModel.playerTitle.value
        }
        parentActivity.gameListViewModel.playerIcon.value?.let { Log.d("Debug", it) }
        //defines exit animation
        val exitAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_out)
        //exitAnimation behavior
        exitAnimation.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {
                //no need to implement
            }
            //starts steamFragment's exitAnimation
            //removes fragment from the backstack
            override fun onAnimationEnd(animation: Animation?){
                if(exit) {
                    parentActivity.steamFragment.root?.startAnimation(parentActivity.steamFragment.exitAnimation)
                    parentActivity.steamFragment.mode = true
                }
                parentActivity.supportFragmentManager.popBackStack("steamConfirmFragment", POP_BACK_STACK_INCLUSIVE)
            }

            override fun onAnimationStart(animation: Animation?) {
                //no need to implement
            }
        })

        buttonNo.setOnClickListener{
            //runs exitAnimation, once animation ends the fragment is removed from the backstack
            root.startAnimation(exitAnimation)
        }

        buttonYes.setOnClickListener {
            exit = true
            root.startAnimation(exitAnimation)
        }
        //TODO I want, whenever this fragment is opened, for an imageView field to fill in
        //TODO with an image which corresponds with repository.playerIcon
        //TODO whenever this fragment is opened, the value which is saved in playerIcon is logged
        //TODO under "parentActivity.gameListViewModel.playerIcon.value "
        return root
    }
}