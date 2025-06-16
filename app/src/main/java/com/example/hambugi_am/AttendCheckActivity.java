package com.example.hambugi_am;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class AttendCheckActivity extends AppCompatActivity {

    private String TAG = "AttendCheckActivity";
    private TextView dateTextView, backTextView;
    private Button submitButton;
    private LinearLayout mainContainer;
    private String[] statuses = {"출석", "지각", "결석", "공결"};
    private List<String> lectureSubjects = new ArrayList<>();
    private List<Spinner[]> attendanceSpinners = new ArrayList<>();
    private List<Integer> lectureHours = new ArrayList<>();
    private String displayDate, dbDate, userId, selectedDayOfWeek, semester;

    // 날짜와 요일을 기반으로 실제 강의가 있는 학기를 찾아서 반환
    private String findSemesterWithLectures(String dateStr, String dayOfWeek) {
        try {
            String[] dateParts = dateStr.split("-");
            int year = Integer.parseInt(dateParts[0]);

            DBHelper dbHelper = new DBHelper(this);
            String dayWithSuffix = dayOfWeek + "요일";

            // 가능한 학기들을 체크
            String[] possibleSemesters = {
                    year + "년 1학기",
                    year + "년 2학기"
            };

            for (String testSemester : possibleSemesters) {
                // 1. 날짜가 해당 학기 기간 내에 있는지 확인
                if (SemesterUtils.isDateInSemester(dateStr, testSemester)) {
                    // 2. 해당 학기에 실제 강의가 있는지 확인
                    Cursor cursor = dbHelper.getSubjectByDaySemester(userId, dayWithSuffix, testSemester);
                    if (cursor != null && cursor.getCount() > 0) {
                        Log.d(TAG, "Found lectures in " + testSemester + " for " + dayWithSuffix + " on " + dateStr);
                        cursor.close();
                        return testSemester;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }

            Log.d(TAG, "No lectures found for " + dayWithSuffix + " on " + dateStr + " (outside semester periods or no lectures)");
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Error finding semester with lectures: " + dateStr, e);
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attend_check);

        Log.d(TAG, "onCreate started");

        // 날짜 및 버튼, 제목 TextView 연결
        dateTextView = findViewById(R.id.dateTextView);
        backTextView = findViewById(R.id.back);
        mainContainer = findViewById(R.id.mainContainer);

        //인텐트에서 데이터 받기
        Intent intent = getIntent();
        if (intent != null) {
            displayDate = intent.getStringExtra("selectedDate");
            dbDate = intent.getStringExtra("dbSelectedDate");
            selectedDayOfWeek = intent.getStringExtra("selectedDayOfWeek");
            userId = intent.getStringExtra("userId");
            semester = findSemesterWithLectures(dbDate, selectedDayOfWeek);

            Log.d(TAG, "Intent data - displayDate: " + displayDate);
            Log.d(TAG, "Intent data - dbDate: " + dbDate);
            Log.d(TAG, "Intent data - selectedDayOfWeek: " + selectedDayOfWeek);
            Log.d(TAG, "Intent data - userId: " + userId);
            Log.d(TAG, "Intent data - semester: " + semester);

            if (displayDate != null) {
                dateTextView.setText(displayDate);
            }
        }

        // userId가 null인 경우 대비
        if (userId == null || userId.isEmpty()) {
            userId = "defaultUser";
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다. 기본 사용자로 진행합니다.", Toast.LENGTH_LONG).show();
        }

        // 학기 정보가 null인 경우 강의 없음 처리
        if (semester == null) {
            selectedDayOfWeek += "요일";

            // 강의 없음 메시지 직접 표시
            TextView noLecture = new TextView(this);
            noLecture.setText("해당 날짜에 등록된 강의가 없습니다.");
            noLecture.setTextSize(16);
            noLecture.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16,32,16,32);
            noLecture.setLayoutParams(params);
            mainContainer.addView(noLecture);

            // 뒤로가기 클릭 → 이전 화면으로
            backTextView.setOnClickListener(v -> finish());
            return;
        }

        // 요일별 강의 제목 로딩
        if (selectedDayOfWeek != null) {
            selectedDayOfWeek += "요일";
            Log.d(TAG, "Final selectedDayOfWeek: " + selectedDayOfWeek);
            loadLectureTitlesByDay(userId, selectedDayOfWeek, semester);
            // 저장된 출결 상태 불러오기
            loadAttendance(userId, dbDate);
        }

        // 뒤로가기 클릭 → 이전 화면으로
        backTextView.setOnClickListener(v -> finish());
    }

    //확인버튼 생성
    private void createSubmitButton() {
        Log.d(TAG, "Creating submit button");

        // 기존 확인 버튼 제거
        if (submitButton != null && submitButton.getParent() == mainContainer) {
            mainContainer.removeView(submitButton);
        }

        submitButton = new Button(this);
        submitButton.setText("확인");
        submitButton.setTextColor(Color.WHITE);
        submitButton.setBackgroundColor(Color.parseColor("#9099B8"));

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.gravity = android.view.Gravity.END;

        // dp를 px로 변환하여 마진 설정
        int marginTopPx = (int) (16 * getResources().getDisplayMetrics().density);
        buttonParams.setMargins(0, marginTopPx, 0, 0);

        submitButton.setLayoutParams(buttonParams);

        submitButton.setOnClickListener(v -> saveAttendance());

        mainContainer.addView(submitButton);
        Log.d(TAG, "Submit button added to mainContainer with simple styling");
    }


    //강의별 UI 생성
    private void createLectureUI(String lectureName, int lectureIndex, int classHours) {
        Log.d(TAG, "Creating UI for lecture: " + lectureName + ", classhours: " + classHours);

        try {
            // 강의명 TextView
            TextView lectureTitle = new TextView(this);
            lectureTitle.setText(lectureName);
            lectureTitle.setTextSize(16);
            lectureTitle.setTextColor(ContextCompat.getColor(this, android.R.color.black));

            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            titleParams.setMargins(0, lectureIndex > 0 ? 48 : 0, 0, 16);
            lectureTitle.setLayoutParams(titleParams);

            mainContainer.addView(lectureTitle);
            Log.d(TAG, "Added lecture title: " + lectureName);

            // 스피너 컨테이너
            LinearLayout spinnerContainer = new LinearLayout(this);
            spinnerContainer.setOrientation(LinearLayout.HORIZONTAL);
            spinnerContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // 교시 스피너 생성 - classHours에 따라 개수 조정
            Spinner[] lectureSpinners = new Spinner[3]; // 최대 3개까지 지원
            for (int i = 0; i < 3; i++) {
                Log.d(TAG, "Creating spinner " + (i + 1) + " for lecture: " + lectureName);

                LinearLayout periodContainer = new LinearLayout(this);
                periodContainer.setOrientation(LinearLayout.VERTICAL);
                periodContainer.setGravity(android.view.Gravity.CENTER);

                //고정 너비 적용
                LinearLayout.LayoutParams periodParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
                );
                periodParams.setMargins(4, 0, 4, 0);
                periodContainer.setLayoutParams(periodParams);

                // 교시 라벨
                TextView periodLabel = new TextView(this);
                periodLabel.setText((i + 1) + "교시");
                periodLabel.setGravity(android.view.Gravity.CENTER);
                periodLabel.setTextSize(14);
                periodLabel.setPadding(0, 0, 0, 8);
                periodContainer.addView(periodLabel);

                // 스피너 생성
                Spinner spinner = new Spinner(this);
                LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                spinner.setLayoutParams(spinnerParams);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, statuses);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setSelection(0); // "출석"을 기본값으로 설정

                // 강의 시수에 맞춰 교시 스피너 활성화/비활성화
                if (i < classHours) {
                    // 활성화된 스피너
                    spinner.setEnabled(true);
                    periodLabel.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                    spinner.setAlpha(1.0f);
                } else {
                    // 비활성화된 스피너
                    spinner.setEnabled(false);
                    spinner.setSelection(0); // 비활성화된 스피너도 "출석"으로 설정
                    periodLabel.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                    spinner.setAlpha(0.5f);
                }

                lectureSpinners[i] = spinner;
                periodContainer.addView(spinner);
                spinnerContainer.addView(periodContainer);

                Log.d(TAG, "Spinner " + (i + 1) + " created and added, enabled: " + (i < classHours));
            }
            attendanceSpinners.add(lectureSpinners);
            Log.d(TAG, "Added spinners to attendanceSpinners list");

            mainContainer.addView(spinnerContainer);
            Log.d(TAG, "Added spinners container to main container");

            Log.d(TAG, "UI created successfully for lecture: " + lectureName + " with 3 spinners (active: "
                    + classHours + ")");
        } catch (Exception e) {
            Log.e(TAG, "Error creating UI for lecture: " + lectureName, e);
            // UI 생성 실패
            TextView errorText = new TextView(this);
            errorText.setText("강의 UI 생성 실패:" + lectureName);
            errorText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            mainContainer.addView(errorText);
        }
    }

    // 요일별 강의 목록 로딩
    private void loadLectureTitlesByDay(String userId, String day, String semester) {
        Log.d(TAG, "=== loadLectureTitlesByDay START ===");
        Log.d(TAG, "userId: " + userId + ", day: " + day + ", semester: " + semester);

        DBHelper dbHelper = null;
        Cursor cursor = null;

        try {
            Log.d(TAG, "Creating DBHelper instance...");
            dbHelper = new DBHelper(this);
            Log.d(TAG, "DBHelper created successfully");

            lectureSubjects.clear();
            attendanceSpinners.clear();
            lectureHours.clear();

            // 날짜 텍스트뷰 이후의 모든 뷰 제거
            int childCount = mainContainer.getChildCount();
            Log.d(TAG, "mainContainer childCount before removal: " + childCount);
            for(int i = childCount - 1; i >= 1; i--) {
                mainContainer.removeViewAt(i);
            }
            Log.d(TAG, "Views removed, new childCount: " + mainContainer.getChildCount());

            // 쿼리 실행
            Log.d(TAG, "Executing database query...");
            cursor = dbHelper.getSubjectsWithTimeByUserAndDay(userId, day, semester);
            Log.d(TAG, "Query executed. Cursor is " + (cursor != null ? "not null" : "null"));

            if (cursor != null) {
                int count = cursor.getCount();
                Log.d(TAG, "Query result count: " + count);

                if (count > 0) {
                    int lectureIndex = 0;
                    while (cursor.moveToNext()) {
                        try {
                            String lectureName = cursor.getString(0);
                            Log.d(TAG, "Processing lecture " + lectureIndex + ": " + lectureName);

                            if (lectureName == null || lectureName.trim().isEmpty()) {
                                Log.w(TAG, "Empty lecture name at index " + lectureIndex + ", skipping...");
                                continue;
                            }
                            lectureSubjects.add(lectureName);

                            Log.d(TAG, "Getting class hours for: " + lectureName);
                            int classHours = getClassHours(dbHelper, userId, lectureName);
                            lectureHours.add(classHours);

                            Log.d(TAG, "Class hours for " + lectureName + ": " + classHours);

                            Log.d(TAG, "Creating UI for lecture: " + lectureName);
                            createLectureUI(lectureName, lectureIndex, classHours);
                            Log.d(TAG, "UI creation completed for lecture: " + lectureName);
                            lectureIndex++;
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing lecture at index " + lectureIndex, e);
                            // 개별 강의처리 오류 건너뜀
                        }
                    }
                    Log.d(TAG, "Total lectures processed: " + lectureIndex);

                    //강의 있을 때만 확인 버튼 표시
                    if(lectureIndex > 0) {
                        createSubmitButton();
                        Log.d(TAG, "Submit button created because lectures exist");
                    } else {
                        Log.d(TAG, "No submit button created - no lectures found");
                    }
                } else {
                    Log.d(TAG, "No lectures found for the day");
                    TextView noLecture = new TextView(this);
                    noLecture.setText("해당 요일에 등록된 강의가 없습니다.");
                    noLecture.setTextSize(16);
                    noLecture.setGravity(android.view.Gravity.CENTER);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(16,32,16,32);
                    noLecture.setLayoutParams(params);
                    mainContainer.addView(noLecture);

                    // 강의가 없으면 확인 버튼 표시 X
                    Log.d(TAG, "No submit button created - no lectures gor this day");
                }

            } else {
                Log.e(TAG, "Cursor is null - database query failed");
                throw new Exception("Database query returned null cursor");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading lectures", e);
            Log.e(TAG, "Error Details: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            if(e.getCause() != null) {
                Log.e(TAG, "Caused by: " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
            }
            TextView errorText = new TextView(this);
            errorText.setText("강의 정보를 불러오는 중 오류가 발생했습니다:\n" +
                    e.getClass().getSimpleName() + "\n" +
                    (e.getMessage() != null ? e.getMessage() : "알 수 없는 오류"));
            errorText.setGravity(android.view.Gravity.CENTER);
            errorText.setTextSize(12);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16,32,16,32);
            errorText.setLayoutParams(params);
            mainContainer.addView(errorText);

            // 오류가 발생해도 확인 버튼은 표시 X
            Log.d(TAG, "No submit button created due to error");

            // 사용자에게 알림
            Toast.makeText(this, "강의 정보 로딩 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null) {
                try{
                    cursor.close();
                    Log.d(TAG, "Cursor closed");
                } catch (Exception e) {
                    Log.e(TAG, "Error closing cursor", e);
                }
            }
        }
        Log.d(TAG, "=== loadLectureTitlesByDay END ===");
    }

    // 강의 시간 수 계산 메서드
    private int getClassHours(DBHelper dbHelper, String userId, String subject) {
        Log.d(TAG, "Getting class hours for subject: " + subject);
        Cursor cursor = null;
        try {
            cursor = dbHelper.getSubjectInfo(userId, subject);
            if (cursor != null && cursor.moveToFirst()) {
                String startTime = cursor.getString(cursor.getColumnIndexOrThrow("startTime"));
                String endTime = cursor.getString(cursor.getColumnIndexOrThrow("endTime"));

                Log.d(TAG, "Subject: " + subject + ", Start: " + startTime + ", End: " + endTime);

                String[] start = startTime.split(":");
                String[] end = endTime.split(":");

                int startMinutes = Integer.parseInt(start[0]) * 60 + Integer.parseInt(start[1]);
                int endMinutes = Integer.parseInt(end[0]) * 60 + Integer.parseInt(end[1]);

                int durationMinutes = endMinutes - startMinutes;

                int classHours;
                if (durationMinutes <= 60) {
                    classHours = 1; // 1시간 강의
                } else if (durationMinutes <= 120) {
                    classHours = 2; // 2시간 강의
                } else {
                    classHours = 3; // 3시간 강의
                }

                Log.d(TAG, "Duration: " + durationMinutes + " minutes, Class hours: " + classHours);
                return classHours;
            } else {
                Log.w(TAG, "No subject info found for: " + subject + ", returning default 3 hours");
                return 3; // 3시간 강의
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting class hours for subject: " + subject, e);
            return 3;
        } finally {
            if(cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing cursor in getClassHours", e);
                }
            }
        }
    }

    // 출결 데이터 저장
    private void saveAttendance() {
        Log.d(TAG, "Saving attendance for " + lectureSubjects.size() + " lectures");

        DBHelper dbHelper = new DBHelper(this);

        for (int i = 0; i < lectureSubjects.size(); i++) {
            String subject = lectureSubjects.get(i);
            Spinner[] spinners = attendanceSpinners.get(i);
            int classHours = lectureHours.get(i);

            //활성화된 교시만 저장
            for (int j = 0; j < classHours; j++) {
                if (spinners[j].isEnabled()) {
                    String status = spinners[j].getSelectedItem().toString();
                    dbHelper.insertOrUpdateAttendance(userId, dbDate, subject, i, j, status);
                }
            }
        }

        Toast.makeText(this, "출결 정보가 저장되었습니다.", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    // 저장된 출결 데이터 불러오기
    private void loadAttendance(String userId, String date) {
        if (lectureSubjects.isEmpty()) {
            Log.d(TAG, "No lectures to load attendance for");
            return; //강의가 없으면 리턴
        }

        Log.d(TAG, "Loading attendance for date: " + date);

        DBHelper dbHelper = new DBHelper(this);
        Cursor cursor = null;
        try {
            cursor = dbHelper.getAttendanceByUserAndDateWithSubject(userId, date);
            while (cursor != null && cursor.moveToNext()) {
                String subject = cursor.getString(0);
                int rowNum = cursor.getInt(1);
                int col = cursor.getInt(2);
                String status = cursor.getString(3);

                Log.d(TAG, "Loading attendance - subject: " + subject + ", status: " + status);

                // 과목명으로 인덱스 찾기
                int lectureIndex = -1;
                for (int i = 0; i < lectureSubjects.size(); i++) {
                    if (lectureSubjects.get(i).equals(subject)) {
                        lectureIndex = i;
                        break;
                    }
                }

                if (lectureIndex >= 0 && lectureIndex < attendanceSpinners.size()) {
                    int classHours = lectureHours.get(lectureIndex);
                    if (col >= 0 && col < classHours) {
                        Spinner[] spinners = attendanceSpinners.get(lectureIndex);
                        Spinner spinner = spinners[col];

                        // 활성화된 스피너에만 값 설정
                        if (spinner.isEnabled()) {
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
        } catch (Exception e) {
            Log.e(TAG, "Error loading attendance", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
