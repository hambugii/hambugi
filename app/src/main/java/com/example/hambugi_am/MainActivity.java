package com.example.hambugi_am;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView tvSubject, tvWarning, tvPercent;
    private ProgressBar progressAttendance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Button btnLogout = findViewById(R.id.logout);

        //세션 제거 및 로그인 화면 이동
        btnLogout.setOnClickListener(v -> {
        SharedPreferences prefs = getSharedPreferences("login_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("user_id");
        editor.apply();

        Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainActivity.this, StartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // 학기 선택 Spinner를 레이아웃에서 찾아오기
        Spinner spinnerSemester = findViewById(R.id.spinner_semester);

        // 어댑터 설정: strings.xml의 semester_array 데이터를 Spinner에 연결
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.semester_array, android.R.layout.simple_spinner_item);
        // 드롭다운 펼쳐졌을 때 사용할 레이아웃 설정
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Spinner에 어댑터 연결
        spinnerSemester.setAdapter(adapter);

        // 2025년 1학기를 기본으로 선택되도록 설정
        int defaultPosition = adapter.getPosition("2025년 1학기");
        if (defaultPosition >= 0) {
            spinnerSemester.setSelection(defaultPosition);
        }

        // Spinner에서 선택된 항목을 처리하는 리스너 설정
        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // 선택된 학기 얻기 (배열에서 position 위치의 값)
                String selectedSemester = parentView.getItemAtPosition(position).toString();
                // 선택된 학기를 토스트로 보여주기
                Toast.makeText(MainActivity.this, "선택된 학기: " + selectedSemester, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 아무것도 선택되지 않았을 때 처리
            }
        });

        // 추가 버튼 연결
        Button btnEdit = findViewById(R.id.btn_edit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddscheduleActivity.class);
                startActivity(intent);
            }
        });


        // 캘린더 버튼 연결
        ImageButton btnCalendar= findViewById(R.id.btn_calendar);
        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CalenderActivity.class);
                startActivity(intent);
            }
        });

        TextView tvTodayDay = findViewById(R.id.tv_today_day);

        // 요일 구하기
        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.KOREAN).format(new Date());
        String text = "오늘은 " + dayOfWeek + "입니다";

        // Spannable로 요일 색상 설정
        SpannableString spannable = new SpannableString(text);
        int start = text.indexOf(dayOfWeek); // 요일 시작 인덱스
        int end = start + dayOfWeek.length(); // 요일 끝 인덱스
        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor("#0044D7")),
                start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // 요일 TextView에 적용
        tvTodayDay.setText(spannable);

        // 출석률 관련 뷰 초기화
        tvSubject = findViewById(R.id.tv_subject);
        tvWarning = findViewById(R.id.tv_warning);
        tvPercent = findViewById(R.id.tv_percent);
        progressAttendance = findViewById(R.id.progress_attendance);

        // 예시 (나중에 사용자의 시간표 데이터 불러와야 함)
        String subjectName = "C# 프로그래밍";
        int attendanceRate = 50; // 80퍼 출석
        int remainingHours = 5;

        // 데이터 적용
        tvSubject.setText(subjectName + "의 출석률");
        progressAttendance.setProgress(attendanceRate);
        tvPercent.setText(attendanceRate + "%");
        tvWarning.setText("앞으로 " + remainingHours + "시간 결석 시 F");
    }
}
