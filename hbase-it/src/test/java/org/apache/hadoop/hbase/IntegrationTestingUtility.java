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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * Facility for<strong>integration/system</strong> tests. This extends {@link HBaseTestingUtility}  * and adds-in the functionality needed by integration and system tests. This class understands  * distributed and pseudo-distributed/local cluster deployments, and abstracts those from the tests  * in this module.  *<p>  * IntegrationTestingUtility is constructed and used by the integration tests, but the tests  * themselves should not assume a particular deployment. They can rely on the methods in this  * class and HBaseCluster. Before the testing begins, the test should initialize the cluster by  * calling {@link #initializeCluster(int)}.  *<p>  * The cluster that is used defaults to a mini cluster, but it can be forced to use a distributed  * cluster by calling {@link #setUseDistributedCluster(Configuration)}. This method is invoked by  * test drivers (maven, IntegrationTestsDriver, etc) before initializing the cluster  * via {@link #initializeCluster(int)}. Individual tests should not directly call  * {@link #setUseDistributedCluster(Configuration)}.  */
end_comment

begin_class
specifier|public
class|class
name|IntegrationTestingUtility
extends|extends
name|HBaseTestingUtility
block|{
specifier|public
name|IntegrationTestingUtility
parameter_list|()
block|{
name|this
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|IntegrationTestingUtility
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Configuration that controls whether this utility assumes a running/deployed cluster.    * This is different than "hbase.cluster.distributed" since that parameter indicates whether the    * cluster is in an actual distributed environment, while this shows that there is a    * deployed (distributed or pseudo-distributed) cluster running, and we do not need to    * start a mini-cluster for tests.    */
specifier|public
specifier|static
specifier|final
name|String
name|IS_DISTRIBUTED_CLUSTER
init|=
literal|"hbase.test.cluster.distributed"
decl_stmt|;
comment|/** Config for pluggable hbase cluster manager */
specifier|private
specifier|static
specifier|final
name|String
name|HBASE_CLUSTER_MANAGER_CLASS
init|=
literal|"hbase.it.clustermanager.class"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|ClusterManager
argument_list|>
name|DEFAULT_HBASE_CLUSTER_MANAGER_CLASS
init|=
name|HBaseClusterManager
operator|.
name|class
decl_stmt|;
comment|/**    * Initializes the state of the cluster. It starts a new in-process mini cluster, OR    * if we are given an already deployed distributed cluster it initializes the state.    * @param numSlaves Number of slaves to start up if we are booting a mini cluster. Otherwise    * we check whether this many nodes are available and throw an exception if not.    */
specifier|public
name|void
name|initializeCluster
parameter_list|(
name|int
name|numSlaves
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|isDistributedCluster
argument_list|()
condition|)
block|{
name|createDistributedHBaseCluster
argument_list|()
expr_stmt|;
name|checkNodeCount
argument_list|(
name|numSlaves
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|startMiniCluster
argument_list|(
name|numSlaves
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Checks whether we have more than numSlaves nodes. Throws an    * exception otherwise.    */
specifier|public
name|void
name|checkNodeCount
parameter_list|(
name|int
name|numSlaves
parameter_list|)
throws|throws
name|Exception
block|{
name|HBaseCluster
name|cluster
init|=
name|getHBaseClusterInterface
argument_list|()
decl_stmt|;
if|if
condition|(
name|cluster
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServers
argument_list|()
operator|.
name|size
argument_list|()
operator|<
name|numSlaves
condition|)
block|{
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Cluster does not have enough nodes:"
operator|+
name|numSlaves
argument_list|)
throw|;
block|}
block|}
comment|/**    * Restores the cluster to the initial state if it is a distributed cluster, otherwise, shutdowns the    * mini cluster.    */
specifier|public
name|void
name|restoreCluster
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|isDistributedCluster
argument_list|()
condition|)
block|{
name|getHBaseClusterInterface
argument_list|()
operator|.
name|restoreInitialStatus
argument_list|()
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// re-wrap into IOException
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**    * Sets the configuration property to use a distributed cluster for the integration tests. Test drivers    * should use this to enforce cluster deployment.    */
specifier|public
specifier|static
name|void
name|setUseDistributedCluster
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|conf
operator|.
name|setBoolean
argument_list|(
name|IS_DISTRIBUTED_CLUSTER
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
name|IS_DISTRIBUTED_CLUSTER
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return whether we are interacting with a distributed cluster as opposed to and in-process mini    * cluster or a local cluster.    * @see IntegrationTestingUtility#setUseDistributedCluster(Configuration)    */
specifier|public
name|boolean
name|isDistributedCluster
parameter_list|()
block|{
name|Configuration
name|conf
init|=
name|getConfiguration
argument_list|()
decl_stmt|;
name|boolean
name|isDistributedCluster
init|=
literal|false
decl_stmt|;
name|isDistributedCluster
operator|=
name|Boolean
operator|.
name|parseBoolean
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
name|IS_DISTRIBUTED_CLUSTER
argument_list|,
literal|"false"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isDistributedCluster
condition|)
block|{
name|isDistributedCluster
operator|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|IS_DISTRIBUTED_CLUSTER
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
return|return
name|isDistributedCluster
return|;
block|}
specifier|public
name|void
name|createDistributedHBaseCluster
parameter_list|()
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|getConfiguration
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
extends|extends
name|ClusterManager
argument_list|>
name|clusterManagerClass
init|=
name|conf
operator|.
name|getClass
argument_list|(
name|HBASE_CLUSTER_MANAGER_CLASS
argument_list|,
name|DEFAULT_HBASE_CLUSTER_MANAGER_CLASS
argument_list|,
name|ClusterManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|ClusterManager
name|clusterManager
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|clusterManagerClass
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|setHBaseCluster
argument_list|(
operator|new
name|DistributedHBaseCluster
argument_list|(
name|conf
argument_list|,
name|clusterManager
argument_list|)
argument_list|)
expr_stmt|;
name|getAdmin
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

