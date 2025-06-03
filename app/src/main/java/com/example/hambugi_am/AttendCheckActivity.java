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
        backTextView = findViewById(R.id.back);
        Button submitButton = findViewById(R.id.submitButton);

        // 뒤로가기 기능
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 날짜 설정
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

        // 어댑터 설정
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

        // 로그인한 유저 아이디 받아오기
        String tempUserId = getIntent().getStringExtra("userId");
        if (tempUserId == null) {
            tempUserId = "defaultUser";
        }
        final String userId = tempUserId;

        // 출결 데이터 불러오기
        if (selectedDate != null) {
            loadAttendance(userId, selectedDate);
        }

        submitButton.setOnClickListener(v -> {
            DBHelper dbHelper = new DBHelper(AttendCheckActivity.this);
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) {
                    Spinner spinner = attendanceSpinners[i][j];
                    if (spinner != null) {
                        String status = spinner.getSelectedItem().toString();
                        dbHelper.insertOrUpdateAttendance(userId, selectedDate, i, j, status);
                    }
                }
            }
            Toast.makeText(AttendCheckActivity.this, "출결 저장 완료", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void loadAttendance(String userId, String date) {
        DBHelper dbHelper = new DBHelper(this);
        try (android.database.Cursor cursor = dbHelper.getAttendanceByUserAndDate(userId, date)) {
            while (cursor.moveToNext()) {
                int rowNum = cursor.getInt(0);
                int col = cursor.getInt(1);
                String status = cursor.getString(2);
                if (rowNum >= 0 && rowNum < 2 && col >= 0 && col < 3) {
                    Spinner spinner = attendanceSpinners[rowNum][col];
                    if (spinner != null) {
                        for (int i = 0; i < statuses.length; i++) {
                            if (statuses[i].equals(status)) {
                                spinner.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}