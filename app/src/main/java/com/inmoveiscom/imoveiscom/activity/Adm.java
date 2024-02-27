package com.inmoveiscom.imoveiscom.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inmoveiscom.imoveiscom.R;
import com.inmoveiscom.imoveiscom.adapter.MyAdapter;
import com.inmoveiscom.imoveiscom.util.ConfiguraçãoDb;

public class Adm extends AppCompatActivity {

    private Button btnAdd, btnDeslogar;
    private DatabaseReference databaseRef;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;

    private FirebaseAuth auth;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Adm", "Activity Adm criada");
        setContentView(R.layout.activity_adm);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseRef = FirebaseDatabase.getInstance().getReference("imoveis");
        myAdapter = new MyAdapter();
        btnAdd = findViewById(R.id.btnAdd2);
        btnDeslogar = findViewById(R.id.btnDeslogarAdm);
        progressBar = findViewById(R.id.progressBar);

        auth = ConfiguraçãoDb.Firebaseautenticar();

        recyclerView.setAdapter(myAdapter);
        carregarDadosFirebase();


        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myAdapter.clear();

                // Itere sobre os dados e adicione ao adapter
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String url = snapshot.child("urlPrincipal").getValue(String.class);
                    String descricao = snapshot.child("descricao").getValue(String.class);
                    String valor = snapshot.child("valor").getValue(String.class);
                    String nodeKey = snapshot.getKey();
                    // Adicione os dados ao adapter
                    myAdapter.addItem(url, descricao, valor, nodeKey);
                    Log.d("TAG", "URL: " + url);
                    Log.d("TAG", "Descrição: " + descricao);
                    Log.d("TAG", "Node Key: " + nodeKey);
                }

                // Notifique o adapter sobre as mudanças nos dados
                myAdapter.notifyDataSetChanged();

                Log.d("TAG", "Adapter notificado sobre as mudanças nos dados");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Lidar com erros de leitura do banco de dados, se necessário
                Log.e("TAG", "Erro ao ler dados do Firebase", databaseError.toException());
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Adm", "Botão 'Adicionar' clicado");
                Intent intent = new Intent(Adm.this, add.class);
                startActivity(intent);
                finish();
            }
        });

        btnDeslogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deslogarAdm();
            }
        });

        myAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String nodeKey = myAdapter.getNodeKey(position);
                String imageUrl = myAdapter.getUrl(position);
                String descricao = myAdapter.getDescricao(position);
                String valor = myAdapter.getValor(position);

                Intent intent = new Intent(Adm.this, Editar.class);
                intent.putExtra("nodeKey", nodeKey);
                intent.putExtra("urls", imageUrl);
                intent.putExtra("descricao", descricao);
                intent.putExtra("valor", valor);
                startActivityForResult(intent, 1); // Inicia a atividade de edição com um código de solicitação
                finish();
            }
        });
    }

        @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String nodeKey = data.getStringExtra("nodeKey");
            String novaDescricao = data.getStringExtra("nova_descricao");
            String novoValor = data.getStringExtra("novo_valor");
            String novaUrl = data.getStringExtra("nova_url");

            // Atualiza os dados na posição correspondente do RecyclerView
            if (nodeKey != null && novaDescricao != null && novaUrl != null) {
                myAdapter.updateItem(nodeKey, novaDescricao, novoValor, novaUrl);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Atualize o RecyclerView aqui
        if (myAdapter != null) {
            myAdapter.notifyDataSetChanged();
        }
    }

    public void deslogarAdm(){
        try {
            auth.signOut();
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void carregarDadosFirebase() {
        // Exibir a barra de progresso
        progressBar.setVisibility(View.VISIBLE);

        // Referência ao seu banco de dados Firebase e recuperar os dados necessários
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("imoveis");
        // Adicionar um listener para recuperar os dados do Firebase
        databaseRef.addValueEventListener(new ValueEventListener() {
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
            }
        });
    }
}