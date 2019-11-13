package com.vo;

/**
 * @author 12130
 * @date 2019/11/13
 * @time 21:17
 */
public class ResponseVo {
    int code;
    String message;
    Object data;

    public ResponseVo(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
