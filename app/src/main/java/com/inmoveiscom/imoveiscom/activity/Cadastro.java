package com.inmoveiscom.imoveiscom.activity;

import static com.inmoveiscom.imoveiscom.R.id.txtLogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.inmoveiscom.imoveiscom.R;
import com.inmoveiscom.imoveiscom.model.Usuario;
import com.inmoveiscom.imoveiscom.util.ConfiguraçãoDb;
import com.inmoveiscom.imoveiscom.util.ConnectivityUtils;

import java.util.Random;

public class Cadastro extends BaseActivity {

    Usuario usuario;
    FirebaseAuth autenticacao;
    FirebaseDatabase database;
    EditText txtNome, txtEmail, txtSenha;
    Button btnCadastrar, btnCadastrarG;
    private NetworkChangeReceiver networkChangeReceiver;
    private GoogleSignInClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        txtNome = findViewById(R.id.txtNome);
        txtEmail = findViewById(R.id.txtEmail);
        txtSenha = findViewById(R.id.txtSenha);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        usuario = new Usuario();
        autenticacao = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://imoveis-com-f10da-default-rtdb.firebaseio.com/");
        btnCadastrarG = findViewById(R.id.btnCadastrarG);

        showNoInternetDialog();

        networkChangeReceiver = new NetworkChangeReceiver();
        NetworkChangeListenerImpl networkChangeListenerImpl = new NetworkChangeListenerImpl(this);
        networkChangeReceiver.setListener(networkChangeListenerImpl);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(this, options);
        TextView txtLogin = findViewById(R.id.txtLogin);
        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Cadastro.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        btnCadastrarG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = client.getSignInIntent();
                startActivityForResult(i, 123);
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


    public void validar(View view) {
        String nome = txtNome.getText().toString();
        String email = txtEmail.getText().toString();
        String senha = txtSenha.getText().toString();

        if (!nome.isEmpty() && !email.isEmpty() && !senha.isEmpty()) {
            usuario.setNome(nome);
            usuario.setEmail(email);
            usuario.setSenha(senha);
            cadastrarUsuario();
        } else {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
        }
    }

    private void cadastrarUsuario() {
        autenticacao = ConfiguraçãoDb.Firebaseautenticar();

        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Cadastro.this, "Sucesso ao cadastrar o usuário", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Cadastro.this, Home.class);
                    startActivity(intent);
                } else {
                    String excecao = "";

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        excecao = "Digite uma senha mais forte";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        excecao = "Digite um endereço de email válido";
                    } catch (FirebaseAuthUserCollisionException e) {
                        excecao = "Esta conta já existe";
                    } catch (Exception e) {
                        excecao = "Erro ao cadastrar o usuário: " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(Cadastro.this, excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}