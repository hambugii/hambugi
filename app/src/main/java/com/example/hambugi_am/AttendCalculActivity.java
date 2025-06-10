package com.example.hambugi_am;

import android.database.Cursor;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class AttendCalculActivity {

    public static final String PRESENT = "출석";
    public static final String LATE = "지각";
    public static final String ABSENT = "결석";
    public static final String EXCUSED = "공결";
    public static final String NONE = "–";

    public static final int WEEKS_PER_SEMESTER = 15;
    public static final int THREE_HOUR_CLASS = 3;
    public static final int TWO_HOUR_CLASS = 2;

    //F학점 기준
    public static final int ONE_HOUR_F_THRESHOLD = 4;
    public static final int TWO_HOUR_F_THRESHOLD = 8;
    public static final int THREE_HOUR_F_THRESHOLD = 12;

    public static final int MAX_ATTENDANCE_SCORE = 20;
    public static final int LATE_TO_ABSENT_RATIO = 3;

    //특정 강의의 출석률 정보를 계산
    public static AttendanceResult calculateAttendance(DBHelper dbHelper, String userId, String subject) {
        Log.d("AttendCalculActivity", "=== calculateAttendance START ===");
        Log.d("AttendCalculActivity", "userId: " + userId + ", subject: " + subject);

        // 1. 강의 정보 가져오기 (시간 수 계산)
        int classHours = getClassHours(dbHelper, userId, subject);
        int totalClassHours = classHours * WEEKS_PER_SEMESTER;
        Log.d("AttendCalculActivity", "classHours: " + classHours + ", totalClassHours: " + totalClassHours);

        // 2. 출석 데이터 가져오기
        Map<String, Integer> attendanceCount = getAttendanceData(dbHelper, userId, subject);
        Log.d("AttendCalculActivity", "attendanceCount: " + attendanceCount.toString());

        // 3. 지각을 결석으로 환산
        int lateCount = attendanceCount.get(LATE);
        int absentFromLate = lateCount / LATE_TO_ABSENT_RATIO;

        // 4. F학점 기준 계산
        int fThreshold;
        if(classHours == 1) {
            fThreshold = ONE_HOUR_F_THRESHOLD;
        } else if (classHours == 2) {
            fThreshold = TWO_HOUR_F_THRESHOLD;
        } else {
            fThreshold = THREE_HOUR_F_THRESHOLD;
        }

        // 5. 총 결석시수 계산 (공결 포함)
        int actualAbsent = attendanceCount.get(ABSENT);
        int excusedAbsent = attendanceCount.get(EXCUSED);
        int totalAbsentHours = actualAbsent + excusedAbsent + absentFromLate;
        Log.d("AttendCalculActivity", "totalAbsentHours: " + totalAbsentHours);

        int remainingHoursToF = Math.max(0, fThreshold - totalAbsentHours);

        // 6. 출석률 계산 (공결도 결석으로 포함)
        int attendedHours = Math.max(0, totalClassHours - totalAbsentHours);
        double attendanceRate = totalClassHours > 0 ? (double) attendedHours / totalClassHours * 100 : 0;
        Log.d("AttendCalculActivity", "attendanceRate: " + attendanceRate + "%");

        // 7. 출석 점수 계산
        int attendanceScore = Math.max(0, MAX_ATTENDANCE_SCORE - totalAbsentHours);

        // 8. F학점 여부
        boolean isF = totalAbsentHours >= fThreshold;

        Log.d("AttendCalculActivity", "=== calculateAttendance END ===");

        return new AttendanceResult(
                subject,
                classHours,
                totalClassHours,
                attendanceRate,
                attendanceScore,
                totalAbsentHours,
                remainingHoursToF,
                isF,
                attendanceCount
        );
    }

    //강의의 시간 수 계산
    private static int getClassHours(DBHelper dbHelper, String userId, String subject) {
        Cursor cursor = dbHelper.getSubjectInfo(userId, subject);
        if (cursor.moveToFirst()) {
            String startTime = cursor.getString(cursor.getColumnIndexOrThrow("startTime"));
            String endTime = cursor.getString(cursor.getColumnIndexOrThrow("endTime"));

            String[] start = startTime.split(":");
            String[] end = endTime.split(":");

            int startMinutes = Integer.parseInt(start[0]) * 60 + Integer.parseInt(start[1]);
            int endMinutes = Integer.parseInt(end[0]) * 60 + Integer.parseInt(end[1]);

            int durationMinutes = endMinutes - startMinutes;

            if(durationMinutes <= 60) {
                cursor.close();
                return 1; // 1시간 강의
            } else if (durationMinutes <= 120) {
                cursor.close();
                return 2; // 2시간 강의
            } else {
                cursor.close();
                return 3; // 3시간 강의
            }
        }
        cursor.close();
        return 3; // 기본값
    }

    //출석 데이터 집계
    private static Map<String, Integer> getAttendanceData(DBHelper dbHelper, String userId, String subject) {
        Map<String, Integer> count = new HashMap<>();
        count.put(PRESENT, 0);
        count.put(LATE, 0);
        count.put(ABSENT, 0);
        count.put(EXCUSED, 0);

        Cursor cursor = dbHelper.getAttendanceBySubject(userId, subject);
        while (cursor.moveToNext()) {
            String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
            if (!status.equals(NONE)) {
                count.put(status, count.get(status) + 1);
            }
        }
        cursor.close();

        return count;
    }

    //특정 날짜의 출석 상태에 따른 색상 결정
    public static int getDateColor(DBHelper dbHelper, String userId, String date) {
        Cursor cursor = dbHelper.getAttendanceByUserAndDateWithSubject(userId, date);

        int totalAbsentHours = 0;
        int totalExcusedHours = 0;
        int totalLateHours = 0;
        int totalPresentHours = 0;
        int totalHours = 0;

        while (cursor.moveToNext()) {
            String status = cursor.getString(3); // status 컬럼
            if (!status.equals(NONE)) {
                totalHours++;
                switch (status) {
                    case PRESENT:
                        totalPresentHours++;
                        break;
                    case LATE:
                        totalLateHours++;
                        break;
                    case ABSENT:
                        totalAbsentHours++;
                        break;
                    case EXCUSED:
                        totalExcusedHours++;
                        break;
                }
            }
        }
        cursor.close();

        // 색상 결정 로직
        if (totalHours == 0) {
            return 0; // 기본 색상
        }

        // 모두 출석
        if (totalHours == totalPresentHours) {
            return 0xFF8DBBFF; //파란색 (전부 출석)
        }

        // 출석+지각 (결석이나 공결 없음)
        if (totalLateHours > 0 && totalAbsentHours == 0 && totalExcusedHours == 0) {
            return 0xFFFFF9C4; //노란색 (출석+지각)
        }

        // 공결만 있는 경우 (결석 없음)
        if (totalExcusedHours > 0 && totalAbsentHours == 0) {
            return 0xFFAFAFAF; //회색 (공결)
        }

        // 결석이 있는 경우 - 시간에 따른 색상 구분
        if (totalAbsentHours > 0) {
            if (totalAbsentHours < 3) {
                return 0xFFC8C4FF; // 보라색 (결석 3시간 미만)
            } else {
                return 0xFFFFB8B8; // 빨간색 (결석 3시간 이상)
            }
        }

        return 0; //기본 색상
    }

    public static class AttendanceResult {
        public final String subject;
        public final int classHours;
        public final int totalClassHours;
        public final double attendanceRate;
        public final int attendanceScore;
        public final int totalAbsentHours;
        public final int remainingHoursToF;
        public final boolean isF;
        public final Map<String, Integer> attendanceCount;

        public AttendanceResult(String subject, int classHours, int totalClassHours,
                                double attendanceRate, int attendanceScore, int totalAbsentHours,
                                int remainingHoursToF, boolean isF, Map<String, Integer> attendanceCount) {
            this.subject = subject;
            this.classHours = classHours;
            this.totalClassHours = totalClassHours;
            this.attendanceRate = attendanceRate;
            this.attendanceScore = attendanceScore;
            this.totalAbsentHours = totalAbsentHours;
            this.remainingHoursToF = remainingHoursToF;
            this.isF = isF;
            this.attendanceCount = attendanceCount;
        }
    }
}
