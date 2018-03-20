begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ipc
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|MetricsHBaseServerSourceFactory
block|{
comment|/**    * The name of the metrics    */
specifier|static
specifier|final
name|String
name|METRICS_NAME
init|=
literal|"IPC"
decl_stmt|;
comment|/**    * Description    */
specifier|static
specifier|final
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about HBase Server IPC"
decl_stmt|;
comment|/**    * The Suffix of the JMX Context that a MetricsHBaseServerSource will register under.    *    * JMX_CONTEXT will be created by createContextName(serverClassName) + METRICS_JMX_CONTEXT_SUFFIX    */
specifier|static
specifier|final
name|String
name|METRICS_JMX_CONTEXT_SUFFIX
init|=
literal|",sub="
operator|+
name|METRICS_NAME
decl_stmt|;
specifier|abstract
name|MetricsHBaseServerSource
name|create
parameter_list|(
name|String
name|serverName
parameter_list|,
name|MetricsHBaseServerWrapper
name|wrapper
parameter_list|)
function_decl|;
comment|/**    * From the name of the class that's starting up create the    * context that an IPC source should register itself.    *    * @param serverName The name of the class that's starting up.    * @return The Camel Cased context name.    */
specifier|protected
specifier|static
name|String
name|createContextName
parameter_list|(
name|String
name|serverName
parameter_list|)
block|{
if|if
condition|(
name|serverName
operator|.
name|startsWith
argument_list|(
literal|"HMaster"
argument_list|)
operator|||
name|serverName
operator|.
name|startsWith
argument_list|(
literal|"master"
argument_list|)
condition|)
block|{
return|return
literal|"Master"
return|;
block|}
elseif|else
if|if
condition|(
name|serverName
operator|.
name|startsWith
argument_list|(
literal|"HRegion"
argument_list|)
operator|||
name|serverName
operator|.
name|startsWith
argument_list|(
literal|"regionserver"
argument_list|)
condition|)
block|{
return|return
literal|"RegionServer"
return|;
block|}
return|return
literal|"IPC"
return|;
block|}
block|}
end_class

end_unit

