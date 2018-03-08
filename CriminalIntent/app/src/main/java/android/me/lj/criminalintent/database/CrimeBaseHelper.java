package android.me.lj.criminalintent.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.me.lj.criminalintent.database.CrimeDbSchema.CrimeTable;

/**
 * Created by Administrator on 2018/3/8.
 */

public class CrimeBaseHelper extends SQLiteOpenHelper {

    /**
     * 调用new CrimeBaseHelper(mContext).getWritableDatabase()方法时， CrimeBaseHelper会做如下工作。
     * (1) 打开/data/data/android.me.lj.criminalintent/databases/crimeBase.db数据库；如果不存在，就先创建crimeBase.db数据库文件。
     * (2) 如果是首次创建数据库，就调用onCreate(SQLiteDatabase)方法，然后保存最新的版本号。
     * (3) 如果已创建过数据库，首先检查它的版本号。如果CrimeBaseHelper中的版本号更高，就调用onUpgrade(SQLiteDatabase, int, int)方法升级。
     */


    private static final int VERSION = 1;
    private static final String DB_NAME = "crimeBase.db";

    public CrimeBaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + CrimeTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                CrimeTable.Cols.UUID + ", " +
                CrimeTable.Cols.TITLE + ", " +
                CrimeTable.Cols.DATE + ", " +
                CrimeTable.Cols.SOLVED +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
