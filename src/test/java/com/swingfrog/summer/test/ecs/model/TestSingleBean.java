package com.swingfrog.summer.test.ecs.model;

import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.Table;
import com.swingfrog.summer.ecs.quick.QuickSingleBean;

@Table(name = "t_test_single_bean")
public class TestSingleBean extends QuickSingleBean {

    @Column
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "TestSingleBean{" +
                "content='" + content + '\'' +
                "} " + super.toString();
    }

}
