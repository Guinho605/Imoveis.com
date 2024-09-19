package com.inmoveiscom.imoveiscom.activity;

import static com.inmoveiscom.imoveiscom.R.id.txtRedefinir;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.inmoveiscom.imoveiscom.R;
import com.inmoveiscom.imoveiscom.activity.Cadastro;
import com.inmoveiscom.imoveiscom.model.Usuario;
import com.inmoveiscom.imoveiscom.util.ConfiguraçãoDb;

public class Login extends BaseActivity {

    EditText edtLogin, edtSenha;
    Button btnLogar;

    private AlertDialog dialog;

    private AlertDialog noInternetDialog;

    private NetworkChangeReceiver networkChangeReceiver;
    private FirebaseAuth auth;

    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtLogin = findViewById(R.id.edtLogin);
        edtSenha = findViewById(R.id.edtSenha);
        btnLogar = findViewById(R.id.btnLogar);
        auth = ConfiguraçãoDb.Firebaseautenticar();

        showNoInternetDialog();

        networkChangeReceiver = new NetworkChangeReceiver();
        NetworkChangeListenerImpl networkChangeListenerImpl = new NetworkChangeListenerImpl(this);
        networkChangeReceiver.setListener(networkChangeListenerImpl);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


        TextView txtCadastro = findViewById(R.id.txtCadastrar);
        txtCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Cadastro.class);
                startActivity(intent);
                finish();
            }
        });

        TextView txtRedefinir = findViewById(R.id.txtRedefinir);
        txtRedefinir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recuperarSenha();
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        dismissNoInternetDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissNoInternetDialog();
    }

    public void validarLogin(View view){
        String email = edtLogin.getText().toString();
        String senha = edtSenha.getText().toString();

        if (!email.isEmpty()){
            if (!senha.isEmpty()){
                Usuario usuario = new Usuario();

                usuario.setEmail(email);
                usuario.setSenha(senha);

                logar(usuario);

            }else {
                Toast.makeText(this, "Preencha a senha", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Preencha o email", Toast.LENGTH_SHORT).show();
        }
    }
    private void logar(Usuario usuario) {
        auth.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null && user.getEmail().equals("igorsantana1710@gmail.com")) {
                        // Se o e-mail for do administrador, direcione para a tela de administrador
                        abrirAdm();
                    } else {
                        // Se não for o e-mail do administrador, direcione para a tela comum
                        abrirHome();
                    }
                } else {
                    // Lida com falhas no login
                    String excecao = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        excecao = "O usuário não está cadastrado";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        excecao = "O email ou a senha estão incorretos";
                    } catch (Exception e) {
                        excecao = "Erro ao logar o usuario, tente verificar o seu usuário ou senha, se não funcionar tente novamente mais tarde";
                        e.printStackTrace();
                    }
                    Toast.makeText(Login.this, excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void abrirHome() {
        Intent intent = new Intent(Login.this, Home.class);
        startActivity(intent);
    }

    private void abrirAdm() {
        Intent intent = new Intent(Login.this, Adm.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioautent = auth.getCurrentUser();
        if (usuarioautent != null) {
            verificarSeEAdmin(usuarioautent);
        }
    }

    private void verificarSeEAdmin(FirebaseUser user) {
        if (user != null && user.getEmail().equals("igorsantana1710@gmail.com")) {
            abrirAdm();
        } else {
            abrirHome();
        }
    }
    private void recuperarSenha(){
        String email = edtLogin.getText().toString();

        if (email.isEmpty()){
            Toast.makeText(this, "Preencha o email", Toast.LENGTH_SHORT).show();
        } else {
            enviarEmail(email);
        }


    }

    private void enviarEmail(String email){

        auth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getBaseContext(), "Enviamos uma mensagem para o seu email",
                        Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), "Erro ao enviar o email",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}