package com.swingfrog.summer.db.repository;

import java.util.Iterator;
import java.util.List;

public class SqlBuilder {

    public static String getTableExists(String tableName) {
        return String.format("SHOW TABLES LIKE '%s';", tableName);
    }

    public static String getTableColumn(String tableName) {
        return String.format("SELECT column_name FROM information_schema.columns WHERE table_name='%s';", tableName);
    }

    public static String getCreateTable(TableMeta tableMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS `").append(tableMeta.getName()).append("` (\n");
        builder.append(" ").append(getCreateColumn(tableMeta.getPrimaryColumn())).append(",\n");
        tableMeta.getColumns().forEach(columnMeta ->
                builder.append(" ").append(getCreateColumn(columnMeta)).append(",\n"));
        builder.append(" PRIMARY KEY (`").append(tableMeta.getPrimaryColumn().getName()).append("`)");
        tableMeta.getIndexKeys().forEach(columnMeta ->
                builder.append(",\n").append(columnMeta.getIndex()).append(" INDEX `idx_").append(columnMeta.getName())
                        .append("` (`").append(columnMeta.getName()).append("`) USING BTREE "));
        builder.append("\n)");
        if (tableMeta.getCharset() != null) {
            builder.append(" DEFAULT CHARACTER SET=").append(tableMeta.getCharset());
            if (tableMeta.getCollate() != null) {
                builder.append(" COLLATE=").append(tableMeta.getCollate());
            }
        }
        if (tableMeta.getComment() != null) {
            builder.append("COMMENT='").append(tableMeta.getComment()).append("'");
        }
        builder.append(";");
        return builder.toString();
    }

    private static String getCreateColumn(TableMeta.ColumnMeta columnMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append(" `").append(columnMeta.getName()).append("` ").append(columnMeta.getType());
        if (columnMeta.isUnsigned()) {
            builder.append(" unsigned");
        }
        if (columnMeta.isNonNull()) {
            builder.append(" NOT NULL");
        } else {
            builder.append(" NULL");
        }
        if (columnMeta.getDefaultValue() != null) {
            builder.append(" DEFAULT '").append(columnMeta.getDefaultValue()).append("' ");
        }
        if (columnMeta.getComment() != null) {
            builder.append(" COMMENT '").append(columnMeta.getComment()).append("'");
        }
        return builder.toString();
    }

    public static String getAddColumn(TableMeta tableMeta, TableMeta.ColumnMeta columnMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append("ALTER TABLE `").append(tableMeta.getName()).append("`\n");
        builder.append(" ADD COLUMN").append(getCreateColumn(columnMeta));
        builder.append(";");
        return builder.toString();
    }

    public static String getAddColumnIndex(TableMeta tableMeta, TableMeta.ColumnMeta columnMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append("ALTER TABLE `").append(tableMeta.getName()).append("`\n");
        builder.append(" ADD ").append(columnMeta.getIndex()).append(" INDEX `idx_")
                .append(columnMeta.getName()).append("` (`").append(columnMeta.getName()).append("`) USING BTREE;");
        return builder.toString();
    }

    public static String getInsert(TableMeta tableMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO `").append(tableMeta.getName()).append("` (");
        builder.append("`").append(tableMeta.getPrimaryColumn().getName()).append("`");
        tableMeta.getColumns().forEach(columnMeta -> builder.append(",`").append(columnMeta.getName()).append("`"));
        builder.append(") VALUES(?");
        tableMeta.getColumns().forEach(columnMeta -> builder.append(",?"));
        builder.append(");");
        return builder.toString();
    }

    public static String getDelete(TableMeta tableMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM `").append(tableMeta.getName()).append("` WHERE `")
                .append(tableMeta.getPrimaryColumn().getName()).append("` = ?;");
        return builder.toString();
    }

    public static String getUpdate(TableMeta tableMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE `").append(tableMeta.getName()).append("`");
        if (!tableMeta.getColumns().isEmpty()) {
            builder.append(" SET");
            Iterator<TableMeta.ColumnMeta> iterator = tableMeta.getColumns().iterator();
            if (iterator.hasNext()) {
                while (true) {
                    TableMeta.ColumnMeta columnMeta = iterator.next();
                    if (columnMeta.isReadOnly()) {
                        if (!iterator.hasNext()) {
                            break;
                        }
                    } else{
                        builder.append(" `").append(columnMeta.getName()).append("` = ?");
                        if (iterator.hasNext()) {
                            builder.append(",");
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        builder.append(" WHERE `").append(tableMeta.getPrimaryColumn().getName()).append("` = ?;");
        return builder.toString();
    }

    public static String getSelect(TableMeta tableMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * FROM `").append(tableMeta.getName()).append("` WHERE `")
                .append(tableMeta.getPrimaryColumn().getName()).append("` = ?;");
        return builder.toString();
    }

    public static String getSelectField(TableMeta tableMeta, List<String> fields) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * FROM `").append(tableMeta.getName()).append("`");
        Iterator<String> iterator = fields.iterator();
        if (iterator.hasNext()) {
            builder.append(" WHERE");
            while (true) {
                String key = iterator.next();
                builder.append(" `").append(key).append("` = ?");
                if (iterator.hasNext()) {
                    builder.append(" and");
                } else {
                    break;
                }
            }
        }
        builder.append(";");
        return builder.toString();
    }

    public static String getSelectAll(TableMeta tableMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * FROM `").append(tableMeta.getName()).append("`;");
        return builder.toString();
    }

    public static String getMaxPrimaryKey(TableMeta tableMeta) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT MAX(`").append(tableMeta.getPrimaryColumn().getName()).append("`) FROM `")
                .append(tableMeta.getName()).append("`;");
        return builder.toString();
    }

    public static String getPrimaryColumnSelectField(TableMeta tableMeta, List<String> fields) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT `").append(tableMeta.getPrimaryColumn().getName()).append("` FROM `")
                .append(tableMeta.getName()).append("`");
        Iterator<String> iterator = fields.iterator();
        if (iterator.hasNext()) {
            builder.append(" WHERE");
            while (true) {
                String key = iterator.next();
                builder.append(" `").append(key).append("` = ?");
                if (iterator.hasNext()) {
                    builder.append(" and");
                } else {
                    break;
                }
            }
        }
        builder.append(";");
        return builder.toString();
    }

}
