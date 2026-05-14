package com.quizai.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果类
 * @param <T> 数据类型
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码：200成功；500 失败
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 业务数据
     */
    private T data;

    public R() {
    }

    public R(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功返回（无数据）
     */
    public static <T> R<T> success() {
        return new R<>(200, "success", null);
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> R<T> success(T data) {
        return new R<>(200, "success", data);
    }

    /**
     * 成功返回（带消息和数据）
     */
    public static <T> R<T> success(String msg, T data) {
        return new R<>(200, msg, data);
    }

    /**
     * 失败返回
     */
    public static <T> R<T> error() {
        return new R<>(500, "error", null);
    }

    /**
     * 失败返回（带消息）
     */
    public static <T> R<T> error(String msg) {
        return new R<>(500, msg, null);
    }

    /**
     * 失败返回（带消息和数据）
     */
    public static <T> R<T> error(String msg, T data) {
        return new R<>(500, msg, data);
    }
}
