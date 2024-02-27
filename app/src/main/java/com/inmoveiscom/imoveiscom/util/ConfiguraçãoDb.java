package com.inmoveiscom.imoveiscom.util;

import com.google.firebase.auth.FirebaseAuth;

public class ConfiguraçãoDb {

    private static FirebaseAuth auth;

    public static FirebaseAuth Firebaseautenticar(){
        if (auth == null){
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }
}
