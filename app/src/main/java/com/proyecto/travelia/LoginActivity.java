package com.proyecto.travelia;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.proyecto.travelia.data.Constantes;
import com.proyecto.travelia.InicioActivity;


public class LoginActivity extends AppCompatActivity {

    // Vistas
    private EditText etUsuario;
    private EditText etContrasena;
    private TextView tvOlvido;
    private Button btnIngresar;
    private ImageButton btnGoogle, btnFacebook, btnOtra;
    private Button btnAbrirDialogoRegistro;

    // Google & Firebase
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    // Launcher de Google
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this, "Fallo Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 2. Google Options
        // NOTA: Si 'default_web_client_id' sale en rojo, haz BUILD > REBUILD PROJECT.
        // Se genera automáticamente desde tu google-services.json.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // CORREGIR AQUÍ
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        inicializarVistas();
        configurarEventos();
    }
    // --- LÓGICA DE GOOGLE SIGN IN ---

    private void iniciarSesionGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login exitoso
                        FirebaseUser user = mAuth.getCurrentUser();
                        actualizarUIConUsuarioGoogle(user);
                    } else {
                        Toast.makeText(this, "Error de autenticación con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void actualizarUIConUsuarioGoogle(FirebaseUser user) {
        if (user != null) {
            // Guardamos los datos de Google en tus SharedPreferences para que el resto de la app funcione igual
            String email = user.getEmail();
            String nombre = user.getDisplayName();
            // Usamos el UID de Google como "contraseña" interna para que la lógica no falle,
            // aunque el usuario no necesite escribirla.
            String uid = user.getUid();

            SharedPreferences prefs = getSharedPreferences(Constantes.PREFS_USUARIO, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constantes.KEY_EMAIL, email);
            editor.putString(Constantes.KEY_NAME, nombre != null ? nombre : "Viajero");
            editor.putString(Constantes.KEY_PASSWORD, uid); // Guardado técnico
            editor.putBoolean(Constantes.KEY_IS_LOGGED, true);
            editor.apply();

            Toast.makeText(this, "¡Bienvenido " + nombre + "!", Toast.LENGTH_SHORT).show();
            irAInicio();
        }
    }
    private void irAInicio() {
        Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
        startActivity(intent);
        finish();
    }
    private void intentarLogin(){
        String usuarioIngresado = etUsuario.getText().toString().trim();
        String contrasenaIngresada = etContrasena.getText().toString();

        // Validaciones
        if (!validarUsuario(usuarioIngresado) || !validarContrasena(contrasenaIngresada)) {
            return;
        }

        // 1. Obtener la instancia de SharedPreferences
        SharedPreferences prefs = getSharedPreferences(Constantes.PREFS_USUARIO, Context.MODE_PRIVATE);

        // 2. Obtener las credenciales guardadas (valor por defecto es null)
        String savedEmail = prefs.getString(Constantes.KEY_EMAIL, null);
        String savedPassword = prefs.getString(Constantes.KEY_PASSWORD, null);

        // 3. Verificar si existe una cuenta
        if (savedEmail == null) {
            Toast.makeText(this, "No existe una cuenta. Por favor, cree una.", Toast.LENGTH_LONG).show();
            return;
        }

        // 4. Verificar credenciales ingresadas contra las guardadas
        if (usuarioIngresado.equals(savedEmail) && contrasenaIngresada.equals(savedPassword)) {

            Toast.makeText(this, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show();

            prefs.edit().putBoolean(Constantes.KEY_IS_LOGGED, true).apply();

            // Navegar a la pantalla principal
            Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }
    private void mostrarDialogoRegistro() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_registro, null);
        builder.setView(dialogView);

        // Obtener los campos del diálogo
        EditText etDialogNombre = dialogView.findViewById(R.id.et_dialog_nombre);
        EditText etDialogEmail = dialogView.findViewById(R.id.et_dialog_email);
        EditText etDialogPassword = dialogView.findViewById(R.id.et_dialog_password);
        Button btnDialogGuardar = dialogView.findViewById(R.id.btn_dialog_guardar);

        AlertDialog dialog = builder.create();

        btnDialogGuardar.setOnClickListener(v -> {
            String nombre = etDialogNombre.getText().toString().trim();
            String email = etDialogEmail.getText().toString().trim();
            String password = etDialogPassword.getText().toString();
            // Validaciones (en línea)
            if (TextUtils.isEmpty(nombre)) {
                etDialogNombre.setError("Debe ingresar un nombre");
                return;
            }
            if (!validarUsuario(email, etDialogEmail)) { // Usamos la validación adaptada
                return;
            }
            if (!validarContrasena(password, etDialogPassword)) { // Usamos la validación adaptada
                return;
            }

            // Guardar las nuevas credenciales en SharedPreferences
            SharedPreferences prefs = getSharedPreferences(Constantes.PREFS_USUARIO, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString(Constantes.KEY_EMAIL, email);
            editor.putString(Constantes.KEY_PASSWORD, password);
            editor.putString(Constantes.KEY_NAME, nombre); // Guardamos el nombre para el Perfil
            editor.apply();

            Toast.makeText(this, "Cuenta creada con éxito. Ya puede iniciar sesión.", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialog.show();
    }
    private boolean validarUsuario(String usuario, EditText campoTexto) {
        if (TextUtils.isEmpty(usuario)) {
            campoTexto.setError("Ingrese correo electrónico");
            campoTexto.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(usuario).matches()) {
            campoTexto.setError("Ingrese un email válido");
            campoTexto.requestFocus();
            return false;
        }
        campoTexto.setError(null);
        return true;
    }

    private boolean validarUsuario(String usuario) {
        return validarUsuario(usuario, etUsuario);
    }

    private boolean validarContrasena(String contrasena, EditText campoTexto){
        if (TextUtils.isEmpty(contrasena)) {
            campoTexto.setError("Ingrese contraseña");
            campoTexto.requestFocus();
            return false;
        }
        if (contrasena.length() < 4) {
            campoTexto.setError("La contraseña debe tener al menos 4 caracteres");
            campoTexto.requestFocus();
            return false;
        }
        campoTexto.setError(null);
        return true;
    }
    private boolean validarContrasena(String contrasena) {
        return validarContrasena(contrasena, etContrasena);
    }
    private void configurarEventos() {
        btnIngresar.setOnClickListener(v -> intentarLogin());

        // Evento del nuevo botón
        btnAbrirDialogoRegistro.setOnClickListener(v -> mostrarDialogoRegistro());

        tvOlvido.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Funcionalidad 'Olvidé mi contraseña' pendiente", Toast.LENGTH_SHORT).show()
        );

        btnGoogle.setOnClickListener(v -> iniciarSesionGoogle());

        btnFacebook.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Iniciar sesión con Facebook (pendiente)", Toast.LENGTH_SHORT).show()
        );
        btnOtra.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Otra opción (pendiente)", Toast.LENGTH_SHORT).show()
        );
    }

    private void inicializarVistas() {
        etUsuario = findViewById(R.id.et_usuario);
        etContrasena = findViewById(R.id.et_contrasena);
        tvOlvido = findViewById(R.id.tv_olvido);
        btnIngresar = findViewById(R.id.btn_ingresar);
        btnGoogle = findViewById(R.id.btn_google);
        btnFacebook = findViewById(R.id.button3);
        btnOtra = findViewById(R.id.button4);

        // Inicializar el nuevo botón
        btnAbrirDialogoRegistro = findViewById(R.id.btn_abrir_dialogo_registro);
    }
}