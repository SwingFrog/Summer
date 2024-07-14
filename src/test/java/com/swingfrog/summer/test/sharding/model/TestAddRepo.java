package com.swingfrog.summer.test.sharding.model;

import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.PrimaryKey;
import com.swingfrog.summer.db.repository.annotation.ShardingKey;
import com.swingfrog.summer.db.repository.annotation.Table;

import java.util.Date;

@Table(name = "t_test_sharding_add_repo", comment = "测试-分片")
public class TestAddRepo {

    @PrimaryKey
    @Column
    private int id;

    @ShardingKey
    @Column(readOnly = true)
    private int year;

    @ShardingKey
    @Column(readOnly = true)
    private int month;

    @ShardingKey
    @Column(readOnly = true)
    private int day;

    @Column
    private int value;

    public TestAddRepo() {}

    public TestAddRepo(int year, int month, int day, int value) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "TestAddRepo{" +
                "id=" + id +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", value=" + value +
                '}';
    }
}
