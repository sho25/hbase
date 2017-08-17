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
name|zookeeper
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|HBaseConfiguration
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
name|ZooKeeperConnectionException
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
name|testclassification
operator|.
name|SmallTests
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
name|EnvironmentEdgeManager
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
name|WatchedEvent
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
name|Watcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestZooKeeperWatcher
block|{
specifier|private
specifier|final
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestZooKeeperWatcher
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testIsClientReadable
parameter_list|()
throws|throws
name|ZooKeeperConnectionException
throws|,
name|IOException
block|{
name|ZooKeeperWatcher
name|watcher
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
literal|"testIsClientReadable"
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|baseZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|getZNodeForReplica
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|masterAddressZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|clusterIdZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|tableZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|ZKUtil
operator|.
name|joinZNode
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|tableZNode
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|rsZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|tableLockZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|balancerZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|regionNormalizerZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|clusterStateZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|drainingZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|recoveringRegionsZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|splitLogZNode
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|watcher
operator|.
name|isClientReadable
argument_list|(
name|watcher
operator|.
name|znodePaths
operator|.
name|backupMasterAddressesZNode
argument_list|)
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConnectionEvent
parameter_list|()
throws|throws
name|ZooKeeperConnectionException
throws|,
name|IOException
block|{
name|long
name|zkSessionTimeout
init|=
literal|15000l
decl_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"zookeeper.session.timeout"
argument_list|,
literal|"15000"
argument_list|)
expr_stmt|;
name|Abortable
name|abortable
init|=
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
name|LOG
operator|.
name|error
argument_list|(
name|why
argument_list|,
name|e
argument_list|)
expr_stmt|;
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
decl_stmt|;
name|ZooKeeperWatcher
name|watcher
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
literal|"testConnectionEvent"
argument_list|,
name|abortable
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|WatchedEvent
name|event
init|=
operator|new
name|WatchedEvent
argument_list|(
name|Watcher
operator|.
name|Event
operator|.
name|EventType
operator|.
name|None
argument_list|,
name|Watcher
operator|.
name|Event
operator|.
name|KeeperState
operator|.
name|Disconnected
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|long
name|startTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|abortable
operator|.
name|isAborted
argument_list|()
operator|&&
operator|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|-
name|startTime
operator|<
name|zkSessionTimeout
operator|)
condition|)
block|{
name|watcher
operator|.
name|process
argument_list|(
name|event
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|abortable
operator|.
name|isAborted
argument_list|()
argument_list|)
expr_stmt|;
name|watcher
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

