package com.swingfrog.summer.test.repository.model;

import com.swingfrog.summer.db.repository.annotation.CacheKey;
import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.IndexKey;
import com.swingfrog.summer.db.repository.annotation.PrimaryKey;
import com.swingfrog.summer.db.repository.annotation.Table;

@Table(name = "t_test", comment = "测试")
public class Test {

    @PrimaryKey(auto = false)
    @Column(comment = "ID")
    private long id;

    @CacheKey
    @IndexKey
    @Column(comment = "类型", readOnly = true)
    private int type;

    @Column(comment = "内容")
    private String content;

    public Test() {

    }

    public Test(long id, int type, String content) {
        this.id = id;
        this.type = type;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Test{" +
                "id=" + id +
                ", type=" + type +
                ", content='" + content + '\'' +
                '}';
    }
}
