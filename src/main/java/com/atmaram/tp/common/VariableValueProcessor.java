package com.atmaram.tp.common;

import com.atmaram.tp.common.exceptions.ValueNotFoundException;

import java.util.Date;
import java.util.HashMap;

public class VariableValueProcessor {
    public static Object getValue(String name, HashMap<String,Object> data) throws ValueNotFoundException {
        if(name.startsWith("_")){
            if(name.equals("_this")){
                return data.get("_this");
            } else if(name.equals("_timestamp")){
                return new Date().getTime();
            } else {
                throw new ValueNotFoundException("Given function is not supported by system "+name);
            }
        }
        if(data.containsKey(name)){
            return data.get(name);
        }
        throw new ValueNotFoundException("Variable value not found for variable "+name);

    }
}
