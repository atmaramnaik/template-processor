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

    private Template() {

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
                JSONObject transformed_object=transform(jsonTemplate);
                return new Template(transformed_object);
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
                        ojoObject.put(key, getVariableValue(variableName,data));
                } else {
                    ojoObject.put(key,value);
                }
            } else if(value instanceof JSONLoop){
                String loopVariable=( (JSONLoop)value).variable;
                List<Object> dataArray=(List<Object>)getVariableValue(loopVariable,data);
                JSONArray outputArray=new JSONArray();
                Template template=new Template(((JSONLoop)value).inner_object);
                for(int i=0;i<dataArray.size();i++){
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
                    JSONObject newOutputObject=newTemplate.fillJSON(data);
                    outputArray.add(newOutputObject);
                }
                ojoObject.put(key,outputArray);
            }
            else if (value instanceof JSONObject){
                Template template=new Template((JSONObject) value);
                ojoObject.put(key,template.fillJSON(data));
            } else if (value instanceof JSONArray){
                JSONArray jaValue=(JSONArray)value;
                JSONArray filled_array=new JSONArray();
                for(int i=0;i<jaValue.size();i++){
                    Object arrayObject=jaValue.get(i);
                    if(arrayObject instanceof JSONObject){
                        Template template=new Template((JSONObject) arrayObject);
                        filled_array.add(template.fillJSON(data));
                    } else if(arrayObject instanceof String){
                        if(((String)arrayObject).startsWith("${") && ((String)arrayObject).endsWith("}")){
                            String variableName=((String)arrayObject).substring(2,((String)arrayObject).length()-1);
                            filled_array.add(getVariableValue(variableName,data));
//                        }
                        } else {
                            filled_array.add(arrayObject);
                        }
                    } else {
                        filled_array.add(arrayObject);
                    }

                }
                ojoObject.put(key,filled_array);
            } else {
                ojoObject.put(key,value);
            }
        }

        return ojoObject;
    }
    public Object getVariableValue(String variableName,HashMap<String,Object> data){
        if(variableName.startsWith("_")){
            if(variableName.equals("_ones()")){
                List<Integer> lst=new ArrayList<>();
                lst.add(new Integer(0));
                return lst;
            } else if(variableName.equals("_this")){
                return data.get("_this");
            }
            return null;
        }
        return data.get(variableName);
    }
    public static JSONObject transform(JSONObject object) {
        JSONObject ojoObject = new JSONObject();
        for (Object key :
                object.keySet()) {
            Object value = object.get(key);
            if (value instanceof JSONArray) {
                Object inner_object=((JSONArray) value).get(0);
                if(inner_object instanceof JSONObject){
                    JSONObject inner_joobject_template = (JSONObject) ((JSONArray) value).get(0);
                    if (inner_joobject_template.containsKey("variable") && inner_joobject_template.containsKey("template")) {
                        JSONLoop loopObject = new JSONLoop();
                        loopObject.variable = (String) inner_joobject_template.get("variable");
                        loopObject.inner_object = transform((JSONObject) inner_joobject_template.get("template"));
                        ojoObject.put(key, loopObject);
                    } else {
                        ojoObject.put(key, value);
                    }

                } else {
                    ojoObject.put(key, value);
                }

            } else if(value instanceof JSONObject){
                ojoObject.put(key,transform((JSONObject) value));
            } else {
                ojoObject.put(key,value);
            }

        }
        return ojoObject;
    }

    private HashMap<String,Object> extract(JSONObject jsonResult,HashMap<String,Object> retData){

        for (Object key:
                jsonTemplate.keySet()) {
            Object oValue=jsonTemplate.get(key);

            if(oValue instanceof String){
                if(isVariable((String)oValue)){
                    String variableName=getVariableName((String)oValue);
                    retData.put(variableName,jsonResult.get(key));
                }
            } else if(oValue instanceof JSONLoop){
                JSONLoop jlValue=(JSONLoop)oValue;
                String variableName=jlValue.variable;
                List lst=new ArrayList();
                JSONArray resultArray=(JSONArray)jsonResult.get(key);
                for (Object arrayElementResult:
                     resultArray) {
                    if(arrayElementResult instanceof JSONObject){
                        JSONObject arrayElementJSONResult=(JSONObject)arrayElementResult;
                        Template template=new Template();
                        template.templateType=TemplateType.JSON;
                        template.jsonTemplate=jlValue.inner_object;
                        lst.add(template.extract(arrayElementJSONResult));
                    }
                }
                retData.put(variableName,lst);
            }
        }
        return retData;
    }
    public HashMap<String,Object> extract(JSONObject jsonResult){
        HashMap<String,Object> retData=new HashMap<>();
        extract(jsonResult,retData);
        return retData;
    }
    private boolean isVariable(String strValue){
        return (strValue.startsWith("${") && strValue.endsWith("}"));
    }
    private String getVariableName(String strValue){
        return strValue.substring(2,strValue.length()-1);
    }
}
