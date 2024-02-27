package com.inmoveiscom.imoveiscom.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inmoveiscom.imoveiscom.R;
import com.inmoveiscom.imoveiscom.adapter.MyAdapter;
import com.inmoveiscom.imoveiscom.adapter.MyAdapterClient;
import com.inmoveiscom.imoveiscom.util.ConfiguraçãoDb;

public class Home extends AppCompatActivity {
    private FirebaseAuth auth;
    private RecyclerView recyclerViewCliente;
    private MyAdapterClient myAdapterclient;
    ProgressBar progressBar;

    Button btnMenu;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String SHOW_TUTORIAL_KEY = "ShowTutorial";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = ConfiguraçãoDb.Firebaseautenticar();


        recyclerViewCliente = findViewById(R.id.recyclerViewCliente);
        // Configura um LinearLayoutManager (ou outro layout manager de sua escolha)
        recyclerViewCliente.setLayoutManager(new LinearLayoutManager(this));
        myAdapterclient = new MyAdapterClient();

        // Configura o adapter para o RecyclerView
        recyclerViewCliente.setAdapter(myAdapterclient);
        btnMenu = findViewById(R.id.btnMenu);
        progressBar = findViewById(R.id.progressBar);
        carregarDadosFirebase();
        showTutorialPopup();
        Log.d("Home", "showTutorialPopup() chamado");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference imoveisRef = database.getReference("imoveis");

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(Home.this, btnMenu);
                // Infla o menu pré-definido
                popupMenu.inflate(R.menu.home_menu);

                // Obtém o item de menu único
                MenuItem menuItem = popupMenu.getMenu().findItem(R.id.btnDeslogar);

                // Define o que acontece quando o item de menu é clicado
                menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Chama a função deslogar()
                        deslogar(v);
                        return true;
                    }
                });

                // Exibe o menu pop-up
                popupMenu.show();
            }
        });


        imoveisRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Limpa o adapter para evitar duplicações
                myAdapterclient.clear();

                // Itera sobre os dados e adiciona ao adapter
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String url = snapshot.child("urlPrincipal").getValue(String.class);
                    String descricao = snapshot.child("descricao").getValue(String.class);
                    String valor = snapshot.child("valor").getValue(String.class);
                    String nodeKey = snapshot.getKey();


                    // Adiciona os dados ao adapter
                    myAdapterclient.addItem(url, descricao, valor, nodeKey);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Lidar com erros de leitura do banco de dados, se necessário
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Verifique se o pop-up do tutorial deve ser exibido
        if (shouldShowTutorial()) {
            showTutorialPopup();
        }
    }

    private boolean shouldShowTutorial() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(SHOW_TUTORIAL_KEY, true);
    }

    private void showTutorialPopup() {
        if (!shouldShowTutorial()) {
            return;
        }

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bem-vindo ao imóveis.com");
        builder.setMessage("Aqui tem um catálogo de todos os nossos imóveis, com uma imagem, uma descrição e o valor de todos os nossos produtos. Se algum dos nossos imóveis te gerar interesse, clique no imóvel desejado para abrir mais informações sobre o produto.");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24); // Ajuste de margens

        CheckBox checkBox = new CheckBox(this);
        checkBox.setText("Não mostrar novamente");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16); // Ajuste de margens
        layout.addView(checkBox, params);

        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean isChecked = checkBox.isChecked();
                if (isChecked) {
                    // Atualize SharedPreferences para não mostrar o pop-up novamente
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(SHOW_TUTORIAL_KEY, false);
                    editor.apply();
                }
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }


    public void deslogar(View view) {
        try {
            auth.signOut();
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void carregarDadosFirebase() {
        // Exibir a barra de progresso
        progressBar.setVisibility(View.VISIBLE);

        // Referência ao seu banco de dados Firebase e recuperar os dados necessários
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("imoveis");
        // Adicionar um listener para recuperar os dados do Firebase
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Limpar dados antigos do RecyclerView
                // Adicionar novos dados ao RecyclerView
                // Esconder a barra de progresso quando os dados forem carregados
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Tratar erros de leitura do Firebase, se necessário
                // Esconder a barra de progresso se ocorrer um erro
                progressBar.setVisibility(View.GONE);
                // Exibir uma mensagem de erro para o usuário
                AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                builder.setTitle("Erro");
                builder.setMessage("Não foi possível carregar os dados. Por favor, tente novamente mais tarde.");
                builder.setPositiveButton("OK", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

}