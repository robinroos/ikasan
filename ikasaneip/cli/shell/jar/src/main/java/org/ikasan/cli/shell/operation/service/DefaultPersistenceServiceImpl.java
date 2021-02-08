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
package org.ikasan.cli.shell.operation.service;

import org.ikasan.cli.shell.operation.dao.ProcessPersistenceDao;
import org.ikasan.cli.shell.operation.model.IkasanProcess;

import java.util.Optional;

/**
 * Implementation of the Persistence Service contract.
 *
 * @author Ikasan Development Team
 */
public class DefaultPersistenceServiceImpl implements PersistenceService
{
    /** DAO handle */
    ProcessPersistenceDao processPersistenceDao;

    /**
     * Constructor
     * @param processPersistenceDao
     */
    public DefaultPersistenceServiceImpl(ProcessPersistenceDao processPersistenceDao)
    {
        this.processPersistenceDao = processPersistenceDao;
        if(processPersistenceDao == null)
        {
            throw new IllegalArgumentException("processPersistenceDao cannot be 'null'");
        }
    }

    @Override
    public ProcessHandle find(String type, String name)
    {
        IkasanProcess ikasanProcess = processPersistenceDao.find(type, name);
        if(ikasanProcess != null)
        {
            Optional<ProcessHandle> processHandle = ProcessHandle.of(ikasanProcess.getPid());
            if(processHandle.isEmpty())
            {
                return null;
            }

            return processHandle.get();
        }

        return null;
    }

    @Override
    public void persist(String type, String name, Process process)
    {
        Optional<String> user = process.info().user();
        processPersistenceDao.save(new IkasanProcess(type, name, process.pid(),
            (user.isEmpty() ? null : user.get())));
    }

    @Override
    public void remove(String type, String name)
    {
        processPersistenceDao.delete(type, name);
    }
}