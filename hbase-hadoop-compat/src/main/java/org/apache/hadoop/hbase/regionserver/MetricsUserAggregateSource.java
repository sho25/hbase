begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|metrics
operator|.
name|BaseSource
import|;
end_import

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

begin_comment
comment|/** * This interface will be implemented by a MetricsSource that will export metrics from * multiple users into the hadoop metrics system. */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsUserAggregateSource
extends|extends
name|BaseSource
block|{
comment|/**    * The name of the metrics    */
specifier|static
specifier|final
name|String
name|METRICS_NAME
init|=
literal|"Users"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under.    */
specifier|static
specifier|final
name|String
name|METRICS_CONTEXT
init|=
literal|"regionserver"
decl_stmt|;
comment|/**    * Description    */
specifier|static
specifier|final
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about users connected to the regionserver"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under in jmx    */
specifier|static
specifier|final
name|String
name|METRICS_JMX_CONTEXT
init|=
literal|"RegionServer,sub="
operator|+
name|METRICS_NAME
decl_stmt|;
specifier|static
specifier|final
name|String
name|NUM_USERS
init|=
literal|"numUsers"
decl_stmt|;
specifier|static
specifier|final
name|String
name|NUMBER_OF_USERS_DESC
init|=
literal|"Number of users in the metrics system"
decl_stmt|;
comment|/**    * Returns a MetricsUserSource if already exists, or creates and registers one for this user    * @param user the user name    * @return a metrics user source    */
name|MetricsUserSource
name|getOrCreateMetricsUser
parameter_list|(
name|String
name|user
parameter_list|)
function_decl|;
name|void
name|deregister
parameter_list|(
name|MetricsUserSource
name|toRemove
parameter_list|)
function_decl|;
name|Map
argument_list|<
name|String
argument_list|,
name|MetricsUserSource
argument_list|>
name|getUserSources
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

