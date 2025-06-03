package com.example.hambugi_am;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddscheduleActivity extends AppCompatActivity {

    private EditText editCourseName, editProfessor, editRoom;
    private Spinner spinnerDay, spinnerStartTime, spinnerEndTime;
    private Button buttonConfirm;
    private List<String> timeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_add);

        // View 연결
        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerStartTime = findViewById(R.id.spinnerStartTime);
        spinnerEndTime = findViewById(R.id.spinnerEndTime);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        editCourseName = findViewById(R.id.editCourseName);
        editProfessor = findViewById(R.id.editProfessor);
        editRoom = findViewById(R.id.editRoom);

        TextView backButton = findViewById(R.id.back_to_start);
        backButton.setOnClickListener(v -> finish());

        // 요일 어댑터 설정
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.day_array2, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        // 시간 목록
        timeList = new ArrayList<>();
        for (int hour = 8; hour <= 18; hour++) {
            for (int min = 0; min < 60; min += 10) {
                if (hour == 18 && min > 0) break;
                timeList.add(String.format("%02d:%02d", hour, min));
            }
        }

        // 시간 어댑터 설정
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, timeList);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStartTime.setAdapter(timeAdapter);
        spinnerEndTime.setAdapter(timeAdapter);

        // 시작/종료 기본값 설정
        spinnerStartTime.setSelection(0); // 08:00
        spinnerEndTime.setSelection(2);   // 08:10

        // 무한 루프 방지용 플래그
        final boolean[] isAdjustingTime = {false};

        // 시작시간 Spinner 리스너
        spinnerStartTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isAdjustingTime[0]) return;

                try {
                    String startTime = timeList.get(position);
                    String endTime = spinnerEndTime.getSelectedItem().toString();

                    SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.KOREA);
                    Date start = format.parse(startTime);
                    Date end = format.parse(endTime);

                    if (!start.before(end)) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(end);
                        cal.add(Calendar.MINUTE, -10);
                        String corrected = format.format(cal.getTime());

                        int correctedIndex = timeList.indexOf(corrected);
                        if (correctedIndex >= 0) {
                            isAdjustingTime[0] = true;
                            spinnerStartTime.setSelection(correctedIndex);
                            Toast.makeText(AddscheduleActivity.this, "시작 시간이 종료 시간보다 늦거나 같아 자동 조정되었습니다.", Toast.LENGTH_SHORT).show();
                            isAdjustingTime[0] = false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 종료시간 Spinner 리스너
        spinnerEndTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isAdjustingTime[0]) return;

                try {
                    String endTime = timeList.get(position);
                    String startTime = spinnerStartTime.getSelectedItem().toString();

                    SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.KOREA);
                    Date end = format.parse(endTime);
                    Date start = format.parse(startTime);

                    if (!end.after(start)) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(start);
                        cal.add(Calendar.MINUTE, 10);
                        String corrected = format.format(cal.getTime());

                        int correctedIndex = timeList.indexOf(corrected);
                        if (correctedIndex >= 0) {
                            isAdjustingTime[0] = true;
                            spinnerEndTime.setSelection(correctedIndex);
                            Toast.makeText(AddscheduleActivity.this, "종료 시간이 시작 시간보다 빠르거나 같아 자동 조정되었습니다.", Toast.LENGTH_SHORT).show();
                            isAdjustingTime[0] = false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 전달받은 인텐트 확인
        Intent intent = getIntent();
        boolean isEditMode = intent.getBooleanExtra("from_view", false);
        if (isEditMode) {
            // 전달된 값으로 화면 채우기
            editCourseName.setText(intent.getStringExtra("subject"));
            editRoom.setText(intent.getStringExtra("room"));
            editProfessor.setText(intent.getStringExtra("professor"));

            // 요일 스피너 선택
            String day = intent.getStringExtra("day");
            int dayPos = ((ArrayAdapter<String>) spinnerDay.getAdapter()).getPosition(day);
            spinnerDay.setSelection(dayPos);

            // 시간 스피너 선택
            int startPos = timeList.indexOf(intent.getStringExtra("startTime"));
            int endPos = timeList.indexOf(intent.getStringExtra("endTime"));
            spinnerStartTime.setSelection(startPos);
            spinnerEndTime.setSelection(endPos);

            buttonConfirm.setText("수정"); //수정 모드일 경우 버튼 텍스트로 변경

            // 삭제 버튼 보이게 하고 이벤트 달기
            Button deleteBtn = findViewById(R.id.buttonDelete);
            deleteBtn.setVisibility(View.VISIBLE);

            deleteBtn.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(AddscheduleActivity.this)
                        .setTitle("삭제 확인")
                        .setMessage("정말 이 강의를 삭제하시겠습니까?")
                        .setPositiveButton("확인", (dialog, which) -> {
                            SharedPreferences prefs = getSharedPreferences("login_session", MODE_PRIVATE);
                            String userId = prefs.getString("user_id", null);
                            DBHelper db = new DBHelper(AddscheduleActivity.this);
                            db.deleteTimetable(
                                    userId,
                                    intent.getStringExtra("subject"),
                                    intent.getStringExtra("day"),
                                    intent.getStringExtra("startTime"),
                                    intent.getStringExtra("endTime"),
                                    intent.getStringExtra("semester")
                            );

                            Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            finish(); // 현재 화면 닫기 → MainActivity로 돌아감
                        })
                        .setNegativeButton("취소", null)
                        .show();
            });
        }


        // 버튼 클릭 시 DB 저장
        buttonConfirm.setOnClickListener(v -> {
            // SharedPreferences에서 유저 ID 가져오기
            SharedPreferences prefs = getSharedPreferences("login_session", MODE_PRIVATE);
            String userId = prefs.getString("user_id", null);
            if (userId == null) {
                Toast.makeText(this, "로그인 정보를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // MainActivity에서 전달받은 학기 값 가져오기
            String semester = getIntent().getStringExtra("semester");
            if (semester == null) {
                Toast.makeText(this, "학기 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 입력값 가져오기
            String subject = editCourseName.getText().toString().trim();
            String professor = editProfessor.getText().toString().trim();
            String room = editRoom.getText().toString().trim();
            String day = spinnerDay.getSelectedItem().toString();
            String startTime = spinnerStartTime.getSelectedItem().toString();
            String endTime = spinnerEndTime.getSelectedItem().toString();

            if (subject.isEmpty()) {
                Toast.makeText(this, "강의명을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (professor.isEmpty()) {
                Toast.makeText(this, "교수명을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (room.isEmpty()) {
                Toast.makeText(this, "강의실을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 시간 충돌 감지 및 수정 모드 처리
            DBHelper dbHelper = new DBHelper(this);
            if (isEditMode) {
                dbHelper.deleteTimetable(
                        userId,
                        intent.getStringExtra("subject"),
                        intent.getStringExtra("day"),
                        intent.getStringExtra("startTime"),
                        intent.getStringExtra("endTime"),
                        intent.getStringExtra("semester")
                );
            } else {
                if (dbHelper.isTimeConflict(userId, semester, day, startTime, endTime)) {
                    Toast.makeText(this, "해당 시간에 이미 등록된 강의가 있습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            dbHelper.insertTimetable(userId, subject, professor, room, day, startTime, endTime, semester);
            Toast.makeText(this, isEditMode ? "강의가 수정되었습니다." : "강의가 추가되었습니다.", Toast.LENGTH_SHORT).show();

            // 1.5초 후에 메인으로 이동
            new android.os.Handler().postDelayed(() -> {
                Intent mainIntent = new Intent(AddscheduleActivity.this, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                finish();
            }, 1500);
        });
    }
}
