package com.inmoveiscom.imoveiscom.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inmoveiscom.imoveiscom.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Editar extends BaseActivity {

    ImageView Add2;
    TextView imageIndicatorTextView;
    ImageView imageView;
    EditText descricaoTextView;
    EditText valorTextView;
    Button btnEditar;
    Button btnEsquerda;
    Button btnDireita;
    private static final int PICK_IMAGE_REQUEST = 1;

    private List<String> listaDeUrls = new ArrayList<>();

    private int indiceAtual = 0;

    String novaDescricao;
    String novoValor;
    String novaURL;
    private ProgressBar progressBar2;
    private Uri imagemSelecionada;
    private String nodeKey;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);
        showNoInternetDialog();

        progressBar2 = findViewById(R.id.progressBar2);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Editando item...");
        progressDialog.setCancelable(false);

        NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
        NetworkChangeListenerImpl networkChangeListenerImpl = new NetworkChangeListenerImpl(this);
        networkChangeReceiver.setListener(networkChangeListenerImpl);
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


        nodeKey = getIntent().getStringExtra("nodeKey");

        descricaoTextView = findViewById(R.id.edtNdesc);
        valorTextView = findViewById(R.id.edtNval);
        btnEditar = findViewById(R.id.btnEditar);
        Add2 = findViewById(R.id.add2);
        imageView = findViewById(R.id.imageViewContainer);
        btnEsquerda = findViewById(R.id.btnEsquerda);
        btnDireita = findViewById(R.id.btnDireita);
        imageIndicatorTextView = findViewById(R.id.imageIndicatorTextView);

        // Chamada para carregar as URLs das imagens do Firebase
        carregarUrlsDoFirebase();
        Intent intent = getIntent();
        if (intent != null) {
            String imageUrl = intent.getStringExtra("imageUrl");
            if (imageUrl != null) {
                Glide.with(this)
                        .load(Uri.parse(imageUrl))
                        .into(imageView);
            }

            String descricaoImovel = intent.getStringExtra("descricao");
            String valorImovel = intent.getStringExtra("valor");
            descricaoTextView.setText(descricaoImovel);
            valorTextView.setText(valorImovel);
        }

        Add2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Abrir a galeria de imagens
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Permitir seleção de várias imagens
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                novaDescricao = descricaoTextView.getText().toString();
                novoValor = valorTextView.getText().toString();

                // Verifica se o nodeKey não é nulo
                if (nodeKey != null) {
                    progressDialog.show();

                    // Se a imagem é uma URI de conteúdo, converte-a para um arquivo local e, em seguida, faz o upload
                    if (imagemSelecionada != null && imagemSelecionada.getScheme().equals("content")) {
                        try {
                            File file = File.createTempFile("temp_image", null, getCacheDir());
                            copyUriToFile(imagemSelecionada, file);
                            Uri localFileUri = Uri.fromFile(file);

                            // Obtenha uma referência para o Firebase Storage
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                            // Defina o nome do arquivo para upload
                            String nomeArquivo = "imagem_" + System.currentTimeMillis() + ".jpg";

                            // Defina a referência para o local de armazenamento da imagem no Firebase Storage
                            StorageReference imagemRef = storageRef.child(nomeArquivo);

                            // Faça o upload da imagem para o Firebase Storage
                            UploadTask uploadTask = imagemRef.putFile(localFileUri);

                            // Defina um listener para tratar o sucesso do upload
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Imagem carregada com sucesso, obtenha a URL da imagem
                                    imagemRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            novaURL = uri.toString(); // Converta a URI da imagem em URL do Firebase Storage

                                            // Atualize os valores no banco de dados Firebase Realtime Database
                                            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("imoveis").child(nodeKey);
                                            databaseRef.child("descricao").setValue(novaDescricao);
                                            databaseRef.child("valor").setValue(novoValor);
                                            databaseRef.child("urlPrincipal").setValue(novaURL);

                                            // Salve as alterações
                                            salvarAlteracoes();

                                            // Retorne para a atividade anterior
                                            Intent intent1 = new Intent();
                                            intent1.putExtra("nova_descricao", novaDescricao);
                                            intent1.putExtra("novo_valor", novoValor);
                                            intent1.putExtra("nova_url", novaURL);
                                            setResult(RESULT_OK, intent1);
                                            finish();

                                            // Exiba uma mensagem de sucesso
                                            Toast.makeText(Editar.this, "item atualizado com sucesso", Toast.LENGTH_SHORT).show();

                                            // Feche o diálogo de progresso
                                            progressDialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Se falhar ao obter a URL da imagem, exiba uma mensagem de erro
                                            progressDialog.dismiss();
                                            Toast.makeText(Editar.this, "Erro ao obter a URL da imagem", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle any errors here
                                    progressDialog.dismiss();
                                    Toast.makeText(Editar.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                                }
                            });
                            uploadImageToFirebase(localFileUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(Editar.this, "Erro ao processar a imagem", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Se não houver uma imagem selecionada, atualize apenas a descrição e o valor no banco de dados
                        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("imoveis").child(nodeKey);
                        databaseRef.child("descricao").setValue(novaDescricao);
                        databaseRef.child("valor").setValue(novoValor);

                        // Salve as alterações
                        salvarAlteracoes();

                        // Retorne para a atividade anterior
                        Intent intent1 = new Intent();
                        intent1.putExtra("nova_descricao", novaDescricao);
                        intent1.putExtra("novo_valor", novoValor);
                        setResult(RESULT_OK, intent1);
                        finish();

                        // Exiba uma mensagem de sucesso
                        Toast.makeText(Editar.this, "item atualizado com sucesso", Toast.LENGTH_SHORT).show();

                        // Feche o diálogo de progresso
                        progressDialog.dismiss();
                    }
                }
            }
        });

        btnEsquerda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickBotaoEsquerda(view);
            }
        });

        btnDireita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickBotaoDireita(view);
            }
        });
    }

    private void exibirImagemAtual() {
        if (!listaDeUrls.isEmpty()) {
            String url = listaDeUrls.get(indiceAtual);
            Log.d("Editar", "Exibindo imagem atual: " + url); // Adicione este log para verificar a URL da imagem sendo exibida
            Glide.with(this)
                    .load(Uri.parse(url))
                    .into(imageView);
            String imageIndicator = (indiceAtual + 1) + "/" + listaDeUrls.size();
            imageIndicatorTextView.setText(imageIndicator);
            imageIndicatorTextView.setVisibility(View.VISIBLE);
        }
    }

    public void onClickBotaoEsquerda(View view) {
        if (!listaDeUrls.isEmpty()) {
            if (indiceAtual > 0) {
                indiceAtual--;
            } else {

            }
            exibirImagemAtual();
        }
    }

    public void onClickBotaoDireita(View view) {
        if (!listaDeUrls.isEmpty()) {
            if (indiceAtual < listaDeUrls.size() - 1) {
                indiceAtual++;
            } else {


            }
            exibirImagemAtual();
        }
    }

    private void salvarAlteracoes() {
        if (!listaDeUrls.isEmpty() && nodeKey != null) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("imoveis").child(nodeKey).child("urls");

            // Salvar as URLs no Firebase Realtime Database
            databaseRef.setValue(listaDeUrls)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Editar.this, "Erro ao atualizar imagens", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void carregarUrlsDoFirebase() {

        progressBar2.setVisibility(View.VISIBLE);
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("imoveis").child(nodeKey).child("urls");
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaDeUrls.clear(); // Limpar a lista antes de adicionar as URLs

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String url = snapshot.getValue(String.class);
                    listaDeUrls.add(url);
                }

                // Após carregar todas as URLs, exiba a primeira imagem
                exibirImagemAtual();

                progressBar2.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Trate o erro aqui, se necessário
            }
        });
    }

    // Método chamado após o usuário selecionar uma imagem da galeria
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Limpar lista de URLs
            listaDeUrls.clear();

            if (data.getData() != null) { // Se apenas uma imagem foi selecionada
                Uri imageUri = data.getData();
                listaDeUrls.add(imageUri.toString()); // Adiciona a URI diretamente à lista de URLs
            } else if (data.getClipData() != null) { // Verifica se múltiplas imagens foram selecionadas
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    listaDeUrls.add(imageUri.toString()); // Adiciona a URI diretamente à lista de URLs
                }
            }
            displaySelectedImages(listaDeUrls);

            // Processar as URLs para fazer upload das imagens no Firebase Storage, se necessário
            processarUrls(listaDeUrls);

            // Verificar se pelo menos uma imagem foi selecionada
            if (!listaDeUrls.isEmpty()) {
                // Atribui a URI da primeira imagem selecionada à variável imagemSelecionada
                imagemSelecionada = Uri.parse(listaDeUrls.get(0));
                // Exiba a primeira imagem na lista
                exibirImagemAtual();

                // Se a URI da imagem selecionada não for nula, faça o upload diretamente
                if (imagemSelecionada != null) {
                    // Adicione um log para verificar a URI da imagem selecionada
                    Log.d("Editar", "URI da imagem selecionada: " + imagemSelecionada.toString());
                    // Verifica o tipo de URI
                    if (imagemSelecionada.getScheme().equals("content")) {
                        // Se for URI de conteúdo, faça uma cópia para um arquivo temporário e depois faça o upload
                        try {
                            File tempFile = File.createTempFile("temp_image", null, getCacheDir());
                            copyUriToFile(imagemSelecionada, tempFile);
                            Uri localFileUri = Uri.fromFile(tempFile);
                            uploadImageToFirebase(localFileUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(Editar.this, "Erro ao processar a imagem", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Se for URI de arquivo, faça o upload diretamente
                        uploadImageToFirebase(imagemSelecionada);
                    }
                }
            } else {
                Toast.makeText(Editar.this, "Nenhuma imagem selecionada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para copiar o conteúdo de uma URI para um arquivo local
    private void copyUriToFile(Uri uri, File destinationFile) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        FileOutputStream outputStream = new FileOutputStream(destinationFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        inputStream.close();
    }

    // Método para fazer o upload da imagem para o Firebase Storage
    private void uploadImageToFirebase(Uri imageUri) {
        try {
            // Obtenha um Bitmap a partir da Uri
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // Obter uma referência para o Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

            // Defina o nome do arquivo para upload
            String nomeArquivo = "imagem_" + System.currentTimeMillis() + ".jpg";

            // Defina a referência para o local de armazenamento da imagem no Firebase Storage
            StorageReference imagemRef = storageRef.child(nomeArquivo);

            // Converta o Bitmap em um byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            // Faça o upload do byte array para o Firebase Storage
            UploadTask uploadTask = imagemRef.putBytes(data);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue com a tarefa para obter a URL de download
                    return imagemRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        // URL de download obtida com sucesso
                        Uri downloadUri = task.getResult();
                        String urlDireta = downloadUri.toString();
                        Log.d("Editar", "URL da imagem obtida com sucesso: " + urlDireta);
                        listaDeUrls.set(indiceAtual, urlDireta);

                        salvarAlteracoes();
                        // Agora você pode usar a URL para exibir a imagem ou salvá-la no banco de dados
                        progressDialog.dismiss();

                    } else {
                        // Se ocorrer um erro ao obter a URL de download
                        progressDialog.dismiss();
                        Toast.makeText(Editar.this, "Erro ao obter a URL da imagem", Toast.LENGTH_SHORT).show();
                        Log.e("Editar", "Erro ao obter a URL da imagem", task.getException());
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Toast.makeText(Editar.this, "Erro ao processar a imagem", Toast.LENGTH_SHORT).show();
        }
    }

    private void displaySelectedImages(List<String> imageUrls) {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String url : imageUrls) {
                if (url != null) {
                    if (url.startsWith("content://")) {
                        // Se a URL é uma URI de conteúdo, carregue a imagem usando um método alternativo
                        loadImageFromUri(Uri.parse(url));
                    } else if (url.startsWith("http")) {
                        // Se a URL é uma URL direta, use Glide para carregar a imagem
                        Glide.with(this).load(url).into(imageView);
                    }
                }
            }
        }
    }

    private void loadImageFromUri(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            // Tratar o erro, se necessário
        }
    }

    private void processarUrls(List<String> urls) {
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            if (url.startsWith("content://")) {
                // Se a URL é uma URL de conteúdo, faça o upload da imagem correspondente para o Firebase Storage
                // e substitua a URL de conteúdo pela nova URL do Firebase Storage na lista de URLs
                uploadImageFromContentUri(Uri.parse(url), i);
            }
        }
    }

    private void uploadImageFromContentUri(Uri contentUri, int index) {
        try {
            // 1. Obter o InputStream da Uri de conteúdo
            InputStream inputStream = getContentResolver().openInputStream(contentUri);

            // 2. Criar um arquivo temporário
            File tempFile = File.createTempFile("temp_image", null, getCacheDir());

            // 3. Copiar os bytes da imagem para o arquivo temporário
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            // 4. Fazer o upload do arquivo temporário para o Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            String nomeArquivo = "imagem_" + System.currentTimeMillis() + ".jpg";
            StorageReference imagemRef = storageRef.child(nomeArquivo);
            UploadTask uploadTask = imagemRef.putFile(Uri.fromFile(tempFile));

            // 5. Obter a URL de download da imagem após o upload bem-sucedido
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imagemRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        // 6. Substituir a URL de conteúdo pela nova URL do Firebase Storage na lista de URLs
                        Uri downloadUri = task.getResult();
                        String novaUrl = downloadUri.toString();
                        listaDeUrls.set(index, novaUrl);

                        // 7. Salvar as alterações no banco de dados
                        salvarAlteracoes();

                    } else {
                        // Tratar o erro, se necessário
                        Toast.makeText(Editar.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            // Tratar o erro, se necessário
            Toast.makeText(Editar.this, "Erro ao processar a imagem", Toast.LENGTH_SHORT).show();
        }
    }

    // Método chamado ao clicar no botão "Cancelar"
    public void Cancelar(View view){
        Intent intent = new Intent(this, Adm.class);
        startActivity(intent);
        finish();
    }
}