package com.al.dbspider.plugin;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Properties;


/**
 * 增加了在使用MyBatis Cache时数据刷新的功能
 * <p>
 * 使用Cache时，建议使用Apache ignite!
 * <p>
 * Created on Create on 2017/10/24
 *
 * @author Asin Liu
 * @version 1.0
 * @since 1.0
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PagingInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(PagingInterceptor.class);

    private String dialect = "mysql"; //数据库方言
    private String idSuffix = "ByPage"; //mapper.xml中需要拦截的ID(正则匹配)
    private String primitiveSQL = "";
    private Page page = null;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        StatementHandler handler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(handler);
        MappedStatement statement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        String mapId = statement.getId();
        if (mapId.matches(".+" + idSuffix)) {
            BoundSql boundSql = handler.getBoundSql();
            //Object obj = boundSql.getParameterObject();
            // Get the primitive jdbc sql
            primitiveSQL = boundSql.getSql();
            String countSql = getCountSQL(primitiveSQL);
            Connection conn = (Connection) invocation.getArgs()[0];
            PreparedStatement countStatement = conn.prepareStatement(countSql);
            ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue("delegate.parameterHandler");
            parameterHandler.setParameters(countStatement);
            ResultSet rs = countStatement.executeQuery();

            if (rs.next()) {
                long cnt = rs.getLong(1);
                if (cnt < 1) {
                    return invocation.proceed();    // 可以在以后优化的点,这一次的查询就不需要了不是？？
                }

                // Get the parameter from mapper parameter
                Map<?, ?> param = (Map<?, ?>) boundSql.getParameterObject();
                page = (Page) param.get("page");
                if (page != null) {
                    page.setRowCount((int) cnt);
                    String pagingSQL = getPagingSql(page.getIndex(), page.getPageSize(), primitiveSQL);
                    logger.debug("执行:" + pagingSQL);
                    metaObject.setValue("delegate.boundSql.sql", pagingSQL);
                }
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
    }

    /**
     * Generate count SQL statement.
     *
     * @param primitiveSQL
     * @return count sql statement.
     */
    private String getCountSQL(final String primitiveSQL) {
        String cntSQL = primitiveSQL;
        // 有可能FROM 单独一行，并且没有进行首行缩进，将" FROM " 替换成"FROM "
        // Note: 不能处理:: 在FROM使用独立行且换行的情况
        int idx = cntSQL.toUpperCase().indexOf("FROM ");
        if (dialect.equalsIgnoreCase("mysql")) {
            cntSQL = "SELECT COUNT(*) " + primitiveSQL.substring(idx, primitiveSQL.length());
        } else {
            throw new IllegalArgumentException("Dialect " + dialect + " is not support!");
        }
        return cntSQL;
    }

    /**
     * Common method for generate a count sql statement.
     *
     * @return count sql statement.
     */
    @Deprecated
    private String getCountSql(String primitiveSQL) {
        String cntSQL = primitiveSQL;
        /*if (dialect.equalsIgnoreCase("mysql")) {
            if (cntSQL.endsWith(";")) {
                cntSQL = cntSQL.substring(0, primitiveSQL.length() - 1);
            }
            return " select count(1) from ("+ cntSQL + ")";
        }*/
        //throw new IllegalArgumentException("暂不支付的数据库");
        return " select count(1) from (" + cntSQL + ")";
    }

    /**
     * Generate Paging SQL command
     *
     * @param startIndex start index of data in data table.
     * @param pageSize   page size of the request.
     * @return paging sql command.
     */
    private String getPagingSql(int startIndex, int pageSize, String primitiveSQL) {
        String pgSQL = primitiveSQL;
        if (dialect.equalsIgnoreCase("mysql")) {
            if (this.page != null) {
                pgSQL = String.format("%s LIMIT %d , %d", pgSQL, startIndex, pageSize);
            } else {
                return pgSQL;
            }
        } else {
            throw new IllegalArgumentException("Unknown dialect " + dialect);
        }
        return pgSQL;
    }

}