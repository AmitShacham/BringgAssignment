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

    public void setDate(Date date) {
        mDate = date;
    }

    public long getTotalWorkTime() {
        return mTotalWorkTime;
    }

    public void setTotalWorkTime(long totalWorkTime) {
        mTotalWorkTime = totalWorkTime;
    }
}
