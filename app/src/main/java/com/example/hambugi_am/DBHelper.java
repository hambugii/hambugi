package com.example.hambugi_am;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "UserDB.db", null, 1);
    }

    @Override
    //회원가입 DB(users 테이블)
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE users (" +
                        "id TEXT PRIMARY KEY," +
                        "password TEXT," +
                        "name TEXT)"
        );
    }
    @Override
    //DB 버전이 변경될 시 호출됨(기존 테이블 삭제 및 생성)
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}