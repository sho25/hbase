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
name|File
import|;
end_import

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
name|fs
operator|.
name|Path
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
name|zookeeper
operator|.
name|MiniZooKeeperCluster
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
name|zookeeper
operator|.
name|ZKWatcher
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
comment|/**  * Helpers for testing HBase that do not depend on specific server/etc. things. The main difference  * from {@link HBaseCommonTestingUtility} is that we can start a zookeeper cluster.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|HBaseZKTestingUtility
extends|extends
name|HBaseCommonTestingUtility
block|{
specifier|private
name|MiniZooKeeperCluster
name|zkCluster
decl_stmt|;
comment|/**    * Set if we were passed a zkCluster. If so, we won't shutdown zk as part of general shutdown.    */
specifier|private
name|boolean
name|passedZkCluster
decl_stmt|;
specifier|protected
name|ZKWatcher
name|zooKeeperWatcher
decl_stmt|;
comment|/** Directory (a subdirectory of dataTestDir) used by the dfs cluster if any */
specifier|protected
name|File
name|clusterTestDir
decl_stmt|;
specifier|public
name|HBaseZKTestingUtility
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
name|HBaseZKTestingUtility
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
comment|/**    * @return Where the cluster will write data on the local subsystem. Creates it if it does not    *         exist already. A subdir of {@code HBaseCommonTestingUtility#getBaseTestDir()}    */
name|Path
name|getClusterTestDir
parameter_list|()
block|{
if|if
condition|(
name|clusterTestDir
operator|==
literal|null
condition|)
block|{
name|setupClusterTestDir
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|Path
argument_list|(
name|clusterTestDir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Creates a directory for the cluster, under the test data    */
specifier|protected
name|void
name|setupClusterTestDir
parameter_list|()
block|{
if|if
condition|(
name|clusterTestDir
operator|!=
literal|null
condition|)
block|{
return|return;
block|}
comment|// Using randomUUID ensures that multiple clusters can be launched by
comment|// a same test, if it stops& starts them
name|Path
name|testDir
init|=
name|getDataTestDir
argument_list|(
literal|"cluster_"
operator|+
name|getRandomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|clusterTestDir
operator|=
operator|new
name|File
argument_list|(
name|testDir
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|getAbsoluteFile
argument_list|()
expr_stmt|;
comment|// Have it cleaned up on exit
name|boolean
name|b
init|=
name|deleteOnExit
argument_list|()
decl_stmt|;
if|if
condition|(
name|b
condition|)
block|{
name|clusterTestDir
operator|.
name|deleteOnExit
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Created new mini-cluster data directory: "
operator|+
name|clusterTestDir
operator|+
literal|", deleteOnExit="
operator|+
name|b
argument_list|)
expr_stmt|;
block|}
comment|/**    * Call this if you only want a zk cluster.    * @see #shutdownMiniZKCluster()    * @return zk cluster started.    */
specifier|public
name|MiniZooKeeperCluster
name|startMiniZKCluster
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|startMiniZKCluster
argument_list|(
literal|1
argument_list|)
return|;
block|}
comment|/**    * Call this if you only want a zk cluster.    * @see #shutdownMiniZKCluster()    * @return zk cluster started.    */
specifier|public
name|MiniZooKeeperCluster
name|startMiniZKCluster
parameter_list|(
name|int
name|zooKeeperServerNum
parameter_list|,
name|int
modifier|...
name|clientPortList
parameter_list|)
throws|throws
name|Exception
block|{
name|setupClusterTestDir
argument_list|()
expr_stmt|;
return|return
name|startMiniZKCluster
argument_list|(
name|clusterTestDir
argument_list|,
name|zooKeeperServerNum
argument_list|,
name|clientPortList
argument_list|)
return|;
block|}
comment|/**    * Start a mini ZK cluster. If the property "test.hbase.zookeeper.property.clientPort" is set the    * port mentioned is used as the default port for ZooKeeper.    */
specifier|private
name|MiniZooKeeperCluster
name|startMiniZKCluster
parameter_list|(
name|File
name|dir
parameter_list|,
name|int
name|zooKeeperServerNum
parameter_list|,
name|int
index|[]
name|clientPortList
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|this
operator|.
name|zkCluster
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Cluster already running at "
operator|+
name|dir
argument_list|)
throw|;
block|}
name|this
operator|.
name|passedZkCluster
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|zkCluster
operator|=
operator|new
name|MiniZooKeeperCluster
argument_list|(
name|this
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|defPort
init|=
name|this
operator|.
name|conf
operator|.
name|getInt
argument_list|(
literal|"test.hbase.zookeeper.property.clientPort"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|defPort
operator|>
literal|0
condition|)
block|{
comment|// If there is a port in the config file, we use it.
name|this
operator|.
name|zkCluster
operator|.
name|setDefaultClientPort
argument_list|(
name|defPort
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|clientPortList
operator|!=
literal|null
condition|)
block|{
comment|// Ignore extra client ports
name|int
name|clientPortListSize
init|=
name|Math
operator|.
name|min
argument_list|(
name|clientPortList
operator|.
name|length
argument_list|,
name|zooKeeperServerNum
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|clientPortListSize
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|zkCluster
operator|.
name|addClientPort
argument_list|(
name|clientPortList
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|int
name|clientPort
init|=
name|this
operator|.
name|zkCluster
operator|.
name|startup
argument_list|(
name|dir
argument_list|,
name|zooKeeperServerNum
argument_list|)
decl_stmt|;
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|clientPort
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|zkCluster
return|;
block|}
specifier|public
name|MiniZooKeeperCluster
name|getZkCluster
parameter_list|()
block|{
return|return
name|zkCluster
return|;
block|}
specifier|public
name|void
name|setZkCluster
parameter_list|(
name|MiniZooKeeperCluster
name|zkCluster
parameter_list|)
block|{
name|this
operator|.
name|passedZkCluster
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|zkCluster
operator|=
name|zkCluster
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|,
name|zkCluster
operator|.
name|getClientPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Shuts down zk cluster created by call to {@link #startMiniZKCluster()} or does nothing.    * @see #startMiniZKCluster()    */
specifier|public
name|void
name|shutdownMiniZKCluster
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|passedZkCluster
operator|&&
name|this
operator|.
name|zkCluster
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|zkCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|this
operator|.
name|zkCluster
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**    * Returns a ZKWatcher instance. This instance is shared between HBaseTestingUtility instance    * users. Don't close it, it will be closed automatically when the cluster shutdowns    * @return The ZKWatcher instance.    */
specifier|public
specifier|synchronized
name|ZKWatcher
name|getZooKeeperWatcher
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|zooKeeperWatcher
operator|==
literal|null
condition|)
block|{
name|zooKeeperWatcher
operator|=
operator|new
name|ZKWatcher
argument_list|(
name|conf
argument_list|,
literal|"testing utility"
argument_list|,
operator|new
name|Abortable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unexpected abort in HBaseZKTestingUtility:"
operator|+
name|why
argument_list|,
name|e
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|zooKeeperWatcher
return|;
block|}
comment|/**    * Gets a ZKWatcher.    */
specifier|public
specifier|static
name|ZKWatcher
name|getZooKeeperWatcher
parameter_list|(
name|HBaseZKTestingUtility
name|testUtil
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ZKWatcher
argument_list|(
name|testUtil
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|"unittest"
argument_list|,
operator|new
name|Abortable
argument_list|()
block|{
name|boolean
name|aborted
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|aborted
operator|=
literal|true
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Fatal ZK error, why="
operator|+
name|why
argument_list|,
name|e
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|aborted
return|;
block|}
block|}
argument_list|)
return|;
block|}
comment|/**    * @return True if we removed the test dirs    */
annotation|@
name|Override
specifier|public
name|boolean
name|cleanupTestDir
parameter_list|()
block|{
name|boolean
name|ret
init|=
name|super
operator|.
name|cleanupTestDir
argument_list|()
decl_stmt|;
if|if
condition|(
name|deleteDir
argument_list|(
name|this
operator|.
name|clusterTestDir
argument_list|)
condition|)
block|{
name|this
operator|.
name|clusterTestDir
operator|=
literal|null
expr_stmt|;
return|return
name|ret
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

