package com.example.myfinance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val userLogin: EditText = findViewById(R.id.user_login_auth)
        val userPass: EditText = findViewById(R.id.user_pass_auth)
        val button: Button = findViewById(R.id.button_auth)

        val linkToReg: TextView = findViewById(R.id.link_to_reg)

        linkToReg.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        button.setOnClickListener{
            val login = userLogin.text.toString().trim()
            val pass = userPass.text.toString().trim()

            if (login == "" || pass == "")
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()
            else {
                val db = AppDatabase.getInstance(this)
                val userDao = db.userDao()
                lifecycleScope.launch {
                    val user = userDao.getUserByLogin(login)
                    if(user != null && user.password == pass) {
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@AuthActivity, "Пользователь $login авторизован", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@AuthActivity, MainScreenActivity::class.java)
                            intent.putExtra("user_id", user.id)  // Передаем ID пользователя
                            intent.putExtra("user_login", login)
                            startActivity(intent)
                            userLogin.text.clear()
                            userPass.text.clear()
                        }
                    } else {
                        Toast.makeText(this@AuthActivity, "Пользователь $login НЕ авторизован", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}