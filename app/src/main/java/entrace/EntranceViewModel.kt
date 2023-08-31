package entrace

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EntranceViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    val isFirstLaunch: Boolean
        get() = sharedPreferences.getBoolean("isFirstLaunch", true)

    fun markFirstLaunchDone() {
        sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
    }
}