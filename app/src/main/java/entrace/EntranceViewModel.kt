package entrace

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class EntranceViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    val isFirstLaunchFlow: Flow<Boolean> = flow {
        emit(sharedPreferences.getBoolean("isFirstLaunch", true))
    }

    fun markFirstLaunchDone() {
        sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
    }
}
