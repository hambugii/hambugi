package com.example.hambugi_am;
import android.content.ContentValues;
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

public class SignUpActivity extends AppCompatActivity {

    EditText inputId, inputPw, inputPwCheck, inputName;
    Button btnSignUp, btnCheckId;
    DBHelper dbHelper;

    boolean isIdChecked = false; // 중복 확인 상태 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 뷰 연결
        TextView backToStart = findViewById(R.id.back_to_start);
        inputId = findViewById(R.id.inputSignUpId);
        inputPw = findViewById(R.id.inputPassword);
        inputPwCheck = findViewById(R.id.inputPasswordCheck);
        inputName = findViewById(R.id.inputName);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnCheckId = findViewById(R.id.btnCheckId);

        dbHelper = new DBHelper(this);

        // 뒤로 가기 버튼
        backToStart.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        });

        // 아이디 중복확인
        btnCheckId.setOnClickListener(v -> {
            String id = inputId.getText().toString().trim();

            if (!isValidId(id)) {
                Toast.makeText(this, "아이디는 6자 이상, 영문+숫자 포함해야 합니다.", Toast.LENGTH_SHORT).show();
                isIdChecked = false;
                return;
            }

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id FROM users WHERE id = ?", new String[]{id});

            if (cursor.moveToFirst()) {
                Toast.makeText(this, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show();
                isIdChecked = false;
            } else {
                Toast.makeText(this, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                isIdChecked = true;
            }
            cursor.close();
        });

        // 회원가입 버튼 클릭 시
        btnSignUp.setOnClickListener(v -> {
            String id = inputId.getText().toString().trim();
            String pw = inputPw.getText().toString().trim();
            String pwCheck = inputPwCheck.getText().toString().trim();
            String name = inputName.getText().toString().trim();

            if (!isIdChecked) {
                Toast.makeText(this, "아이디 중복 확인을 해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidId(id)) {
                Toast.makeText(this, "아이디는 6자 이상, 영문+숫자 포함해야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidPassword(pw)) {
                Toast.makeText(this, "비밀번호는 8자 이상, 영문+숫자 포함해야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pw.equals(pwCheck)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (name.isEmpty()) {
                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("password", pw);
            values.put("name", name);

            try {
                db.insertOrThrow("users", null, values);
                Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(SignUpActivity.this, StartActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                Toast.makeText(this, "회원가입 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 아이디 유효성 검사: 영문+숫자 포함, 6자 이상
    public boolean isValidId(String id) {
        return id.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$");
    }

    // 비밀번호 유효성 검사: 영문+숫자 포함, 8자 이상
    public boolean isValidPassword(String pw) {
        return pw.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
    }
}
