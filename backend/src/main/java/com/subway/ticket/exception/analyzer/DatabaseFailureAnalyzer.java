package com.subway.ticket.exception.analyzer;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

import java.sql.SQLException;

/**
 * 自定义数据库连接失败分析器
 * 当数据库配置错误（如密码错误、URL无法连接等）时，提供清晰的报错信息。
 */
public class DatabaseFailureAnalyzer extends AbstractFailureAnalyzer<SQLException> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, SQLException cause) {
        String description = "数据库连接失败，请检查配置信息。";
        String action = "1. 请检查 application-db.yml 中的数据库配置（URL, 用户名, 密码）。\n" +
                        "2. 确保数据库服务已启动并可访问。\n" +
                        "3. 如果是密码错误，请确认环境变量或配置文件中的密码是否正确。";

        // 可以根据具体的 SQLState 或 ErrorCode 细化错误信息
        String sqlState = cause.getSQLState();
        int errorCode = cause.getErrorCode();

        if ("28000".equals(sqlState)) {
            description = "数据库认证失败：用户名或密码错误。";
            action = "请检查 application-db.yml 中的 username 和 password 配置。";
        } else if ("08001".equals(sqlState) || "08004".equals(sqlState)) {
            description = "无法建立数据库连接：URL 错误或数据库服务器拒绝连接。";
            action = "请检查 application-db.yml 中的 url 配置，并确保数据库服务器已启动。";
        }

        return new FailureAnalysis(description, action, cause);
    }
}
