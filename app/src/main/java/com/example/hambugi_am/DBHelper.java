package com.example.hambugi_am;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "UserDB.db", null, 3);
    }

    @Override
    // 유저 테이블
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE users (" +
                        "id TEXT PRIMARY KEY," +
                        "password TEXT," +
                        "name TEXT)"
        );

        // 시간표 테이블
        db.execSQL("CREATE TABLE timetable (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userId TEXT NOT NULL," +
                "subject TEXT NOT NULL," +
                "professor TEXT," +
                "room TEXT," +
                "day TEXT NOT NULL," +
                "startTime TEXT NOT NULL," +
                "endTime TEXT NOT NULL," +
                "semester TEXT NOT NULL)"
        );
    }
    @Override
    //DB 버전이 변경될 시 호출됨(기존 테이블 삭제 및 생성)
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS timetable");
        onCreate(db);
    }

    // 시간표 데이터 삽입 함수
    public void insertTimetable(String userId, String subject, String professor, String room,
                                String day, String startTime, String endTime, String semester) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO timetable (userId, subject, professor, room, day, startTime, endTime, semester) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                new Object[]{userId, subject, professor, room, day, startTime, endTime, semester});
        db.close();
    }

    // 학기별 시간표 데이터 조회
    public Cursor getTimetableByUserAndSemester(String userId, String semester) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM timetable WHERE userId = ? AND semester = ?",
                new String[]{userId, semester});
    }

    // 시간 충돌 감지
    public boolean isTimeConflict(String userId, String semester, String day, String newStart, String newEnd) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT startTime, endTime FROM timetable WHERE userId = ? AND semester = ? AND day = ?",
                new String[]{userId, semester, day});

        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.KOREA);
        try {
            Date newStartTime = format.parse(newStart);
            Date newEndTime = format.parse(newEnd);

            while (cursor.moveToNext()) {
                Date existingStart = format.parse(cursor.getString(0));
                Date existingEnd = format.parse(cursor.getString(1));

                // 시간 겹침 판단
                if (newStartTime.before(existingEnd) && newEndTime.after(existingStart)) {
                    cursor.close();
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cursor.close();
        return false;
    }

    // 시간표 삭제
    public void deleteTimetable(String userId, String subject, String day, String startTime, String endTime, String semester) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM timetable WHERE userId = ? AND subject = ? AND day = ? AND startTime = ? AND endTime = ? AND semester = ?",
                new Object[]{userId, subject, day, startTime, endTime, semester});
        db.close();
    }


}