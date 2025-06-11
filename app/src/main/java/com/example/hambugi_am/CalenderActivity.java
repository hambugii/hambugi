package com.example.hambugi_am;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalenderActivity extends AppCompatActivity {

    private TableLayout calendarTable;
    private Spinner yearSpinner, monthSpinner;
    private String[] weekDays = {"일", "월", "화", "수", "목", "금", "토"};
    private TextView selectedDateView = null;
    private String loggedInUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender);

        // 뷰 연결
        calendarTable = findViewById(R.id.calendarTable);
        yearSpinner = findViewById(R.id.yearSpinner);
        monthSpinner = findViewById(R.id.monthSpinner);

        // 스피너 초기화
        setupSpinners();

        // 로그인 정보 확인 (없으면 기본 사용자로 설정)
        SharedPreferences sharedPref = getSharedPreferences("login_session", Context.MODE_PRIVATE);
        loggedInUserId = sharedPref.getString("user_id", null);
        if (loggedInUserId == null || loggedInUserId.isEmpty()) {
            loggedInUserId = "defaultUser";
            Toast.makeText(this, "로그인 정보가 없습니다. 기본 사용자로 진행합니다.", Toast.LENGTH_LONG).show();
        }

        // 초기 달력 표시
        addDateRows();

        // 메인화면 버튼 클릭 이벤트
        ImageButton btnMain = findViewById(R.id.btn_main);
        btnMain.setOnClickListener(v -> {
            Intent intent = new Intent(CalenderActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // 스피너 변경 시 달력 갱신
        yearSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int i, long l) {
                addDateRows();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> adapterView) {}
        });

        monthSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int i, long l) {
                addDateRows();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> adapterView) {}
        });

        ImageButton btnAlarm = findViewById(R.id.btn_alarm);
        SharedPreferences prefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        String key = "alarm_toggle";
        boolean isOn = prefs.getBoolean(key, false);
        btnAlarm.setImageResource(isOn ? R.drawable.alarm_on : R.drawable.alarm_off);

        btnAlarm.setOnClickListener(v -> {
            boolean newState = !AlarmHelper.isAlarmEnabled(this);
            AlarmHelper.setAlarmEnabled(this, newState);
            btnAlarm.setImageResource(newState ? R.drawable.alarm_on : R.drawable.alarm_off);

            // 로그인, 학기 정보
            SharedPreferences loginPrefs = getSharedPreferences("login_session", MODE_PRIVATE);
            String userId = loginPrefs.getString("user_id", null);

            // 학기는 2025년 1학기로 고정(아니면 Spinner 등에서 가져오기)
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


    }

    // 년도 및 월 스피너 데이터 설정
    private void setupSpinners() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

        List<String> yearList = new ArrayList<>();
        for (int year = 2000; year <= currentYear + 5; year++) {
            yearList.add(year + "년");
        }

        List<String> monthList = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            monthList.add(month + "월");
        }

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearList);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthList);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        // 현재 날짜를 기본 선택
        int yearPosition = yearList.indexOf(currentYear + "년");
        if (yearPosition == -1 && !yearList.isEmpty()) {
            yearSpinner.setSelection(0);
        } else {
            yearSpinner.setSelection(yearPosition);
        }
        monthSpinner.setSelection(currentMonth - 1);
    }

    // 요일 표시 행 추가
    private void addWeekDaysRow() {
        TableRow weekDaysRow = new TableRow(this);
        weekDaysRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        weekDaysRow.setGravity(Gravity.CENTER_HORIZONTAL);

        for (String day : weekDays) {
            TextView dayView = new TextView(this);
            dayView.setText(day);
            dayView.setGravity(Gravity.CENTER);
            dayView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            dayView.setPadding(8, 8, 8, 8);
            dayView.setTextColor(Color.parseColor("#797979"));
            weekDaysRow.addView(dayView, new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        calendarTable.addView(weekDaysRow);

        // 구분선 추가
        View divider = new View(this);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, 2);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(Color.LTGRAY);
        calendarTable.addView(divider);
    }

    // 날짜 행과 구분선 추가
    private void addRowWithDivider(TableRow row) {
        if (row.getChildCount() > 0) {
            calendarTable.addView(row);
            View divider = new View(this);
            TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, 3);
            divider.setLayoutParams(params);
            divider.setBackgroundColor(Color.LTGRAY);
            calendarTable.addView(divider);
        }
    }

    // 달력 날짜 표시
    private void addDateRows() {
        // 이전 선택 날짜 초기화
        calendarTable.removeAllViews();
        addWeekDaysRow();

        // 선택된 년월로 캘린더 설정
        int selectedYear = Integer.parseInt(yearSpinner.getSelectedItem().toString().replace("년", ""));
        int selectedMonth0Based = monthSpinner.getSelectedItemPosition();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth0Based);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setGravity(Gravity.CENTER_HORIZONTAL);

        // 첫 주 빈칸 채우기
        for (int i = 0; i < firstDayOfWeek; i++) {
            TextView emptyCell = new TextView(this);
            emptyCell.setText("");
            emptyCell.setPadding(16, 20, 16, 140);
            emptyCell.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(emptyCell);
        }

        // 날짜 셀 추가
        for (int day = 1; day <= daysInMonth; day++) {
            TextView dateView = new TextView(this);
            dateView.setText(String.valueOf(day));
            dateView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            dateView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            dateView.setPadding(16, 20, 16, 140);
            dateView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            final String dateKey = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth0Based + 1, day);
            dateView.setTag(dateKey);

            // 요일에 따른 색상 지정
            int dow = (firstDayOfWeek + day - 1) % 7;
            int defaultTextColor;
            if (dow == 0) defaultTextColor = Color.parseColor("#FF6363");       // 일요일
            else if (dow == 6) defaultTextColor = Color.parseColor("#678AFF");  // 토요일
            else defaultTextColor = Color.parseColor("#797979");               // 평일

            // 출석 상태에 따른 배경 색상 적용
            DBHelper dbHelper = new DBHelper(this);
            int attendanceColor = AttendCalculActivity.getDateColor(dbHelper, loggedInUserId, dateKey);
            if (attendanceColor != 0) {
                dateView.setBackgroundColor(attendanceColor);
            } else {
                dateView.setTextColor(defaultTextColor);
            }

            // 날짜 클릭 시 처리
            dateView.setOnClickListener(v -> {
                // 날짜 및 요일 추출
                int y = selectedYear;
                int m = selectedMonth0Based + 1;
                int d = Integer.parseInt(dateView.getText().toString());

                String displayFormattedDate = String.format(Locale.getDefault(), "%04d년 %02d월 %02d일", y, m, d);
                String dbFormattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m, d);

                Calendar selCal = Calendar.getInstance();
                selCal.set(y, m - 1, d);
                int dowInt = selCal.get(Calendar.DAY_OF_WEEK);
                String selectedDayOfWeek = "";
                switch (dowInt) {
                    case Calendar.SUNDAY: selectedDayOfWeek = "일"; break;
                    case Calendar.MONDAY: selectedDayOfWeek = "월"; break;
                    case Calendar.TUESDAY: selectedDayOfWeek = "화"; break;
                    case Calendar.WEDNESDAY: selectedDayOfWeek = "수"; break;
                    case Calendar.THURSDAY: selectedDayOfWeek = "목"; break;
                    case Calendar.FRIDAY: selectedDayOfWeek = "금"; break;
                    case Calendar.SATURDAY: selectedDayOfWeek = "토"; break;
                }

                // 출결 확인 화면으로 이동
                Intent intent = new Intent(CalenderActivity.this, AttendCheckActivity.class);
                intent.putExtra("selectedDate", displayFormattedDate);
                intent.putExtra("dbSelectedDate", dbFormattedDate);
                intent.putExtra("userId", loggedInUserId);
                intent.putExtra("selectedDayOfWeek", selectedDayOfWeek);
                startActivityForResult(intent, 1001);
            });

            row.addView(dateView);

            // 토요일이면 줄 바꿈
            if (dow == 6) {
                addRowWithDivider(row);
                row = new TableRow(this);
                row.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                row.setGravity(Gravity.CENTER_HORIZONTAL);
            }
        }

        // 마지막 주 빈칸 채우기
        if (row.getChildCount() > 0) {
            while (row.getChildCount() < 7) {
                TextView emptyCell = new TextView(this);
                emptyCell.setText("");
                emptyCell.setPadding(16, 20, 16, 140);
                emptyCell.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(emptyCell);
            }
            addRowWithDivider(row);
        }
    }

    //출결 입력 후 캘린더 새로고침
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1001 && resultCode == RESULT_OK) {
            addDateRows();
        }
    }
}
