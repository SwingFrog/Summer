package com.swingfrog.summer.test.ecs.model;

import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.PrimaryKey;
import com.swingfrog.summer.db.repository.annotation.Table;

@Table(name = "t_test_all_bean")
public class TestAllBean {

    @PrimaryKey
    @Column
    private long id;

    @Column
    private String content;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "TestAllBean{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }

}
