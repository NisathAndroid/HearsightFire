package com.codewithkael.firebasevideocall.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.codewithkael.firebasevideocall.databinding.ActivityLoginBinding
import com.codewithkael.firebasevideocall.repository.MainRepository
import com.codewithkael.firebasevideocall.utils.getCameraAndMicPermission
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var views: ActivityLoginBinding
    @Inject
    lateinit var mainRepository: MainRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        views = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(views.root)
        views.apply {
            if (Build.BRAND == "samsung")  //samsung - Redmi
            {usernameEt.setText("guest")
                passwordEt.setText("1234")}
            else{
                usernameEt.setText("host")
                passwordEt.setText("1234")
            }
        }
        init()
    }


    private fun init() {
        views.apply {
            btn.setOnClickListener {
                getCameraAndMicPermission {
                    mainRepository.login(
                        usernameEt.text.toString(), passwordEt.text.toString()
                    ) { isDone, reason ->
                        if (!isDone) {
                            Toast.makeText(this@LoginActivity, reason, Toast.LENGTH_SHORT).show()
                        } else {
                            //start moving to our main activity
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
                                putExtra("username", usernameEt.text.toString())
                            })
                        }
                    }
                }

            }
        }
    }
}