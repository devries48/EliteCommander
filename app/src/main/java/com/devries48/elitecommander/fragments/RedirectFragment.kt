package com.devries48.elitecommander.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.devries48.elitecommander.databinding.FragmentRedirectBinding

class RedirectFragment : Fragment() {

    private lateinit var mBinding: FragmentRedirectBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentRedirectBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shrinkImage(mBinding.loginImageView)
    }

    private fun shrinkImage(view: ImageView) {
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f)
        scaleDownX.duration = 2000
        scaleDownY.duration = 2000

        val scaleDown = AnimatorSet()
        scaleDown.play(scaleDownX).with(scaleDownY)

        scaleDown.start()
    }
}
