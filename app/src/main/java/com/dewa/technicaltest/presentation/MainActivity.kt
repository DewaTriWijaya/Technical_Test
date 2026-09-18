package com.dewa.technicaltest.presentation

import android.os.Bundle
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dewa.technicaltest.data.remote.RetrofitClient
import com.dewa.technicaltest.data.repository.UserRepository
import com.dewa.technicaltest.databinding.ActivityMainBinding
import com.dewa.technicaltest.data.model.User
import com.dewa.technicaltest.presentation.user.UserAdapter
import com.dewa.technicaltest.presentation.user.UserUiState
import com.dewa.technicaltest.presentation.user.UserViewModel
import com.dewa.technicaltest.presentation.user.UserViewModelFactory
import com.dewa.technicaltest.utils.getActiveAdultUserNames
import com.dewa.technicaltest.utils.searchUsers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userAdapter: UserAdapter
    private var allUsers: List<User> = emptyList()

    private val viewModel: UserViewModel by viewModels {
        UserViewModelFactory(
            UserRepository(
                RetrofitClient.userApi
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        observeUiState()
        viewModel.getUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter()
        binding.recyclerView.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(
                    query: String?
                ): Boolean {
                    filterUsers(query.orEmpty())
                    return true
                }

                override fun onQueryTextChange(
                    newText: String?
                ): Boolean {
                    filterUsers(newText.orEmpty())
                    return true
                }
            }
        )
    }

    private fun filterUsers(
        keyword: String
    ) {
        val filteredUsers =
            searchUsers(
                users = allUsers,
                keyword = keyword
            )

        userAdapter.submitList(filteredUsers)
    }


    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        UserUiState.Loading -> { showLoading() }
                        is UserUiState.Success -> { showSuccess(state.users) }
                        is UserUiState.Error -> { showError(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.recyclerView.isVisible = false
        binding.tvError.isVisible = false
    }

    private fun showSuccess(
        users: List<User>
    ) {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = true
        binding.tvError.isVisible = false

        allUsers = users

        val activeAdultNames = getActiveAdultUserNames(users)
        binding.tvActiveAdultUsers.text = activeAdultNames.joinToString(separator = "\n")

        userAdapter.submitList(
            users.sortedBy { it.name }
        )
    }

    private fun showError(
        message: String
    ) {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = false
        binding.tvError.isVisible = true
        binding.tvError.text = message
    }
}