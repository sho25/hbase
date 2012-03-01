begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|zookeeper
package|;
end_package

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
name|Abortable
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
name|ClusterStatus
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
name|Bytes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Tracker on cluster settings up in zookeeper.  * This is not related to {@link ClusterStatus}.  That class is a data structure  * that holds snapshot of current view on cluster.  This class is about tracking  * cluster attributes up in zookeeper.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ClusterStatusTracker
extends|extends
name|ZooKeeperNodeTracker
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
name|ClusterStatusTracker
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Creates a cluster status tracker.    *    *<p>After construction, use {@link #start} to kick off tracking.    *    * @param watcher    * @param abortable    */
specifier|public
name|ClusterStatusTracker
parameter_list|(
name|ZooKeeperWatcher
name|watcher
parameter_list|,
name|Abortable
name|abortable
parameter_list|)
block|{
name|super
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|clusterStateZNode
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
block|}
comment|/**    * Checks if cluster is up.    * @return true if root region location is available, false if not    */
specifier|public
name|boolean
name|isClusterUp
parameter_list|()
block|{
return|return
name|super
operator|.
name|getData
argument_list|(
literal|false
argument_list|)
operator|!=
literal|null
return|;
block|}
comment|/**    * Sets the cluster as up.    * @throws KeeperException unexpected zk exception    */
specifier|public
name|void
name|setClusterUp
parameter_list|()
throws|throws
name|KeeperException
block|{
name|byte
index|[]
name|upData
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
operator|new
name|java
operator|.
name|util
operator|.
name|Date
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|createAndWatch
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|clusterStateZNode
argument_list|,
name|upData
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NodeExistsException
name|nee
parameter_list|)
block|{
name|ZKUtil
operator|.
name|setData
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|clusterStateZNode
argument_list|,
name|upData
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Sets the cluster as down by deleting the znode.    * @throws KeeperException unexpected zk exception    */
specifier|public
name|void
name|setClusterDown
parameter_list|()
throws|throws
name|KeeperException
block|{
try|try
block|{
name|ZKUtil
operator|.
name|deleteNode
argument_list|(
name|watcher
argument_list|,
name|watcher
operator|.
name|clusterStateZNode
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
operator|.
name|NoNodeException
name|nne
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Attempted to set cluster as down but already down, cluster "
operator|+
literal|"state node ("
operator|+
name|watcher
operator|.
name|clusterStateZNode
operator|+
literal|") not found"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

