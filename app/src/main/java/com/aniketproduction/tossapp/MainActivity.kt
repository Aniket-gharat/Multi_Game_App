package com.aniketproduction.tossapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.aniketproduction.tossapp.databinding.ActivityMainBinding
import com.aniketproduction.tossapp.fragments.BottleSpinFragment
import com.aniketproduction.tossapp.fragments.CoinTossFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up card click listeners to switch fragments
        binding.cardCoinToss.setOnClickListener {
            supportFragmentManager.commit {
                replace<CoinTossFragment>(R.id.fragment_container_view)
                setReorderingAllowed(true)
                addToBackStack(null)
            }
        }

        binding.cardBottleSpin.setOnClickListener {
            supportFragmentManager.commit {
                replace<BottleSpinFragment>(R.id.fragment_container_view)
                setReorderingAllowed(true)
                addToBackStack(null)
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}