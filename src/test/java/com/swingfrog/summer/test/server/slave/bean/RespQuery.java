package com.swingfrog.summer.test.server.slave.bean;

public class RespQuery {

    private int id;
    private String content;

    public RespQuery(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
        return "RespQuery{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }

}
