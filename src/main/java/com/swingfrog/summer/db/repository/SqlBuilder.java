package com.swingfrog.summer.db.repository;

import java.util.Iterator;
import java.util.List;

public class SqlBuilder {

    public static String getTableExists(String tableName) {
        return "SHOW TABLES LIKE '" + tableName + "';";
    }

    public static String getTableExistsList(String tableName) {
        return "SHOW TABLES LIKE '" + tableName + "%';";
    }

    public static String getTableColumn(String tableName) {
        return "SELECT column_name FROM information_schema.columns WHERE table_name='" + tableName + "';";
    }

    public static String getCount(String tableName) {
        return "SELECT COUNT(1) FROM `" + tableName + "`;";
    }

    public static String getCreateTable(TableMeta tableMeta, String tableName) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (\n");
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

    public static String getDropTable(String tableName) {
        return "DROP TABLE IF EXISTS `" + tableName + "`;";
    }

    public static String getCreateTable(TableMeta tableMeta) {
        return getCreateTable(tableMeta, tableMeta.getName());
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

    public static String getAddColumn(TableMeta.ColumnMeta columnMeta, String tableName) {
        return "ALTER TABLE `" + tableName + "`\n" +
                " ADD COLUMN" + getCreateColumn(columnMeta) +
                ";";
    }

    public static String getAddColumn(TableMeta tableMeta, TableMeta.ColumnMeta columnMeta) {
        return getAddColumn(columnMeta, tableMeta.getName());
    }

    public static String getAddColumnIndex(TableMeta.ColumnMeta columnMeta, String tableName) {
        return "ALTER TABLE `" + tableName + "`\n" +
                " ADD " + columnMeta.getIndex() + " INDEX `idx_" +
                columnMeta.getName() + "` (`" + columnMeta.getName() + "`) USING BTREE;";
    }

    public static String getAddColumnIndex(TableMeta tableMeta, TableMeta.ColumnMeta columnMeta) {
        return getAddColumnIndex(columnMeta, tableMeta.getName());
    }

    public static String getInsert(TableMeta tableMeta, String tableName) {
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO `").append(tableName).append("` (");
        builder.append("`").append(tableMeta.getPrimaryColumn().getName()).append("`");
        tableMeta.getColumns().forEach(columnMeta -> builder.append(",`").append(columnMeta.getName()).append("`"));
        builder.append(") VALUES(?");
        tableMeta.getColumns().forEach(columnMeta -> builder.append(",?"));
        builder.append(");");
        return builder.toString();
    }

    public static String getInsert(TableMeta tableMeta) {
        return getInsert(tableMeta, tableMeta.getName());
    }

    public static String getReplace(TableMeta tableMeta, String tableName) {
        StringBuilder builder = new StringBuilder();
        builder.append("REPLACE INTO `").append(tableName).append("` (");
        builder.append("`").append(tableMeta.getPrimaryColumn().getName()).append("`");
        tableMeta.getColumns().forEach(columnMeta -> builder.append(",`").append(columnMeta.getName()).append("`"));
        builder.append(") VALUES(?");
        tableMeta.getColumns().forEach(columnMeta -> builder.append(",?"));
        builder.append(");");
        return builder.toString();
    }

    public static String getReplace(TableMeta tableMeta) {
        return getReplace(tableMeta, tableMeta.getName());
    }

    public static String getDelete(TableMeta tableMeta, String tableName) {
        return "DELETE FROM `" + tableName + "` WHERE `" +
                tableMeta.getPrimaryColumn().getName() + "` = ?;";
    }

    public static String getDelete(TableMeta tableMeta) {
        return getDelete(tableMeta, tableMeta.getName());
    }

    public static String getDeleteAll(String tableName) {
        return String.format("DELETE FROM `%s`;", tableName);
    }

    public static String getDeleteAll(TableMeta tableMeta) {
        return getDeleteAll(tableMeta.getName());
    }

    public static String getUpdate(TableMeta tableMeta, String tableName) {
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE `").append(tableName).append("`");
        if (!tableMeta.getColumns().isEmpty()) {
            builder.append(" SET");
            Iterator<TableMeta.ColumnMeta> iterator = tableMeta.getColumns().iterator();
            if (iterator.hasNext()) {
                TableMeta.ColumnMeta columnMeta = iterator.next();
                outLayer: for (;;) {
                    if (columnMeta.isReadOnly()) {
                        if (iterator.hasNext()) {
                            columnMeta = iterator.next();
                        } else {
                            break;
                        }
                    } else {
                        builder.append(" `").append(columnMeta.getName()).append("` = ?");
                        for (;;) {
                            if (iterator.hasNext()) {
                                columnMeta = iterator.next();
                                if (!columnMeta.isReadOnly()) {
                                    builder.append(",");
                                    break;
                                }
                            } else {
                                break outLayer;
                            }
                        }
                    }
                }
            }
        }
        builder.append(" WHERE `").append(tableMeta.getPrimaryColumn().getName()).append("` = ?;");
        return builder.toString();
    }

    public static String getUpdate(TableMeta tableMeta) {
        return getUpdate(tableMeta, tableMeta.getName());
    }

    public static String getSelect(TableMeta tableMeta, String tableName) {
        return "SELECT * FROM `" + tableName + "` WHERE `" +
                tableMeta.getPrimaryColumn().getName() + "` = ?;";
    }

    public static String getSelect(TableMeta tableMeta) {
        return getSelect(tableMeta, tableMeta.getName());
    }

    public static String getSelectField(List<String> fields, String tableName) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * FROM `").append(tableName).append("`");
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

    public static String getSelectField(TableMeta tableMeta, List<String> fields) {
        return getSelectField(fields, tableMeta.getName());
    }

    public static String getSelectAll(String tableName) {
        return "SELECT * FROM `" + tableName + "`;";
    }

    public static String getSelectAll(TableMeta tableMeta) {
        return getSelectAll(tableMeta.getName());
    }

    public static String getMaxPrimaryKey(TableMeta tableMeta, String tableName) {
        return "SELECT MAX(`" + tableMeta.getPrimaryColumn().getName() + "`) FROM `" +
                tableName + "`;";
    }

    public static String getMaxPrimaryKey(TableMeta tableMeta) {
        return getMaxPrimaryKey(tableMeta, tableMeta.getName());
    }

    public static String getPrimaryColumnSelectField(TableMeta tableMeta, List<String> fields, String tableName) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT `").append(tableMeta.getPrimaryColumn().getName()).append("` FROM `")
                .append(tableName).append("`");
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

    public static String getPrimaryColumnSelectField(TableMeta tableMeta, List<String> fields) {
        return getPrimaryColumnSelectField(tableMeta, fields, tableMeta.getName());
    }

}
