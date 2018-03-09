package android.me.lj.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.me.lj.criminalintent.database.CrimeBaseHelper;
import android.me.lj.criminalintent.database.CrimeCursorWrapper;
import android.me.lj.criminalintent.database.CrimeDbSchema;
import android.me.lj.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2018/3/5 0005.
 */

public class CrimeLab {

    /**
     * 应用能在内存里活多久，单例就能活多久。因此将对象列表保存在单例里的话，就能随时获
     * 取crime数据，不管activity和fragment的生命周期怎么变化。
     * 虽然CrimeLab单例不是数据持久保存的好方案，
     * 但它确实能保证仅拥有一份crime数据，并且能让控制器层类间的数据传递更容易。
     */

    private static CrimeLab sCrimeLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    private CrimeLab(Context context) {
        /**
         * CrimeLab是一个单例。这表明，一旦创建，它就会一直存在，直至整个应用进程被销毁。
         * 由代码可知， CrimeLab引用着mContext对象。
         * 显然，如果把activity作为mContext对象保存的话，这个由CrimeLab一直引用着的activity肯定会免遭垃圾回收器的清理，即便用户跳转离开这个activity时也是如此。
         * 为了避免资源浪费，我们使用了应用上下文。这样， CrimeLab仍可以引用Context对象，而activity的生死也不用受它束缚了。
         */
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
    }

    public static CrimeLab getInstance(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    public void addCrime(Crime crime) {
        ContentValues values = getContentValues(crime);

        /**
         * insert(String, String, ContentValues)方法的第一和第三个参数很重要，第二个很少用到。
         * 传入的第一个参数是数据表名（CrimeTable.NAME），第三个是要写入的数据。
         *
         */
        mDatabase.insert(CrimeTable.NAME, null, values);
    }

    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();
        CrimeCursorWrapper cursor = queryCrimes(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return crimes;
    }

    public Crime getCrime(UUID id) {

        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?",
                    new String[] {id.toString()});

        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    public File getPhotoFile(Crime crime) {
        File fileDir = mContext.getFilesDir();
        return new File(fileDir, crime.getPhotoFilename());
    }

    public void updateCrime(Crime crime) {
        String uuidStr = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        /**
         * update(String, ContentValues, String, String[])方法类似于insert(...)方法，向其传入要更新的数据表名和为表记录准备的ContentValues。
         * 然而，与insert(...)方法不同的是，你要确定该更新哪些记录。
         * 具体的做法是： 创建where子句（第三个参数），然后指定where子句中的参数值（String[]数组参数）。
         * 问题来了，为什么不直接在where子句中放入uuidString呢？这可比使用?然后传入String[]简单多了！
         * 事实上，很多时候， String本身会包含SQL代码。如果将它直接放入query语句中，这些代码可能会改变query语句的含义，甚至会修改数据库资料。
         * 这实际就是SQL脚本注入，其危害相当严重。
         * 使用?的话，就不用关心String包含什么，代码执行的效果肯定就是我们想要的。因此，建议你保持这种良好的编码习惯。
         */
        mDatabase.update(CrimeTable.NAME, values, CrimeTable.Cols.UUID + " = ?", new String[]{uuidStr});
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {

        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,// table name
                null,// columns - null selects all columns
                whereClause,
                whereArgs,
                null,// groupBy
                null,// having
                null// orderBy
        );

        return new CrimeCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Crime crime) {
        /**
         * 负责处理数据库写入和更新操作的辅助类是ContentValues。
         * 它是一个键值存储类，类似于Java的HashMap和前面用过的Bundle。
         * 不同的是， ContentValues只能用于处理SQLite数据。
         */
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());

        return values;
    }

}
