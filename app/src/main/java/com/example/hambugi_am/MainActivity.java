package com.example.hambugi_am;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // Spinner를 레이아웃에서 찾아오기
        Spinner spinnerSemester = findViewById(R.id.spinner_semester);

        // 어댑터 설정: strings.xml의 semester_array 데이터를 Spinner에 연결
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.semester_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(adapter);

        // Spinner에서 선택된 항목을 처리하는 리스너 설정
        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // 선택된 학기 얻기
                String selectedSemester = parentView.getItemAtPosition(position).toString();
                // 선택된 학기를 토스트로 보여주기
                Toast.makeText(MainActivity.this, "선택된 학기: " + selectedSemester, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 아무것도 선택되지 않았을 때 처리
            }
        });

        TextView tvTodayDay = findViewById(R.id.tv_today_day);

        // 요일 구하기
        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.KOREAN).format(new Date());
        String text = "오늘은 " + dayOfWeek + "입니다";

        // Spannable로 부분 색상 설정
        SpannableString spannable = new SpannableString(text);
        int start = text.indexOf(dayOfWeek);
        int end = start + dayOfWeek.length();
        spannable.setSpan(
                new ForegroundColorSpan(Color.parseColor("#0044D7")),
                start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // TextView에 적용
        tvTodayDay.setText(spannable);
    }
}
