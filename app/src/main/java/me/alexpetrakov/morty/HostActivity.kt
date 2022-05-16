package me.alexpetrakov.morty

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Replace
import com.github.terrakok.cicerone.androidx.AppNavigator
import dagger.hilt.android.AndroidEntryPoint
import me.alexpetrakov.morty.databinding.ActivityHostBinding
import javax.inject.Inject

@AndroidEntryPoint
class HostActivity : AppCompatActivity() {

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    private val navigator = AppNavigator(this, R.id.fragment_container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHostBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            navigator.applyCommands(arrayOf(Replace(AppScreens.characters())))
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }
}