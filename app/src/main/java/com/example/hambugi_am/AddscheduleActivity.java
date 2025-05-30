package com.example.hambugi_am; // 패키지 이름은 프로젝트에 맞게 바꿔주세요

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class AddscheduleActivity extends AppCompatActivity {

    private EditText editCourseName, editProfessor, editRoom;
    private Spinner spinnerDay;
    private TextView textStartTime, textEndTime;
    private Button buttonConfirm;

    private int startHour = 9, startMinute = 0;
    private int endHour = 11, endMinute = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_add);

        // 뷰 연결
        editCourseName = findViewById(R.id.editCourseName);
        editProfessor = findViewById(R.id.editProfessor);
        editRoom = findViewById(R.id.editRoom);
        spinnerDay = findViewById(R.id.spinnerDay);
        textStartTime = findViewById(R.id.textStartTime);
        textEndTime = findViewById(R.id.textEndTime);
        buttonConfirm = findViewById(R.id.buttonConfirm);


        TextView backButton = findViewById(R.id.back_to_start);
        // 현재 액티비티 종료
        backButton.setOnClickListener(v -> finish());

        // 스피너에 요일 데이터 추가
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.day_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(adapter);

        // 시작 시간 선택
        textStartTime.setOnClickListener(v -> showTimePicker(true));

        // 종료 시간 선택
        textEndTime.setOnClickListener(v -> showTimePicker(false));
    }

    //시간 형식
    private void showTimePicker(boolean isStart) {
        int hour = isStart ? startHour : endHour;
        int minute = isStart ? startMinute : endMinute;

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute1) -> {
                    String time = String.format("%02d:%02d", hourOfDay, minute1);
                    if (isStart) {
                        startHour = hourOfDay;
                        startMinute = minute1;
                        textStartTime.setText(time);
                    } else {
                        endHour = hourOfDay;
                        endMinute = minute1;
                        textEndTime.setText(time);
                    }
                },
                hour, minute, true
        );
        dialog.show();
    }
}
