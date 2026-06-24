package com.tx.outsourcingmis.common;

import lombok.Data;

/**
 * 统一响应封装
 *
 * <p>使用示例：
 * <pre>
 * // 成功响应
 * ResultVO.success(data);           // code=200, msg="success"
 * ResultVO.success();               // code=200, msg="success", data=null
 *
 * // 失败响应
 * ResultVO.error("错误信息");        // code=500
 * ResultVO.error(403, "无权限");     // 自定义错误码
 * </pre>
 *
 * @param <T> 响应数据类型
 */
@Data
public class ResultVO<T> {

    /** 成功状态码 */
    public static final int CODE_SUCCESS = 200;

    /** 失败状态码 */
    public static final int CODE_ERROR = 500;

    /** 响应状态码 */
    private Integer code;

    /** 响应消息 */
    private String msg;

    /** 响应数据 */
    private T data;

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return ResultVO
     */
    public static <T> ResultVO<T> success(T data) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(CODE_SUCCESS);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return ResultVO
     */
    public static <T> ResultVO<T> success() {
        return success(null);
    }

    /**
     * 失败响应（默认 500 状态码）
     *
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return ResultVO
     */
    public static <T> ResultVO<T> error(String msg) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(CODE_ERROR);
        result.setMsg(msg);
        return result;
    }

    /**
     * 失败响应（自定义状态码）
     *
     * @param code 状态码
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return ResultVO
     */
    public static <T> ResultVO<T> error(Integer code, String msg) {
        ResultVO<T> result = new ResultVO<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}