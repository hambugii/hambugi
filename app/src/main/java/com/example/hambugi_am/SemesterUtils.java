package com.example.hambugi_am;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SemesterUtils {

    public static class SemesterPeriod {
        public final Date startDate;
        public final Date endDate;

        public SemesterPeriod(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    // 학기로 해당 학기의 시작일과 종료일을 반환
    public static SemesterPeriod getSemesterPeriod(String semester) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            if (semester.equals("2025년 1학기")) {
                Date startDate = sdf.parse("2025-03-04");
                Date endDate = sdf.parse("2025-06-25");
                return new SemesterPeriod(startDate, endDate);
            } else if (semester.equals("2025년 2학기")) {
                Date startDate = sdf.parse("2025-08-25");
                Date endDate = sdf.parse("2025-12-12");
                return new SemesterPeriod(startDate, endDate);
            } else if (semester.equals("2024년 1학기")) {
                Date startDate = sdf.parse("2024-03-04");
                Date endDate = sdf.parse("2024-06-24");
                return new SemesterPeriod(startDate, endDate);
            } else if (semester.equals("2024년 2학기")) {
                Date startDate = sdf.parse("2024-08-26");
                Date endDate = sdf.parse("2024-12-06");
                return new SemesterPeriod(startDate, endDate);
            } else if (semester.equals("2023년 1학기")) {
                Date startDate = sdf.parse("2023-03-02");
                Date endDate = sdf.parse("2023-06-21");
                return new SemesterPeriod(startDate, endDate);
            } else if (semester.equals("2023년 2학기")) {
                Date startDate = sdf.parse("2023-09-01");
                Date endDate = sdf.parse("2023-12-14");
                return new SemesterPeriod(startDate, endDate);
            }

            // 기본값 (현재 년도)
            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);
            Date startDate = sdf.parse(currentYear + "-03-01");
            Date endDate = sdf.parse(currentYear + "-06-30");
            return new SemesterPeriod(startDate, endDate);
        } catch (ParseException e) {
            e.printStackTrace();
            Calendar cal = Calendar.getInstance();
            return new SemesterPeriod(cal.getTime(), cal.getTime());
        }
    }

    // 특정 날짜가 해당 학기에 포함되는지 확인
    public static boolean isDateInSemester (String dateStr, String semester) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            Date targetDate = sdf.parse(dateStr);
            SemesterPeriod period = getSemesterPeriod(semester);

            return !targetDate.before(period.startDate) && !targetDate.after(period.endDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 사용자가 등록한 학기 목록을 가져오기
    public static String[] getRegisteredSemester (DBHelper dbHelper, String userId) {
        return new String[] {"2023년 1학기", "2023년 2학기", "2024년 1학기", "2024년 2학기", "2025년 1학기", "2025년 2학기"};
    }
}
