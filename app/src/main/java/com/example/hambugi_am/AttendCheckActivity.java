package com.example.hambugi_am;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AttendCheckActivity extends AppCompatActivity {

    private TextView dateTextView, backTextView;
    private TextView lecture1TitleTextView, lecture2TitleTextView;
    private Spinner[][] attendanceSpinners = new Spinner[2][3];
    private String[] statuses = {"출석", "지각", "결석", "공결", "–"};

    private String displayDate, dbDate, userId, selectedDayOfWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend_check);

        // 날짜 및 버튼, 제목 TextView 연결
        dateTextView = findViewById(R.id.dateTextView);
        backTextView = findViewById(R.id.back);
        Button submitButton = findViewById(R.id.submitButton);
        lecture1TitleTextView = findViewById(R.id.lecture1TitleTextView);
        lecture2TitleTextView = findViewById(R.id.lecture2TitleTextView);

        // 출결 스피너 초기화
        attendanceSpinners[0][0] = findViewById(R.id.spinner_0_0);
        attendanceSpinners[0][1] = findViewById(R.id.spinner_0_1);
        attendanceSpinners[0][2] = findViewById(R.id.spinner_0_2);
        attendanceSpinners[1][0] = findViewById(R.id.spinner_1_0);
        attendanceSpinners[1][1] = findViewById(R.id.spinner_1_1);
        attendanceSpinners[1][2] = findViewById(R.id.spinner_1_2);

        // 출결 상태 어댑터 연결 및 기본값 설정
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                if (attendanceSpinners[i][j] != null) {
                    attendanceSpinners[i][j].setAdapter(adapter);
                    attendanceSpinners[i][j].setSelection(statuses.length - 1); // 기본: "-"
                }
            }
        }

        // 인텐트에서 날짜, 요일, 사용자 ID 받기
        Intent intent = getIntent();
        if (intent != null) {
            displayDate = intent.getStringExtra("selectedDate");
            dbDate = intent.getStringExtra("dbSelectedDate");
            selectedDayOfWeek = intent.getStringExtra("selectedDayOfWeek");
            userId = intent.getStringExtra("userId");

            if (displayDate != null) {
                dateTextView.setText(displayDate);
            }
        }

        // userId가 null인 경우 대비
        if (userId == null || userId.isEmpty()) {
            userId = "defaultUser";
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다. 기본 사용자로 진행합니다.", Toast.LENGTH_LONG).show();
        }

        // 요일별 강의 제목 로딩
        if (selectedDayOfWeek != null) {
            selectedDayOfWeek += "요일";
            loadLectureTitlesByDay(userId, selectedDayOfWeek);
        }

        // 확인 버튼 클릭 → 출결 저장
        submitButton.setOnClickListener(v -> saveAttendance());

        // 뒤로가기 클릭 → 이전 화면으로
        backTextView.setOnClickListener(v -> finish());

        // 저장된 출결 상태 불러오기
        loadAttendance(userId, dbDate);
    }

    // 출결 데이터 저장
    private void saveAttendance() {
        DBHelper dbHelper = new DBHelper(this);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                if (attendanceSpinners[i][j] != null) {
                    String status = attendanceSpinners[i][j].getSelectedItem().toString();
                    dbHelper.insertOrUpdateAttendance(userId, dbDate, i, j, status);
                }
            }
        }
        Toast.makeText(this, "출결 정보가 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    // 저장된 출결 데이터 불러와 Spinner에 적용
    private void loadAttendance(String userId, String date) {
        DBHelper dbHelper = new DBHelper(this);
        try (Cursor cursor = dbHelper.getAttendanceByUserAndDate(userId, date)) {
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

    // 시간표에서 해당 요일 강의 2개를 불러와 위/아래 텍스트뷰에 설정
    private void loadLectureTitlesByDay(String userId, String day) {
        DBHelper dbHelper = new DBHelper(this);
        try (Cursor cursor = dbHelper.getSubjectsWithTimeByUserAndDay(userId, day)) {
            String firstLecture = "";
            String secondLecture = "";

            if (cursor.moveToNext()) firstLecture = cursor.getString(0);
            if (cursor.moveToNext()) secondLecture = cursor.getString(0);

            lecture1TitleTextView.setText(firstLecture.isEmpty() ? "강의 없음" : firstLecture);
            lecture2TitleTextView.setText(secondLecture.isEmpty() ? "강의 없음" : secondLecture);
        } catch (Exception e) {
            lecture1TitleTextView.setText("강의 불러오기 오류");
            lecture2TitleTextView.setText("");
            e.printStackTrace();
        }
    }
}
