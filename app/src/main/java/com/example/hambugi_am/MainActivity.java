package com.example.hambugi_am;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView tvSubject, tvWarning, tvPercent;
    private ProgressBar progressAttendance;
    private LinearLayout attendanceContainer; // 출석률 컨테이너

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.wtf("MainActivity", "=== MainActivity onCreate 시작 ===");
        System.out.println("=== MainActivity onCreate 시작 ===");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Log.wtf("MainActivity", "setContentView 완료");

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
        Log.wtf("MainActivity", "spinnerSemester 찾기 완료");

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

        Log.wtf("MainActivity", "스피너 설정 완료");

        // Spinner에서 선택된 항목을 처리하는 리스너 설정
        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.wtf("MainActivity", "스피너 선택 이벤트 발생");
                // 선택된 학기 얻기 (배열에서 position 위치의 값)
                String selectedSemester = parentView.getItemAtPosition(position).toString();
                drawTimetable();
                updateAttendanceDisplay();
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

        // 출석률 컨테이너 초기화
        attendanceContainer = findViewById(R.id.attendanceContainer);

        if (attendanceContainer == null) {
            Log.wtf("MainActivity", "attendanceContainer가 null입니다!");
        } else {
            Log.wtf("MainActivity", "attendanceContainer 초기화 완료");
        }

        Log.wtf("MainActivity", "updateAttendanceDisplay 호출 전");

        updateAttendanceDisplay();

        //시간표 코드 호출
        drawTimetable();

        Log.wtf("MainActivity", "=== MainActivity onCreate 완료 ===");

        ImageButton btnAlarm = findViewById(R.id.btn_alarm);
        SharedPreferences prefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        String key = "alarm_toggle";
        boolean isOn = prefs.getBoolean(key, false);
        btnAlarm.setImageResource(isOn ? R.drawable.alarm_on : R.drawable.alarm_off);

        btnAlarm.setOnClickListener(v -> {
            boolean newState = !AlarmHelper.isAlarmEnabled(this);
            AlarmHelper.setAlarmEnabled(this, newState);
            btnAlarm.setImageResource(newState ? R.drawable.alarm_on : R.drawable.alarm_off);

            // === 전체 알람 반복 등록/해제 ===
            SharedPreferences loginPrefs = getSharedPreferences("login_session", MODE_PRIVATE);
            String userId = loginPrefs.getString("user_id", null);
            String semester = "2025년 1학기";
            DBHelper dbHelper = new DBHelper(this);
            Cursor cursor = dbHelper.getTimetableByUserAndSemester(userId, semester);
            while (cursor.moveToNext()) {
                String subject = cursor.getString(cursor.getColumnIndexOrThrow("subject"));
                String day = cursor.getString(cursor.getColumnIndexOrThrow("day"));
                String startTime = cursor.getString(cursor.getColumnIndexOrThrow("startTime"));
                if (newState) {
                    AlarmHelper.setAlarmIfValid(this, subject, day, startTime, semester);
                } else {
                    AlarmHelper.cancelAlarm(this, subject, day, startTime);
                }
            }
            cursor.close();
        });



        //권한요청
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

    }

    //출석률 표시 업데이트
    private void updateAttendanceDisplay() {
        Log.wtf("MainActivity", "=== updateAttendanceDisplay 시작 ===");
        System.out.println("=== updateAttendanceDisplay 시작 ===");

        try {
            SharedPreferences prefs = getSharedPreferences("login_session", MODE_PRIVATE);
            String userId = prefs.getString("user_id", null);

            Log.wtf("MainActivity", "userId: " + userId);

            if (userId == null) {
                Log.wtf("MainActivity", "userId가 null입니다 - 테스트용 메시지 표시");
                // userId가 null일 때 테스트용 메시지 표시
                TextView testMessage = new TextView(this);
                testMessage.setText("로그인 정보가 없습니다\n시간표를 먼저 등록해주세요");
                testMessage.setTextColor(Color.parseColor("#525252"));
                testMessage.setTextSize(14);
                testMessage.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(30,20,30,20);
                testMessage.setLayoutParams(params);
                attendanceContainer.addView(testMessage);
                return;
            }

            DBHelper dbHelper = new DBHelper(this);
            attendanceContainer.removeAllViews(); //기존 뷰 제거

            // 오늘 요일 구하기
            String todayDayOfWeek = new SimpleDateFormat("EEEE", Locale.KOREAN).format(new Date());
            String todayDayWithSuffix = todayDayOfWeek;

            // 요일이 이미 "요일"로 끝나는지 확인하고, 아니면 "요일"을 붙임
            if (!todayDayOfWeek.endsWith("요일")) {
                todayDayWithSuffix = todayDayOfWeek + "요일";
            }

            Log.wtf("MainActivity", "오늘 요일: " + todayDayWithSuffix);

            // 먼저 데이터베이스에 시간표 데이터가 있는지 확인
            Spinner spinnerSemester = findViewById(R.id.spinner_semester);
            String semester = spinnerSemester.getSelectedItem().toString();

            Log.wtf("MainActivity", "선택된 학기: " + semester);

            // 데이터베이스 연결 확인
            try {
                Cursor timetableCursor = dbHelper.getTimetableByUserAndSemester(userId, semester);
                int timetableCount = timetableCursor != null ? timetableCursor.getCount() : 0;

                Log.wtf("MainActivity", "전체 시간표 개수: " + timetableCount);

                if (timetableCursor != null) {
                    int index = 0;
                    while (timetableCursor.moveToNext()) {
                        String subject = timetableCursor.getString(timetableCursor.getColumnIndexOrThrow("subject"));
                        String day = timetableCursor.getString(timetableCursor.getColumnIndexOrThrow("day"));
                        String startTime = timetableCursor.getString(timetableCursor.getColumnIndexOrThrow("startTime"));
                        String endTime = timetableCursor.getString(timetableCursor.getColumnIndexOrThrow("endTime"));
                        Log.wtf("MainActivity", "시간표[" + index + "] - 과목: " + subject + ", 요일: " + day + ", 시간: " + startTime + "~" + endTime);
                        index++;
                    }
                    timetableCursor.close();
                } else {
                    Log.wtf("MainActivity", "timetableCursor가 null입니다!");
                }
            } catch (Exception e) {
                Log.wtf("MainActivity", "시간표 조회 중 오류", e);
            }

            // 모든 강의 목록 확인
            try {
                Cursor allSubjectsCursor = dbHelper.getAllSubjectsByUserId(userId);
                int allSubjectsCount = allSubjectsCursor != null ? allSubjectsCursor.getCount() : 0;

                Log.wtf("MainActivity", "전체 강의 개수: " + allSubjectsCount);

                if (allSubjectsCursor != null) {
                    int index = 0;
                    while (allSubjectsCursor.moveToNext()) {
                        String subject = allSubjectsCursor.getString(0);
                        Log.wtf("MainActivity", "전체 강의[" + index + "]: " + subject);
                        index++;
                    }
                    allSubjectsCursor.close();
                } else {
                    Log.wtf("MainActivity", "allSubjectsCursor가 null입니다!");
                }
            } catch (Exception e) {
                Log.wtf("MainActivity", "전체 강의 조회 중 오류", e);
            }

            // 오늘 요일에 해당하는 강의만 가져오기
            try {
                Cursor subjectCursor = dbHelper.getSubjectsByUserIdAndDay(userId, todayDayWithSuffix);
                int todaySubjectsCount = subjectCursor != null ? subjectCursor.getCount() : 0;

                Log.wtf("MainActivity", "오늘 강의 개수: " + todaySubjectsCount);

                if (subjectCursor != null && subjectCursor.getCount() > 0) {
                    int index = 0;
                    while (subjectCursor.moveToNext()) {
                        String subject = subjectCursor.getString(0);
                        Log.wtf("MainActivity", "오늘 강의[" + index + "]: " + subject);

                        // 출석률 계산
                        try {
                            AttendCalculActivity.AttendanceResult result =
                                    AttendCalculActivity.calculateAttendance(dbHelper, userId, subject);
                            Log.wtf("MainActivity", "출석률 계산 완료 - " + subject + ": " + result.attendanceRate + "%");

                            // 과목별 출석률 UI 생성
                            createAttendanceUI(result);
                        } catch (Exception e) {
                            Log.wtf("MainActivity", "출석률 계산 중 오류 - " + subject, e);
                        }
                        index++;
                    }
                    subjectCursor.close();
                } else {
                    Log.wtf("MainActivity", "오늘 강의가 없음");
                    createNoTodaySubjectUI();
                    if (subjectCursor != null) {
                        subjectCursor.close();
                    }
                }
            } catch (Exception e) {
                Log.wtf("MainActivity", "오늘 강의 조회 중 오류", e);
            }

        } catch (Exception e) {
            Log.wtf("MainActivity", "updateAttendanceDisplay 전체 오류", e);
        }

        Log.wtf("MainActivity", "=== updateAttendanceDisplay 완료 ===");
    }

    // 출석률 UI 생성
    private void createAttendanceUI(AttendCalculActivity.AttendanceResult result) {
        //과목명
        TextView tvSubject = new TextView(this);
        tvSubject.setText(result.subject + "의 출석률");
        tvSubject.setTextColor(Color.parseColor("#525252"));
        tvSubject.setTextSize(14);
        LinearLayout.LayoutParams subjectParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subjectParams.setMargins(30, 20, 30, 0);
        tvSubject.setLayoutParams(subjectParams);
        attendanceContainer.addView(tvSubject);

        // 출석률 바 컨테이너
        RelativeLayout progressContainer = new RelativeLayout(this);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (39 * getResources().getDisplayMetrics().density)
        );
        containerParams.setMargins(30, 0, 30, 0);
        progressContainer.setLayoutParams(containerParams);

        //배경 바
        ProgressBar progressBg = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBg.setMax(100);
        progressBg.setProgress(100);
        progressBg.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.progress_attendance_custom));
        RelativeLayout.LayoutParams bgParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                (int) (30 * getResources().getDisplayMetrics().density)
        );
        bgParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBg.setLayoutParams(bgParams);
        progressContainer.addView(progressBg);

        //출석률 바
        ProgressBar progressAttendance = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressAttendance.setMax(100);
        progressAttendance.setProgress((int) result.attendanceRate);
        progressAttendance.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.progress_attendance_custom));
        RelativeLayout.LayoutParams progressParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                (int) (30 * getResources().getDisplayMetrics().density)
        );
        progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressAttendance.setLayoutParams(progressParams);
        progressContainer.addView(progressAttendance);

        //경고 문구
        TextView tvWarning = new TextView(this);
        if (result.isF) {
            tvWarning.setText("F학점 확정");
            tvWarning.setTextColor(Color.RED);
        } else {
            tvWarning.setText("앞으로 " + result.remainingHoursToF + "시간 결석 시 F");
            tvWarning.setTextColor(Color.WHITE);
        }
        tvWarning.setTextSize(12);
        RelativeLayout.LayoutParams warningParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        warningParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        warningParams.addRule(RelativeLayout.CENTER_VERTICAL);
        warningParams.setMargins(15,0,0,0);
        tvWarning.setLayoutParams(warningParams);
        progressContainer.addView(tvWarning);

        //퍼센트
        TextView tvPercent = new TextView(this);
        tvPercent.setText((int) result.attendanceRate + "%");
        tvPercent.setTextSize(12);
        RelativeLayout.LayoutParams percentParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        percentParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        percentParams.addRule(RelativeLayout.CENTER_VERTICAL);
        percentParams.setMargins(0,0,10,0);
        tvPercent.setLayoutParams(percentParams);
        progressContainer.addView(tvPercent);

        attendanceContainer.addView(progressContainer);
    }

    // 오늘 요일 강의가 없을 때
    private void createNoTodaySubjectUI() {
        TextView tvNoSubject = new TextView(this);
        String todayDayOfWeek = new SimpleDateFormat("EEEE", Locale.KOREAN).format(new Date());
        tvNoSubject.setText(todayDayOfWeek + "에 등록된 강의가 없습니다\n시간표를 등록해 주세요");
        tvNoSubject.setTextColor(Color.parseColor("#525252"));
        tvNoSubject.setTextSize(14);
        tvNoSubject.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(30,20,30,20);
        tvNoSubject.setLayoutParams(params);
        attendanceContainer.addView(tvNoSubject);
    }

    private void drawTimetable() {
        RelativeLayout canvas = findViewById(R.id.timetableCanvas);
        canvas.removeAllViews(); // 기존 내용 초기화

        int startHour = 8;
        int endHour = 23;
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
        updateAttendanceDisplay(); // 출석률 업데이트

        ImageButton btnAlarm = findViewById(R.id.btn_alarm);
        boolean isOn = AlarmHelper.isAlarmEnabled(this);
        btnAlarm.setImageResource(isOn ? R.drawable.alarm_on : R.drawable.alarm_off);

    }

}
