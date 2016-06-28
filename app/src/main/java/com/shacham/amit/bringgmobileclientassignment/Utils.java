package com.shacham.amit.bringgmobileclientassignment;

import java.util.Date;

public class Utils {

    public static long getTotalWorkTimeInHours(Date start, Date end) {
        long diff = end.getTime() - start.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        return minutes / 60;
    }
}
