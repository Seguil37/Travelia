package com.proyecto.travelia;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.proyecto.travelia.data.Constantes;
import com.proyecto.travelia.ui.Usuario;

public class BaseActivity extends AppCompatActivity {

    protected void setupTopBar() {
        ImageView profileImage = findViewById(R.id.iv_profile_pic);
        if (profileImage == null) return;

        SharedPreferences prefs = getSharedPreferences(Constantes.PREFS_USUARIO, Context.MODE_PRIVATE);
        String profileUri = prefs.getString("profile_image_uri", null);

        if (profileUri != null && !profileUri.isEmpty()) {
            Glide.with(this).load(Uri.parse(profileUri)).circleCrop().into(profileImage);
        } else {
            Glide.with(this).load(R.drawable.ic_usuario).circleCrop().into(profileImage);
        }

        profileImage.setOnClickListener(view -> {
            Intent intent = new Intent(this, Usuario.class);
            startActivity(intent);
        });
    }
}
