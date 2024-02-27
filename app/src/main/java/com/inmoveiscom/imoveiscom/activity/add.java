package com.inmoveiscom.imoveiscom.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inmoveiscom.imoveiscom.R;
import com.inmoveiscom.imoveiscom.adapter.MyAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class add extends BaseActivity {

    ImageView Add;
    EditText edtDesc, edtValor;

    Button btnAdi;
    private List<Uri> listaDeImagens = new ArrayList<>();

    private Uri imagemSelecionada;

    private int indiceAtual = 0;

    private String descricaoImovel, valorImovel;
    // Firebase
    private MyAdapter myAdapter;
    private NetworkChangeReceiver networkChangeReceiver;



    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Add = findViewById(R.id.Add);
        edtDesc = findViewById(R.id.edtDesc);
        edtValor = findViewById(R.id.edtValor);
        btnAdi = findViewById(R.id.btnAdi);

        showNoInternetDialog();

        networkChangeReceiver = new NetworkChangeReceiver();
        NetworkChangeListenerImpl networkChangeListenerImpl = new NetworkChangeListenerImpl(this);
        networkChangeReceiver.setListener(networkChangeListenerImpl);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirGaleria();
            }
        });
    }

    public void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Permitir múltiplas seleções
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) { // Verifica se há várias imagens selecionadas
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imagemUri = data.getClipData().getItemAt(i).getUri();
                    listaDeImagens.add(imagemUri);
                }
            } else if (data.getData() != null) { // Se apenas uma imagem for selecionada
                Uri imagemUri = data.getData();
                listaDeImagens.add(imagemUri);
            }
            // Exibe a primeira imagem na lista
            exibirImagemAtual();
        }
    }

    private void exibirImagemAtual() {
        if (!listaDeImagens.isEmpty()) {
            Uri imagemUri = listaDeImagens.get(indiceAtual);
            ImageView imageView = findViewById(R.id.imageViewContainer);
            imageView.setImageURI(imagemUri);
        }
    }

    public void onClickBotaoEsquerda(View view) {
        if (!listaDeImagens.isEmpty()) {
            if (indiceAtual > 0) {
                indiceAtual--;
            } else {
                indiceAtual = listaDeImagens.size() - 1;
            }
            exibirImagemAtual();
        }
    }

    public void onClickBotaoDireita(View view) {
        if (!listaDeImagens.isEmpty()) {
            if (indiceAtual < listaDeImagens.size() - 1) {
                indiceAtual++;
            } else {
                indiceAtual = 0;
            }
            exibirImagemAtual();
        }
    }

    public void adicionar(View view) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Verifica se há imagens na lista
        if (listaDeImagens.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Selecione uma ou mais imagens", Toast.LENGTH_SHORT).show();
            return;
        }

        // Desabilita os elementos da interface do usuário relevantes
        btnAdi.setEnabled(false);
        edtDesc.setEnabled(false);
        edtValor.setEnabled(false);
        Add.setEnabled(false);

        // Exibe um indicador de progresso
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adicionando item...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Lista para armazenar os URLs das imagens
        List<String> urlsImagens = new ArrayList<>();

        // Variável para armazenar a URL da imagem principal
        final String[] urlImagemPrincipal = {null};

        // Variáveis locais finais para armazenar a descrição e o valor atuais
        final String descricaoAtual = edtDesc.getText().toString();
        final String valorAtual = edtValor.getText().toString();

        // Itera sobre todas as imagens na lista
        for (Uri imagemUri : listaDeImagens) {
            // Define o nome do arquivo de destino no Storage (por exemplo, usando um timestamp)
            String nomeArquivo = "imagem_" + System.currentTimeMillis() + ".jpg";

            // Cria a referência para o arquivo no Storage
            StorageReference imagemRef = storageRef.child(nomeArquivo);

            // Faz o upload da imagem para o Storage
            imagemRef.putFile(imagemUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Se o upload for bem-sucedido, obtenha a URL da imagem
                        imagemRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String urlImagem = uri.toString();

                            // Adiciona a URL da imagem à lista de URLs
                            urlsImagens.add(urlImagem);

                            // Se a URL da imagem principal ainda não foi definida, defina-a como a URL da imagem atual
                            if (urlImagemPrincipal[0] == null) {
                                urlImagemPrincipal[0] = urlImagem;
                            }

                            // Se todas as imagens foram carregadas com sucesso, salva os URLs no Realtime Database
                            if (urlsImagens.size() == listaDeImagens.size()) {
                                // Verifica se a descrição está vazia
                                if (descricaoAtual.isEmpty()) {
                                    Toast.makeText(getApplicationContext(), "A descrição não pode estar vazia", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    btnAdi.setEnabled(true);
                                    edtDesc.setEnabled(true);
                                    edtValor.setEnabled(true);
                                    Add.setEnabled(true);
                                    return;
                                }

                                // Verifica se o valor está vazio
                                if (valorAtual.isEmpty()) {
                                    Toast.makeText(getApplicationContext(), "Insira um valor para o imóvel", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    btnAdi.setEnabled(true);
                                    edtDesc.setEnabled(true);
                                    edtValor.setEnabled(true);
                                    Add.setEnabled(true);
                                    return;
                                }

                                // Referência para o Firebase Realtime Database
                                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("imoveis");

                                // Cria um novo nó com um ID único gerado pelo push() e salva os dados nele
                                String key = databaseRef.push().getKey();

                                // Salva a URL principal no Realtime Database
                                databaseRef.child(key).child("urlPrincipal").setValue(urlImagemPrincipal[0]);

                                // Salva as URLs individuais como filhos do nó principal
                                for (int i = 0; i < urlsImagens.size(); i++) {
                                    String url = urlsImagens.get(i);
                                    databaseRef.child(key).child("urls").child("url" + i).setValue(url);
                                }

                                // Salva os outros dados do imóvel
                                databaseRef.child(key).child("descricao").setValue(descricaoAtual);
                                databaseRef.child(key).child("valor").setValue(valorAtual);

                                // Mostre uma mensagem ao usuário quando todas as imagens forem salvas com sucesso
                                Toast.makeText(getApplicationContext(), "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

                                // Oculte o indicador de progresso e restaure a interface do usuário
                                progressDialog.dismiss();
                                btnAdi.setEnabled(true);
                                edtDesc.setEnabled(true);
                                edtValor.setEnabled(true);
                                Add.setEnabled(true);
                            }
                        });
                    })
                    .addOnFailureListener(exception -> {
                        // Exiba uma mensagem de erro para o usuário se ocorrer algum problema durante o upload
                        Toast.makeText(getApplicationContext(), "Erro ao adicionar item. Tente novamente.", Toast.LENGTH_SHORT).show();

                        // Oculte o indicador de progresso e restaure a interface do usuário
                        progressDialog.dismiss();
                        btnAdi.setEnabled(true);
                        edtDesc.setEnabled(true);
                        edtValor.setEnabled(true);
                        Add.setEnabled(true);
                    });

        }
    }


    public void voltar(View view){
        Log.d("add", "Voltar para a tela Adm");
        Intent intent = new Intent(this, Adm.class);
        startActivity(intent);
        finish();
    }
}