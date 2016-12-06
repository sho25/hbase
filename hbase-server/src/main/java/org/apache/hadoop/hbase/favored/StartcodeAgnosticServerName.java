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
operator|.
name|favored
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|net
operator|.
name|HostAndPort
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
name|ServerName
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
name|Addressing
import|;
end_import

begin_comment
comment|/**  * This class differs from ServerName in that start code is always ignored. This is because  * start code, ServerName.NON_STARTCODE is used to persist favored nodes and keeping this separate  * from {@link ServerName} is much cleaner. This should only be used by Favored node specific  * classes and should not be used outside favored nodes.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|StartcodeAgnosticServerName
extends|extends
name|ServerName
block|{
specifier|public
name|StartcodeAgnosticServerName
parameter_list|(
specifier|final
name|String
name|hostname
parameter_list|,
specifier|final
name|int
name|port
parameter_list|,
name|long
name|startcode
parameter_list|)
block|{
name|super
argument_list|(
name|hostname
argument_list|,
name|port
argument_list|,
name|startcode
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|StartcodeAgnosticServerName
name|valueOf
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
operator|new
name|StartcodeAgnosticServerName
argument_list|(
name|serverName
operator|.
name|getHostname
argument_list|()
argument_list|,
name|serverName
operator|.
name|getPort
argument_list|()
argument_list|,
name|serverName
operator|.
name|getStartcode
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|StartcodeAgnosticServerName
name|valueOf
parameter_list|(
specifier|final
name|String
name|hostnameAndPort
parameter_list|,
name|long
name|startcode
parameter_list|)
block|{
return|return
operator|new
name|StartcodeAgnosticServerName
argument_list|(
name|Addressing
operator|.
name|parseHostname
argument_list|(
name|hostnameAndPort
argument_list|)
argument_list|,
name|Addressing
operator|.
name|parsePort
argument_list|(
name|hostnameAndPort
argument_list|)
argument_list|,
name|startcode
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|StartcodeAgnosticServerName
name|valueOf
parameter_list|(
specifier|final
name|HostAndPort
name|hostnameAndPort
parameter_list|,
name|long
name|startcode
parameter_list|)
block|{
return|return
operator|new
name|StartcodeAgnosticServerName
argument_list|(
name|hostnameAndPort
operator|.
name|getHostText
argument_list|()
argument_list|,
name|hostnameAndPort
operator|.
name|getPort
argument_list|()
argument_list|,
name|startcode
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|ServerName
name|other
parameter_list|)
block|{
name|int
name|compare
init|=
name|this
operator|.
name|getHostname
argument_list|()
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|getHostname
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|compare
operator|!=
literal|0
condition|)
return|return
name|compare
return|;
name|compare
operator|=
name|this
operator|.
name|getPort
argument_list|()
operator|-
name|other
operator|.
name|getPort
argument_list|()
expr_stmt|;
if|if
condition|(
name|compare
operator|!=
literal|0
condition|)
return|return
name|compare
return|;
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|getHostAndPort
argument_list|()
operator|.
name|hashCode
argument_list|()
return|;
block|}
block|}
end_class

end_unit

