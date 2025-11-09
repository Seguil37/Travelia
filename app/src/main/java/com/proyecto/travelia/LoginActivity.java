package com.proyecto.travelia;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario;
    private EditText etContrasena;
    private TextView tvOlvido;
    private Button btnIngresar;
    private ImageButton btnGoogle;
    private ImageButton btnFacebook;
    private ImageButton btnOtra;

    private static final String PREFS_NOMBRE = "preferencias_usuario";
    private static final String KEY_USUARIO = "usuario_guardado";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inicializarVistas();
        configurarEventos();
        cargarUsuarioGuardado();
    }

    private void cargarUsuarioGuardado() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NOMBRE, Context.MODE_PRIVATE);
        String usuarioGuardado = prefs.getString(KEY_USUARIO, "");
        if (!usuarioGuardado.isEmpty()) {
            etUsuario.setText(usuarioGuardado);
        }
    }
    private void intentarLogin(){
        String usuario = etUsuario.getText().toString().trim();
        String contrasena = etContrasena.getText().toString();

        // Validaciones
        if (!validaruduario(usuario) || !validarContrasena(contrasena)) {
            return;
        }

        // Aquí va tu lógica real de autenticación (API / Firebase / etc.)
        // POR AHORA: validación local de ejemplo
        if (usuario.equals("admin@gmail.com") && contrasena.equals("1234")) {
            guardarUsuario(usuario);
            Toast.makeText(this, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show();

            // Navegar a la pantalla principal (cambiar MainActivity.class por la tuya)
            Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }

    }
    private boolean validaruduario(String usuario){
        if (TextUtils.isEmpty(usuario)) {
            etUsuario.setError("Ingrese correo electrónico");
            etUsuario.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(usuario).matches()) {
            etUsuario.setError("Ingrese un email válido");
            etUsuario.requestFocus();
            return false;
        }
        etUsuario.setError(null);
        return true;
    }

    private boolean validarContrasena(String contrasena){
        if (TextUtils.isEmpty(contrasena)) {
            etContrasena.setError("Ingrese contraseña");
            etContrasena.requestFocus();
            return false;
        }
        if (contrasena.length() < 4) {
            etContrasena.setError("La contraseña debe tener al menos 4 caracteres");
            etContrasena.requestFocus();
            return false;
        }
        etContrasena.setError(null);
        return true;
    }

    private void guardarUsuario(String usuario){
        SharedPreferences prefs = getSharedPreferences(PREFS_NOMBRE, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USUARIO, usuario).apply();
    }

    private void configurarEventos() {
        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentarLogin();
            }
        });

        tvOlvido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Funcionalidad 'Olvidé mi contraseña' pendiente", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Iniciar sesión con Google (pendiente)", Toast.LENGTH_SHORT).show();
            }
        });

        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Iniciar sesión con Facebook (pendiente)", Toast.LENGTH_SHORT).show();
            }
        });
        btnOtra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Otra opción (pendiente)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void inicializarVistas() {
        etUsuario = findViewById(R.id.et_usuario);
        etContrasena = findViewById(R.id.et_contrasena);
        tvOlvido = findViewById(R.id.tv_olvido);
        btnIngresar = findViewById(R.id.btn_ingresar);
        btnGoogle = findViewById(R.id.btn_google);
        btnFacebook = findViewById(R.id.button3);
        btnOtra = findViewById(R.id.button4);
    }


}