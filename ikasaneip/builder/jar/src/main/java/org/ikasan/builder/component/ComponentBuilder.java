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
package org.ikasan.builder.component;

import org.ikasan.builder.AopProxyProvider;
import org.ikasan.builder.component.converter.ObjectToXmlStringConverterBuilder;
import org.ikasan.builder.component.converter.ObjectToXmlStringConverterBuilderImpl;
import org.ikasan.builder.component.converter.XmlStringToObjectConverterBuilder;
import org.ikasan.builder.component.converter.XmlStringToObjectConverterBuilderImpl;
import org.ikasan.builder.component.endpoint.*;
import org.ikasan.builder.component.filter.MessageFilterBuilder;
import org.ikasan.builder.component.filter.MessageFilterBuilderImpl;
import org.ikasan.builder.component.splitting.ListSplitterBuilderImpl;
import org.ikasan.component.converter.xml.XmlConfiguration;
import org.ikasan.component.converter.xml.XmlStringToObjectConfiguration;
import org.ikasan.component.endpoint.db.messageprovider.DbMessageProvider;
import org.ikasan.component.endpoint.filesystem.messageprovider.FileMessageProvider;
import org.ikasan.component.endpoint.jms.spring.consumer.JmsContainerConsumer;
import org.ikasan.component.endpoint.util.producer.LogProducer;
import org.ikasan.component.splitter.DefaultListSplitter;
import org.ikasan.connector.base.command.TransactionalResourceCommandDAO;
import org.ikasan.connector.basefiletransfer.outbound.persistence.BaseFileTransferDao;
import org.ikasan.connector.util.chunking.model.dao.FileChunkDao;
import org.ikasan.filter.duplicate.service.DuplicateFilterService;
import org.ikasan.scheduler.ScheduledJobFactory;
import org.ikasan.spec.component.endpoint.Producer;
import org.ikasan.spec.component.splitting.Splitter;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.IkasanJmsTemplate;
import org.springframework.transaction.jta.JtaTransactionManager;

import java.util.ArrayList;

/**
 * A simple Component builder.
 * 
 * @author Ikasan Development Team
 */
public class ComponentBuilder
{
    /** logger */
    private static Logger logger = LoggerFactory.getLogger(ComponentBuilder.class);

    /** handle to spring context */
    ApplicationContext applicationContext;

    public ComponentBuilder(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
        if(applicationContext == null)
        {
            throw new IllegalArgumentException("applicationContext cannot be 'null'");
        }
    }

    /**
     * Get an instance of an Ikasan default scheduledConsumer
     * @return scheduledConsumerBuilder
     */
    public ScheduledConsumerBuilder scheduledConsumer()
    {
        ScheduledConsumerBuilder scheduledConsumerBuilder = new ScheduledConsumerBuilderImpl(
                this.applicationContext.getBean(Scheduler.class),
                this.applicationContext.getBean(ScheduledJobFactory.class),
                this.applicationContext.getBean(AopProxyProvider.class));
        return scheduledConsumerBuilder;
    }

    /**
     * Get an instance of an Ikasan default scheduledConsumer using scheduledJobName and defaulted job group based on the scheduled job name
     * @param scheduledJobName
     * @return scheduledConsumerBuilder
     */
    public ScheduledConsumerBuilder scheduledConsumer(String scheduledJobName)
    {
        ScheduledConsumerBuilder scheduledConsumerBuilder = new ScheduledConsumerBuilderImpl(
                this.applicationContext.getBean(Scheduler.class),
                this.applicationContext.getBean(ScheduledJobFactory.class),
                this.applicationContext.getBean(AopProxyProvider.class));
        scheduledConsumerBuilder.setScheduledJobName(scheduledJobName);
        scheduledConsumerBuilder.setScheduledJobGroupName(scheduledJobName + "_group");
        return scheduledConsumerBuilder;
    }

    /**
     * Get an instance of an Ikasan default scheduledConsumer with SFTP message Provider
     * @return sftpConsumerBuilder
     */
    public SftpConsumerBuilder sftpConsumer()
    {
        SftpConsumerBuilder sftpConsumerBuilder = new SftpConsumerBuilderImpl(
                this.applicationContext.getBean(Scheduler.class),
                this.applicationContext.getBean(ScheduledJobFactory.class),
                this.applicationContext.getBean(AopProxyProvider.class),
                this.applicationContext.getBean("transactionManager", JtaTransactionManager.class),
                this.applicationContext.getBean(BaseFileTransferDao.class),
                this.applicationContext.getBean(FileChunkDao.class),
                this.applicationContext.getBean(TransactionalResourceCommandDAO.class)

        );
        return sftpConsumerBuilder;
    }

    /**
     * Get an instance of an Ikasan default SFTP Producer
     * @return sftpProducerBuilder
     */
    public SftpProducerBuilder sftpProducer()
    {
        SftpProducerBuilder sftpProducerBuilder = new SftpProducerBuilderImpl(
                this.applicationContext.getBean("transactionManager", JtaTransactionManager.class)
                ,this.applicationContext.getBean(BaseFileTransferDao.class)
                ,this.applicationContext.getBean(FileChunkDao.class)
                ,this.applicationContext.getBean(TransactionalResourceCommandDAO.class)

        );
        return sftpProducerBuilder;
    }

    /**
     * Get an instance of an Ikasan default Chunk SFTP Producer
     * @return chunkSftpProducerBuilder
     */
    public ChunkSftpProducerBuilder chunkSftpProducer()
    {
        ChunkSftpProducerBuilder chunkSftpProducerBuilder = new ChunkSftpProducerBuilderImpl(
                this.applicationContext.getBean("transactionManager", JtaTransactionManager.class)
                ,this.applicationContext.getBean(BaseFileTransferDao.class)
                ,this.applicationContext.getBean(FileChunkDao.class)
                ,this.applicationContext.getBean(TransactionalResourceCommandDAO.class)

        );
        return chunkSftpProducerBuilder;
    }

    /**
     * Get an instance of an Ikasan default scheduledConsumer with FTP message Provider
     * @return FtpConsumerBuilder
     */
    public FtpConsumerBuilder ftpConsumer()
    {
        return new FtpConsumerBuilderImpl(this.applicationContext.getBean(Scheduler.class),
                this.applicationContext.getBean(ScheduledJobFactory.class), this.applicationContext.getBean(AopProxyProvider.class)
                ,this.applicationContext.getBean("transactionManager", JtaTransactionManager.class)
                ,this.applicationContext.getBean(BaseFileTransferDao.class)
                ,this.applicationContext.getBean(FileChunkDao.class)
                ,this.applicationContext.getBean(TransactionalResourceCommandDAO.class)
        );
    }

    /**
     * Get an instance of an Ikasan default FTP Producer
     * @return ftpProducerBuilder
     */
    public FtpProducerBuilder ftpProducer()
    {

        FtpProducerBuilder ftpProducerBuilder = new FtpProducerBuilderImpl(
                this.applicationContext.getBean("transactionManager", JtaTransactionManager.class)
                ,this.applicationContext.getBean(BaseFileTransferDao.class)
                ,this.applicationContext.getBean(FileChunkDao.class)
                ,this.applicationContext.getBean(TransactionalResourceCommandDAO.class)

        );
        return ftpProducerBuilder;
    }

    /**
     * Get an instance of an Ikasan default fileConsumer
     * @return scheduledConsumerBuilder
     */
    public FileConsumerBuilder fileConsumer()
    {
        FileConsumerBuilder fileConsumerBuilder = new FileConsumerBuilderImpl(
                this.applicationContext.getBean(Scheduler.class),
                this.applicationContext.getBean(ScheduledJobFactory.class),
                this.applicationContext.getBean(AopProxyProvider.class),
                new FileMessageProvider() );
        return fileConsumerBuilder;
    }

    /**
     * Get an instance of an Ikasan default fileProducer
     * @return FileProducerBuilder
     */
    public FileProducerBuilder fileProducer()
    {
        return new FileProducerBuilderImpl();
    }

    /**
     * Get an instance of an Ikasan default jmsConsumer
     * @return jmsConsumerBuilder
     */
    public JmsConsumerBuilder jmsConsumer()
    {
        JmsContainerConsumer jmsConsumer = new JmsContainerConsumer();
        JmsConsumerBuilder jmsConsumerBuilder = new JmsConsumerBuilderImpl(jmsConsumer,
                this.applicationContext.getBean("transactionManager", JtaTransactionManager.class), this.applicationContext.getBean(AopProxyProvider.class));
        return jmsConsumerBuilder;
    }

    /**
     * Get an instance of an Ikasan default jmsProducer
     * @return jmsProducerBuilder
     */
    public JmsProducerBuilder jmsProducer() {
        JmsProducerBuilder jmsProducerBuilder = new JmsProducerBuilderImpl(new IkasanJmsTemplate(), this.applicationContext.getBean("transactionManager", JtaTransactionManager.class));
        return jmsProducerBuilder;
    }

    /**
     * Get an instance of an Ikasan logProducerBuilder
     * @return LogProducerBuilder
     */
    public LogProducerBuilder logProducer()
    {
        return new LogProducerBuilderImpl( new LogProducer() );
    }

    /**
     * Get an instance of an Ikasan devNullProducerBuilder
     * @return Builder<Producer>
     */
    public Builder<Producer<?>> devNullProducer()
    {
        return new DevNullProducerBuilderImpl();
    }

    /**
     * Get an instance of an Ikasan listSplitterBuilder
     * @return Builder<Splitter>
     */
    public Builder<Splitter> listSplitter()
    {
        return new ListSplitterBuilderImpl( new DefaultListSplitter() );
    }

    /**
     * Get an instance of an Ikasan messageFilterBuilder
     * @return MessageFilterBuilder
     */
    public MessageFilterBuilder messageFilter()
    {
        return new MessageFilterBuilderImpl(this.applicationContext.getBean(DuplicateFilterService.class));
    }

    /**
     * Get an instance of an Ikasan emailProducerBuilder
     * @return emailProducerBuilder
     */
    public EmailProducerBuilder emailProducer()
    {
        return EmailProducerBuilder.getInstance();
    }

    /**
     * Get an instance of an Ikasan messageFilterBuilder
     * @return MessageFilterBuilder
     */
    public DbConsumerBuilder dbConsumer()
    {
        return new DbConsumerBuilderImpl(this.applicationContext.getBean(Scheduler.class),
                this.applicationContext.getBean(ScheduledJobFactory.class),
                this.applicationContext.getBean(AopProxyProvider.class),
                new DbMessageProvider());
    }

    /**
     * Get an instance of an Ikasan EventGeneratingConsumerBuilder
     * @return EventGeneratingConsumerBuilder
     */
    public EventGeneratingConsumerBuilder eventGeneratingConsumer()
    {
        try
        {
            return new EventGeneratingConsumerBuilderImpl(this.applicationContext.getBean(AopProxyProvider.class) );
        }
        catch(NoClassDefFoundError e)
        {
            throw new RuntimeException("Check your pom.xml dependencies to ensure you include\n"
                    + "<dependency>\n"
                    + "  <groupId>org.ikasan</groupId>\n"
                    + "  <artifactId>ikasan-test-endpoint</artifactId>\n"
                    + "</dependency>\n", e);
        }
    }

    /**
     * Get an instance of an Ikasan BigQueueConsumerBuilder
     *
     * @return BigQueueConsumerBuilder
     */
    public BigQueueConsumerBuilder bigQueueConsumer()
    {
        try
        {
            return new BigQueueConsumerBuilderImpl(this.applicationContext.getBean(AopProxyProvider.class),
                this.applicationContext.getBean(JtaTransactionManager.class).getTransactionManager());
        }
        catch(NoClassDefFoundError e)
        {
            throw new RuntimeException("Check your pom.xml dependencies to ensure you include\n"
                + "<dependency>\n"
                + "  <groupId>org.ikasan</groupId>\n"
                + "  <artifactId>ikasan-big-queue</artifactId>\n"
                + "</dependency>\n", e);
        }
    }

    /**
     * Get an instance of an Ikasan BigQueueConsumerBuilder
     *
     * @return BigQueueConsumerBuilder
     */
    public BigQueueProducerLRCOBuilder bigQueueProducer()
    {
        try
        {
            return new BigQueueProducerLRCOBuilderImpl(this.applicationContext.getBean(JtaTransactionManager.class).getTransactionManager());
        }
        catch(NoClassDefFoundError e)
        {
            throw new RuntimeException("Check your pom.xml dependencies to ensure you include\n"
                + "<dependency>\n"
                + "  <groupId>org.ikasan</groupId>\n"
                + "  <artifactId>ikasan-big-queue</artifactId>\n"
                + "</dependency>\n", e);
        }
    }

    /**
     * New instance of an Ikasan ObjectToXmlStringConverterBuilder
     * @return ObjectToXmlStringConverterBuilderImpl
     */
    public ObjectToXmlStringConverterBuilder objectToXmlStringConverter()
    {
        try
        {
            return new ObjectToXmlStringConverterBuilderImpl( new ArrayList<Class>(), new XmlConfiguration() );
        }
        catch(NoClassDefFoundError e)
        {
            throw new RuntimeException("Check your pom.xml dependencies to ensure you include\n"
                    + "<dependency>\n"
                    + "  <groupId>org.ikasan</groupId>\n"
                    + "  <artifactId>ikasan-component-converter</artifactId>\n"
                    + "</dependency>\n", e);
        }
    }

    /**
     * New instance of an Ikasan XmlStringToObjectConverterBuilder
     * @return XmlStringToObjectConverterBuilder
     */
    public XmlStringToObjectConverterBuilder xmlStringToObjectConverter()
    {
        try
        {
            return new XmlStringToObjectConverterBuilderImpl( new XmlStringToObjectConfiguration() );
        }
        catch(NoClassDefFoundError e)
        {
            throw new RuntimeException("Check your pom.xml dependencies to ensure you include\n"
                    + "<dependency>\n"
                    + "  <groupId>org.ikasan</groupId>\n"
                    + "  <artifactId>ikasan-component-converter</artifactId>\n"
                    + "</dependency>\n", e);
        }
    }
}




