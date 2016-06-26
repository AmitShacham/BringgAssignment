package com.shacham.amit.bringgmobileclientassignment;

import java.util.Date;

public class DayStatistics {

    private Date mDate;
    private long mTotalWorkTime;

    public DayStatistics(Date date, long totalWorkTime) {
        mDate = date;
        mTotalWorkTime = totalWorkTime;
    }

    public Date getDate() {
        return mDate;
    }

    public long getTotalWorkTime() {
        return mTotalWorkTime;
    }
}
