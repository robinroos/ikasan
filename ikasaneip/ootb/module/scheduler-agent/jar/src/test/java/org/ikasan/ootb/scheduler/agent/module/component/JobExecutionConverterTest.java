/*
 * $Id$
 * $URL$
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
package org.ikasan.ootb.scheduler.agent.module.component;

import org.ikasan.spec.scheduled.ScheduledProcessEvent;
import org.ikasan.spec.component.transformation.Converter;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

import java.util.Date;

/**
 * This test class supports the <code>JobExecutionConverter</code>.
 *
 * @author Ikasan Development Team
 */
public class JobExecutionConverterTest
{
    /**
     * Mockery for mocking concrete classes
     */
    private Mockery mockery = new Mockery()
    {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    JobExecutionContext jobExecutionContext = mockery.mock(JobExecutionContext.class,"mockJobExecutionContext");
    JobDetail jobDetail = mockery.mock(JobDetail.class,"mockJobDetail");
    JobKey jobKey = new JobKey("name", "group");

    /**
     * Test simple invocation.
     */
    @Test
    public void test_successful_converter()
    {
        Converter<JobExecutionContext, ScheduledProcessEvent> converter = new JobExecutionConverter("moduleName");
        Date currentFireDate = new Date();
        Date nextFireDate = new Date();

        mockery.checking(new Expectations()
        {
            {
                exactly(1).of(jobExecutionContext).getFireTime();
                will(returnValue(currentFireDate));

                exactly(1).of(jobExecutionContext).getJobDetail();
                will(returnValue(jobDetail));

                exactly(1).of(jobDetail).getDescription();
                will(returnValue("job detail"));

                exactly(1).of(jobDetail).getKey();
                will(returnValue(jobKey));

                exactly(2).of(jobExecutionContext).getNextFireTime();
                will(returnValue(nextFireDate));
            }
        });

        ScheduledProcessEvent scheduledProcessEvent = converter.convert(jobExecutionContext);

        Assert.assertEquals(scheduledProcessEvent.getFireTime(),currentFireDate.getTime());
        Assert.assertEquals("moduleName", scheduledProcessEvent.getAgentName());
        Assert.assertEquals("job detail", scheduledProcessEvent.getJobDescription());
        Assert.assertEquals(scheduledProcessEvent.getNextFireTime(),nextFireDate.getTime());
        Assert.assertNull(scheduledProcessEvent.getCommandLine());
        Assert.assertTrue(scheduledProcessEvent.getPid() == 0);
        Assert.assertNull(scheduledProcessEvent.getCommandLine());
        Assert.assertNull(scheduledProcessEvent.getUser());

        mockery.assertIsSatisfied();
    }
}
