begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|io
operator|.
name|PrintWriter
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
name|fs
operator|.
name|FileSystem
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
name|client
operator|.
name|HConnectionManager
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
name|client
operator|.
name|HTable
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
name|FSUtils
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
name|hdfs
operator|.
name|MiniDFSCluster
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
comment|/**  * Abstract base class for HBase cluster junit tests.  Spins up an hbase  * cluster in setup and tears it down again in tearDown.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|HBaseClusterTestCase
extends|extends
name|HBaseTestCase
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
name|HBaseClusterTestCase
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|MiniHBaseCluster
name|cluster
decl_stmt|;
specifier|protected
name|MiniDFSCluster
name|dfsCluster
decl_stmt|;
specifier|protected
name|MiniZooKeeperCluster
name|zooKeeperCluster
decl_stmt|;
specifier|protected
name|int
name|regionServers
decl_stmt|;
specifier|protected
name|int
name|numZooKeeperPeers
decl_stmt|;
specifier|protected
name|boolean
name|startDfs
decl_stmt|;
specifier|private
name|boolean
name|openMetaTable
init|=
literal|true
decl_stmt|;
comment|/** default constructor */
specifier|public
name|HBaseClusterTestCase
parameter_list|()
block|{
name|this
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Start a MiniHBaseCluster with regionServers region servers in-process to    * start with. Also, start a MiniDfsCluster before starting the hbase cluster.    * The configuration used will be edited so that this works correctly.    * @param regionServers number of region servers to start.    */
specifier|public
name|HBaseClusterTestCase
parameter_list|(
name|int
name|regionServers
parameter_list|)
block|{
name|this
argument_list|(
name|regionServers
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**  in-process to    * start with. Optionally, startDfs indicates if a MiniDFSCluster should be    * started. If startDfs is false, the assumption is that an external DFS is    * configured in hbase-site.xml and is already started, or you have started a    * MiniDFSCluster on your own and edited the configuration in memory. (You     * can modify the config used by overriding the preHBaseClusterSetup method.)    * @param regionServers number of region servers to start.    * @param startDfs set to true if MiniDFS should be started    */
specifier|public
name|HBaseClusterTestCase
parameter_list|(
name|int
name|regionServers
parameter_list|,
name|boolean
name|startDfs
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|startDfs
operator|=
name|startDfs
expr_stmt|;
name|this
operator|.
name|regionServers
operator|=
name|regionServers
expr_stmt|;
name|this
operator|.
name|numZooKeeperPeers
operator|=
literal|1
expr_stmt|;
block|}
specifier|protected
name|void
name|setOpenMetaTable
parameter_list|(
name|boolean
name|val
parameter_list|)
block|{
name|openMetaTable
operator|=
name|val
expr_stmt|;
block|}
comment|/**    * Run after dfs is ready but before hbase cluster is started up.    */
specifier|protected
name|void
name|preHBaseClusterSetup
parameter_list|()
throws|throws
name|Exception
block|{
comment|// continue
block|}
comment|/**    * Actually start the MiniHBase instance.    */
specifier|protected
name|void
name|hBaseClusterSetup
parameter_list|()
throws|throws
name|Exception
block|{
name|File
name|testDir
init|=
operator|new
name|File
argument_list|(
name|getUnitTestdir
argument_list|(
name|getName
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
comment|// Note that this is done before we create the MiniHBaseCluster because we
comment|// need to edit the config to add the ZooKeeper servers.
name|this
operator|.
name|zooKeeperCluster
operator|=
operator|new
name|MiniZooKeeperCluster
argument_list|()
expr_stmt|;
name|this
operator|.
name|zooKeeperCluster
operator|.
name|startup
argument_list|(
name|numZooKeeperPeers
argument_list|,
name|testDir
argument_list|)
expr_stmt|;
comment|// start the mini cluster
name|this
operator|.
name|cluster
operator|=
operator|new
name|MiniHBaseCluster
argument_list|(
name|conf
argument_list|,
name|regionServers
argument_list|)
expr_stmt|;
if|if
condition|(
name|openMetaTable
condition|)
block|{
comment|// opening the META table ensures that cluster is running
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Run after hbase cluster is started up.    */
specifier|protected
name|void
name|postHBaseClusterSetup
parameter_list|()
throws|throws
name|Exception
block|{
comment|// continue
block|}
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
if|if
condition|(
name|startDfs
condition|)
block|{
comment|// start up the dfs
name|dfsCluster
operator|=
operator|new
name|MiniDFSCluster
argument_list|(
name|conf
argument_list|,
literal|2
argument_list|,
literal|true
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
comment|// mangle the conf so that the fs parameter points to the minidfs we
comment|// just started up
name|FileSystem
name|filesystem
init|=
name|dfsCluster
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"fs.default.name"
argument_list|,
name|filesystem
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|Path
name|parentdir
init|=
name|filesystem
operator|.
name|getHomeDirectory
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|,
name|parentdir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|filesystem
operator|.
name|mkdirs
argument_list|(
name|parentdir
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|setVersion
argument_list|(
name|filesystem
argument_list|,
name|parentdir
argument_list|)
expr_stmt|;
block|}
comment|// do the super setup now. if we had done it first, then we would have
comment|// gotten our conf all mangled and a local fs started up.
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
comment|// run the pre-cluster setup
name|preHBaseClusterSetup
argument_list|()
expr_stmt|;
comment|// start the instance
name|hBaseClusterSetup
argument_list|()
expr_stmt|;
comment|// run post-cluster setup
name|postHBaseClusterSetup
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Exception in setup!"
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|cluster
operator|!=
literal|null
condition|)
block|{
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|zooKeeperCluster
operator|!=
literal|null
condition|)
block|{
name|zooKeeperCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|dfsCluster
operator|!=
literal|null
condition|)
block|{
name|shutdownDfs
argument_list|(
name|dfsCluster
argument_list|)
expr_stmt|;
block|}
throw|throw
name|e
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
operator|!
name|openMetaTable
condition|)
block|{
comment|// open the META table now to ensure cluster is running before shutdown.
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
try|try
block|{
name|HConnectionManager
operator|.
name|deleteConnectionInfo
argument_list|(
name|conf
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|cluster
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|this
operator|.
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Closing mini dfs"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|this
operator|.
name|zooKeeperCluster
operator|.
name|shutdown
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
name|warn
argument_list|(
literal|"Shutting down ZooKeeper cluster"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|startDfs
condition|)
block|{
name|shutdownDfs
argument_list|(
name|dfsCluster
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
comment|// ReflectionUtils.printThreadInfo(new PrintWriter(System.out),
comment|//  "Temporary end-of-test thread dump debugging HADOOP-2040: " + getName());
block|}
comment|/**    * Use this utility method debugging why cluster won't go down.  On a    * period it throws a thread dump.  Method ends when all cluster    * regionservers and master threads are no long alive.    */
specifier|public
name|void
name|threadDumpingJoin
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|cluster
operator|.
name|getRegionThreads
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Thread
name|t
range|:
name|this
operator|.
name|cluster
operator|.
name|getRegionThreads
argument_list|()
control|)
block|{
name|threadDumpingJoin
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
name|threadDumpingJoin
argument_list|(
name|this
operator|.
name|cluster
operator|.
name|getMaster
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|threadDumpingJoin
parameter_list|(
specifier|final
name|Thread
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
name|t
operator|.
name|isAlive
argument_list|()
condition|)
block|{
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Continuing..."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|>
literal|60000
condition|)
block|{
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|ReflectionUtils
operator|.
name|printThreadInfo
argument_list|(
operator|new
name|PrintWriter
argument_list|(
name|System
operator|.
name|out
argument_list|)
argument_list|,
literal|"Automatic Stack Trace every 60 seconds waiting on "
operator|+
name|t
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

