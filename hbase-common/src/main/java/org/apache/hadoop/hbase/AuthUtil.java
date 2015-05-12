begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|security
operator|.
name|UserProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Strings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|net
operator|.
name|DNS
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_comment
comment|/**  * Utility methods for helping with security tasks.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|AuthUtil
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|AuthUtil
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|AuthUtil
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Checks if security is enabled and if so, launches chore for refreshing kerberos ticket.    */
specifier|public
specifier|static
name|ScheduledChore
name|getAuthChore
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|UserProvider
name|userProvider
init|=
name|UserProvider
operator|.
name|instantiate
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// login the principal (if using secure Hadoop)
name|boolean
name|securityEnabled
init|=
name|userProvider
operator|.
name|isHadoopSecurityEnabled
argument_list|()
operator|&&
name|userProvider
operator|.
name|isHBaseSecurityEnabled
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|securityEnabled
condition|)
return|return
literal|null
return|;
name|String
name|host
init|=
literal|null
decl_stmt|;
try|try
block|{
name|host
operator|=
name|Strings
operator|.
name|domainNamePointerToHostName
argument_list|(
name|DNS
operator|.
name|getDefaultHost
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.client.dns.interface"
argument_list|,
literal|"default"
argument_list|)
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.client.dns.nameserver"
argument_list|,
literal|"default"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|userProvider
operator|.
name|login
argument_list|(
literal|"hbase.client.keytab.file"
argument_list|,
literal|"hbase.client.kerberos.principal"
argument_list|,
name|host
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error resolving host name: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error while trying to perform the initial login: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
specifier|final
name|UserGroupInformation
name|ugi
init|=
name|userProvider
operator|.
name|getCurrent
argument_list|()
operator|.
name|getUGI
argument_list|()
decl_stmt|;
name|Stoppable
name|stoppable
init|=
operator|new
name|Stoppable
argument_list|()
block|{
specifier|private
specifier|volatile
name|boolean
name|isStopped
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
name|isStopped
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|isStopped
return|;
block|}
block|}
decl_stmt|;
comment|// if you're in debug mode this is useful to avoid getting spammed by the getTGT()
comment|// you can increase this, keeping in mind that the default refresh window is 0.8
comment|// e.g. 5min tgt * 0.8 = 4min refresh so interval is better be way less than 1min
specifier|final
name|int
name|CHECK_TGT_INTERVAL
init|=
literal|30
operator|*
literal|1000
decl_stmt|;
comment|// 30sec
name|ScheduledChore
name|refreshCredentials
init|=
operator|new
name|ScheduledChore
argument_list|(
literal|"RefreshCredentials"
argument_list|,
name|stoppable
argument_list|,
name|CHECK_TGT_INTERVAL
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|chore
parameter_list|()
block|{
try|try
block|{
name|ugi
operator|.
name|checkTGTAndReloginFromKeytab
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Got exception while trying to refresh credentials: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
return|return
name|refreshCredentials
return|;
block|}
block|}
end_class

end_unit

