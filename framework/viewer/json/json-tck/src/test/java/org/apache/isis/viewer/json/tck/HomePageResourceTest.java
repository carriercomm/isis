package org.apache.isis.viewer.json.tck;

import static org.apache.isis.viewer.json.tck.RepresentationMatchers.assertThat;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.entityOf;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isArray;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isFollowableLinkToSelf;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isLink;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulRequest;
import org.apache.isis.viewer.json.applib.RestfulRequest.QueryParameter;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.json.applib.homepage.HomePageResource;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;


public class HomePageResourceTest {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();
    
    private RestfulClient client;
    private HomePageResource resource;

    @Before
    public void setUp() throws Exception {
        WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        
        resource = client.getHomePageResource();
    }

    @Test
    public void representation() throws Exception {

        // given
        Response resp = resource.resources();
        
        // when
        RestfulResponse<HomePageRepresentation> jsonResp = RestfulResponse.ofT(resp);
        assertThat(jsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.OK));
        assertThat(jsonResp.getHeader(RestfulResponse.Header.CONTENT_TYPE), is(RepresentationType.HOME_PAGE.getMediaType()));
        assertThat(jsonResp.getHeader(RestfulResponse.Header.CACHE_CONTROL).getMaxAge(), is(24*60*60));
        
        HomePageRepresentation repr = jsonResp.getEntity();
        assertThat(repr, is(not(nullValue())));
        assertThat(repr.isMap(), is(true));
        
        assertThat(repr.getSelf(), isLink(client).method(Method.GET));
        assertThat(repr.getUser(), isLink(client).method(Method.GET));
        assertThat(repr.getServices(), isLink(client).method(Method.GET));
        assertThat(repr.getCapabilities(), isLink(client).method(Method.GET));
        
        assertThat(repr.getLinks(), isArray());
        assertThat(repr.getExtensions(), isMap());
    }

    @Test
    public void self_isFollowable() throws Exception {
        // given
        HomePageRepresentation repr = givenRepresentation();

        // when, then
        assertThat(repr, isFollowableLinkToSelf(client));
    }
    
    @Test
    public void links() throws Exception {
        // given
        HomePageRepresentation repr = givenRepresentation();

        // when, then
        assertThat(repr.getServices(), isLink(client).returning(HttpStatusCode.OK));
        assertThat(repr.getUser(), isLink(client).returning(HttpStatusCode.OK));
        assertThat(repr.getCapabilities(), isLink(client).returning(HttpStatusCode.OK));
    }

    private HomePageRepresentation givenRepresentation() throws JsonParseException, JsonMappingException, IOException {
        return entityOf(resource.resources(), HomePageRepresentation.class);
    }

    
    @Ignore("in progress")
    @Test
    public void xrofollowLinks() throws Exception {

        final RestfulRequest request = client.createRequest(HttpMethod.GET, "/").withArg(QueryParameter.FOLLOW_LINKS, "user");
        final RestfulResponse<HomePageRepresentation> restfulResponse = request.executeT();
        
        
    }
    

}


    