package com.zhukai.project.partner.server.wrapper;

/**
 * Created by homolo on 17-8-7.
 */
public class RestResult {
    private int code;
    private String text;

    public RestResult(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
