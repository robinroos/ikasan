/*
 *  ====================================================================
 *  Ikasan Enterprise Integration Platform
 *
 *  Distributed under the Modified BSD License.
 *  Copyright notice: The copyright for this software and a full listing
 *  of individual contributors are as shown in the packaged copyright.txt
 *  file.
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   - Neither the name of the ORGANIZATION nor the names of its contributors may
 *     be used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 *  USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  ====================================================================
 *
 */
package org.ikasan.component.endpoint.filesystem.messageprovider;

import org.ikasan.component.endpoint.quartz.consumer.CorrelatingScheduledConsumer;
import org.ikasan.component.endpoint.quartz.consumer.MessageProvider;
import org.ikasan.component.endpoint.quartz.consumer.ScheduledConsumer;
import org.ikasan.spec.component.endpoint.EndpointListener;
import org.ikasan.spec.configuration.Configured;
import org.ikasan.spec.event.ForceTransactionRollbackException;
import org.ikasan.spec.management.ManagedResource;
import org.ikasan.spec.management.ManagedResourceRecoveryManager;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Implementation of a MessageProvider based on returning a correlated list of File references.
 *
 * @author Ikasan Development Team
 */
public class CorrelatingFileMessageProvider implements MessageProvider<CorrelatedFileList>,
        ManagedResource, Configured<CorrelatedFileConsumerConfiguration>, EndpointListener<String,IOException>
{
    /** logger instance */
    private static Logger logger = LoggerFactory.getLogger(CorrelatingFileMessageProvider.class);

    /** path separator */
    private static final String FQN_PATH_SEPARATOR = "/";

    /** file consumer configuration */
    private CorrelatedFileConsumerConfiguration fileConsumerConfiguration;

    /** list of file matchers to be invoked */
    List<FileMatcher> fileMatchers = new ArrayList<>();

    /** criticality for this resource */
    boolean criticalOnStartup;

    /** handle to the recovery manager for escalating failures */
    ManagedResourceRecoveryManager managedResourceRecoveryManager;

    /** record all filenames returned from the fileMatcher */
    List<String> filenames = new ArrayList<>();

    /** post processor on the read files */
    MessageProviderPostProcessor messageProviderPostProcessor;

    /** maintain a control state to coordinate the stopping of any processing */
    boolean active;

    @Override
    public CorrelatedFileList invoke(JobExecutionContext context)
    {
        String correlatingIdentifier = (String)context.getMergedJobDataMap()
            .get(CorrelatingScheduledConsumer.CORRELATION_ID);

        logger.debug(CorrelatingScheduledConsumer.CORRELATION_ID
            + " " + correlatingIdentifier);

        if(correlatingIdentifier == null || correlatingIdentifier
            .equals(CorrelatingScheduledConsumer.EMPTY_CORRELATION_ID)) {
            return null;
        }

        List<File> files = new ArrayList<>();
        filenames.clear();

        try
        {
            for(FileMatcher fileMatcher:fileMatchers)
            {
                try
                {
                    if(fileMatcher instanceof DynamicFileMatcher) {
                        ((DynamicFileMatcher) fileMatcher).setCorrelatingIdentifier(correlatingIdentifier);
                    }

                    // Note, this class is a listener, file matcher can invoke this.onMessage and thus update filenames.
                    fileMatcher.invoke();
                }
                catch(IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        catch(ConcurrentModificationException e)
        {
            if(isActive())
            {
                throw e;
            }

            throw new ForceTransactionRollbackException("File processing interrupted by a stop request.");
        }

        for(String filename:filenames)
        {
            File file = new File(filename);
            files.add(file);
        }

        if(this.messageProviderPostProcessor != null)
        {
            this.messageProviderPostProcessor.invoke(files);
        }

        if(files.size() > 0)
        {
            if(this.fileConsumerConfiguration.isLogMatchedFilenames())
            {
                for(File file:files)
                {
                    if(logger.isInfoEnabled())
                    {
                        logger.info("Matching filename for " + file.getAbsolutePath());
                    }
                }
            }

            return new CorrelatedFileList(files, correlatingIdentifier);
        }

        if(this.fileConsumerConfiguration.isLogMatchedFilenames())
        {
            for(String filename:this.fileConsumerConfiguration.getFilenames())
            {
                if(logger.isInfoEnabled())
                {
                    if(filename.startsWith("/"))
                    {
                        logger.info("No matching filename for " + filename);
                    }
                    else
                    {
                        logger.info("No matching filename for " + System.getProperty("user.dir") + "/" + filename);
                    }
                }
            }
        }

        return null;
    }

    public void setMessageProviderPostProcessor(MessageProviderPostProcessor messageProviderPostProcessor)
    {
        this.messageProviderPostProcessor = messageProviderPostProcessor;
    }

    public MessageProviderPostProcessor getMessageProviderPostProcessor()
    {
        return this.messageProviderPostProcessor;
    }

    @Override
    public CorrelatedFileConsumerConfiguration getConfiguration()
    {
        return fileConsumerConfiguration;
    }

    @Override
    public void setConfiguration(CorrelatedFileConsumerConfiguration fileConsumerConfiguration)
    {
        this.fileConsumerConfiguration = fileConsumerConfiguration;
        if(messageProviderPostProcessor != null && messageProviderPostProcessor instanceof Configured)
        {
            ((Configured)messageProviderPostProcessor).setConfiguration(this.fileConsumerConfiguration);
        }
    }

    /**
     * Split fullyQualifiedFilename into its path and basename (filename), use these to create a matcher.
     * @param fullyQualifiedFilename is a template i.e. /a/b/c/filePattern  path=/a/b/c  name=filePattern
     * @param dynamicFileName return a DynamicFileMatcher instead of a FileMatcher
     * @return the filematcher using the template provided by fullyQualifiedFilename
     */
    protected FileMatcher getFileMatcher(String fullyQualifiedFilename, boolean dynamicFileName)
    {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        if( !isWindows && !fullyQualifiedFilename.startsWith("/") && !fullyQualifiedFilename.startsWith("."))
        {
            // assume relative reference and prefix accordingly
            fullyQualifiedFilename = "./" + fullyQualifiedFilename;
        }
        int lastIndexOffullPath = fullyQualifiedFilename.lastIndexOf(FQN_PATH_SEPARATOR);
        String path = fullyQualifiedFilename.substring(0,lastIndexOffullPath);
        String name = fullyQualifiedFilename.substring(++lastIndexOffullPath);

        if (dynamicFileName)
        {
            return new DynamicFileMatcher(
                this.fileConsumerConfiguration.isIgnoreFileRenameWhilstScanning(),
                path,
                name,
                fileConsumerConfiguration.getDirectoryDepth(),
                this,
                fileConsumerConfiguration.getSpelExpression());
        }
        else
        {
            return new FileMatcher(this.fileConsumerConfiguration.isIgnoreFileRenameWhilstScanning(),
                path,
                name,
                fileConsumerConfiguration.getDirectoryDepth(),
                this);
        }
    }

    @Override
    public void onMessage(String filename)
    {
        filenames.add(filename);
    }

    @Override
    public void onException(IOException throwable)
    {
        managedResourceRecoveryManager.recover(throwable);
    }

    @Override
    public boolean isActive()
    {
        return this.active;
    }

    @Override
    public void startManagedResource()
    {
        if(fileConsumerConfiguration.getFilenames() != null)
        {
            for(String filename : fileConsumerConfiguration.getFilenames())
            {
                this.fileMatchers.add(getFileMatcher(filename, fileConsumerConfiguration.isDynamicFileName()) );
            }
        }

        this.active = true;
        logger.info("  - Started embedded managed component [FileMessageProvider]");
    }

    @Override
    public void stopManagedResource()
    {
        this.active = false;
        this.fileMatchers.clear();
        logger.info("  - Stopped embedded managed component [FileMessageProvider]");
    }

    @Override
    public void setManagedResourceRecoveryManager(ManagedResourceRecoveryManager managedResourceRecoveryManager)
    {
        this.managedResourceRecoveryManager = managedResourceRecoveryManager;
    }

    @Override
    public boolean isCriticalOnStartup()
    {
        return criticalOnStartup;
    }

    @Override
    public void setCriticalOnStartup(boolean criticalOnStartup)
    {
        this.criticalOnStartup = criticalOnStartup;
    }
}
