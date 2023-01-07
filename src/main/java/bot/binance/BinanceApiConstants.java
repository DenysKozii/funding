package bot.binance;

import org.apache.commons.lang3.builder.ToStringStyle;

public class BinanceApiConstants {
    public static final String API_BASE_URL = "https://fapi.binance.com";
    public static final String WS_API_BASE_URL = "wss://fstream.binance.com/ws";
    public static final String API_KEY_HEADER = "X-MBX-APIKEY";
    public static final String ENDPOINT_SECURITY_TYPE_APIKEY = "APIKEY";
    public static final String ENDPOINT_SECURITY_TYPE_APIKEY_HEADER = "APIKEY: #";
    public static final String ENDPOINT_SECURITY_TYPE_SIGNED = "SIGNED";
    public static final String ENDPOINT_SECURITY_TYPE_SIGNED_HEADER = "SIGNED: #";
    public static final long DEFAULT_RECEIVING_WINDOW = 80000L;
    public static ToStringStyle TO_STRING_BUILDER_STYLE;

    public BinanceApiConstants() {
    }

    static {
        TO_STRING_BUILDER_STYLE = ToStringStyle.SHORT_PREFIX_STYLE;
    }
}

