package com.example.hambugi_am;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "UserDB.db", null, 12);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 유저 테이블 생성
        db.execSQL(
                "CREATE TABLE users (" +
                        "id TEXT PRIMARY KEY," +
                        "password TEXT," +
                        "name TEXT)"
        );

        // 시간표 테이블 생성
        db.execSQL("CREATE TABLE timetable (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userId TEXT NOT NULL," +
                "subject TEXT NOT NULL," +
                "professor TEXT," +
                "room TEXT," +
                "day TEXT NOT NULL," +
                "startTime TEXT NOT NULL," +
                "endTime TEXT NOT NULL," +
                "semester TEXT NOT NULL," +
                "alarm INTEGER DEFAULT 0)"
        );

        // 출결 테이블 생성
        db.execSQL("CREATE TABLE IF NOT EXISTS attend (" +
                "userId TEXT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "subject TEXT NOT NULL," +
                "rowNum INTEGER NOT NULL, " +
                "col INTEGER NOT NULL, " +
                "status TEXT NOT NULL, " +
                "PRIMARY KEY(userId, date, subject, rowNum, col))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 테이블이 이미 존재하면 삭제 (업그레이드 시 초기화)
        db.execSQL("DROP TABLE IF EXISTS users"); // users 테이블도 같이 삭제되도록 수정
        db.execSQL("DROP TABLE IF EXISTS timetable");
        db.execSQL("DROP TABLE IF EXISTS attend");
        onCreate(db);
    }

    // 시간표 정보 추가
    public void insertTimetable(String userId, String subject, String professor, String room,
                                String day, String startTime, String endTime, String semester) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO timetable (userId, subject, professor, room, day, startTime, endTime, semester) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                new Object[]{userId, subject, professor, room, day, startTime, endTime, semester});
        db.close();
    }

    // 학기별 시간표 조회
    public Cursor getTimetableByUserAndSemester(String userId, String semester) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM timetable WHERE userId = ? AND semester = ?",
                new String[]{userId, semester});
    }

    // 수업 시간 겹침 검사
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

                // 수업이 겹치는지 비교
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

    // 시간표 삭제 (트랜잭션 적용해서 안전하게 처리)
    public void deleteTimetable(String userId, String subject, String day, String startTime, String endTime, String semester) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // 시간표 삭제
            db.execSQL("DELETE FROM timetable WHERE userId = ? AND subject = ? AND day = ? AND startTime = ? AND endTime = ? AND semester = ?",
                    new Object[]{userId, subject, day, startTime, endTime, semester});
            // 출결 데이터도 같이 삭제
            deleteAttendanceBySubject(db, userId, subject); // 내부에서 db 재오픈 안 하게 수정
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // 모든 강의 과목 목록 가져오기
    public Cursor getAllSubjectsByUserId(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT subject FROM (" +
                        "SELECT subject, MIN(startTime) AS minTime " +
                        "FROM timetable WHERE userId = ? GROUP BY subject" +
                        ") ORDER BY minTime ASC",
                new String[]{userId});
    }

    // 모든 강의 목록 가져오기 (학기 필터링)
    public Cursor getSubjectByUserIdSemester(String userId, String semester) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT subject FROM (" +
                        "SELECT subject, MIN(startTime) AS minTime " +
                        "FROM timetable WHERE userId = ? AND semester = ? GROUP BY subject" +
                        ") ORDER BY minTime ASC",
                new String[]{userId, semester});
    }

    // 특정 요일에 등록된 과목들 가져오기
    public Cursor getSubjectsByUserIdAndDay(String userId, String day) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT DISTINCT subject FROM timetable WHERE userId = ? AND day = ?",
                new String[]{userId, day});
    }

    // 특정 요일에 등록된 과목들 가져오기 (학기 필터링)
    public Cursor getSubjectByDaySemester(String userId, String day, String semester) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT DISTINCT subject FROM timetable WHERE userId = ? AND day = ? AND semester = ?",
                new String[]{userId, day, semester});
    }

    // 요일별 과목 + 시작 시간 정렬해서 가져오기
    public Cursor getSubjectsWithTimeByUserAndDay(String userId, String day) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT subject, startTime FROM timetable WHERE userId = ? AND day = ? ORDER BY startTime ASC",
                new String[]{userId, day});
    }

    // 요일별 과목 + 시작 시간 정렬 (학기 필터링)
    public Cursor getSubjectsWithTimeByUserAndDay(String userId, String day, String semester) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT subject, startTime FROM timetable WHERE userId = ? AND day = ? AND semester = ? " +
                "ORDER BY startTime ASC", new String[]{userId, day, semester});
    }

    // 출결 저장 및 수정 (중복 시 덮어씀)
    public void insertOrUpdateAttendance(String userId, String date, String subject, int rowNum, int col, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userId", userId);
        values.put("date", date);
        values.put("subject", subject);
        values.put("rowNum", rowNum);
        values.put("col", col);
        values.put("status", status);

        db.insertWithOnConflict("attend", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // 과목별 출결 내역 조회
    public Cursor getAttendanceBySubject(String userId, String subject) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT date, rowNum, col, status FROM attend WHERE userId = ? AND subject = ?",
                new String[]{userId, subject});
    }

    // 특정 날짜의 출결 조회
    public Cursor getAttendanceByUserAndDateWithSubject(String userId, String date) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT subject, rowNum, col, status FROM attend WHERE userId = ? AND date = ?",
                new String[]{userId, date});
    }

    // 특정 과목의 시간 정보 가져오기
    public Cursor getSubjectInfo(String userId, String subject) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT startTime, endTime FROM timetable WHERE userId = ? AND subject = ? LIMIT 1",
                new String[]{userId, subject});
    }

    // 과목 삭제 시 출결 정보도 같이 삭제 (내부 호출 전용 버전)
    private void deleteAttendanceBySubject(SQLiteDatabase db, String userId, String subject) {
        db.execSQL("DELETE FROM attend WHERE userId = ? AND subject = ?", new Object[]{userId, subject});
    }

    // 외부에서 사용하는 출결 삭제 함수
    public void deleteAttendanceBySubject(String userId, String subject) {
        SQLiteDatabase db = getWritableDatabase();
        deleteAttendanceBySubject(db, userId, subject);
        db.close();
    }

}
