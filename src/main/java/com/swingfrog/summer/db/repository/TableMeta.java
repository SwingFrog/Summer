package com.swingfrog.summer.db.repository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableMeta {

    private String name;
    private String charset;
    private String collate;
    private String comment;
    private ColumnMeta primaryColumn;
    private List<ColumnMeta> columns;
    private Set<ColumnMeta> indexKeys;
    private Set<ColumnMeta> cacheKeys;
    private Map<String, ColumnMeta> columnMetaMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCollate() {
        return collate;
    }

    public void setCollate(String collate) {
        this.collate = collate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ColumnMeta getPrimaryColumn() {
        return primaryColumn;
    }

    public void setPrimaryColumn(ColumnMeta primaryColumn) {
        this.primaryColumn = primaryColumn;
    }

    public List<ColumnMeta> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnMeta> columns) {
        this.columns = columns;
    }

    public Set<ColumnMeta> getIndexKeys() {
        return indexKeys;
    }

    public void setIndexKeys(Set<ColumnMeta> indexKeys) {
        this.indexKeys = indexKeys;
    }

    public Set<ColumnMeta> getCacheKeys() {
        return cacheKeys;
    }

    public void setCacheKeys(Set<ColumnMeta> cacheKeys) {
        this.cacheKeys = cacheKeys;
    }

    public Map<String, ColumnMeta> getColumnMetaMap() {
        return columnMetaMap;
    }

    public void setColumnMetaMap(Map<String, ColumnMeta> columnMetaMap) {
        this.columnMetaMap = columnMetaMap;
    }

    public static class ColumnMeta {
        private String name;
        private String type;
        private boolean readOnly;
        private Field field;
        private boolean nonNull;
        private boolean unsigned;
        private int length;
        private boolean auto;
        private String index;
        private String comment;
        private boolean intNumber;
        private String defaultValue;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }

        public Field getField() {
            return field;
        }

        public void setField(Field field) {
            this.field = field;
        }

        public boolean isNonNull() {
            return nonNull;
        }

        public void setNonNull(boolean nonNull) {
            this.nonNull = nonNull;
        }

        public boolean isUnsigned() {
            return unsigned;
        }

        public void setUnsigned(boolean unsigned) {
            this.unsigned = unsigned;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public boolean isAuto() {
            return auto;
        }

        public void setAuto(boolean auto) {
            this.auto = auto;
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public boolean isIntNumber() {
            return intNumber;
        }

        public void setIntNumber(boolean intNumber) {
            this.intNumber = intNumber;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

}
