/* 
 * $Id: SchedulerFactoryTest.java 3629 2011-04-18 10:00:52Z mitcje $
 * $URL: http://open.jira.com/svn/IKASAN/branches/ikasaneip-0.9.x/scheduler/src/test/java/org/ikasan/scheduler/SchedulerFactoryTest.java $
 *
 * ====================================================================
 * Ikasan Enterprise Integration Platform
 * 
 * Distributed under the Modified BSD License.
 * Copyright notice: The copyright for this software and a full listing 
 * of individual contributors are as shown in the packaged copyright.txt 
 * file. 
 * 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 *  - Neither the name of the ORGANIZATION nor the names of its contributors may
 *    be used to endorse or promote products derived from this software without 
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package com.ikasan.sample.spring.boot.builderpattern;

import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.ikasan.builder.IkasanApplication;
import org.ikasan.spec.flow.Flow;
import org.ikasan.spec.module.Module;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SocketUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This test class supports the <code>SimpleExample</code> class.
 * 
 * @author Ikasan Development Team
 */
@RunWith(SpringRunner.class)
public class ApplicationTest
{
    public EmbeddedActiveMQBroker broker = new EmbeddedActiveMQBroker();

    private IkasanApplication ikasanApplication;

    @Before
    public void setup()
    {
        broker.start();

        String[] args = { "--server.port="+ SocketUtils.findAvailableTcpPort(8000,9000)};

        Application myApplication = new Application();
        ikasanApplication = myApplication.boot(args);
        System.out.println("Check is module healthy.");

    }

    @After
    public void shutdown()
    {
        ikasanApplication.close();
        broker.stop();
    }

    
    /**
     * Test simple invocation.
     */
    @DirtiesContext
    @Test
    public void test_createModule_start_and_stop_flow() throws Exception
    {

        List<Module> modules  = ikasanApplication.getModules();
        assertTrue("There should only be 1 module in this application, but found " + modules.size(), modules.size() == 1);
        Module<Flow> module = modules.get(0);
        Flow flow = module.getFlow("sourceFileFlow");

        // start flow
        flow.start();
        assertEquals("running",flow.getState());
        flow.stop();
        assertEquals("stopped",flow.getState());
    }

    /**
     * Sleep for value in millis
     * @param value
     */
    private void pause(long value)
    {
        try
        {
            Thread.sleep(value);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
