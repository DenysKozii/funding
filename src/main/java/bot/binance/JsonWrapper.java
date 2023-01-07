package bot.binance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonWrapper {
    private final JSONObject json;

    public static JsonWrapper parseFromString(String text) {
        try {
            JSONObject jsonObject;
            if (JSON.parse(text) instanceof JSONArray) {
                jsonObject = (JSONObject)JSON.parse("{data:" + text + "}");
            } else {
                jsonObject = (JSONObject)JSON.parse(text);
            }

            if (jsonObject != null) {
                return new JsonWrapper(jsonObject);
            } else {
                throw new BinanceApiException("RuntimeError", "[Json] Unknown error when parse: " + text);
            }
        } catch (JSONException var2) {
            throw new BinanceApiException("RuntimeError", "[Json] Fail to parse json: " + text);
        } catch (Exception var3) {
            throw new BinanceApiException("RuntimeError", "[Json] " + var3.getMessage());
        }
    }

    public JsonWrapper(JSONObject json) {
        this.json = json;
    }

    private void checkMandatoryField(String name) {
        if (!this.json.containsKey(name)) {
            throw new BinanceApiException("RuntimeError", "[Json] Get json item field: " + name + " does not exist");
        }
    }

    public boolean containKey(String name) {
        return this.json.containsKey(name);
    }

    public String getString(String name) {
        this.checkMandatoryField(name);

        try {
            return this.json.getString(name);
        } catch (Exception var3) {
            throw new BinanceApiException("RuntimeError", "[Json] Get string error: " + name + " " + var3.getMessage());
        }
    }

    public String getStringOrDefault(String name, String def) {
        return !this.containKey(name) ? def : this.getString(name);
    }

    public Boolean getBooleanOrDefault(String name, Boolean defaultValue) {
        return !this.containKey(name) ? defaultValue : this.getBoolean(name);
    }

    public boolean getBoolean(String name) {
        this.checkMandatoryField(name);

        try {
            return this.json.getBoolean(name);
        } catch (Exception var3) {
            throw new BinanceApiException("RuntimeError", "[Json] Get boolean error: " + name + " " + var3.getMessage());
        }
    }

    public int getInteger(String name) {
        this.checkMandatoryField(name);

        try {
            return this.json.getInteger(name);
        } catch (Exception var3) {
            throw new BinanceApiException("RuntimeError", "[Json] Get integer error: " + name + " " + var3.getMessage());
        }
    }

    public Integer getIntegerOrDefault(String name, Integer defValue) {
        try {
            return !this.containKey(name) ? defValue : this.json.getInteger(name);
        } catch (Exception var4) {
            throw new BinanceApiException("RuntimeError", "[Json] Get integer error: " + name + " " + var4.getMessage());
        }
    }

    public long getLong(String name) {
        this.checkMandatoryField(name);

        try {
            return this.json.getLong(name);
        } catch (Exception var3) {
            throw new BinanceApiException("RuntimeError", "[Json] Get long error: " + name + " " + var3.getMessage());
        }
    }

    public long getLongOrDefault(String name, long defValue) {
        try {
            return !this.containKey(name) ? defValue : this.json.getLong(name);
        } catch (Exception var5) {
            throw new BinanceApiException("RuntimeError", "[Json] Get long error: " + name + " " + var5.getMessage());
        }
    }

    public Double getDouble(String name) {
        this.checkMandatoryField(name);

        try {
            return this.json.getDouble(name);
        } catch (Exception var3) {
            throw new BinanceApiException("RuntimeError", "[Json] Get double error: " + name + " " + var3.getMessage());
        }
    }

    public Double getDoubleOrDefault(String name, Double defValue) {
        try {
            return !this.containKey(name) ? defValue : this.json.getDouble(name);
        } catch (Exception var4) {
            throw new BinanceApiException("RuntimeError", "[Json] Get double error: " + name + " " + var4.getMessage());
        }
    }

    public BigDecimal getBigDecimal(String name) {
        this.checkMandatoryField(name);

        try {
            return new BigDecimal(this.json.getBigDecimal(name).stripTrailingZeros().toPlainString());
        } catch (Exception var3) {
            throw new BinanceApiException("RuntimeError", "[Json] Get decimal error: " + name + " " + var3.getMessage());
        }
    }

    public BigDecimal getBigDecimalOrDefault(String name, BigDecimal defValue) {
        if (!this.containKey(name)) {
            return defValue;
        } else {
            try {
                return new BigDecimal(this.json.getBigDecimal(name).stripTrailingZeros().toPlainString());
            } catch (Exception var4) {
                throw new BinanceApiException("RuntimeError", "[Json] Get decimal error: " + name + " " + var4.getMessage());
            }
        }
    }

    public JsonWrapper getJsonObject(String name) {
        this.checkMandatoryField(name);
        return new JsonWrapper(this.json.getJSONObject(name));
    }

    public JSONObject convert2JsonObject() {
        return this.json;
    }

    public void getJsonObject(String name, Handler<JsonWrapper> todo) {
        this.checkMandatoryField(name);
        todo.handle(new JsonWrapper(this.json.getJSONObject(name)));
    }

    public JsonWrapperArray getJsonArray(String name) {
        this.checkMandatoryField(name);
        JSONArray array = null;

        try {
            array = this.json.getJSONArray(name);
        } catch (Exception var4) {
            throw new BinanceApiException("RuntimeError", "[Json] Get array: " + name + " error");
        }

        if (array == null) {
            throw new BinanceApiException("RuntimeError", "[Json] Array: " + name + " does not exist");
        } else {
            return new JsonWrapperArray(array);
        }
    }

    public JSONObject getJson() {
        return this.json;
    }

    public List<Map<String, String>> convert2DictList() {
        List<Map<String, String>> result = new LinkedList();
        Set<String> keys = this.json.keySet();
        keys.forEach((key) -> {
            Map<String, String> temp = new LinkedHashMap();
            temp.put(key, this.getString(key));
            result.add(temp);
        });
        return result;
    }
}

