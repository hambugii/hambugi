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
import android.widget.Button;
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

    // 연도 및 월 스피너 설정
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

        // 현재 연도와 월을 스피너에 설정
        int yearPosition = yearList.indexOf(currentYear + "년");
        if (yearPosition == -1 && !yearList.isEmpty()) {
            yearSpinner.setSelection(0);
        } else {
            yearSpinner.setSelection(yearPosition);
        }
        monthSpinner.setSelection(currentMonth - 1);
    }

    // 요일 (일, 월, 화...) 행 추가
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
            dayView.setTypeface(null, Typeface.BOLD);
            dayView.setPadding(8, 8, 8, 8);

            switch (day) {
                case "일":
                    dayView.setTextColor(Color.RED);
                    break;
                case "토":
                    dayView.setTextColor(Color.BLUE);
                    break;
                default:
                    dayView.setTextColor(Color.BLACK);
                    break;
            }

            weekDaysRow.addView(dayView, new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        calendarTable.addView(weekDaysRow);

        // 요일 행 아래에 구분선 추가
        View divider = new View(this);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, 2);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(Color.LTGRAY);
        calendarTable.addView(divider);
    }

    // 날짜 행 아래에 구분선 추가 (가로줄 생성)
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

    // 달력의 날짜들을 생성하고 테이블에 추가
    private void addDateRows() {
        if (selectedDateView != null) {
            selectedDateView.setBackgroundColor(Color.TRANSPARENT);
            selectedDateView = null;
        }

        calendarTable.removeAllViews();
        addWeekDaysRow();

        // 스피너에서 선택된 연도와 월을 가져옴
        int selectedYear = Integer.parseInt(yearSpinner.getSelectedItem().toString().replace("년", ""));
        int selectedMonth0Based = monthSpinner.getSelectedItemPosition(); // 0-based (0: 1월, 11: 12월)

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth0Based);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 해당 월의 첫 번째 날짜의 요일 (0: 일요일, 6: 토요일)
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 해당 월의 총 일수

        TableRow row = new TableRow(this);

        // 첫 번째 주 빈칸 채우기
        for (int i = 0; i < firstDayOfWeek; i++) {
            TextView emptyView = new TextView(this);
            emptyView.setText("");
            emptyView.setPadding(16, 20, 16, 140);
            emptyView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(emptyView);
        }

        // 날짜 셀 생성 및 추가
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
            if (dayOfWeek == 0) dateView.setTextColor(Color.RED);
            else if (dayOfWeek == 6) dateView.setTextColor(Color.BLUE);
            else dateView.setTextColor(Color.BLACK);

            dateView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView clickedCell = (TextView) v;

                    if (selectedDateView != null) {
                        selectedDateView.setBackgroundColor(Color.TRANSPARENT);
                    }
                    clickedCell.setBackgroundColor(Color.LTGRAY);
                    selectedDateView = clickedCell;
                }
            });

            row.addView(dateView);

            // 토요일이면 다음 행으로 넘어가거나 마지막 날짜면 행 추가
            if (dayOfWeek == 6) {
                addRowWithDivider(row); // 가로줄 추가
                row = new TableRow(this);
                row.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));
                row.setGravity(Gravity.CENTER_HORIZONTAL);
            }
        }

        // 마지막 주 빈칸 채우기 및 행 추가
        if (row.getChildCount() > 0) {
            while (row.getChildCount() < 7) {
                TextView emptyView = new TextView(this);
                emptyView.setText("");
                emptyView.setPadding(16, 20, 16, 140);
                emptyView.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(emptyView);
            }
            addRowWithDivider(row); // 마지막 행에 가로줄 추가
        }

        // 메인 버튼 연결
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