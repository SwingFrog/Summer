package com.swingfrog.summer.test.ecs.model;

import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.PrimaryKey;
import com.swingfrog.summer.db.repository.annotation.Table;
import com.swingfrog.summer.ecs.quick.QuickMultiBean;

@Table(name = "t_test_multi_bean")
public class TestMultiBean extends QuickMultiBean {

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
        return "TestMultiBean{" +
                "id=" + id +
                ", content='" + content + '\'' +
                "} " + super.toString();
    }

}
