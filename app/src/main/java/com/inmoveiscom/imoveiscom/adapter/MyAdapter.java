package com.inmoveiscom.imoveiscom.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.inmoveiscom.imoveiscom.R;
import com.inmoveiscom.imoveiscom.activity.Editar;

import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<String> urls;
    private List<String> descricoes;

    private List<String> valores;
    private List<String> nodeKeys;
    private OnItemClickListener mListener;




    public MyAdapter() {
        urls = new ArrayList<>();
        descricoes = new ArrayList<>();
        valores = new ArrayList<>();
        nodeKeys = new ArrayList<>();

    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
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

    public String getNodeKey(int position) {
        return nodeKeys.get(position);
    }

    public String getUrl(int position) {
        return urls.get(position);
    }

    public String getDescricao(int position) {
        return descricoes.get(position);
    }

    public String getValor(int position) { return valores.get(position);}

    public void updateItem(String nodeKey, String novaDescricao, String novoValor, String novaUrl) {
        int position = nodeKeys.indexOf(nodeKey);
        if (position != -1) {
            descricoes.set(position, novaDescricao);
            valores.set(position, novoValor);
            urls.set(position, novaUrl);
            notifyItemChanged(position);
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(urls.get(position), descricoes.get(position), valores.get(position));

    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView descricaoTextView;

        private TextView valorTextView;
        private ImageView imageView;
        private ImageView editImageView;
        private ImageView deleteImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            descricaoTextView = itemView.findViewById(R.id.descricaoTextView);
            valorTextView = itemView.findViewById(R.id.valorTextView1);
            imageView = itemView.findViewById(R.id.imageView);
            editImageView = itemView.findViewById(R.id.editImageView);
            deleteImageView = itemView.findViewById(R.id.deleteImageView);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);
                        }
                    }
                }
            });

            editImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        String nodeKey = nodeKeys.get(position);
                        String imageUrl = urls.get(position);
                        String descricao = descricoes.get(position);
                        String valor = valores.get(position);

                        Log.d("Adapter", "Node key: " + nodeKey);

                        Intent intent = new Intent(itemView.getContext(), Editar.class);
                        intent.putExtra("imageUrl", imageUrl); // Passa a URL da imagem para a atividade de edição
                        intent.putExtra("descricao", descricao);
                        intent.putExtra("valor", valor);
                        intent.putExtra("nodeKey", nodeKey);// Passa a descrição para a atividade de edição
                        itemView.getContext().startActivity(intent);

                        Log.d("Adapter", "Edit clicked at position: " + position);
                    }
                }
            });

            imageView.setOnClickListener(null);
            descricaoTextView.setOnClickListener(null);
            valorTextView.setOnClickListener(null);

            deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Confirmação");
                    builder.setMessage("Tem certeza de que deseja excluir este item?");

                    builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                String nodeKey = nodeKeys.get(position);
                                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("imoveis").child(nodeKey);
                                databaseRef.removeValue();

                                urls.remove(position);
                                descricoes.remove(position);
                                valores.remove(position);
                                nodeKeys.remove(position);
                                notifyItemRemoved(position);
                            }
                        }
                    });

                    builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss(); // Fechar o diálogo se o usuário escolher "Não"
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }

        public void bindData(String url, String descricao, String valor) {
            Glide.with(itemView.getContext())
                    .load(url)
                    .into(imageView);
            descricaoTextView.setText(descricao);
            if (valor != null && !valor.trim().isEmpty()) {
                double valorDouble = Double.parseDouble(valor); // Converte o valor String para double
                Locale brasil = new Locale("pt", "BR"); // Define o local para o Brasil para formatar como moeda brasileira
                NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(brasil);
                String textoValorMonetario = formatoMoeda.format(valorDouble);
                valorTextView.setText(textoValorMonetario); // Define o texto do TextView como o valor monetário formatado
            } else {
                valorTextView.setText(""); // Defina um texto vazio ou uma mensagem de erro alternativa, caso o valor seja nulo
            }
        }
        }
    }
