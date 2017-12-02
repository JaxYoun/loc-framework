package com.loc.framework.autoconfigure.test.springmvc;

import com.google.common.collect.Lists;

import com.loc.framework.autoconfigure.springmvc.LocAccessLogFilter;
import com.loc.framework.autoconfigure.springmvc.LocSpringMvcProperties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created on 2017/12/1.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class LocAccessLogTest {

  @Autowired
  private MappingJackson2HttpMessageConverter jackson2HttpMessageConverter;

  private MockMvc requestMockMvc;
  private MockMvc bothMockMvc;

  @Before
  public void setUp() throws Exception {
    LocSpringMvcProperties requestProperties = new LocSpringMvcProperties();

    this.requestMockMvc = MockMvcBuilders
        .standaloneSetup(new GetController())
        .setMessageConverters(jackson2HttpMessageConverter)
        .addFilters(new LocAccessLogFilter(requestProperties))
        .build();

    LocSpringMvcProperties bothProperties = new LocSpringMvcProperties();
    bothProperties.setIncludeResponse(true);
    bothMockMvc = MockMvcBuilders
        .standaloneSetup(new GetController())
        .setMessageConverters(jackson2HttpMessageConverter)
        .addFilters(new LocAccessLogFilter(bothProperties)).build();
  }

  @Test
  public void getTest1() throws Exception {
    this.requestMockMvc
        .perform(get("/get/test1").header("header-key", "header-value").accept("application/json"))
        .andExpect(status().isOk()).andReturn();

    this.bothMockMvc
        .perform(get("/get/test1").header("header-key", "header-value").accept("application/json"))
        .andExpect(status().isOk()).andReturn();
  }

  @Test
  public void getSleep() throws Exception {
    this.requestMockMvc.perform(get("/get/sleep?time=1000").accept("application/json"))
        .andExpect(status().isOk()).andReturn();

    this.bothMockMvc.perform(get("/get/sleep?time=1000").accept("application/json"))
        .andExpect(status().isOk()).andReturn();
  }

  @Test
  public void getDemo() throws Exception {
    this.requestMockMvc.perform(
        get("/get/demo")
            .param("name", "thomas")
            .param("age", "29")
            .param("address", "a1", "a2")
            .accept("application/json"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.name" ).value("thomas"))
        .andExpect(jsonPath("$.age" ).value("29"))
        .andExpect(jsonPath("$.address" ).value(Lists.newArrayList("a1", "a2")))
        .andReturn();

    this.bothMockMvc.perform(
        get("/get/demo")
            .param("name", "thomas")
            .param("age", "29")
            .param("address", "a1", "a2")
            .accept("application/json"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.name" ).value("thomas"))
        .andExpect(jsonPath("$.age" ).value("29"))
        .andExpect(jsonPath("$.address" ).value(Lists.newArrayList("a1", "a2")))
        .andReturn();
  }


  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Demo {
    private String name;
    private int age;
    private List<String> address;
  }


  @MinimalWebConfiguration
  @RestController
  public static class GetController {

    @GetMapping(value = "/get/test1")
    public String responsePlainTest() {
      return "OK";
    }

    @GetMapping(value = "/get/sleep")
    public String responseSleep(
        @RequestParam(value = "time")
            long time) {
      try {
        Thread.sleep(time);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return String.valueOf(time);
    }

    @GetMapping(value = "/get/demo")
    public Demo responseDemo(
        @RequestParam(value = "name")
            String name,
        @RequestParam(value = "age")
            int age,
        @RequestParam(value = "address")
            List<String> address) {
      Demo demo = new Demo();
      demo.setName(name);
      demo.setAge(age);
      demo.setAddress(address);
      return demo;
    }
  }

  @Configuration
  public static class WebConfig {

    @Bean
    public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
      MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
      Jackson2ObjectMapperBuilder builder = this.jacksonBuilder();
      converter.setObjectMapper(builder.build());

      return converter;
    }

    public Jackson2ObjectMapperBuilder jacksonBuilder() {
      return new Jackson2ObjectMapperBuilder();
    }
  }

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  @Configuration
  @Import({
      ServletWebServerFactoryAutoConfiguration.class,
      JacksonAutoConfiguration.class
  })
  protected @interface MinimalWebConfiguration {

  }

}