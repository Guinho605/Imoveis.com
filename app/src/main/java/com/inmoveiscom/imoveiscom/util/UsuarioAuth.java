package com.inmoveiscom.imoveiscom.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UsuarioAuth {

    public static FirebaseUser usuarioLogado(){
        FirebaseAuth usuario = ConfiguraçãoDb.Firebaseautenticar();
        return usuario.getCurrentUser();
    }
}
