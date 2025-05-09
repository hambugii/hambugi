package com.example.hambugi_am;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 돌아가기 TextView 찾기
        TextView backButton = findViewById(R.id.backButton);

        // 현재 액티비티 종료
        backButton.setOnClickListener(v -> finish());
    }
}
