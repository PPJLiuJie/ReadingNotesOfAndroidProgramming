package android.me.lj.criminalintent.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018/3/7.
 */

public class DateFormatUtil {

    public static String format(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd EEEE");
        // 设置时区为东八区
        format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String dateStr = format.format(date);
        return dateStr;
    }

}
