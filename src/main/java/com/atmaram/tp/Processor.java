package com.atmaram.tp;

import com.atmaram.tp.exceptions.TemplateParseException;
import java.util.HashMap;

public class Processor {
    public Object fill(String template, Template.TemplateType templateType,HashMap<String,Object> data) throws TemplateParseException {
        Template tTemplate=Template.parse(template,templateType);
        return tTemplate.fill(data);
    }
}
