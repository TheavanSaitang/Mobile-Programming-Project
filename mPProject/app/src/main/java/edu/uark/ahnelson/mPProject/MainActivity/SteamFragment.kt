package edu.uark.ahnelson.mPProject.MainActivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import edu.uark.ahnelson.mPProject.R
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.commit


class SteamFragment : Fragment() {
    var mode:Boolean = false
    var confirmMode: Boolean = false
    var root: View? = null
    var exitAnimation: Animation? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        root = inflater.inflate(R.layout.fragment_steam_login, container, false)
        val parentActivity = activity as MainActivity
        val inputUsername = root?.findViewById<EditText>(R.id.inputUsername)!!
        confirmMode = false
        mode = false
        exitAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_out)
        //defines exit animation
        //exitAnimation behavior
        exitAnimation?.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {
                //no need to implement
            }
            //removes fragment from the backstack
            override fun onAnimationEnd(animation: Animation?){
                parentActivity.supportFragmentManager.popBackStack("steamFragment", POP_BACK_STACK_INCLUSIVE)
                if(mode) parentActivity.getSteamGames()
            }

            override fun onAnimationStart(animation: Animation?) {
                //no need to implement
            }
        })
        val buttonCancel:Button = root?.findViewById(R.id.btnCancel)!!
        buttonCancel.setOnClickListener{
            //runs exitAnimation, once animation ends the fragment is removed from the backstack
            root?.startAnimation(exitAnimation)
        }
        val buttonSubmit = root?.findViewById<Button>(R.id.btnSubmit)!!
        buttonSubmit.setOnClickListener {
            parentActivity.getSteamUser(inputUsername.text.toString())
            confirmMode = true
        }
        parentActivity.gameListViewModel.userInfoComplete.observe(viewLifecycleOwner){it ->
            if(it && confirmMode){
                val steamConfirmFragment = SteamConfirmFragment()
                parentFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out
                    )
                    replace(R.id.fragment_container_view_2, steamConfirmFragment, "steamConfirmFragment")
                    addToBackStack("steamConfirmFragment")
                }
            }

        }
        return root
    }
}