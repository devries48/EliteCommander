package com.devries48.elitecommander.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.devries48.elitecommander.R
import kotlinx.android.synthetic.main.fragment_redirect.*

class RedirectFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_redirect, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shrinkImage(loginImageView)
    }

    private fun shrinkImage(view: ImageView) {
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f)
        scaleDownX.duration = 200
        scaleDownY.duration = 200

        val scaleDown = AnimatorSet()
        scaleDown.play(scaleDownX).with(scaleDownY)

        scaleDown.start()
    }
}
