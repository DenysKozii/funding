package bot.binance;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ResponseResult {
    private int code;
    private String msg;

    public ResponseResult() {
    }

    public ResponseResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String toString() {
        return (new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)).append("code", this.code).append("msg", this.msg).toString();
    }
}

