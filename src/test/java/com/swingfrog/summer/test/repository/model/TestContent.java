package com.swingfrog.summer.test.repository.model;

import com.swingfrog.summer.db.repository.annotation.Column;

public abstract class TestContent {

    @Column(comment = "内容")
    protected String content;

}
