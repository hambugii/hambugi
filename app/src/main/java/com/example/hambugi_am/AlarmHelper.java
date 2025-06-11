package com.example.hambugi_am;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmHelper {
    public static void setAlarmIfValid(Context context, String courseName, String day, String startTime, String semester) {
        if (!"2025년 1학기".equals(semester)) return;
        setAlarm(context, courseName, day, startTime);
    }

    private static void setAlarm(Context context, String courseName, String day, String startTime) {
        Calendar calendar = getAlarmTime(day, startTime);
        if (calendar == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("courseName", courseName);

        int requestCode = (courseName + day + startTime).hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // 정확한 알람 권한 체크 (Android 12 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!manager.canScheduleExactAlarms()) {
                Toast.makeText(context, "정확한 알람 권한이 필요합니다. 설정 화면으로 이동합니다.", Toast.LENGTH_LONG).show();
                Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                permissionIntent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
                permissionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(permissionIntent);
                Log.w("AlarmHelper", "정확한 알람 권한이 없어 설정 화면으로 이동");
                return;
            }
        }

        // setExactAndAllowWhileIdle로 예약
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        // 예약 시간 로그 출력
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREAN);
        Log.d("AlarmHelper", "알람 등록됨: " + courseName + ", 요일=" + day + ", 시작=" + startTime +
                ", 예약시각=" + sdf.format(calendar.getTime()));
    }

    public static void cancelAlarm(Context context, String courseName, String day, String startTime) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        int requestCode = (courseName + day + startTime).hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

    public static boolean isAlarmEnabled(Context context) {
        return context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE).getBoolean("alarm_toggle", false);
    }

    public static void setAlarmEnabled(Context context, boolean enabled) {
        context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
                .edit().putBoolean("alarm_toggle", enabled).apply();
    }

    private static Calendar getAlarmTime(String dayKor, String timeStr) {
        String[] parts = timeStr.split(":");
        if (parts.length != 2) return null;

        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]) - 5;
        if (minute < 0) {
            hour -= 1;
            minute += 60;
            if (hour < 0) {
                hour = 23;
            }
        }

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = getDayOfWeek(dayKor);
        if (dayOfWeek == -1) return null;

        int today = calendar.get(Calendar.DAY_OF_WEEK);
        int diff = (dayOfWeek - today + 7) % 7;
        calendar.add(Calendar.DAY_OF_YEAR, diff);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 이미 지난 시간이면 다음 주로 미룸
        Calendar now = Calendar.getInstance();
        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }

        return calendar;
    }

    private static int getDayOfWeek(String day) {
        switch (day) {
            case "일요일": return Calendar.SUNDAY;
            case "월요일": return Calendar.MONDAY;
            case "화요일": return Calendar.TUESDAY;
            case "수요일": return Calendar.WEDNESDAY;
            case "목요일": return Calendar.THURSDAY;
            case "금요일": return Calendar.FRIDAY;
            case "토요일": return Calendar.SATURDAY;
            default: return -1;
        }
    }
}
