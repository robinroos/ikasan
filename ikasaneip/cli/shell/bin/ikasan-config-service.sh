# /*
#  * $Id$
#  * $URL$
#  *
#  * Do not change this script. For environment specifics change the env.sh script.
#  *
#  * ====================================================================
#  * Ikasan Enterprise Integration Platform
#  *
#  * Distributed under the Modified BSD License.
#  * Copyright notice: The copyright for this software and a full listing
#  * of individual contributors are as shown in the packaged copyright.txt
#  * file.
#  *
#  * All rights reserved.
#  *
#  * Redistribution and use in source and binary forms, with or without
#  * modification, are permitted provided that the following conditions are met:
#  *
#  *  - Redistributions of source code must retain the above copyright notice,
#  *    this list of conditions and the following disclaimer.
#  *
#  *  - Redistributions in binary form must reproduce the above copyright notice,
#  *    this list of conditions and the following disclaimer in the documentation
#  *    and/or other materials provided with the distribution.
#  *
#  *  - Neither the name of the ORGANIZATION nor the names of its contributors may
#  *    be used to endorse or promote products derived from this software without
#  *    specific prior written permission.
#  *
#  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
#  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
#  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
#  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
#  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
#  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
#  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
#  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
#  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
#  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#  * ====================================================================
#  */

#!/bin/bash
#set -u
# This script assumes you are running with a spring config service to source application properties
# Do not change this script, change the config-service-env.bat script fpr you custom environment settings.

SCRIPT_DIR=$(pwd)

LOCAL_ENV=$(pwd)/config-service-env.sh
if [[ -f "$LOCAL_ENV" ]]; then
    . ${LOCAL_ENV}
fi

CONFIG_SERVICE_URL=${CONFIG_SERVICE_URL:-"http://localhost:8880"}
CONFIG_SERVICE_BOOTSTRAP_LOCATION=${CONFIG_SERVICE_BOOTSTRAP_LOCATION:-"$HOME/bootstrap.properties"}


$JAVA_HOME/bin/java --illegal-access=deny -Dspring.cloud.config.enabled=true -Dspring.cloud.bootstrap.location=file:$CONFIG_SERVICE_BOOTSTRAP_LOCATION -Dspring.cloud.config.uri=$CONFIG_SERVICE_URL -Dspring.application.name=$MODULE_NAME -jar $SCRIPT_DIR/lib/ikasan-shell-${project.version}.jar "$@"
