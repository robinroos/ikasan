package org.ikasan.rest.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.ikasan.rest.client.TriggerRestServiceImpl;
import org.ikasan.rest.client.dto.TriggerDto;
import org.ikasan.spec.trigger.TriggerRelationship;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import static org.junit.Assert.assertEquals;

public class TriggerRestServiceImplTest
{
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    private TriggerRestServiceImpl uut;

    private String contexBaseUrl;

    private ObjectMapper mapper = new ObjectMapper();
    @Before
    public void setup()
    {
        contexBaseUrl = "http://localhost:" + wireMockRule.port();
        Environment environment = new StandardEnvironment();
        uut = new TriggerRestServiceImpl(environment, new HttpComponentsClientHttpRequestFactory());
    }

    @Test
    public void create() throws JsonProcessingException
    {
        TriggerDto dto = new TriggerDto("testModule", "testFlow", "component", TriggerRelationship.AFTER.getDescription(), "wiretap", "100" );

        stubFor(put(urlEqualTo(TriggerRestServiceImpl.PUT_TRIGGER_URL))
                    .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
                    .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON.toString())).withRequestBody(
                containing(
                    mapper.writeValueAsString(dto)))
                    .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString()).withStatus(200)));


        boolean result = uut.create(contexBaseUrl, dto);
        assertEquals(true, result);
    }

    @Test
    public void createreturns400() throws JsonProcessingException
    {
        TriggerDto dto = new TriggerDto("testModule", "testFlow", "component", TriggerRelationship.AFTER.getDescription(), "wiretap", "100" );

        stubFor(put(urlEqualTo(TriggerRestServiceImpl.PUT_TRIGGER_URL))
                    .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
                    .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON.toString())).withRequestBody(
                containing(
                    mapper.writeValueAsString(dto)))
                    .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString()).withStatus(400)));
        boolean result = uut.create(contexBaseUrl, dto);
        assertEquals(false, result);
    }

    @Test
    public void resubmit_returns404() throws JsonProcessingException
    {
        TriggerDto dto = new TriggerDto("testModule", "testFlow", "component", TriggerRelationship.AFTER.getDescription(), "wiretap", "100" );

        stubFor(put(urlEqualTo(TriggerRestServiceImpl.PUT_TRIGGER_URL))
                    .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
                    .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON.toString())).withRequestBody(
                containing(
                    mapper.writeValueAsString(dto)))
                    .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                                           .withStatus(404)));
        boolean result = uut.create(contexBaseUrl, dto);
        assertEquals(false, result);
    }

    @Test
    public void resubmit_returns500() throws JsonProcessingException
    {

        TriggerDto dto = new TriggerDto("testModule", "testFlow", "component", TriggerRelationship.AFTER.getDescription(), "wiretap", "100" );

        stubFor(put(urlEqualTo(TriggerRestServiceImpl.PUT_TRIGGER_URL))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON.toString())).withRequestBody(
                containing(
                    mapper.writeValueAsString(dto)))
            .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                                   .withStatus(500)));
        boolean result = uut.create(contexBaseUrl, dto);
        assertEquals(false, result);

    }

    @Test
    public void deleteTrigger()
    {

        stubFor(delete(urlEqualTo(TriggerRestServiceImpl.PUT_TRIGGER_URL+"/1201"))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON.toString()))
            .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                                   .withStatus(200)));


        boolean result = uut.delete(contexBaseUrl, "1201");
        assertEquals(true, result);
    }

    @Test
    public void testTimeout() throws JsonProcessingException {
        Environment environment = new StandardEnvironment();
        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory
            = new HttpComponentsClientHttpRequestFactory();

        httpComponentsClientHttpRequestFactory.setConnectTimeout(1000);
        httpComponentsClientHttpRequestFactory.setReadTimeout(1000);
        httpComponentsClientHttpRequestFactory.setConnectionRequestTimeout(1000);

        uut = new TriggerRestServiceImpl(environment, httpComponentsClientHttpRequestFactory);

        TriggerDto dto = new TriggerDto("testModule", "testFlow", "component", TriggerRelationship.AFTER.getDescription(), "wiretap", "100" );

        stubFor(put(urlEqualTo(TriggerRestServiceImpl.PUT_TRIGGER_URL))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON.toString()))
            .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON.toString())).withRequestBody(
                containing(
                    mapper.writeValueAsString(dto)))
            .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString()).withStatus(200).withFixedDelay(2000)));

       Assert.assertFalse(uut.create(contexBaseUrl, dto));
    }

}