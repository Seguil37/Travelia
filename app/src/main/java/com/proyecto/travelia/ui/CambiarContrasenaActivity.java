package com.proyecto.travelia.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.proyecto.travelia.data.Constantes;

import com.proyecto.travelia.R;

public class CambiarContrasenaActivity extends AppCompatActivity {
    private EditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private Button btnSaveNewPassword;
    private SharedPreferences sharedPreferences; //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cambiar_contrasena);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sharedPreferences = getSharedPreferences(Constantes.PREFS_USUARIO, Context.MODE_PRIVATE);

        initViews();
        setupListeners();
    }
    private void initViews() {
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmNewPassword = findViewById(R.id.et_confirm_new_password);
        btnSaveNewPassword = findViewById(R.id.btn_save_new_password);
    }
    private void setupListeners() {
        btnSaveNewPassword.setOnClickListener(v -> attemptPasswordChange());
    }
    private void attemptPasswordChange() {
        String currentPass = etCurrentPassword.getText().toString();
        String newPass = etNewPassword.getText().toString();
        String confirmPass = etConfirmNewPassword.getText().toString();

        // 1. Validaciones de campos (vacío, coincidencia, etc.)
        if (newPass.isEmpty() || confirmPass.isEmpty() || currentPass.isEmpty()) {
            Toast.makeText(this, "Todos los campos deben estar llenos.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Las nuevas contraseñas no coinciden.", Toast.LENGTH_LONG).show();
            etNewPassword.setError("No coinciden");
            etConfirmNewPassword.setError("No coinciden");
            return;
        }
        if (newPass.equals(currentPass)) {
            Toast.makeText(this, "La nueva contraseña no puede ser igual a la actual.", Toast.LENGTH_LONG).show();
            etNewPassword.setError("Contraseña idéntica");
            return;
        }

        // 2. VERIFICACIÓN DE SEGURIDAD (El paso que estaba fallando)

        // Obtenemos la contraseña guardada (la que se creó en el registro)
        // Usamos 'null' como valor por defecto.
        String savedPass = sharedPreferences.getString(Constantes.KEY_PASSWORD, null);

        // 3. Comprobar si la contraseña guardada existe Y si coincide
        if (savedPass == null || !currentPass.equals(savedPass)) {
            // Si savedPass es nulo (nunca debería pasar si el usuario está logueado)
            // O si la contraseña actual que escribió el usuario NO es igual a la guardada.

            Toast.makeText(this, "La Contraseña Actual es incorrecta.", Toast.LENGTH_LONG).show();
            etCurrentPassword.setError("Contraseña incorrecta");
            return; // ¡Detenemos el proceso!
        }

        // 4. Si la verificación es exitosa, guardar la nueva contraseña
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constantes.KEY_PASSWORD, newPass);
        editor.apply();

        Toast.makeText(this, "Contraseña cambiada con éxito.", Toast.LENGTH_LONG).show();
        finish(); // Regresar a la pantalla de Perfil (Usuario.java)
    }
}