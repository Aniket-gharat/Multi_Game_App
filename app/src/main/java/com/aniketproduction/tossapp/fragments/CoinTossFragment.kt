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
    private var isHeadsResult: Boolean = true // Store the final determined result

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

        // Set camera distance for a realistic 3D rotation perspective
        val scale = resources.displayMetrics.density
        binding.ivCoin.cameraDistance = 8000 * scale // Adjust as needed for desired perspective

        // Reset coin position and rotation to a clean start
        binding.ivCoin.translationY = 0f
        binding.ivCoin.rotationY = 0f
        binding.ivCoin.rotationX = 0f // Also reset X rotation

        // Determine the result for this toss
        isHeadsResult = Random.nextBoolean()
        val numberOfFlips = Random.nextInt(5, 10) // Random flips between 5-9 for a good spin

        // Upward movement animation
        val moveUp = ObjectAnimator.ofFloat(binding.ivCoin, "translationY", 0f, -600f).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
        }

        // Downward movement animation
        val moveDown = ObjectAnimator.ofFloat(binding.ivCoin, "translationY", -600f, 0f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Y-axis rotation (flipping effect)
        val rotationY = ObjectAnimator.ofFloat(binding.ivCoin, "rotationY", 0f, 360f * numberOfFlips).apply {
            duration = 1200 // Total duration for the main flip
            interpolator = AccelerateDecelerateInterpolator()

            // Listener to swap image resource during rotation
            addUpdateListener { animator ->
                val currentRotation = animator.animatedValue as Float
                // Normalize rotation to 0-360 degrees
                val normalizedRotation = currentRotation % 360

                // If the "back" of the coin is facing us (roughly between 90 and 270 degrees)
                if (normalizedRotation > 90 && normalizedRotation < 270) {
                    binding.ivCoin.setImageResource(R.drawable.tails)
                } else {
                    binding.ivCoin.setImageResource(R.drawable.heads)
                }
            }
        }

        // Combine all toss animations
        val tossAnimatorSet = AnimatorSet().apply {
            // Play rotation with upward movement, then downward movement
            play(rotationY)
            play(moveDown).after(moveUp)

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // After the main toss animation, start the settling animation
                    settleCoin()
                }
            })
        }

        // Start the main toss animation
        tossAnimatorSet.start()
    }

    private fun settleCoin() {
        // Calculate the target rotation to show the correct result (0 for heads, 180 for tails)
        // We calculate the shortest path to get there from the current rotation.
        val currentRotationY = binding.ivCoin.rotationY % 360 // Get current rotation from 0 to 360
        val targetRotationY = if (isHeadsResult) 0f else 180f

        var rotationDifference = targetRotationY - currentRotationY
        // Adjust difference to take the shortest path (e.g., from 350 to 0 is +10, not -350)
        if (rotationDifference > 180) rotationDifference -= 360
        if (rotationDifference < -180) rotationDifference += 360

        val finalRotationValue = binding.ivCoin.rotationY + rotationDifference

        val settleAnimator = ObjectAnimator.ofFloat(binding.ivCoin, "rotationY", finalRotationValue).apply {
            duration = 250 // Quick, smooth settle
            interpolator = DecelerateInterpolator()

            addUpdateListener { animator ->
                val currentRotation = animator.animatedValue as Float
                val normalizedRotation = currentRotation % 360

                if (normalizedRotation > 90 && normalizedRotation < 270) {
                    binding.ivCoin.setImageResource(R.drawable.tails)
                } else {
                    binding.ivCoin.setImageResource(R.drawable.heads)
                }
            }
        }

        settleAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Ensure the final image is correctly set after settling
                if (isHeadsResult) {
                    binding.ivCoin.setImageResource(R.drawable.heads)
                    binding.tvResult.text = getString(R.string.heads)
                } else {
                    binding.ivCoin.setImageResource(R.drawable.tails)
                    binding.tvResult.text = getString(R.string.tails)
                }

                // Explicitly set rotationY to 0 to ensure it's perfectly flat
                // (or 180 if you want tails to be at 180, though 0 for heads/tails is simpler if just image changes)
                binding.ivCoin.rotationY = 0f
                binding.ivCoin.rotationX = 0f // Reset X rotation as well if it was used

                // Re-enable the button
                binding.btnToss.isEnabled = true
            }
        })
        settleAnimator.start()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}