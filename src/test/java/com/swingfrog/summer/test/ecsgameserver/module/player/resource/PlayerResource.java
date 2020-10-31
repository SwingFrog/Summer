package com.swingfrog.summer.test.ecsgameserver.module.player.resource;

import com.swingfrog.summer.db.repository.annotation.Column;
import com.swingfrog.summer.db.repository.annotation.Table;
import com.swingfrog.summer.test.ecsgameserver.module.player.base.PlayerBean;

@Table
public class PlayerResource extends PlayerBean {

    @Column
    private long gold;

    @Column
    private int friendly;

    public long getGold() {
        return gold;
    }

    public void setGold(long gold) {
        this.gold = gold;
    }

    public int getFriendly() {
        return friendly;
    }

    public void setFriendly(int friendly) {
        this.friendly = friendly;
    }

}
