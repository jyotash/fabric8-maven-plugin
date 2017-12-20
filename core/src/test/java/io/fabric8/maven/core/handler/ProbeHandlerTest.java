/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.fabric8.maven.core.handler;

import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.maven.core.config.ProbeConfig;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProbeHandlerTest {

    @Test
    //EmptyProbeConfig
    public void getProbeTestFirst() {

        Probe probe;
        ProbeHandler probeHandler = new ProbeHandler();

        ProbeConfig probeConfig = null;

        probe = probeHandler.getProbe(probeConfig);

        assertNull(probe);
    }

    @Test
    //ProbeConfig without any action
    public void getProbeTestSecond() {

        Probe probe;
        ProbeHandler probeHandler = new ProbeHandler();

        ProbeConfig probeConfig = new ProbeConfig.Builder()
                .build();

        probe = probeHandler.getProbe(probeConfig);

        assertNull(probe);
    }

    @Test
    //ProbeConfig with HTTPGet Action
    public void getProbeTestThird() {

        Probe probe;
        ProbeHandler probeHandler = new ProbeHandler();
        //withEmptyUrl
        ProbeConfig probeConfig = new ProbeConfig.Builder()
                .initialDelaySeconds(5).timeoutSeconds(5).getUrl(null)
                .build();

        probe = probeHandler.getProbe(probeConfig);
        //assertion
        assertNull(probe);

        //withUrl
        probeConfig = new ProbeConfig.Builder()
                .initialDelaySeconds(5).timeoutSeconds(5).getUrl("http://www.healthcheck.com:8080/healthz")
                .build();

        probe = probeHandler.getProbe(probeConfig);
        //assertion
        assertNotNull(probe);
        assertEquals(5,probe.getInitialDelaySeconds().intValue());
        assertEquals(5,probe.getTimeoutSeconds().intValue());
        assertEquals("www.healthcheck.com",probe.getHttpGet().getHost());
        assertEquals(null,probe.getHttpGet().getHttpHeaders());
        assertEquals("/healthz",probe.getHttpGet().getPath());
        assertEquals(8080,probe.getHttpGet().getPort().getIntVal().intValue());
        assertEquals("http",probe.getHttpGet().getScheme());
        assertNull(probe.getExec());
        assertNull(probe.getTcpSocket());

        //URL Without http Portocol
        probeConfig = new ProbeConfig.Builder()
                .initialDelaySeconds(5).timeoutSeconds(5).getUrl("www.healthcheck.com:8080/healthz")
                .build();

        probe = probeHandler.getProbe(probeConfig);
        assertNull(probe);

        //withInvalidUrl
        probeConfig = new ProbeConfig.Builder()
                .initialDelaySeconds(5).timeoutSeconds(5).getUrl("httphealthcheck.com:8080/healthz")
                .build();

        try {
            probe = probeHandler.getProbe(probeConfig);
        }
        catch(IllegalArgumentException e){
            //assertion
            assertEquals("Invalid URL httphealthcheck.com:8080/healthz " +
                    "given for HTTP GET readiness check",e.getMessage());
        }
    }

    @Test
    //ProbeConfig with Exec Action
    public void getProbeTestFourth() {

        Probe probe;
        ProbeHandler probeHandler = new ProbeHandler();
        //withEmptyExec
        ProbeConfig probeConfig = new ProbeConfig.Builder()
                .initialDelaySeconds(5).timeoutSeconds(5).exec("")
                .build();

        probe = probeHandler.getProbe(probeConfig);
        //assertion
        assertNull(probe);

        //withExec
        probeConfig = new ProbeConfig.Builder()
                .initialDelaySeconds(5).timeoutSeconds(5).exec("cat /tmp/probe")
                .build();

        probe = probeHandler.getProbe(probeConfig);
        //assertion
        assertNotNull(probe);
        assertEquals(5,probe.getInitialDelaySeconds().intValue());
        assertEquals(5,probe.getTimeoutSeconds().intValue());
        assertNotNull(probe.getExec());
        assertEquals(2,probe.getExec().getCommand().size());
        assertEquals("[cat, /tmp/probe]",probe.getExec().getCommand().toString());
        assertNull(probe.getHttpGet());
        assertNull(probe.getTcpSocket());

        //withInvalidExec
        probeConfig = new ProbeConfig.Builder()
                .initialDelaySeconds(5).timeoutSeconds(5).exec("   ")
                .build();

        probe = probeHandler.getProbe(probeConfig);
        //assertion
        assertNull(probe);
    }

    @Test
    //ProbeConfig with TCP Action
    public void getProbeTestFifth() {

        Probe probe;
        ProbeHandler probeHandler = new ProbeHandler();
        //withno url, only port
        ProbeConfig probeConfig = new ProbeConfig.Builder()
                .initialDelaySeconds(5).timeoutSeconds(5).tcpPort("80")
                .build();

        probe = probeHandler.getProbe(probeConfig);
        //assertion
        assertNotNull(probe);
        assertNull(probe.getHttpGet());
        assertNotNull(probe.getTcpSocket());
        assertEquals(80,probe.getTcpSocket().getPort().getIntVal().intValue());
        assertNull(probe.getTcpSocket().getHost());
        assertNull(probe.getExec());
        assertEquals(5,probe.getInitialDelaySeconds().intValue());
        assertEquals(5,probe.getTimeoutSeconds().intValue());

        //withport and url but with http request
        probeConfig = new ProbeConfig.Builder()
                .initialDelaySeconds(5).timeoutSeconds(5)
                .getUrl("http://www.healthcheck.com:8080/healthz").tcpPort("80")
                .build();

        probe = probeHandler.getProbe(probeConfig);
        //assertion
        assertNotNull(probe);
        assertNotNull(probe.getHttpGet());
        assertNull(probe.getTcpSocket());
        assertNull(probe.getExec());
        assertEquals(5,probe.getInitialDelaySeconds().intValue());
        assertEquals(5,probe.getTimeoutSeconds().intValue());
        assertEquals("www.healthcheck.com",probe.getHttpGet().getHost());
        assertEquals(null,probe.getHttpGet().getHttpHeaders());
        assertEquals("/healthz",probe.getHttpGet().getPath());
        assertEquals(8080,probe.getHttpGet().getPort().getIntVal().intValue());
        assertEquals("http",probe.getHttpGet().getScheme());

        //withport and url but with other request and port as int
        probeConfig = new ProbeConfig.Builder()
                .initialDelaySeconds(5).timeoutSeconds(5)
                .getUrl("tcp://www.healthcheck.com:8080/healthz").tcpPort("80")
                .build();

        probe = probeHandler.getProbe(probeConfig);
        //assertion
        assertNotNull(probe);
        assertNull(probe.getHttpGet());
        assertNotNull(probe.getTcpSocket());
        assertNull(probe.getExec());
        assertEquals(80,probe.getTcpSocket().getPort().getIntVal().intValue());
        assertEquals("www.healthcheck.com",probe.getTcpSocket().getHost());
        assertEquals(5,probe.getInitialDelaySeconds().intValue());
        assertEquals(5,probe.getTimeoutSeconds().intValue());

        //withport and url but with other request and port as string
        probeConfig = new ProbeConfig.Builder()
                .initialDelaySeconds(5).timeoutSeconds(5)
                .getUrl("tcp://www.healthcheck.com:8080/healthz").tcpPort("httpPort")
                .build();

        probe = probeHandler.getProbe(probeConfig);
        //assertion
        assertNotNull(probe);
        assertNull(probe.getHttpGet());
        assertNotNull(probe.getTcpSocket());
        assertNull(probe.getExec());
        assertEquals("httpPort",probe.getTcpSocket().getPort().getStrVal());
        assertEquals("www.healthcheck.com",probe.getTcpSocket().getHost());
        assertEquals(5,probe.getInitialDelaySeconds().intValue());
        assertEquals(5,probe.getTimeoutSeconds().intValue());

        //without port and url with http request
        probeConfig = new ProbeConfig.Builder()
                .initialDelaySeconds(5).timeoutSeconds(5)
                .getUrl("http://www.healthcheck.com:8080/healthz")
                .build();

        probe = probeHandler.getProbe(probeConfig);
        //assertion
        assertNotNull(probe);
        assertNotNull(probe.getHttpGet());
        assertNull(probe.getTcpSocket());
        assertNull(probe.getExec());
        assertEquals(5,probe.getInitialDelaySeconds().intValue());
        assertEquals(5,probe.getTimeoutSeconds().intValue());
        assertEquals("www.healthcheck.com",probe.getHttpGet().getHost());
        assertEquals(null,probe.getHttpGet().getHttpHeaders());
        assertEquals("/healthz",probe.getHttpGet().getPath());
        assertEquals(8080,probe.getHttpGet().getPort().getIntVal().intValue());
        assertEquals("http",probe.getHttpGet().getScheme());

        //without port and url with tcp request
        probeConfig = new ProbeConfig.Builder()
                .initialDelaySeconds(5).timeoutSeconds(5)
                .getUrl("tcp://www.healthcheck.com:8080/healthz")
                .build();

        probe = probeHandler.getProbe(probeConfig);
        //assertion
        assertNull(probe);

        //withInvalidUrl
        probeConfig = new ProbeConfig.Builder()
                .initialDelaySeconds(5).timeoutSeconds(5).getUrl("healthcheck.com:8080/healthz")
                .tcpPort("80")
                .build();

        try {
            probe = probeHandler.getProbe(probeConfig);
        }
        catch(IllegalArgumentException e){
            //assertion
            assertEquals("Invalid URL healthcheck.com:8080/healthz " +
                    "given for TCP readiness check",e.getMessage());
        }
    }
}