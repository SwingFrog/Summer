package com.swingfrog.summer.test.ecsgameserver.module.player.item;

import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.PrimaryKey;
import com.swingfrog.summer.db.repository.annotation.Table;
import com.swingfrog.summer.test.ecsgameserver.module.player.base.PlayerMultiBean;

@Table
public class PlayerItem extends PlayerMultiBean {

    @PrimaryKey
    @Column
    private long id;

    @Column
    private int itemId;

    @Column
    private int itemCount;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

}
