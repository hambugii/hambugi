package com.example.hambugi_am;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpSuccessActivity extends AppCompatActivity {

    Button btnGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_success);

        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        btnGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그인 화면으로 이동
                Intent intent = new Intent(SignUpSuccessActivity.this, StartActivity.class);
                startActivity(intent);
                finish(); // 현재 화면은 종료
            }
        });
    }
}