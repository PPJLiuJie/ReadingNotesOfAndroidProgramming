package android.me.lj.criminalintent.database;

/**
 * Created by Administrator on 2018/3/8.
 */

public class CrimeDbSchema {

    public static final class CrimeTable {

        /**
         * 表名
         */
        public static final String NAME = "crimes";

        /**
         * 表中字段
         */
        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
        }
    }

}
