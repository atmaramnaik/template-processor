package com.atmaram.tp;

import com.atmaram.tp.exceptions.TemplateParseException;
import java.util.HashMap;

public class Processor {
    public Object fillJSON(String template,HashMap<String,Object> data) throws TemplateParseException {
        JSONTemplate tJSONTemplate =JSONTemplate.parse(template);
        return tJSONTemplate.fill(data);
    }
}
