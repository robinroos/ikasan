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
package org.ikasan.ootb.scheduler.agent.module.component.broker;

import ch.qos.logback.core.util.FileUtil;

import org.ikasan.ootb.scheduled.model.Outcome;
import org.ikasan.ootb.scheduler.agent.module.component.cli.CommandLinesArgConverter;
import org.ikasan.ootb.scheduler.agent.module.model.EnrichedContextualisedScheduledProcessEvent;
import org.ikasan.spec.component.endpoint.Broker;
import org.ikasan.spec.component.endpoint.EndpointException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Map;

/**
 * Job Starting Broker implementation for the execution of the command line process.
 *
 * @author Ikasan Development Team
 */
public class JobStartingBroker implements Broker<EnrichedContextualisedScheduledProcessEvent, EnrichedContextualisedScheduledProcessEvent>
{
    /** logger */
    private static Logger logger = LoggerFactory.getLogger(JobStartingBroker.class);

    private DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    private CommandLinesArgConverter commandLinesArgConverter;

    public JobStartingBroker(CommandLinesArgConverter commandLinesArgConverter) {
        this.commandLinesArgConverter = commandLinesArgConverter;
    }

    @Override
    public EnrichedContextualisedScheduledProcessEvent invoke(EnrichedContextualisedScheduledProcessEvent scheduledProcessEvent) throws EndpointException
    {
        scheduledProcessEvent.setJobStarting(true);
        scheduledProcessEvent.setOutcome(Outcome.EXECUTION_INVOKED);

        // Skipping a job is as simple as marking the job as successful.
        if(scheduledProcessEvent.isSkipped() || scheduledProcessEvent.isDryRun() ) {
            return scheduledProcessEvent;
        }

        // If any day of weeks are defined, we only run the job on the day of the
        // week that is defined.
        if(scheduledProcessEvent.getInternalEventDrivenJob().getDaysOfWeekToRun() != null
            && !scheduledProcessEvent.getInternalEventDrivenJob().getDaysOfWeekToRun().isEmpty()
            && !scheduledProcessEvent.getInternalEventDrivenJob().getDaysOfWeekToRun()
            .contains(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))) {
            return scheduledProcessEvent;
        }

        String[] commandLineArgs = commandLinesArgConverter.getCommandLineArgs(scheduledProcessEvent.getInternalEventDrivenJob().getCommandLine());
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(commandLineArgs);


        // allow change of the new process working directory
        if(scheduledProcessEvent.getInternalEventDrivenJob().getWorkingDirectory() != null
            && scheduledProcessEvent.getInternalEventDrivenJob().getWorkingDirectory().length() > 0) {
            File workingDirectory = new File(scheduledProcessEvent.getInternalEventDrivenJob().getWorkingDirectory());
            processBuilder.directory(workingDirectory);
        }

        // We set up the std out and error log files and set them on the process builder.
        String formattedDate = formatter.format(LocalDateTime.now());
        File outputLog = new File(scheduledProcessEvent.getResultOutput());
        if(outputLog.exists()) {
            outputLog.renameTo(new File(scheduledProcessEvent.getResultOutput() + "." + formattedDate));
        }

        FileUtil.createMissingParentDirectories(outputLog);
        processBuilder.redirectOutput(outputLog);
        scheduledProcessEvent.setResultOutput(outputLog.getAbsolutePath());

        File errorLog = new File(scheduledProcessEvent.getResultError());
        if(errorLog.exists()) {
            errorLog.renameTo(new File(scheduledProcessEvent.getResultError() + "." + formattedDate));
        }

        FileUtil.createMissingParentDirectories(errorLog);
        processBuilder.redirectError(errorLog);
        scheduledProcessEvent.setResultError(errorLog.getAbsolutePath());

        // Add job context parameters to the process environment if there are any.
        if(scheduledProcessEvent.getContextParameters() != null
            && !scheduledProcessEvent.getContextParameters().isEmpty()) {
            Map<String, String> env = processBuilder.environment();

            scheduledProcessEvent.getContextParameters()
                .forEach(contextParameter -> env.put(contextParameter.getName()
                    , (String)contextParameter.getValue()));
        }

        try {
            // Start the process and enrich the payload.
            Process process = processBuilder.start();
            scheduledProcessEvent.setPid(process.pid());
            scheduledProcessEvent.setProcess(process);
        }
        catch (IOException e) {
            throw new EndpointException(e);
        }

        return scheduledProcessEvent;
    }
}
