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
package org.ikasan.module;

import java.util.ArrayList;
import java.util.List;

import org.ikasan.spec.flow.Flow;
import org.ikasan.spec.module.Module;

/**
 * A simple representation of a Module
 * 
 * @author Ikasan Development Team
 */
public class SimpleModule extends AbstractModule implements Module
{
    /**
     * Constructor
     * 
     * @param name The name of the module
     * @param flows A list of Flows for the module
     */
    public SimpleModule(String name, List<Flow> flows)
    {
        super(name, flows);
    }

    /**
     * Constructor
     *
     * @param name The name of the module
     * @param version version of the module
     * @param flows A list of Flows for the module
     */
    public SimpleModule(String name, String version, List<Flow> flows)
    {
        super(name, flows, version);
    }

    /**
     * Constructor
     *
     * @param name The name of the module
     * @param version version of the module
     * @param flows A list of Flows for the module
     */
    public SimpleModule(String name, String version, List<Flow> flows, String url)
    {
        super(name, flows, version, url);
    }

    public SimpleModule(String name, String version, String url)
    {
        super(name, version, url);
    }

    /**
     * Constructor
     *
     * @param name Name of the module
     */
    public SimpleModule(String name)
    {
        super(name);
    }

    /**
     * Constructor
     *
     * @param name Name of the module
     * @param version version of the module
     */
    public SimpleModule(String name, String version)
    {
        super(name, version);
    }

}
