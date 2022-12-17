package com.chen.community.entity;

import java.util.Map;

/**
 * 封装分页相关信息
 */
public class Page {
    // 当前页码
    private int current = 1;

    // 每页显示上限
    private int limit = 10;

    // 数据的总数（用户计算总的页数）
    private int rows;

    // 查询路径（用来复用分页的链接）
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     *  根据当前页和limit算出要查询的起始行offset。offset = current * limit - limit;  limit:每页最多显示的行数
     * @return
     */
    public int getOffset() {
        return (current - 1) * limit;
    }

    /**
     * 获取总的页数（传给前端的）
     * @return
     */
    public int getTotal() {
        if (rows % limit == 0) {
            return rows / limit;
        }
        return rows / limit + 1;
    }

    /**
     * 获取起始页码，因为前端显示页面选择选项时，要限制可选的页面数目，比如可以显示1 2 3 4 5，但如果显示1 ~ 100就太多了。因此这里要做一个限制
     * @return
     */
    public int getFrom() {
        int from = current - 2;
        return from > 1? from : 1;
    }

    /**
     * 获取显示页码数量的结束页码
     * @return
     */
    public int getTo() {
        int To = current + 2;
        return To > getTotal()? getTotal() : To;
    }
}
