package com.atmaram.tp.json;

import com.atmaram.tp.Variable;
import com.atmaram.tp.common.VariableValueProcessor;
import com.atmaram.tp.exceptions.TemplateParseException;
import com.atmaram.tp.exceptions.ValueNotFoundException;
import com.atmaram.tp.util.JSONTemplateParsingUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JSONTemplate {
    private JSONObject jsonTemplate;

    public JSONTemplate(JSONObject jsonTemplate){
        this.jsonTemplate=jsonTemplate;
    }

    private JSONTemplate() {

    }
    public static JSONTemplate parse(String template) throws TemplateParseException {
        JSONParser jsonParser=new JSONParser();
        template= JSONTemplateParsingUtil.replaceVariablesWithQuotedVariables(template);
        template= JSONTemplateParsingUtil.replaceLoopsWithTransformedJSON(template);
        try {
            JSONObject jsonTemplate=(JSONObject) jsonParser.parse(template);
            JSONObject transformed_object=transform(jsonTemplate);
            return new JSONTemplate(transformed_object);
        } catch (ParseException ex){
            throw new TemplateParseException();
        }
    }
    public JSONObject fill(HashMap<String,Object> data) throws ValueNotFoundException {
        JSONObject ojoObject=new JSONObject();
        JSONObject joObject=this.jsonTemplate;

        for (Object key:
                joObject.keySet()) {
            Object value=joObject.get(key);
            if(value instanceof String){
                if(((String)value).startsWith("${") && ((String)value).endsWith("}")){
                    String variableName=((String)value).substring(2,((String)value).length()-1);
                        ojoObject.put(key, VariableValueProcessor.getValue(variableName,data));
                } else {
                    ojoObject.put(key,value);
                }
            } else if(value instanceof JSONLoop){
                String loopVariable=( (JSONLoop)value).variable;
                List<Object> dataArray=(List<Object>)VariableValueProcessor.getValue(loopVariable,data);
                JSONArray outputArray=new JSONArray();
                JSONTemplate JSONTemplate =new JSONTemplate(((JSONLoop)value).inner_object);
                for(int i=0;i<dataArray.size();i++){
                    Object dataObject=dataArray.get(i);
                    JSONObject outputObject;
                    if(dataObject instanceof HashMap){
                        outputObject= JSONTemplate.fill((HashMap<String,Object>)dataObject);
                    } else {
                        HashMap<String,Object> innerData=new HashMap<>();
                        innerData.put("_this",dataObject);
                        outputObject= JSONTemplate.fill(innerData);
                    }
                    JSONTemplate newJSONTemplate =new JSONTemplate(outputObject);
                    JSONObject newOutputObject= newJSONTemplate.fill(data);
                    outputArray.add(newOutputObject);
                }
                ojoObject.put(key,outputArray);
            }
            else if (value instanceof JSONObject){
                JSONTemplate JSONTemplate =new JSONTemplate((JSONObject) value);
                ojoObject.put(key, JSONTemplate.fill(data));
            } else if (value instanceof JSONArray){
                JSONArray jaValue=(JSONArray)value;
                JSONArray filled_array=new JSONArray();
                for(int i=0;i<jaValue.size();i++){
                    Object arrayObject=jaValue.get(i);
                    if(arrayObject instanceof JSONObject){
                        JSONTemplate JSONTemplate =new JSONTemplate((JSONObject) arrayObject);
                        filled_array.add(JSONTemplate.fill(data));
                    } else if(arrayObject instanceof String){
                        if(((String)arrayObject).startsWith("${") && ((String)arrayObject).endsWith("}")){
                            String variableName=((String)arrayObject).substring(2,((String)arrayObject).length()-1);
                            filled_array.add(VariableValueProcessor.getValue(variableName,data));
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
                        JSONTemplate JSONTemplate =new JSONTemplate();
                        JSONTemplate.jsonTemplate=jlValue.inner_object;
                        lst.add(JSONTemplate.extract(arrayElementJSONResult));
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
    public List<Variable> getVariables(){
        List<Variable> returnValue=new ArrayList<>();
        for (Object key:
                jsonTemplate.keySet()) {
            Object oValue=jsonTemplate.get(key);

            if(oValue instanceof String){
                if(isVariable((String)oValue)){
                    String variableName=getVariableName((String)oValue);
                    Variable variable=new Variable();
                    variable.setName(variableName);
                    variable.setType("String");
                    returnValue.add(variable);
                }
            } else if(oValue instanceof JSONLoop){
                JSONLoop jlValue=(JSONLoop)oValue;
                Variable variable=new Variable();
                variable.setName(jlValue.variable);
                variable.setType("List");
                JSONTemplate inner_jt=new JSONTemplate(jlValue.inner_object);
                List<Variable> inner_variables=inner_jt.getVariables();
                List<Variable> inner_variables_excluding_this=new ArrayList<>();
                boolean found_this_variable=false;
                for (Variable inner_variable:inner_variables
                     ) {
                    if(inner_variable.getName().equals("_this")){
                        found_this_variable=true;
                    } else {
                        inner_variables_excluding_this.add(inner_variable);
                    }
                }
                if(found_this_variable){
                    returnValue.add(variable);
                    returnValue.addAll(inner_variables_excluding_this);
                } else {
                    variable.setInner_variables(inner_variables_excluding_this);
                    returnValue.add(variable);
                }

            } else if(oValue instanceof JSONObject){
                JSONTemplate inner_jt=new JSONTemplate((JSONObject) oValue);
                returnValue.addAll(inner_jt.getVariables());
            }
        }
        return returnValue;
    }
}
