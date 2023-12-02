package edu.uark.ahnelson.mPProject.MainActivity

import android.animation.Animator
import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import edu.uark.ahnelson.mPProject.R
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_CLOSE
import androidx.fragment.app.commit


class SteamFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_steam_login, container, false)
        val inputUsername = root.findViewById<EditText>(R.id.inputUsername)
        //defines exit animation
        val exitAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_out)
        //exitAnimation behavior
        exitAnimation.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {
                //no need to implement
            }
            //removes fragment from the backstack
            override fun onAnimationEnd(animation: Animation?){
                activity?.supportFragmentManager?.popBackStack("steamFragment", POP_BACK_STACK_INCLUSIVE)
            }

            override fun onAnimationStart(animation: Animation?) {
                //no need to implement
            }
        })
        val buttonCancel = root.findViewById<Button>(R.id.btnCancel)
        buttonCancel.setOnClickListener{
            //runs exitAnimation, once animation ends the fragment is removed from the backstack
            root.startAnimation(exitAnimation)
        }
        //TODO MAKE SUBMIT ACTUALLY SUBMIT TO SOMEWHERE
        val buttonSubmit = root.findViewById<Button>(R.id.btnSubmit)
        buttonSubmit.setOnClickListener {
            val act: MainActivity = activity as MainActivity
            act.getSteamInfo(inputUsername.text.toString())
            root.startAnimation(exitAnimation)
        }
        return root
    }
}