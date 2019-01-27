package com.example.sushiyu.smartshot;

/**
 * Created by sushiyu on 2018/7/3.
 */

import java.util.Calendar;


import com.example.sushiyu.smartshot.R;
import android.content.Context;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.Formatter;
import android.widget.NumberPicker.OnValueChangeListener;

public class DateTimepicker extends FrameLayout {

    private final NumberPicker mHourSpinner;
    private final NumberPicker mMinuteSpinner;
    private final NumberPicker mSecondSpinner;
    private Calendar mDate;
    private int mYear, mMonth, mDay, mHour, mMinute, mSecond;
    private OnDateTimeChangedListener mOnDateTimeChangedListener;

    public DateTimepicker(Context context, int hour, int minute, int second) {
        super(context);

        mHour = hour;
        mMinute = minute;
        mSecond = second;
        /*
        mDate = Calendar.getInstance();
        mYear = mDate.get(Calendar.YEAR);
        mMonth = mDate.get(Calendar.MONTH) + 1;
        mHour = mDate.get(Calendar.HOUR_OF_DAY);
        mMinute = mDate.get(Calendar.MINUTE);
        mSecond = mDate.get(Calendar.SECOND);
        */
        /**
         *
         */
        inflate(context, R.layout.numberpicker, this);
        /*
        mYearSpinner = (NumberPicker) this.findViewById(R.id.np_datetime_year);
        mYearSpinner.setMaxValue(2100);
        mYearSpinner.setMinValue(1900);
        mYearSpinner.setValue(mYear);
        mYearSpinner.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);//设置NumberPicker不可编辑
        mYearSpinner.setOnValueChangedListener(mOnYearChangedListener);//注册NumberPicker值变化时的监听事件

        mMonthSpinner = (NumberPicker) this.findViewById(R.id.np_datetime_month);
        mMonthSpinner.setMaxValue(12);
        mMonthSpinner.setMinValue(1);
        mMonthSpinner.setValue(mMonth);
        mMonthSpinner.setFormatter(formatter);//格式化显示数字，个位数前添加0
        mMonthSpinner.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mMonthSpinner.setOnValueChangedListener(mOnMonthChangedListener);

        mDaySpinner = (NumberPicker) this.findViewById(R.id.np_datetime_day);
        judgeMonth();//判断是否闰年，从而设置2月份的天数
        mDay = mDate.get(Calendar.DAY_OF_MONTH);
        mDaySpinner.setValue(mDay);
        mDaySpinner.setFormatter(formatter);
        mDaySpinner.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mDaySpinner.setOnValueChangedListener(mOnDayChangedListener);
        */
        mHourSpinner = (NumberPicker) this.findViewById(R.id.np_datetime_hour);
        mHourSpinner.setMaxValue(255);
        mHourSpinner.setMinValue(0);
        mHourSpinner.setValue(mHour);
        mHourSpinner.setFormatter(formatter);
        mHourSpinner.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mHourSpinner.setOnValueChangedListener(mOnHourChangedListener);

        mMinuteSpinner = (NumberPicker) this.findViewById(R.id.np_datetime_minute);
        mMinuteSpinner.setMaxValue(59);
        mMinuteSpinner.setMinValue(0);
        mMinuteSpinner.setValue(mMinute);
        mMinuteSpinner.setFormatter(formatter);
        mMinuteSpinner.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mMinuteSpinner.setOnValueChangedListener(mOnMinuteChangedListener);

        mSecondSpinner = (NumberPicker) this.findViewById(R.id.np_datetime_second);
        mSecondSpinner.setMaxValue(59);
        mSecondSpinner.setMinValue(0);
        mSecondSpinner.setValue(mSecond);
        mSecondSpinner.setFormatter(formatter);
        mSecondSpinner.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mSecondSpinner.setOnValueChangedListener(mOnSecondChangedListener);
    }

    private NumberPicker.OnValueChangeListener mOnHourChangedListener = new OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mHour = mHourSpinner.getValue();
            onDateTimeChanged();
        }
    };

    private NumberPicker.OnValueChangeListener mOnMinuteChangedListener = new OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mMinute = mMinuteSpinner.getValue();
            onDateTimeChanged();
        }
    };

    private NumberPicker.OnValueChangeListener mOnSecondChangedListener = new OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mSecond = mSecondSpinner.getValue();
            onDateTimeChanged();
        }
    };
    //数字格式化，<10的数字前自动加0
    private NumberPicker.Formatter formatter = new Formatter() {
        @Override
        public String format(int value) {
            String Str = String.valueOf(value);
            if (value < 10) {
                Str = "0" + Str;
            }
            return Str;
        }
    };

    /*
     *接口回调 参数是当前的View 年月日时分秒
     */
    public interface OnDateTimeChangedListener {
        void onDateTimeChanged(DateTimepicker view, int year, int month, int day, int hour, int minute, int second);
    }

    /*
     *对外的公开方法
     */
    public void setOnDateTimeChangedListener(OnDateTimeChangedListener callback) {
        mOnDateTimeChangedListener = callback;
    }

    private void onDateTimeChanged() {
        if (mOnDateTimeChangedListener != null) {
            mOnDateTimeChangedListener.onDateTimeChanged(this, mYear, mMonth, mDay, mHour, mMinute, mSecond);
        }
    }


}