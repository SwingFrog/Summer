package com.swingfrog.summer.test.repository.model;

import com.swingfrog.summer.db.repository.annotation.CacheKey;
import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.IndexKey;
import com.swingfrog.summer.db.repository.annotation.PrimaryKey;
import com.swingfrog.summer.db.repository.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Table(name = "t_test", comment = "测试")
public class Test {

    @PrimaryKey
    @Column(comment = "ID")
    private long id;

    @CacheKey
    @IndexKey
    @Column(comment = "类型", readOnly = true)
    private int type;

    @Column(comment = "内容")
    private String content;

}
