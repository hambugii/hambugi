package com.example.hambugi_am;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AttendCheckActivity extends AppCompatActivity {

    private TextView dateTextView;
    private TextView backTextView;
    private Spinner[][] attendanceSpinners = new Spinner[2][3];
    private Spinner spinner_0_0, spinner_0_1, spinner_0_2;
    private Spinner spinner_1_0, spinner_1_1, spinner_1_2;
    private String[] statuses = {"출석", "지각", "결석", "공결", "–"};
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend_check);

        dateTextView = findViewById(R.id.dateTextView);
        backTextView = findViewById(R.id.back);  // 뒤로가기 텍스트뷰 연결
        Button submitButton = findViewById(R.id.submitButton);

        // 뒤로가기
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 캘린더에서 선택한 날짜 받아오기
        selectedDate = getIntent().getStringExtra("selectedDate");
        if (selectedDate != null) {
            String[] dateParts = selectedDate.split("-");
            if (dateParts.length == 3) {
                String formattedDate = Integer.parseInt(dateParts[1]) + "월 " + Integer.parseInt(dateParts[2]) + "일";
                dateTextView.setText(formattedDate);
            } else {
                dateTextView.setText("날짜 오류");
            }
        } else {
            dateTextView.setText("날짜 선택 안 됨");
        }

        // 스피너 연결
        spinner_0_0 = findViewById(R.id.spinner_0_0);
        spinner_0_1 = findViewById(R.id.spinner_0_1);
        spinner_0_2 = findViewById(R.id.spinner_0_2);
        spinner_1_0 = findViewById(R.id.spinner_1_0);
        spinner_1_1 = findViewById(R.id.spinner_1_1);
        spinner_1_2 = findViewById(R.id.spinner_1_2);

        attendanceSpinners[0][0] = spinner_0_0;
        attendanceSpinners[0][1] = spinner_0_1;
        attendanceSpinners[0][2] = spinner_0_2;
        attendanceSpinners[1][0] = spinner_1_0;
        attendanceSpinners[1][1] = spinner_1_1;
        attendanceSpinners[1][2] = spinner_1_2;

        // 커스텀 어댑터로 텍스트 중앙 정렬
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, statuses) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setGravity(Gravity.CENTER);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                TextView tv;
                if (convertView == null) {
                    tv = new TextView(getContext());
                    tv.setPadding(16, 16, 16, 16);
                    tv.setGravity(Gravity.CENTER);
                    tv.setTextSize(18);
                } else {
                    tv = (TextView) convertView;
                }
                tv.setText(getItem(position));
                return tv;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                if (attendanceSpinners[i][j] != null) {
                    attendanceSpinners[i][j].setAdapter(adapter);
                }
            }
        }

        // 확인 버튼 클릭 시 메시지 표시
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AttendCheckActivity.this, "출결 저장 완료", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
