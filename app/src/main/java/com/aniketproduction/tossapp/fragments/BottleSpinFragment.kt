package com.aniketproduction.tossapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aniketproduction.tossapp.R
import com.aniketproduction.tossapp.databinding.FragmentBottleSpinBinding
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class BottleSpinFragment : Fragment() {

    private var _binding: FragmentBottleSpinBinding? = null
    private val binding get() = _binding!!

    private var lastAngle = 0f
    private val playerNames = mutableListOf<String>()
    private lateinit var namesAdapter: NamesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottleSpinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.btnAddNames.setOnClickListener {
            showAddNamesDialog()
        }

        binding.btnSpin.setOnClickListener {
            if (playerNames.isEmpty()) {
                Toast.makeText(context, "Please add player names first", Toast.LENGTH_SHORT).show()
            } else {
                spinBottle()
            }
        }

        binding.btnClearNames.setOnClickListener {
            playerNames.clear()
            updateNamesDisplay()
            updateNamePositions()
        }
    }

    private fun setupRecyclerView() {
        namesAdapter = NamesAdapter(playerNames) { position ->
            playerNames.removeAt(position)
            updateNamesDisplay()
            updateNamePositions()
        }
        binding.rvPlayerNames.layoutManager = LinearLayoutManager(context)
        binding.rvPlayerNames.adapter = namesAdapter
    }

    private fun showAddNamesDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_names, null)
        val editTextNames = dialogView.findViewById<android.widget.EditText>(R.id.et_names)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Player Names")
            .setMessage("Enter names separated by commas")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val names = editTextNames.text.toString().split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                if (names.isNotEmpty()) {
                    playerNames.addAll(names)
                    updateNamesDisplay()
                    updateNamePositions()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateNamesDisplay() {
        namesAdapter.notifyDataSetChanged()
        binding.tvPlayersCount.text = "Players: ${playerNames.size}"
    }

    private fun updateNamePositions() {
        // Clear existing name TextViews
        binding.gameContainer.removeAllViews()
        binding.gameContainer.addView(binding.ivBottle)

        if (playerNames.isEmpty()) return

        val containerWidth = binding.gameContainer.width
        val containerHeight = binding.gameContainer.height

        if (containerWidth == 0 || containerHeight == 0) {
            // If container dimensions aren't available yet, post to run later
            binding.gameContainer.post { updateNamePositions() }
            return
        }

        val centerX = containerWidth / 2f
        val centerY = containerHeight / 2f
        val radius = minOf(containerWidth, containerHeight) / 3f

        playerNames.forEachIndexed { index, name ->
            val angle = (360.0 / playerNames.size) * index
            val radians = Math.toRadians(angle - 90) // -90 to start from top (12 o'clock)

            val x = centerX + (radius * cos(radians)).toFloat()
            val y = centerY + (radius * sin(radians)).toFloat()

            val nameTextView = TextView(context).apply {
                text = name
                textSize = 16f
                setTextColor(resources.getColor(R.color.dark_blue, null))
                background = resources.getDrawable(R.drawable.name_background, null)
                setPadding(16, 8, 16, 8)
            }

            val layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            binding.gameContainer.addView(nameTextView, layoutParams)

            // Position the TextView
            nameTextView.post {
                nameTextView.x = x - nameTextView.width / 2f
                nameTextView.y = y - nameTextView.height / 2f
            }
        }
    }

    private fun spinBottle() {
        val targetPlayerIndex = Random.nextInt(playerNames.size)
        val anglePerPlayer = 360f / playerNames.size
        val targetAngle = anglePerPlayer * targetPlayerIndex
        val newAngle = targetAngle + Random.nextFloat() * 20 - 10 + 1800 // Add randomness and multiple spins

        val rotateAnimation = RotateAnimation(
            lastAngle, newAngle,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 3000 // 3 seconds
            interpolator = DecelerateInterpolator()
            fillAfter = true
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    binding.btnSpin.isEnabled = false
                    binding.tvResult.text = ""
                }

                override fun onAnimationEnd(animation: Animation?) {
                    binding.btnSpin.isEnabled = true
                    val selectedPlayer = playerNames[targetPlayerIndex]
                    binding.tvResult.text = "Selected: $selectedPlayer"
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }

        lastAngle = newAngle % 360
        binding.ivBottle.startAnimation(rotateAnimation)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Simple adapter for the names RecyclerView
class NamesAdapter(
    private val names: MutableList<String>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<NamesAdapter.NameViewHolder>() {

    class NameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.tv_name)
        val deleteButton: View = view.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player_name, parent, false)
        return NameViewHolder(view)
    }

    override fun onBindViewHolder(holder: NameViewHolder, position: Int) {
        holder.nameText.text = names[position]
        holder.deleteButton.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount() = names.size
}