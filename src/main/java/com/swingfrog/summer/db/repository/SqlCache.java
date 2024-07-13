package com.swingfrog.summer.db.repository;

public class SqlCache {

    private String insert;
    private String replace;
    private String delete;
    private String deleteAll;
    private String update;
    private String select;
    private String selectAll;

    public String getInsert(TableMeta tableMeta, String tableName) {
        if (insert == null) {
            insert = SqlBuilder.getInsert(tableMeta, tableName);
        }
        return insert;
    }

    public String getReplace(TableMeta tableMeta, String tableName) {
        if (replace == null) {
            replace = SqlBuilder.getReplace(tableMeta, tableName);
        }
        return replace;
    }

    public String getDelete(TableMeta tableMeta, String tableName) {
        if (delete == null) {
            delete = SqlBuilder.getDelete(tableMeta, tableName);
        }
        return delete;
    }

    public String getDeleteAll(String tableName) {
        if (deleteAll == null) {
            deleteAll = SqlBuilder.getDeleteAll(tableName);
        }
        return deleteAll;
    }

    public String getUpdate(TableMeta tableMeta, String tableName) {
        if (update == null) {
            update = SqlBuilder.getUpdate(tableMeta, tableName);
        }
        return update;
    }

    public String getSelect(TableMeta tableMeta, String tableName) {
        if (select == null) {
            select = SqlBuilder.getSelect(tableMeta, tableName);
        }
        return select;
    }

    public String getSelectAll(String tableName) {
        if (selectAll == null) {
            selectAll = SqlBuilder.getSelectAll(tableName);
        }
        return selectAll;
    }

}
