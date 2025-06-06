package com.example.myfinance

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.util.Patterns
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
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userLogin: EditText = findViewById(R.id.user_login)
        val userEmail: EditText = findViewById(R.id.user_email)
        val userPass: EditText = findViewById(R.id.user_pass)
        val button: Button = findViewById(R.id.button_reg)
        val linkToAuth: TextView = findViewById(R.id.link_to_auth)

        // Установка фильтров ввода
        userLogin.filters = arrayOf(
            InputFilter.LengthFilter(20),
            InputFilter { source, _, _, _, _, _ ->
                if (source.isNotEmpty() && !Pattern.matches("[a-zA-Z0-9]+", source)) {
                    ""
                } else {
                    null
                }
            }
        )

        userPass.filters = arrayOf(
            InputFilter.LengthFilter(30)
        )

        linkToAuth.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        button.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val email = userEmail.text.toString().trim()
            val pass = userPass.text.toString().trim()

            // Валидация логина
            when {
                login.isEmpty() -> {
                    userLogin.error = "Введите логин"
                    userLogin.requestFocus()
                    return@setOnClickListener
                }
                login.length < 4 -> {
                    userLogin.error = "Логин слишком короткий (мин. 4 символа)"
                    userLogin.requestFocus()
                    return@setOnClickListener
                }
                !isValidLogin(login) -> {
                    userLogin.error = "Только латинские буквы и цифры"
                    userLogin.requestFocus()
                    return@setOnClickListener
                }
            }

            // Валидация email
            when {
                email.isEmpty() -> {
                    userEmail.error = "Введите email"
                    userEmail.requestFocus()
                    return@setOnClickListener
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    userEmail.error = "Введите корректный email"
                    userEmail.requestFocus()
                    return@setOnClickListener
                }
            }

            // Валидация пароля
            when {
                pass.isEmpty() -> {
                    userPass.error = "Введите пароль"
                    userPass.requestFocus()
                    return@setOnClickListener
                }
                pass.length < 6 -> {
                    userPass.error = "Пароль слишком короткий (мин. 6 символов)"
                    userPass.requestFocus()
                    return@setOnClickListener
                }
                pass.length > 30 -> {
                    userPass.error = "Пароль слишком длинный (макс. 30 символов)"
                    userPass.requestFocus()
                    return@setOnClickListener
                }
                !isValidPassword(pass) -> {
                    userPass.error = "Пароль должен содержать буквы и цифры"
                    userPass.requestFocus()
                    return@setOnClickListener
                }
            }

            val user = User(0, login, email, pass)
            val db = AppDatabase.getInstance(this)
            val userDao = db.userDao()

            lifecycleScope.launch {
                try {
                    // Проверяем, существует ли уже такой логин
                    val existingUser = withContext(Dispatchers.IO) {
                        userDao.getUserByLogin(login)
                    }

                    if (existingUser != null) {
                        withContext(Dispatchers.Main) {
                            userLogin.error = "Этот логин уже занят"
                            userLogin.requestFocus()
                            Toast.makeText(
                                this@MainActivity,
                                "Пользователь с таким логином уже существует",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    }

                    // Проверяем, существует ли уже такой email
                    val existingEmail = withContext(Dispatchers.IO) {
                        userDao.getUserByEmail(email)
                    }

                    if (existingEmail != null) {
                        withContext(Dispatchers.Main) {
                            userEmail.error = "Этот email уже зарегистрирован"
                            userEmail.requestFocus()
                            Toast.makeText(
                                this@MainActivity,
                                "Пользователь с таким email уже существует",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@launch
                    }

                    // Если все проверки пройдены - регистрируем пользователя
                    withContext(Dispatchers.IO) {
                        userDao.insert(user)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Пользователь $login успешно зарегистрирован",
                            Toast.LENGTH_LONG
                        ).show()

                        // Переход на экран авторизации
                        val intent = Intent(this@MainActivity, AuthActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Ошибка регистрации: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun isValidLogin(login: String): Boolean {
        val pattern = Pattern.compile("^[a-zA-Z0-9]{4,20}$")
        return pattern.matcher(login).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        // Пароль должен содержать хотя бы одну букву и одну цифру
        val letterPattern = Pattern.compile("[a-zA-Z]")
        val digitPattern = Pattern.compile("[0-9]")
        return letterPattern.matcher(password).find() && digitPattern.matcher(password).find()
    }
}