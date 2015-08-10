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

begin_interface
specifier|public
interface|interface
name|MetricsConnectionSource
extends|extends
name|BaseSource
block|{
comment|/**    * The name of the metrics    */
name|String
name|METRICS_NAME
init|=
literal|"Connection"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under.    */
name|String
name|METRICS_CONTEXT
init|=
literal|"connection"
decl_stmt|;
comment|/**    * Description    */
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about HBase Connection"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under in jmx    */
name|String
name|METRICS_JMX_CONTEXT
init|=
literal|"Client,sub="
decl_stmt|;
comment|/**    * Increment number of meta cache hits    */
name|void
name|incrMetaCacheHit
parameter_list|()
function_decl|;
comment|/**    * Increment number of meta cache misses    */
name|void
name|incrMetaCacheMiss
parameter_list|()
function_decl|;
comment|// Strings used for exporting to metrics system.
name|String
name|CONNECTION_ID_NAME
init|=
literal|"connectionId"
decl_stmt|;
name|String
name|CONNECTION_ID_DESC
init|=
literal|"The connection's process-unique identifier."
decl_stmt|;
name|String
name|USER_NAME_NAME
init|=
literal|"userName"
decl_stmt|;
name|String
name|USER_NAME_DESC
init|=
literal|"The user on behalf of whom the Connection is acting."
decl_stmt|;
name|String
name|CLUSTER_ID_NAME
init|=
literal|"clusterId"
decl_stmt|;
name|String
name|CLUSTER_ID_DESC
init|=
literal|"Cluster Id"
decl_stmt|;
name|String
name|ZOOKEEPER_QUORUM_NAME
init|=
literal|"zookeeperQuorum"
decl_stmt|;
name|String
name|ZOOKEEPER_QUORUM_DESC
init|=
literal|"Zookeeper Quorum"
decl_stmt|;
name|String
name|ZOOKEEPER_ZNODE_NAME
init|=
literal|"zookeeperBaseZNode"
decl_stmt|;
name|String
name|ZOOKEEPER_ZNODE_DESC
init|=
literal|"Base ZNode for this cluster."
decl_stmt|;
name|String
name|META_CACHE_HIT_NAME
init|=
literal|"metaCacheHit"
decl_stmt|;
name|String
name|META_CACHE_HIT_DESC
init|=
literal|"A counter on the number of times this connection's meta cache has a valid region location."
decl_stmt|;
name|String
name|META_CACHE_MISS_NAME
init|=
literal|"metaCacheMiss"
decl_stmt|;
name|String
name|META_CACHE_MISS_DESC
init|=
literal|"A counter on the number of times this connection does not know where to find a region."
decl_stmt|;
name|String
name|META_LOOKUP_POOL_ACTIVE_THREAD_NAME
init|=
literal|"metaLookupPoolActiveThreads"
decl_stmt|;
name|String
name|META_LOOKUP_POOL_ACTIVE_THREAD_DESC
init|=
literal|"The approximate number of threads actively resolving region locations from META."
decl_stmt|;
name|String
name|META_LOOKUP_POOL_LARGEST_SIZE_NAME
init|=
literal|"metaLookupPoolLargestSize"
decl_stmt|;
name|String
name|META_LOOKUP_POOL_LARGEST_SIZE_DESC
init|=
literal|"The largest number of threads that have ever simultaneously been in the pool."
decl_stmt|;
name|String
name|BATCH_POOL_ID_NAME
init|=
literal|"batchPoolId"
decl_stmt|;
name|String
name|BATCH_POOL_ID_DESC
init|=
literal|"The connection's batch pool's unique identifier."
decl_stmt|;
name|String
name|BATCH_POOL_ACTIVE_THREAD_NAME
init|=
literal|"batchPoolActiveThreads"
decl_stmt|;
name|String
name|BATCH_POOL_ACTIVE_THREAD_DESC
init|=
literal|"The approximate number of threads executing table operations."
decl_stmt|;
name|String
name|BATCH_POOL_LARGEST_SIZE_NAME
init|=
literal|"batchPoolLargestSize"
decl_stmt|;
name|String
name|BATCH_POOL_LARGEST_SIZE_DESC
init|=
literal|"The largest number of threads that have ever simultaneously been in the pool."
decl_stmt|;
block|}
end_interface

end_unit

