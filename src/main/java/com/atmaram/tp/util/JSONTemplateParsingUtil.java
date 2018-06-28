package com.atmaram.tp.util;

import com.atmaram.tp.exceptions.TemplateParseException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONTemplateParsingUtil {
    public static String replaceLoopsWithTransformedJSON(String template) throws TemplateParseException {
        String output=""+template;
        final Pattern pattern = Pattern.compile("\\{\\{#(.+?)\\}\\}");
        final Matcher matcher = pattern.matcher(template);
        if(matcher.find()){
            String variable=matcher.group(1);
            final Pattern pattern_loop = Pattern.compile("\\{\\{#"+variable+"\\}\\}(.+?)\\{\\{/"+variable+"\\}\\}");
            final Matcher matcher_loop = pattern_loop.matcher(template);
            if (matcher_loop.find()) {
                String inner_object_string=matcher_loop.group(1);
                String innerJSON=replaceLoopsWithTransformedJSON(inner_object_string);
                JSONParser parser=new JSONParser();
                Object inner_object= null;
                try {
                    inner_object = parser.parse(innerJSON);
                } catch (ParseException e) {
                    throw new TemplateParseException();
                }
                JSONObject newJSON=new JSONObject();
                newJSON.put("variable",variable);
                newJSON.put("template",inner_object);
                return replaceLoopsWithTransformedJSON(matcher_loop.replaceFirst(Matcher.quoteReplacement(newJSON.toJSONString())));
            }
        }
        return template;
    }
    public static String replaceVariablesWithQuotedVariables(String template){
        String output=""+template;
        final Pattern pattern = Pattern.compile("\\$\\{(.+?)}");
        final Matcher matcher = pattern.matcher(template);
        List<String> variables = new ArrayList<>();
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        for (String variable :
                variables) {
            output = output.replace("${" + variable + "}", "\"${" + variable + "}\"");
        }
        return output;
    }
}
