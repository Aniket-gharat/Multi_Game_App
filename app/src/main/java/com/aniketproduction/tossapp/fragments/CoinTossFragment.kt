package com.aniketproduction.tossapp.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import com.aniketproduction.tossapp.R
import com.aniketproduction.tossapp.databinding.FragmentCoinTossBinding
import kotlin.random.Random

class CoinTossFragment : Fragment() {

    private var _binding: FragmentCoinTossBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoinTossBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnToss.setOnClickListener {
            performRealisticCoinToss()
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun performRealisticCoinToss() {
        // Disable the button during animation
        binding.btnToss.isEnabled = false
        binding.tvResult.text = ""

        // Reset coin position and rotation
        binding.ivCoin.translationY = 0f
        binding.ivCoin.rotationY = 0f
        binding.ivCoin.rotationX = 0f

        // Determine the result
        val isHeads = Random.nextBoolean()
        val numberOfFlips = Random.nextInt(4, 8) // Random flips between 4-7

        // Create the upward movement animation
        val moveUp = ObjectAnimator.ofFloat(binding.ivCoin, "translationY", 0f, -400f).apply {
            duration = 800
            interpolator = DecelerateInterpolator()
        }

        // Create the downward movement animation
        val moveDown = ObjectAnimator.ofFloat(binding.ivCoin, "translationY", -400f, 0f).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Create rotation animation (Y-axis for flipping effect)
        val rotationY = ObjectAnimator.ofFloat(binding.ivCoin, "rotationY", 0f, 360f * numberOfFlips).apply {
            duration = 1600 // Total duration for both up and down
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Create slight X-axis rotation for 3D effect
        val rotationX = ObjectAnimator.ofFloat(binding.ivCoin, "rotationX", 0f, 30f, 0f).apply {
            duration = 1600
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Create the complete animation set
        val animatorSet = AnimatorSet().apply {
            // Play move up, then move down
            play(moveDown).after(moveUp)
            // Play rotations throughout the entire duration
            play(rotationY).with(moveUp)
            play(rotationX).with(moveUp)

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    // Show spinning coin image during animation
                    binding.ivCoin.setImageResource(R.drawable.ic_heads)
                }

                override fun onAnimationEnd(animation: Animator) {
                    // Set final result
                    if (isHeads) {
                        binding.ivCoin.setImageResource(R.drawable.ic_heads)
                        binding.tvResult.text = getString(R.string.heads)
                    } else {
                        binding.ivCoin.setImageResource(R.drawable.ic_tails)
                        binding.tvResult.text = getString(R.string.tails)
                    }

                    // Reset rotations to show final result clearly
                    binding.ivCoin.rotationY = 0f
                    binding.ivCoin.rotationX = 0f

                    // Re-enable the button
                    binding.btnToss.isEnabled = true
                }
            })
        }

        // Start the animation
        animatorSet.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}