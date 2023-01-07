package bot.binance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class JsonWrapperArray {
    private JSONArray array = null;

    public JsonWrapperArray(JSONArray array) {
        this.array = array;
    }

    public JsonWrapper getJsonObjectAt(int index) {
        if (this.array != null && this.array.size() > index) {
            JSONObject object = (JSONObject)this.array.get(index);
            if (object == null) {
                throw new BinanceApiException("RuntimeError", "[Json] Cannot get object at index " + index + " in array");
            } else {
                return new JsonWrapper(object);
            }
        } else {
            throw new BinanceApiException("RuntimeError", "[Json] Index is out of bound or array is null");
        }
    }

    public void add(JSON val) {
        this.array.add(val);
    }

    public JsonWrapperArray getArrayAt(int index) {
        if (this.array != null && this.array.size() > index) {
            JSONArray newArray = (JSONArray)this.array.get(index);
            if (newArray == null) {
                throw new BinanceApiException("RuntimeError", "[Json] Cannot get array at index " + index + " in array");
            } else {
                return new JsonWrapperArray(newArray);
            }
        } else {
            throw new BinanceApiException("RuntimeError", "[Json] Index is out of bound or array is null");
        }
    }

    private Object getObjectAt(int index) {
        if (this.array != null && this.array.size() > index) {
            return this.array.get(index);
        } else {
            throw new BinanceApiException("RuntimeError", "[Json] Index is out of bound or array is null");
        }
    }

    public long getLongAt(int index) {
        try {
            return (Long)this.getObjectAt(index);
        } catch (Exception var3) {
            throw new BinanceApiException("RuntimeError", "[Json] Cannot get long at index " + index + " in array: " + var3.getMessage());
        }
    }

    public Integer getIntegerAt(int index) {
        try {
            return (Integer)this.getObjectAt(index);
        } catch (Exception var3) {
            throw new BinanceApiException("RuntimeError", "[Json] Cannot get integer at index " + index + " in array: " + var3.getMessage());
        }
    }

    public BigDecimal getBigDecimalAt(int index) {
        try {
            return new BigDecimal((new BigDecimal(this.getStringAt(index))).stripTrailingZeros().toPlainString());
        } catch (RuntimeException var3) {
            throw new BinanceApiException((String)null, var3.getMessage());
        }
    }

    public String getStringAt(int index) {
        try {
            return (String)this.getObjectAt(index);
        } catch (RuntimeException var3) {
            throw new BinanceApiException((String)null, var3.getMessage());
        }
    }

    public void forEach(Handler<JsonWrapper> objectHandler) {
        this.array.forEach((object) -> {
            if (!(object instanceof JSONObject)) {
                throw new BinanceApiException("RuntimeError", "[Json] Parse array error in forEach");
            } else {
                objectHandler.handle(new JsonWrapper((JSONObject)object));
            }
        });
    }

    public void forEachAsArray(Handler<JsonWrapperArray> objectHandler) {
        this.array.forEach((object) -> {
            if (!(object instanceof JSONArray)) {
                throw new BinanceApiException("RuntimeError", "[Json] Parse array error in forEachAsArray");
            } else {
                objectHandler.handle(new JsonWrapperArray((JSONArray)object));
            }
        });
    }

    public void forEachAsString(Handler<String> objectHandler) {
        this.array.forEach((object) -> {
            if (!(object instanceof String)) {
                throw new BinanceApiException("RuntimeError", "[Json] Parse array error in forEachAsString");
            } else {
                objectHandler.handle((String)object);
            }
        });
    }

    public List<String> convert2StringList() {
        List<String> result = new LinkedList();
        this.forEachAsString((item) -> {
            result.add(item);
        });
        return result;
    }
}

