package entrace

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.countryapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EntranceFragment : Fragment() {
    private lateinit var navController: NavController
    private val viewModel: EntranceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_entrance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        initFirstLaunch()
        initButton(view)
    }

    private fun initFirstLaunch() {
        if (viewModel.isFirstLaunch) {
            viewModel.markFirstLaunchDone()
            navController.navigate(R.id.entrance)
        } else {
            navController.navigate(R.id.action_entrance_to_chooseCountry)
        }
    }

    private fun initButton(view: View) {
        val btn: Button = view.findViewById(R.id.toChoose)
        btn.setOnClickListener {
            navController.navigate(R.id.action_entrance_to_chooseCountry)
        }
    }
}

