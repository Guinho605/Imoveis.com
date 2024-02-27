package com.inmoveiscom.imoveiscom.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inmoveiscom.imoveiscom.R;
import com.inmoveiscom.imoveiscom.activity.Descricao_item;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyAdapterClient extends RecyclerView.Adapter<MyAdapterClient.ViewHolder> {

    private List<String> urls;
    private List<String> descricoes;
    private List<String> valores;
    private List<String> nodeKeys;
    private Context context;

    public MyAdapterClient() {
        urls = new ArrayList<>();
        descricoes = new ArrayList<>();
        valores = new ArrayList<>();
        nodeKeys = new ArrayList<>();
    }

    public void addItem(String url, String descricao, String valor, String nodeKey) {
        urls.add(url);
        descricoes.add(descricao);
        valores.add(valor);
        nodeKeys.add(nodeKey);
        notifyDataSetChanged();
    }


    public void clear() {
        urls.clear();
        descricoes.clear();
        valores.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext(); // Obtém o contexto da View pai
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout_client, parent, false);
        return new ViewHolder(view); // Passa o contexto para o construtor do ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(urls.get(position), descricoes.get(position), valores.get(position), nodeKeys.get(position));
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;
        private TextView descricaoTextView;
        private TextView valorTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            descricaoTextView = itemView.findViewById(R.id.descricaoTextView);
            valorTextView = itemView.findViewById(R.id.valorTextView);
            itemView.setOnClickListener(this); // Adicione isso para definir o OnClickListener no próprio itemView
        }

        public void bindData(String url, String descricao, String valor, String nodeKey) {
            Glide.with(itemView.getContext())
                    .load(url)
                    .into(imageView);

            descricaoTextView.setText(descricao);
            if (valor != null && !valor.trim().isEmpty()) {
                double valorDouble = Double.parseDouble(valor);
                Locale brasil = new Locale("pt", "BR");
                NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(brasil);
                String textoValorMonetario = formatoMoeda.format(valorDouble);
                valorTextView.setText(textoValorMonetario);
            } else {
                valorTextView.setText("");
            }
        }
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                String url = urls.get(position);
                String descricao = descricoes.get(position);
                String valor = valores.get(position);
                String nodeKey = nodeKeys.get(position); // Obtenha o nodeKey correspondente à posição clicada
                List<String> allUrls = getAllUrlsForNode(nodeKey); // Obtenha todas as URLs associadas ao nó

                // Passe todos os dados relevantes para a atividade Descricao_item
                Intent intent = new Intent(v.getContext(), Descricao_item.class);
                intent.putExtra("urls", allUrls.toArray(new String[0])); // Envie todas as URLs pela Intent
                intent.putExtra("descricao", descricao);
                intent.putExtra("valor", valor);
                intent.putExtra("nodeKey", nodeKey);
                v.getContext().startActivity(intent);
            }
        }

        private List<String> getAllUrlsForNode(String nodeKey) {
            List<String> allUrls = new ArrayList<>();
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference()
                    .child("imoveis")
                    .child(nodeKey)
                    .child("urls");

            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String url = snapshot.getValue(String.class);
                        if (url != null && !url.isEmpty()) {
                            allUrls.add(url);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("MyAdapterClient", "Erro ao carregar URLs do Firebase: " + databaseError.getMessage());
                }
            });

            return allUrls;
        }

    }
    }
