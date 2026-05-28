package com.isoft.consultant.common;

/**
 * 手动分页参数（不依赖 MyBatis-Plus 分页插件，避免 3.5.9+ PaginationInnerInterceptor 依赖问题）
 */
public final class PageQueryUtil {

    private PageQueryUtil() {
    }

    public static int safePage(int page) {
        return Math.max(page, 1);
    }

    public static int safeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    public static int offset(int page, int size) {
        return (safePage(page) - 1) * safeSize(size);
    }

    public static String mysqlLimit(int page, int size) {
        return "LIMIT " + offset(page, size) + "," + safeSize(size);
    }
}
