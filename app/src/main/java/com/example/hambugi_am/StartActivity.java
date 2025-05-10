package com.example.hambugi_am;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {
    EditText inputId, inputPassword;
    Button btnSignIn;
    TextView tvGoToSignUp;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);  // 로그인 화면 레이아웃

        // 뷰 연결
        inputId = findViewById(R.id.inputId);
        inputPassword = findViewById(R.id.inputPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp);

        // DB 도우미 객체 생성
        dbHelper = new DBHelper(this);

        // 로그인 버튼 클릭
        btnSignIn.setOnClickListener(v -> {
            String id = inputId.getText().toString();
            String pw = inputPassword.getText().toString();

            if (id.isEmpty() || pw.isEmpty()) {
                Toast.makeText(StartActivity.this, "아이디와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }
            // DB 조회
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM users WHERE id = ? AND password = ?",
                    new String[]{id, pw}
            );
            if (cursor.moveToFirst()) {
                Toast.makeText(StartActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();

                // 로그인 성공 후 이동할 화면
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(StartActivity.this, "아이디 또는 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }

            cursor.close();
        });

        tvGoToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원가입 화면으로 이동
                Intent intent = new Intent(StartActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }
}