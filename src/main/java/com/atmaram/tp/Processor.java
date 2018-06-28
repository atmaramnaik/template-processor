package com.atmaram.tp;

import com.atmaram.tp.exceptions.TemplateParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Processor {
    public Object fill(String template, Template.TemplateType templateType,HashMap<String,Object> data) throws TemplateParseException {
        Template tTemplate=Template.parse(template,templateType);
        return tTemplate.fill(data);
    }
}
