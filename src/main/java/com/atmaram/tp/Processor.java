package com.atmaram.tp;

import com.atmaram.tp.exceptions.TemplateParseException;
import com.atmaram.tp.exceptions.ValueNotFoundException;
import com.atmaram.tp.json.JSONTemplate;

import java.util.HashMap;

public class Processor {
    public Object fillJSON(String template,HashMap<String,Object> data) throws TemplateParseException, ValueNotFoundException {
        JSONTemplate tJSONTemplate =JSONTemplate.parse(template);
        return tJSONTemplate.fill(data);
    }
}
