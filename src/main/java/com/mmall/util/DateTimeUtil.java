package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by hasee on 2018/4/30.
 */
public class DateTimeUtil {

    private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

//    使用的Date和DateTime类型是joda下的
    public static Date strToDate(String dateTimeStr,String formatStr){
        DateTimeFormatter formatter = DateTimeFormat.forPattern(formatStr);
        return formatter.parseDateTime(dateTimeStr).toDate();
    }

    public static String dateToStr(Date date,String formatStr){
        return date == null ? StringUtils.EMPTY : new DateTime(date).toString(formatStr);
    }

    public static Date strToDate(String dateTimeStr){
        DateTimeFormatter formatter = DateTimeFormat.forPattern(STANDARD_FORMAT);
        return formatter.parseDateTime(dateTimeStr).toDate();
    }

    public static String dateToStr(Date date){
        return date == null ? StringUtils.EMPTY : new DateTime(date).toString(STANDARD_FORMAT);
    }
}
