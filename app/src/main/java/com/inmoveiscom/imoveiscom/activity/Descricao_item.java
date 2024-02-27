package com.inmoveiscom.imoveiscom.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inmoveiscom.imoveiscom.R;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Descricao_item extends AppCompatActivity {

    private List<String> urls = new ArrayList<>();
    private int indiceAtual = 0;
    private String nodeKey;

    TextView imageIndicatorTextView2;
    private String descricaoImovel, valorFormatado;
    ImageView imageView, Wpp;
    TextView descricaoTextView;
    private ProgressBar progressBar4;
    TextView valorTextView;
    Button btnWpp;

    private String numeroCorretor = "+5511933355438";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descricao_item);
        imageView = findViewById(R.id.imageViewContainerC);
        imageView.setClickable(false);
        imageView.setFocusable(false);
        imageView.setFocusableInTouchMode(false);
        descricaoTextView = findViewById(R.id.txtDescricaoC);
        valorTextView = findViewById(R.id.txtValorC);
        progressBar4 = findViewById(R.id.progressBar4);
        imageIndicatorTextView2 = findViewById(R.id.imageIndicatorTextView2);

        nodeKey = getIntent().getStringExtra("nodeKey");
        Log.d("DescricaoItemActivity", "NodeKey recebida: " + nodeKey);

        // Recebendo os dados enviados pela Intent da atividade anterior (Home)
        carregarUrlsDasImagens(new UrlsCallback() {
            @Override
            public void onUrlsLoaded(List<String> urls) {
                // URLs carregadas com sucesso, agora atualize a lista e exiba a imagem atual
                Descricao_item.this.urls = urls;
                exibirImagemAtual();
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            descricaoImovel = intent.getStringExtra("descricao");
            String valorImovel = intent.getStringExtra("valor");
            descricaoTextView.setText(descricaoImovel);
            double valorDouble = Double.parseDouble(valorImovel);
            Locale locale = new Locale("pt", "BR"); // Define o local para o Brasil
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
            DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(locale);
            decimalFormat.applyPattern("#,##0.00");
            valorFormatado = currencyFormat.format(valorDouble); // Usa currencyFormat em vez de decimalFormat
            valorTextView.setText(valorFormatado);
        }
    }

    public void onClickBotaoEsquerda(View view) {
        if (!urls.isEmpty()) {
            if (indiceAtual > 0) {
                indiceAtual--;
            } else {

            }
            exibirImagemAtual(); // Corrigido aqui
        }
    }

    public void onClickBotaoDireita(View view) {
        if (!urls.isEmpty()) {
            if (indiceAtual < urls.size() - 1) {
                indiceAtual++;
            } else {

            }
            exibirImagemAtual(); // Corrigido aqui
        }
    }

    // Método para exibir a imagem atual com base no índice
    private void exibirImagemAtual() {
        Log.d("DescricaoItemActivity", "Exibindo imagem para o índice: " + indiceAtual);
        Log.d("DescricaoItemActivity", "Tamanho da lista de URLs: " + urls.size());
        if (urls != null && !urls.isEmpty() && indiceAtual >= 0 && indiceAtual < urls.size()) {
            String url = urls.get(indiceAtual);
            if (url != null && !url.isEmpty()) {
                Log.d("DescricaoItemActivity", "URL da imagem: " + url);
                Glide.with(this).load(url).into(imageView);
                String imageIndicator = (indiceAtual + 1) + "/" + urls.size();
                imageIndicatorTextView2.setText(imageIndicator);
                imageIndicatorTextView2.setVisibility(View.VISIBLE);
            } else {
                Log.e("DescricaoItemActivity", "URL inválida para o índice: " + indiceAtual);
            }
        } else {
            Log.e("DescricaoItemActivity", "Índice inválido ou URLs não inicializadas corretamente");
        }
    }

    // Método para carregar as URLs das imagens de forma assíncrona
    private void carregarUrlsDasImagens(final UrlsCallback callback) {
        progressBar4.setVisibility(View.VISIBLE);
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("imoveis").child(nodeKey).child("urls");
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> urls = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String url = snapshot.getValue(String.class);
                    urls.add(url);
                }
                // Chama o callback com as URLs carregadas
                callback.onUrlsLoaded(urls);
                progressBar4.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DescricaoItemActivity", "Erro ao carregar URLs: " + databaseError.getMessage());
            }
        });
    }


    // Definição da interface de callback para retornar as URLs carregadas
    interface UrlsCallback {
        void onUrlsLoaded(List<String> urls);
    }
    public void enviarWhatsApp(View view) {
        String message = "Olá, eu tenho interesse no imóvel: " + descricaoImovel + " com o valor de: " + valorFormatado;
        String phoneNumber = "+5511933355438"; // Substitua pelo número de telefone do corretor

        // Criar a Intent para abrir o WhatsApp com a mensagem pré-preenchida
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + message));

        // Verificar se o WhatsApp está instalado no dispositivo
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "WhatsApp não está instalado.", Toast.LENGTH_SHORT).show();
        }
    }
}