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
name|client
package|;
end_package

begin_comment
comment|/**  * This is the interface that will expose Connection information to hadoop1/hadoop2  * implementations of the {@link MetricsConnectionSource}.  */
end_comment

begin_interface
specifier|public
interface|interface
name|MetricsConnectionWrapper
block|{
comment|/** Get the connection's unique identifier */
name|String
name|getId
parameter_list|()
function_decl|;
comment|/** Get the User's name. */
name|String
name|getUserName
parameter_list|()
function_decl|;
comment|/** Get the Cluster ID */
name|String
name|getClusterId
parameter_list|()
function_decl|;
comment|/** Get the Zookeeper Quorum Info */
name|String
name|getZookeeperQuorum
parameter_list|()
function_decl|;
comment|/** Get the base ZNode for this cluster. */
name|String
name|getZookeeperBaseNode
parameter_list|()
function_decl|;
name|int
name|getMetaLookupPoolActiveCount
parameter_list|()
function_decl|;
name|int
name|getMetaLookupPoolLargestPoolSize
parameter_list|()
function_decl|;
name|String
name|getBatchPoolId
parameter_list|()
function_decl|;
name|int
name|getBatchPoolActiveCount
parameter_list|()
function_decl|;
name|int
name|getBatchPoolLargestPoolSize
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

