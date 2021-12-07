package com.swingfrog.summer.test.repository.model;

import com.swingfrog.summer.db.repository.annotation.*;

import java.util.Date;

@Table(name = "t_test_log", comment = "测试-日志")
public class TestLog {

    @PrimaryKey
    @Column(comment = "ID")
    private long id;

    @Column(comment = "值", readOnly = true)
    private int value;

    @Column(comment = "时间", readOnly = true)
    private Date time;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

}
