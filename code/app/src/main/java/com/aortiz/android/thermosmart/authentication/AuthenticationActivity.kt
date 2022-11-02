package com.aortiz.android.thermosmart.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aortiz.android.thermosmart.databinding.ActivityAuthenticationBinding
import com.aortiz.android.thermosmart.thermostat.MainActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AuthenticationActivity : AppCompatActivity() {

    val viewModel: AuthenticationViewModel by viewModel()

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { onSignInResult(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityAuthenticationBinding =
            ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = viewModel
        viewModel.authenticationState.observe(this) { authenticationState ->
            Timber.i("authenticationState changed $authenticationState")
            if (authenticationState == AuthenticationViewModel.AuthenticationState.AUTHENTICATED) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        binding.loginButton.setOnClickListener { launchSignInFlow() }
    }

    private fun launchSignInFlow() {
        signInLauncher.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(
                    arrayListOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build()
                    )
                )
                .setIsSmartLockEnabled(false)
                .build()
        )
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            Timber.i("Successfully signed in user " + "${FirebaseAuth.getInstance().currentUser?.displayName}!")
        } else {
            Timber.i("Sign in unsuccessful ${result.resultCode}")
        }
    }
}
