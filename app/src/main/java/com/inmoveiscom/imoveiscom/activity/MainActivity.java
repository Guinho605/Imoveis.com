package com.inmoveiscom.imoveiscom.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.inmoveiscom.imoveiscom.R;
import com.inmoveiscom.imoveiscom.activity.Login;

public class MainActivity extends BaseActivity {

    ImageView imageView;
    int duracaoTransicao = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView2);

        // Verificar se a ImageView não é nula antes de iniciar a transição
        if (imageView != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    iniciarTransicao();
                }
            }, 1500);
        }
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

    private void iniciarTransicao() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(duracaoTransicao);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                // Use 'progress' para controlar a animação gradualmente
                // Aqui você pode atualizar visualmente os elementos para simular a transição

            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Após a animação, inicie a próxima Activity
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish(); // Encerre esta Activity se necessário
            }
        });

        animator.start();
    }
}
