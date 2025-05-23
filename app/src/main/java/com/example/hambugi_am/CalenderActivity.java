package com.example.hambugi_am;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalenderActivity extends AppCompatActivity {

    private TableLayout calendarTable;
    private Spinner yearSpinner, monthSpinner;
    private String[] weekDays = {"일", "월", "화", "수", "목", "금", "토"};
    private TextView selectedDateView = null; // 현재 선택된 날짜 TextView를 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender);

        calendarTable = findViewById(R.id.calendarTable);
        yearSpinner = findViewById(R.id.yearSpinner);
        monthSpinner = findViewById(R.id.monthSpinner);

        setupSpinners(); // 연도 및 월 스피너 초기화 및 현재 날짜로 설정

        yearSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int i, long l) {
                addDateRows(); // 연도 변경 시 달력 날짜를 다시 그림
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> adapterView) {
            }
        });

        monthSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int i, long l) {
                addDateRows(); // 월 변경 시 달력 날짜를 다시 그림
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> adapterView) {
            }
        });

        addDateRows(); // 초기 달력 날짜 그림
    }

    private void setupSpinners() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

        List<String> yearList = new ArrayList<>();
        for (int year = 2000; year <= currentYear; year++) {
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

        int yearPosition = yearList.indexOf(currentYear + "년");
        if (yearPosition == -1 && !yearList.isEmpty()) {
            yearSpinner.setSelection(0);
        } else {
            yearSpinner.setSelection(yearPosition);
        }
        monthSpinner.setSelection(currentMonth - 1);
    }

    private void addWeekDaysRow() {
        TableRow weekDaysRow = new TableRow(this);
        weekDaysRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
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

        View divider = new View(this);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, 2);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(Color.LTGRAY);
        calendarTable.addView(divider);
    }

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

    private void addDateRows() {
        if (selectedDateView != null) {
            selectedDateView.setBackgroundColor(Color.TRANSPARENT);
            selectedDateView = null;
        }

        calendarTable.removeAllViews();
        addWeekDaysRow();

        int selectedYear = Integer.parseInt(yearSpinner.getSelectedItem().toString().replace("년", ""));
        int selectedMonth0Based = monthSpinner.getSelectedItemPosition();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth0Based);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        TableRow row = new TableRow(this);

        for (int i = 0; i < firstDayOfWeek; i++) {
            TextView emptyView = new TextView(this);
            emptyView.setText("");
            emptyView.setPadding(16, 20, 16, 140);
            emptyView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(emptyView);
        }

        for (int day = 1; day <= daysInMonth; day++) {
            final TextView dateView = new TextView(this);
            dateView.setText(String.valueOf(day));
            dateView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            dateView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            dateView.setPadding(16, 20, 16, 140);
            dateView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            final String dateKey = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth0Based + 1, day);
            dateView.setTag(dateKey);

            int dayOfWeek = (firstDayOfWeek + day - 1) % 7;
            if (dayOfWeek == 0) dateView.setTextColor(Color.parseColor("#FF6363")); // 일요일
            else if (dayOfWeek == 6) dateView.setTextColor(Color.parseColor("#678AFF")); // 토요일
            else dateView.setTextColor(Color.parseColor("#797979")); // 평일

            dateView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView clickedCell = (TextView) v;

                    if (selectedDateView != null) {
                        selectedDateView.setBackgroundColor(Color.TRANSPARENT);
                    }
                    clickedCell.setBackgroundColor(Color.LTGRAY);
                    selectedDateView = clickedCell;

                    // 여기에 날짜 클릭 시 출결 입력 화면으로 이동하는 코드 추가
                    Intent intent = new Intent(CalenderActivity.this, AttendCheckActivity.class);
                    intent.putExtra("selectedDate", dateKey);
                    startActivity(intent);
                }
            });

            row.addView(dateView);

            if (dayOfWeek == 6) {
                addRowWithDivider(row);
                row = new TableRow(this);
                row.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));
                row.setGravity(Gravity.CENTER_HORIZONTAL);
            }
        }

        if (row.getChildCount() > 0) {
            while (row.getChildCount() < 7) {
                TextView emptyView = new TextView(this);
                emptyView.setText("");
                emptyView.setPadding(16, 20, 16, 140);
                emptyView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(emptyView);
            }
            addRowWithDivider(row);
        }

        ImageButton btnMain = findViewById(R.id.btn_main);
        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalenderActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
