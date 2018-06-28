package com.atmaram.tp;

import com.atmaram.tp.exceptions.TemplateParseException;
import com.atmaram.tp.util.JSONTemplateParsingUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Template {
    private JSONObject jsonTemplate;
    private TemplateType templateType;

    public Template(JSONObject jsonTemplate){
        this.jsonTemplate=jsonTemplate;
        this.templateType=TemplateType.JSON;
    }
    public enum TemplateType {
        JSON,
        XML,
        TEXT
    }
    public static Template parse(String template,TemplateType templateType) throws TemplateParseException {
        JSONParser jsonParser=new JSONParser();
        template= JSONTemplateParsingUtil.replaceVariablesWithQuotedVariables(template);
//        template=JSONTemplateParsingUtil.replaceStaticArraysWithStaticVariables(template);
        template= JSONTemplateParsingUtil.replaceLoopsWithTransformedJSON(template);
        if(templateType==TemplateType.JSON){
            try {
                JSONObject jsonTemplate=(JSONObject) jsonParser.parse(template);
                return new Template(jsonTemplate);
            } catch (ParseException ex){
                throw new TemplateParseException();
            }
        }
        return null;
    }


    public Object fill(HashMap<String,Object> data){
        if(this.templateType==TemplateType.JSON){
            return fillJSON(data);
        }
        return null;
    }
    private JSONObject fillJSON(HashMap<String,Object> data){
        JSONObject ojoObject=new JSONObject();
        JSONObject joObject=this.jsonTemplate;

        for (Object key:
                joObject.keySet()) {
            Object value=joObject.get(key);
            if(value instanceof String){
                if(((String)value).startsWith("${") && ((String)value).endsWith("}")){
                    String variableName=((String)value).substring(2,((String)value).length()-1);
//                        if(variableName.startsWith(prefix+".")){
//                            String newVarName=variableName.substring(prefix.length()+1,variableName.length());
//                            ojoObject.put(key,data.get(newVarName));
//                        } else {
                        ojoObject.put(key, getVariableValue(variableName,data));
//                        }
                } else {
                    ojoObject.put(key,value);
                }
            } else if(value instanceof JSONArray){
                String loopVariable=getLoopVariable((JSONObject)( (JSONArray)value).get(0));
                List<Object> dataArray=(List<Object>)getVariableValue(loopVariable,data);
                JSONArray outputArray=new JSONArray();
                for(int i=0;i<dataArray.size();i++){
                    Template template=new Template((JSONObject) ((JSONObject)( (JSONArray)value).get(0)).get("template"));
                    Object dataObject=dataArray.get(i);
                    JSONObject outputObject;
                    if(dataObject instanceof HashMap){
                        outputObject=template.fillJSON((HashMap<String,Object>)dataObject);
                    } else {
                        HashMap<String,Object> innerData=new HashMap<>();
                        innerData.put("_this",dataObject);
                        outputObject=template.fillJSON(innerData);
                    }
                    Template newTemplate=new Template(outputObject);
                    outputObject=newTemplate.fillJSON(data);
                    outputArray.add(outputObject);
                }
                ojoObject.put(key,outputArray);
            }
            else if (value instanceof JSONObject){
                Template template=new Template((JSONObject) value);
                ojoObject.put(key,template.fillJSON(data));
            }
        }

        return ojoObject;
    }

    public String getLoopVariable(JSONObject object){
        return (String)object.get("variable");
    }

    public Object getVariableValue(String variableName,HashMap<String,Object> data){
        if(variableName.startsWith("_")){
            if(variableName.equals("_ones()")){
                List<Integer> lst=new ArrayList<>();
                lst.add(new Integer(0));
                return lst;
            } else if(variableName.equals("_this")){
                return data;
            }
            return null;
        }
        return data.get(variableName);
    }
}
