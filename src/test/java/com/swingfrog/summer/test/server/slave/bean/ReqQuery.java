package com.swingfrog.summer.test.server.slave.bean;

public class ReqQuery {

    private int id;
    private int page;

    public ReqQuery(int id, int page) {
        this.id = id;
        this.page = page;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public String toString() {
        return "ReqQuery{" +
                "id=" + id +
                ", page=" + page +
                '}';
    }

}
