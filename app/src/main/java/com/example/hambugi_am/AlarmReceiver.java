package com.example.hambugi_am;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String courseName = intent.getStringExtra("courseName");
        String type = intent.getStringExtra("type");

        // 알림 메시지 설정
        String message;
        if ("early".equals(type)) {
            message = courseName + " 수업 시작 5분 전입니다. 출첵하세요!";
        } else if ("onTime".equals(type)) {
            message = courseName + " 수업 시작 시간입니다. 출석 완료했나요?";
        } else {
            message = courseName + " 수업 알림입니다.";
        }

        // 알림 채널 생성
        String channelId = "course_alarm_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "수업 알림", NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // 알림 클릭 시 MainActivity로 이동
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("수업 알림")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        Log.d("AlarmReceiver", "알람 수신됨: " + courseName + " / type: " + type);
    }


}
