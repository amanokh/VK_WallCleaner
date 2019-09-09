package ru.excalc.vk282.data;

import android.provider.BaseColumns;

public class DbValues {

    public static final class PostsEntry implements BaseColumns {
        public static final String TABLE_NAME = "posts";
        public static final String COLUMN_POST_ID = "pid";
        public static final String COLUMN_TIMESTAMP = "time";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_FROM_ID = "fromid";
    }
    public static final class PublicsEntry implements BaseColumns {
        public static final String TABLE_NAME = "publics";
        public static final String COLUMN_PUBLIC_ID = "publicid";
        public static final String COLUMN_PUBLIC_NAME = "name";
        public static final String COLUMN_QUANTITY = "quant";
    }

}
