package com.atmaram.tp.util;

import com.atmaram.tp.exceptions.TemplateParseException;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
public class JSONTemplateParsingUtilTest {
    @Test
    public void should_replace_loop_variables() throws TemplateParseException {
        String results=JSONTemplateParsingUtil.replaceLoopsWithTransformedJSON("{\"name\":[{{#items}}{\"name\":\"Atmaram\",\"place\":\"India\"}{{/items}}]}");
        assertThat(results).isEqualTo("{\"name\":[{\"template\":{\"name\":\"Atmaram\",\"place\":\"India\"},\"variable\":\"items\"}]}");
    }
    @Test
    public void should_replace_nested_loop_variables() throws TemplateParseException {
        String results=JSONTemplateParsingUtil.replaceLoopsWithTransformedJSON("{\"name\":[{{#items}}{\"name\":[{{#names}}{\"inner\":\"inner_value\"}{{/names}}],\"place\":\"India\"}{{/items}}]}");
        assertThat(results).isEqualTo("{\"name\":[{\"template\":{\"name\":[{\"template\":{\"inner\":\"inner_value\"},\"variable\":\"names\"}],\"place\":\"India\"},\"variable\":\"items\"}]}");
    }
    @Test
    public void should_replace_same_variable_loop_apearing_twise() throws TemplateParseException {
        String results=JSONTemplateParsingUtil.replaceLoopsWithTransformedJSON("{\"name\":[{{#items}}{\"name\":\"Atmaram\",\"place\":\"India\"}{{/items}}],\"place\":[{{#items}}{\"name\":\"Atmaram\",\"place\":\"India\"}{{/items}}]}");
        assertThat(results).isEqualTo("{\"name\":[{\"template\":{\"name\":\"Atmaram\",\"place\":\"India\"},\"variable\":\"items\"}],\"place\":[{\"template\":{\"name\":\"Atmaram\",\"place\":\"India\"},\"variable\":\"items\"}]}");
    }
    @Test()
    public void  should_throw_exception_if_nested_same_variable_loop() throws TemplateParseException {
        String results=JSONTemplateParsingUtil.replaceLoopsWithTransformedJSON("{\"name\":[{{#items}}{\"name\":[{{#items}}{\"inner\":\"inner_value\"}{{/items}}],\"place\":\"India\"}{{/items}}]}");
        assertThat(results).isEqualTo("{\"name\":[{\"template\":{\"name\":[{\"template\":{\"inner\":\"inner_value\"},\"variable\":\"items\"}],\"place\":\"India\"},\"variable\":\"items\"}]}");
    }
    @Test
    public void should_replace_loops_with_variables() throws TemplateParseException {
        String results=JSONTemplateParsingUtil.replaceLoopsWithTransformedJSON("{\"name\":[{{#items}}{\"name\":\"${name}\",\"place\":\"India\"}{{/items}}]}");
        assertThat(results).isEqualTo("{\"name\":[{\"template\":{\"name\":\"${name}\",\"place\":\"India\"},\"variable\":\"items\"}]}");
    }
    @Test
    public void should_replace_static_arrays() throws TemplateParseException{
        String results=JSONTemplateParsingUtil.replaceStaticArraysWithStaticVariables("{\"name\":[{\"name\":\"${name}\",\"place\":\"India\"}]}");
        assertThat(results).isEqualTo("{\"name\":[{{#_ones()}}{\"name\":\"${name}\",\"place\":\"India\"}{{/_ones()}}]}");
    }
    @Test
    public void should_not_replace_static_arrays() throws TemplateParseException{
        String results=JSONTemplateParsingUtil.replaceStaticArraysWithStaticVariables("{\"name\":[{{#names}}{\"name\":\"${name}\",\"place\":\"India\"}{{/names}}]}");
        assertThat(results).isEqualTo("{\"name\":[{{#names}}{\"name\":\"${name}\",\"place\":\"India\"}{{/names}}]}");
    }
}
