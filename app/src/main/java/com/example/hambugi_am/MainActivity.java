package com.example.hambugi_am;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
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
                drawTimetable();
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
                String selectedSemester = spinnerSemester.getSelectedItem().toString();
                Intent intent = new Intent(MainActivity.this, AddscheduleActivity.class);
                intent.putExtra("semester", selectedSemester);
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

        //시간표 코드 호출
        drawTimetable();
    }
    private void drawTimetable() {
        RelativeLayout canvas = findViewById(R.id.timetableCanvas);
        canvas.removeAllViews(); // 기존 내용 초기화

        int startHour = 8;
        int endHour = 19;
        int hourHeight = 200;     // 1시간 높이 (px)
        int dayWidth = 200;       // 하루 칸 너비
        int labelWidth = 100;     // 시간 라벨 너비
        int headerHeight = 100;   // 요일 라벨 높이

        String[] days = {"월", "화", "수", "목", "금"};

        // 좌상단 모서리 칸 (요일/시간 교차점)
        View topLeft = new View(this);
        topLeft.setBackgroundResource(R.drawable.cell_border);
        RelativeLayout.LayoutParams corner = new RelativeLayout.LayoutParams(labelWidth, headerHeight);
        corner.leftMargin = 0;
        corner.topMargin = 0;
        topLeft.setLayoutParams(corner);
        canvas.addView(topLeft);

        // 요일 라벨 (월~금)
        for (int i = 0; i < days.length; i++) {
            // 배경 테두리
            View dayBorder = new View(this);
            dayBorder.setBackgroundResource(R.drawable.cell_border);
            RelativeLayout.LayoutParams borderParams = new RelativeLayout.LayoutParams(dayWidth, headerHeight);
            borderParams.leftMargin = labelWidth + i * dayWidth;
            borderParams.topMargin = 0;
            dayBorder.setLayoutParams(borderParams);
            canvas.addView(dayBorder);

            // 텍스트 있는 라벨
            TextView dayLabel = new TextView(this);
            dayLabel.setText(days[i]);
            dayLabel.setTextSize(12);
            dayLabel.setGravity(Gravity.CENTER);
            dayLabel.setBackgroundColor(Color.TRANSPARENT); // 배경 없음
            RelativeLayout.LayoutParams labelParams = new RelativeLayout.LayoutParams(dayWidth, headerHeight);
            labelParams.leftMargin = labelWidth + i * dayWidth;
            labelParams.topMargin = 0;
            dayLabel.setLayoutParams(labelParams);
            canvas.addView(dayLabel);
        }

        // 시간 라벨 (8~19시)
        for (int hour = startHour; hour <= endHour; hour++) {
            TextView timeLabel = new TextView(this);
            timeLabel.setText(String.valueOf(hour));
            timeLabel.setTextSize(12);
            timeLabel.setGravity(Gravity.CENTER);
            timeLabel.setBackgroundResource(R.drawable.cell_border);

            RelativeLayout.LayoutParams labelParams = new RelativeLayout.LayoutParams(labelWidth, hourHeight);
            labelParams.leftMargin = 0;
            labelParams.topMargin = headerHeight + (hour - startHour) * hourHeight;
            timeLabel.setLayoutParams(labelParams);
            canvas.addView(timeLabel);
        }

        // 시간표 그리드 셀 (월~금, 8~19)
        for (int i = 0; i < days.length; i++) {
            for (int j = 0; j <= endHour - startHour; j++) {
                View cell = new View(this);
                cell.setBackgroundResource(R.drawable.cell_border); // 테두리
                RelativeLayout.LayoutParams cellParams = new RelativeLayout.LayoutParams(dayWidth, hourHeight);
                cellParams.leftMargin = labelWidth + i * dayWidth;
                cellParams.topMargin = headerHeight + j * hourHeight;
                cell.setLayoutParams(cellParams);
                canvas.addView(cell);
            }
        }
        // 로그인 ID와 선택된 학기 가져오기
        SharedPreferences prefs = getSharedPreferences("login_session", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (userId == null) return;

        Spinner spinnerSemester = findViewById(R.id.spinner_semester);
        String semester = spinnerSemester.getSelectedItem().toString();

        //시간표 랜덤 색상
        String[] colorList = {
                "#FFA07A", "#90EE90", "#ADD8E6", "#FFD700",
                "#DDA0DD", "#FFB6C1", "#87CEFA", "#98FB98"
        };
        int colorIndex = 0;

        DBHelper dbHelper = new DBHelper(this);
        Cursor cursor = dbHelper.getTimetableByUserAndSemester(userId, semester);

        while (cursor.moveToNext()) {
            String subject = cursor.getString(cursor.getColumnIndexOrThrow("subject"));
            String professor = cursor.getString(cursor.getColumnIndexOrThrow("professor"));
            String room = cursor.getString(cursor.getColumnIndexOrThrow("room"));
            String day = cursor.getString(cursor.getColumnIndexOrThrow("day"));
            String startTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("startTime"));
            String endTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("endTime"));

            // 시간 계산
            int top = headerHeight + timeToPixel(startTimeStr, startHour, hourHeight);
            int bottom = headerHeight + timeToPixel(endTimeStr, startHour, hourHeight);
            int height = bottom - top;
            int left = labelWidth + getDayOffset(day, dayWidth) + 5;

            TextView block = new TextView(this);
            block.setText(subject + "\n" + room + "\n" + professor + "\n" + startTimeStr + "~" + endTimeStr);
            block.setTextSize(10);
            block.setGravity(Gravity.CENTER);
            String color = colorList[colorIndex % colorList.length];
            colorIndex++;

            block.setBackgroundColor(Color.parseColor(color));


            RelativeLayout.LayoutParams blockParams = new RelativeLayout.LayoutParams(dayWidth - 10, height - 10);
            blockParams.leftMargin = left;
            blockParams.topMargin = top + 5;
            block.setLayoutParams(blockParams);

            canvas.addView(block);

            block.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddscheduleActivity.class);
                intent.putExtra("from_view", true); // "추가" 화면이 아닌 "보기용"임을 표기
                intent.putExtra("subject", subject);
                intent.putExtra("professor", professor);
                intent.putExtra("room", room);
                intent.putExtra("day", day);
                intent.putExtra("startTime", startTimeStr);
                intent.putExtra("endTime", endTimeStr);
                intent.putExtra("semester", semester);
                startActivity(intent);
            });

        }

        cursor.close();

    }

    // 요일에 따라 leftMargin 계산
    private int getDayOffset(String day, int dayWidth) {
        if (day.startsWith("월")) return 0 * dayWidth;
        else if (day.startsWith("화")) return 1 * dayWidth;
        else if (day.startsWith("수")) return 2 * dayWidth;
        else if (day.startsWith("목")) return 3 * dayWidth;
        else if (day.startsWith("금")) return 4 * dayWidth;
        else return 0;
    }


    //픽셀 계산 함수
    private int timeToPixel(String timeStr, int startHour, int hourHeight) {
        String[] parts = timeStr.split(":");
        int hour = Integer.parseInt(parts[0]);
        int min = Integer.parseInt(parts[1]);

        return ((hour - startHour) * hourHeight) + (min * hourHeight / 60);
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawTimetable(); // 화면에 다시 들어올 때마다 시간표 새로 그림
    }

}
