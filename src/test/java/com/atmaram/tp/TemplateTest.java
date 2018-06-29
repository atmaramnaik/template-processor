package com.atmaram.tp;

import com.atmaram.tp.exceptions.TemplateParseException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
public class TemplateTest {
    @Test
    public void should_construct_and_fill_template_when_null_object() throws TemplateParseException {
        JSONObject obj=(JSONObject) Template.parse("{\"name\":[[\"Atmaram\"]]}",Template.TemplateType.JSON).fill(null);
        assertThat(obj.toJSONString()).isEqualTo("{\"name\":[[\"Atmaram\"]]}");
    }
    @Test
    public void should_fill_template_when_nested_this() throws TemplateParseException {
        HashMap<String,Object> obj=new HashMap<>();
        List<List<List<String>>> names=new ArrayList<>();
        List<String> lst1=new ArrayList<>();
        lst1.add("Atmaram");
        lst1.add("Roopa");
        List<List<String>> lst2=new ArrayList<>();
        lst2.add(lst1);
        names.add(lst2);
        obj.put("names",names);

        JSONObject objJSON=(JSONObject) Template.parse("{\"name\":[{{#names}}{\"places\":[{{#_this}}{\"place\":[{{#_this}}{\"value\":${_this}}{{/_this}}]}{{/_this}}]}{{/names}}]}",Template.TemplateType.JSON).fill(obj);
        assertThat(objJSON.toJSONString()).isEqualTo("{\"name\":[{\"places\":[{\"place\":[{\"value\":\"Atmaram\"},{\"value\":\"Roopa\"}]}]}]}");
    }
    @Test
    public void should_give_same_template_when_no_variables_without_arrays() throws TemplateParseException {
        HashMap<String,Object> objData=new HashMap<>();
        List<List<String>> lst=new ArrayList<>();
        List<String> lst1=new ArrayList<>();
        lst1.add("Atmaram");
        lst1.add("Roopa");

        List<String> lst2=new ArrayList<>();
        lst2.add("Hemlata");
        lst2.add("Rohan");
        lst.add(lst1);
        lst.add(lst2);
        objData.put("names",lst);
        Template template=Template.parse("{\"name\":\"Atmaram\"}",Template.TemplateType.JSON);
        JSONObject result=(JSONObject) template.fill(objData);
        assertThat(result.toJSONString()).isEqualTo("{\"name\":\"Atmaram\"}");

    }
    @Test
    public void should_give_same_template_when_no_variables_with_arrays() throws TemplateParseException {
        HashMap<String,Object> objData=new HashMap<>();
        List<List<String>> lst=new ArrayList<>();
        List<String> lst1=new ArrayList<>();
        lst1.add("Atmaram");
        lst1.add("Roopa");

        List<String> lst2=new ArrayList<>();
        lst2.add("Hemlata");
        lst2.add("Rohan");
        lst.add(lst1);
        lst.add(lst2);
        objData.put("names",lst);
        Template template=Template.parse("{\"inner_array\":[\"Hemlata\",\"Rohan\"]}",Template.TemplateType.JSON);
        JSONObject result=(JSONObject) template.fill(objData);
        assertThat(result.toJSONString()).isEqualTo("{\"inner_array\":[\"Hemlata\",\"Rohan\"]}");

    }
    @Test
    public void should_give_same_template_when_no_variables_with_array_containing_static_json_object() throws TemplateParseException {
        HashMap<String,Object> objData=new HashMap<>();
        List<List<String>> lst=new ArrayList<>();
        List<String> lst1=new ArrayList<>();
        lst1.add("Atmaram");
        lst1.add("Roopa");

        List<String> lst2=new ArrayList<>();
        lst2.add("Hemlata");
        lst2.add("Rohan");
        lst.add(lst1);
        lst.add(lst2);
        objData.put("names",lst);
        Template template=Template.parse("{\"name\":[{\"name\":\"Atmaram\"}]}",Template.TemplateType.JSON);
        JSONObject result=(JSONObject) template.fill(objData);
        assertThat(result.toJSONString()).isEqualTo("{\"name\":[{\"name\":\"Atmaram\"}]}");

    }
    @Test
    public void should_fill_single_value() throws TemplateParseException {
        Template template=Template.parse("{\"name\":${name}}",Template.TemplateType.JSON);
        HashMap<String,Object> objData=new HashMap<>();
        objData.put("name","Atmaram");
        JSONObject op=(JSONObject) template.fill(objData);
        assertThat(op.toJSONString()).isEqualTo("{\"name\":\"Atmaram\"}");
    }

    @Test
    public void should_fill_multiple_value() throws TemplateParseException {
        HashMap<String,Object> objData=new HashMap<>();
        objData.put("name","Atmaram");
        objData.put("place","Pune");
        JSONObject op=(JSONObject) Template.parse("{\"name\":${name},\"place\":${place}}", Template.TemplateType.JSON).fill(objData);
        assertThat(op.toJSONString()).isEqualTo("{\"name\":\"Atmaram\",\"place\":\"Pune\"}");
    }

    @Test
    public void should_fill_array() throws ParseException, TemplateParseException {
        HashMap<String,Object> objData=new HashMap<>();
        List<String> lst=new ArrayList<>();
        lst.add("Atmaram");
        lst.add("Roopa");
        objData.put("names",lst);
        JSONObject op=(JSONObject) Template.parse("{\"name\":${names}}", Template.TemplateType.JSON).fill(objData);
        assertThat(op.toJSONString()).isEqualTo("{\"name\":[\"Atmaram\",\"Roopa\"]}");
    }
    @Test
    public void should_fill_objects_with_array_elements() throws TemplateParseException {
        HashMap<String,Object> objData=new HashMap<>();
        List<String> lst=new ArrayList<>();
        lst.add("Atmaram");
        lst.add("Roopa");
        objData.put("names",lst);
        JSONObject op=(JSONObject) Template.parse("{\"name\":[{{#names}}{\"name\":${_this}}{{/names}}]}", Template.TemplateType.JSON).fill(objData);
        assertThat(op.toJSONString()).isEqualTo("{\"name\":[{\"name\":\"Atmaram\"},{\"name\":\"Roopa\"}]}");
    }
    @Test
    public void should_fill_nested_array_constructing_array_with_array() throws TemplateParseException {
        HashMap<String,Object> objData=new HashMap<>();
        List<List<String>> lst=new ArrayList<>();
        List<String> lst1=new ArrayList<>();
        lst1.add("Atmaram");
        lst1.add("Roopa");

        List<String> lst2=new ArrayList<>();
        lst2.add("Hemlata");
        lst2.add("Rohan");
        lst.add(lst1);
        lst.add(lst2);
        objData.put("names",lst);
        JSONObject op=(JSONObject) Template.parse("{\"name\":${names}}", Template.TemplateType.JSON).fill(objData);
        assertThat(op.toJSONString()).isEqualTo("{\"name\":[[\"Atmaram\",\"Roopa\"],[\"Hemlata\",\"Rohan\"]]}");
    }
    @Test
    public void should_return_same_template_if_no_variables() throws TemplateParseException {
        HashMap<String,Object> objData=new HashMap<>();
        objData.put("name","Mayur");
        JSONObject op=(JSONObject) Template.parse("{\"name\":[\"Atmaram\"]}", Template.TemplateType.JSON).fill(objData);
        assertThat(op.toJSONString()).isEqualTo("{\"name\":[\"Atmaram\"]}");
    }
    @Test
    public void should_fill_nested_array_constructing_array_with_object_array() throws TemplateParseException {
        HashMap<String,Object> objData=new HashMap<>();
        List<List<String>> lst=new ArrayList<>();
        List<String> lst1=new ArrayList<>();
        lst1.add("Atmaram");
        lst1.add("Roopa");

        List<String> lst2=new ArrayList<>();
        lst2.add("Hemlata");
        lst2.add("Rohan");
        lst.add(lst1);
        lst.add(lst2);
        objData.put("names",lst);
        JSONObject op=(JSONObject) Template.parse("{\"name\":[{{#names}}{\"inner_array\":${_this}}{{/names}}]}", Template.TemplateType.JSON).fill(objData);
        assertThat(op.toJSONString()).isEqualTo("{\"name\":[{\"inner_array\":[\"Atmaram\",\"Roopa\"]},{\"inner_array\":[\"Hemlata\",\"Rohan\"]}]}");
    }
    @Test
    public void should_fill_array_with_objects() throws TemplateParseException {
        HashMap<String,Object> objData=new JSONObject();
        List<HashMap<String,Object>> objArray=new ArrayList<>();
        HashMap<String,Object> obj1=new HashMap<>();
        obj1.put("name","Atmaram");
        obj1.put("place","Mumbai");
        HashMap<String,Object> obj2=new HashMap<>();
        obj2.put("name","Roopa");
        obj2.put("place","Pune");
        objArray.add(obj1);
        objArray.add(obj2);
        objData.put("items",objArray);
        JSONObject op=(JSONObject) Template.parse("{\"name\":[{{#items}}{\"name\":${name},\"place\":${place}}{{/items}}]}", Template.TemplateType.JSON).fill(objData);
        assertThat(op.toJSONString()).isEqualTo("{\"name\":[{\"name\":\"Atmaram\",\"place\":\"Mumbai\"},{\"name\":\"Roopa\",\"place\":\"Pune\"}]}");
    }

    //Extract Tests

    @Test
    public void should_extract_single_variable() throws TemplateParseException, ParseException {
        JSONParser parser=new JSONParser();
        Template template=Template.parse("{\"name\":${name}}",Template.TemplateType.JSON);
        JSONObject objResult=(JSONObject) parser.parse("{\"name\":\"Atmaram\"}");
        HashMap<String,Object> objData=template.extract(objResult);
        assertThat(objData.containsKey("name")).isTrue();
        assertThat(objData.get("name")).isEqualTo("Atmaram");
    }
    @Test
    public void should_extract_single_variable_in_loop() throws TemplateParseException, ParseException {
        JSONParser parser=new JSONParser();
        Template template=Template.parse("{\"names\":[{{#names}}{\"name\":${name}}{{/names}}]}",Template.TemplateType.JSON);
        JSONObject objResult=(JSONObject) parser.parse("{\"names\":[{\"name\":\"Atmaram\"},{\"name\":\"Roopa\"}]}");
        HashMap<String,Object> objData=template.extract(objResult);
        assertThat(objData.containsKey("names")).isTrue();
        assertThat(objData.get("names")).isInstanceOf(ArrayList.class);
        assertThat(((ArrayList<HashMap<String,Object>>)objData.get("names")).size()).isEqualTo(2);
        ArrayList<HashMap<String,Object>> list=(ArrayList<HashMap<String,Object>>)objData.get("names");
        for(int i=0;i<list.size();i++){
            HashMap<String,Object> listElement=list.get(i);
            assertThat(listElement).containsOnlyKeys("name");
            assertThat(listElement.get("name")).isIn("Atmaram","Roopa");
        }
    }
    @Test
    public void should_extract_multiple_variable_in_loop() throws TemplateParseException, ParseException {
        JSONParser parser=new JSONParser();
        Template template=Template.parse("{\"names\":[{{#names}}{\"name\":${name},\"place\":${place}}{{/names}}]}",Template.TemplateType.JSON);
        JSONObject objResult=(JSONObject) parser.parse("{\"names\":[{\"name\":\"Atmaram\",\"place\":\"Pune\"},{\"name\":\"Roopa\",\"place\":\"Mumbai\"}]}");
        HashMap<String,Object> objData=template.extract(objResult);
        assertThat(objData.containsKey("names")).isTrue();
        assertThat(objData.get("names")).isInstanceOf(ArrayList.class);
        assertThat(((ArrayList<HashMap<String,Object>>)objData.get("names")).size()).isEqualTo(2);
        ArrayList<HashMap<String,Object>> list=(ArrayList<HashMap<String,Object>>)objData.get("names");
        for(int i=0;i<list.size();i++){
            HashMap<String,Object> listElement=list.get(i);
            assertThat(listElement).containsKeys("name","place");
            assertThat(listElement.get("name")).isIn("Atmaram","Roopa");
            assertThat(listElement.get("place")).isIn("Pune","Mumbai");
        }
    }
}
