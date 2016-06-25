package com.shacham.amit.bringgmobileclientassignment;

import java.util.Date;

public class DayStatistics {

    private Date mDate;
    private Date mStartWorkTime;
    private Date mEndWorkTime;

    public DayStatistics(Date date, Date startWorkTime, Date endWorkTime) {
        mDate = date;
        mStartWorkTime = startWorkTime;
        mEndWorkTime = endWorkTime;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public Date getStartWorkTime() {
        return mStartWorkTime;
    }

    public void setStartWorkTime(Date startWorkTime) {
        mStartWorkTime = startWorkTime;
    }

    public Date getEndWorkTime() {
        return mEndWorkTime;
    }

    public void setEndWorkTime(Date endWorkTime) {
        mEndWorkTime = endWorkTime;
    }
}
