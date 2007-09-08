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
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|dfs
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
name|log4j
operator|.
name|Logger
import|;
end_import

begin_comment
comment|/**  * This class creates a single process HBase cluster for junit testing.  * One thread is created for each server.  *   *<p>TestCases do not need to subclass to start a HBaseCluster.  Call  * {@link #startMaster(Configuration)} and  * {@link #startRegionServers(Configuration, int)} to startup master and  * region servers.  Save off the returned values and pass them to  * {@link #shutdown(org.apache.hadoop.hbase.MiniHBaseCluster.MasterThread, List)}  * to shut it all down when done.  *   */
end_comment

begin_class
specifier|public
class|class
name|MiniHBaseCluster
implements|implements
name|HConstants
block|{
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|Logger
operator|.
name|getLogger
argument_list|(
name|MiniHBaseCluster
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|MiniDFSCluster
name|cluster
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Path
name|parentdir
decl_stmt|;
specifier|private
name|MasterThread
name|masterThread
init|=
literal|null
decl_stmt|;
name|ArrayList
argument_list|<
name|RegionServerThread
argument_list|>
name|regionThreads
decl_stmt|;
specifier|private
name|boolean
name|deleteOnExit
init|=
literal|true
decl_stmt|;
comment|/**    * Starts a MiniHBaseCluster on top of a new MiniDFSCluster    *     * @param conf    * @param nRegionNodes    * @throws IOException     */
specifier|public
name|MiniHBaseCluster
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|nRegionNodes
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|nRegionNodes
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Start a MiniHBaseCluster. Use the native file system unless    * miniHdfsFilesystem is set to true.    *     * @param conf    * @param nRegionNodes    * @param miniHdfsFilesystem    * @throws IOException    */
specifier|public
name|MiniHBaseCluster
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|nRegionNodes
parameter_list|,
specifier|final
name|boolean
name|miniHdfsFilesystem
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|nRegionNodes
argument_list|,
name|miniHdfsFilesystem
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Starts a MiniHBaseCluster on top of an existing HDFSCluster    *     * @param conf    * @param nRegionNodes    * @param dfsCluster    * @throws IOException     */
specifier|public
name|MiniHBaseCluster
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|nRegionNodes
parameter_list|,
name|MiniDFSCluster
name|dfsCluster
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|cluster
operator|=
name|dfsCluster
expr_stmt|;
name|init
argument_list|(
name|nRegionNodes
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor.    * @param conf    * @param nRegionNodes    * @param miniHdfsFilesystem If true, set the hbase mini    * cluster atop a mini hdfs cluster.  Otherwise, use the    * filesystem configured in<code>conf</code>.    * @param format the mini hdfs cluster    * @param deleteOnExit clean up mini hdfs files    * @throws IOException     */
specifier|public
name|MiniHBaseCluster
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|int
name|nRegionNodes
parameter_list|,
specifier|final
name|boolean
name|miniHdfsFilesystem
parameter_list|,
name|boolean
name|format
parameter_list|,
name|boolean
name|deleteOnExit
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|deleteOnExit
operator|=
name|deleteOnExit
expr_stmt|;
if|if
condition|(
name|miniHdfsFilesystem
condition|)
block|{
name|this
operator|.
name|cluster
operator|=
operator|new
name|MiniDFSCluster
argument_list|(
name|this
operator|.
name|conf
argument_list|,
literal|2
argument_list|,
name|format
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
name|init
argument_list|(
name|nRegionNodes
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|init
parameter_list|(
specifier|final
name|int
name|nRegionNodes
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|this
operator|.
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentdir
operator|=
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|HBASE_DIR
argument_list|,
name|DEFAULT_HBASE_DIR
argument_list|)
argument_list|)
expr_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|parentdir
argument_list|)
expr_stmt|;
name|this
operator|.
name|masterThread
operator|=
name|startMaster
argument_list|(
name|this
operator|.
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionThreads
operator|=
name|startRegionServers
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|nRegionNodes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|shutdown
argument_list|()
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/** runs the master server */
specifier|public
specifier|static
class|class
name|MasterThread
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|HMaster
name|master
decl_stmt|;
name|MasterThread
parameter_list|(
specifier|final
name|HMaster
name|m
parameter_list|)
block|{
name|super
argument_list|(
name|m
argument_list|,
literal|"Master:"
operator|+
name|m
operator|.
name|getMasterAddress
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|m
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting "
operator|+
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
comment|/** @return master server */
specifier|public
name|HMaster
name|getMaster
parameter_list|()
block|{
return|return
name|this
operator|.
name|master
return|;
block|}
block|}
comment|/** runs region servers */
specifier|public
specifier|static
class|class
name|RegionServerThread
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|HRegionServer
name|regionServer
decl_stmt|;
name|RegionServerThread
parameter_list|(
specifier|final
name|HRegionServer
name|r
parameter_list|,
specifier|final
name|int
name|index
parameter_list|)
block|{
name|super
argument_list|(
name|r
argument_list|,
literal|"RegionServer:"
operator|+
name|index
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionServer
operator|=
name|r
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting "
operator|+
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
comment|/** @return the region server */
specifier|public
name|HRegionServer
name|getRegionServer
parameter_list|()
block|{
return|return
name|this
operator|.
name|regionServer
return|;
block|}
block|}
comment|/**    * Use this method to start a master.    * If you want to start an hbase cluster    * without subclassing this test case, run this method and    * {@link #startRegionServers(Configuration, int)} to start servers.    * Call {@link #shutdown(org.apache.hadoop.hbase.MiniHBaseCluster.MasterThread, List)}    * to shut them down.    * @param c    * @return Thread running the master.    * @throws IOException    * @see #startRegionServers(Configuration, int)    * @see #shutdown(org.apache.hadoop.hbase.MiniHBaseCluster.MasterThread, List)    */
specifier|public
specifier|static
name|MasterThread
name|startMaster
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|c
operator|.
name|get
argument_list|(
name|MASTER_ADDRESS
argument_list|)
operator|==
literal|null
condition|)
block|{
name|c
operator|.
name|set
argument_list|(
name|MASTER_ADDRESS
argument_list|,
literal|"localhost:0"
argument_list|)
expr_stmt|;
block|}
comment|// Create the master
specifier|final
name|HMaster
name|m
init|=
operator|new
name|HMaster
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|MasterThread
name|masterThread
init|=
operator|new
name|MasterThread
argument_list|(
name|m
argument_list|)
decl_stmt|;
comment|// Start up the master
name|masterThread
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// Set the master's port for the HRegionServers
name|c
operator|.
name|set
argument_list|(
name|MASTER_ADDRESS
argument_list|,
name|m
operator|.
name|getMasterAddress
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|masterThread
return|;
block|}
comment|/**    * @param c    * @param count    * @return List of region server threads started.  Synchronize on the    * returned list when iterating to avoid ConcurrentModificationExceptions.    * @throws IOException    * @see #startMaster(Configuration)    */
specifier|public
specifier|static
name|ArrayList
argument_list|<
name|RegionServerThread
argument_list|>
name|startRegionServers
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|,
specifier|final
name|int
name|count
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Start the HRegionServers.  Always have regionservers come up on
comment|// port '0' so there won't be clashes over default port as unit tests
comment|// start/stop ports at different times during the life of the test.
name|c
operator|.
name|set
argument_list|(
name|REGIONSERVER_ADDRESS
argument_list|,
name|DEFAULT_HOST
operator|+
literal|":0"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting HRegionServers"
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|RegionServerThread
argument_list|>
name|threads
init|=
operator|new
name|ArrayList
argument_list|<
name|RegionServerThread
argument_list|>
argument_list|()
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|threads
operator|.
name|add
argument_list|(
name|startRegionServer
argument_list|(
name|c
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|threads
return|;
block|}
comment|/**    * Starts a region server thread running    *     * @throws IOException    */
specifier|public
name|void
name|startRegionServer
parameter_list|()
throws|throws
name|IOException
block|{
name|RegionServerThread
name|t
init|=
name|startRegionServer
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|this
operator|.
name|regionThreads
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|this
operator|.
name|regionThreads
operator|.
name|add
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|RegionServerThread
name|startRegionServer
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|,
specifier|final
name|int
name|index
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|HRegionServer
name|hsr
init|=
operator|new
name|HRegionServer
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|RegionServerThread
name|t
init|=
operator|new
name|RegionServerThread
argument_list|(
name|hsr
argument_list|,
name|index
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|t
return|;
block|}
comment|/**    * Get the cluster on which this HBase cluster is running    *     * @return MiniDFSCluster    */
specifier|public
name|MiniDFSCluster
name|getDFSCluster
parameter_list|()
block|{
return|return
name|cluster
return|;
block|}
comment|/**     * @return Returns the rpc address actually used by the master server, because    * the supplied port is not necessarily the actual port used.    */
specifier|public
name|HServerAddress
name|getHMasterAddress
parameter_list|()
block|{
return|return
name|this
operator|.
name|masterThread
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterAddress
argument_list|()
return|;
block|}
comment|/**    * Cause a region server to exit without cleaning up    *     * @param serverNumber    */
specifier|public
name|void
name|abortRegionServer
parameter_list|(
name|int
name|serverNumber
parameter_list|)
block|{
name|HRegionServer
name|server
init|=
name|this
operator|.
name|regionThreads
operator|.
name|get
argument_list|(
name|serverNumber
argument_list|)
operator|.
name|getRegionServer
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Aborting "
operator|+
name|server
operator|.
name|serverInfo
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|server
operator|.
name|abort
argument_list|()
expr_stmt|;
block|}
comment|/**    * Shut down the specified region server cleanly    *     * @param serverNumber    * @return the region server that was stopped    */
specifier|public
name|HRegionServer
name|stopRegionServer
parameter_list|(
name|int
name|serverNumber
parameter_list|)
block|{
name|HRegionServer
name|server
init|=
name|this
operator|.
name|regionThreads
operator|.
name|get
argument_list|(
name|serverNumber
argument_list|)
operator|.
name|getRegionServer
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Stopping "
operator|+
name|server
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|server
operator|.
name|stop
argument_list|()
expr_stmt|;
return|return
name|server
return|;
block|}
comment|/**    * Wait for the specified region server to stop    * Removes this thread from list of running threads.    * @param serverNumber    */
specifier|public
name|void
name|waitOnRegionServer
parameter_list|(
name|int
name|serverNumber
parameter_list|)
block|{
name|RegionServerThread
name|regionServerThread
init|=
name|this
operator|.
name|regionThreads
operator|.
name|remove
argument_list|(
name|serverNumber
argument_list|)
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting on "
operator|+
name|regionServerThread
operator|.
name|getRegionServer
argument_list|()
operator|.
name|serverInfo
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|regionServerThread
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Wait for Mini HBase Cluster to shut down.    */
specifier|public
name|void
name|join
parameter_list|()
block|{
if|if
condition|(
name|regionThreads
operator|!=
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|regionThreads
init|)
block|{
for|for
control|(
name|Thread
name|t
range|:
name|regionThreads
control|)
block|{
if|if
condition|(
name|t
operator|.
name|isAlive
argument_list|()
condition|)
block|{
try|try
block|{
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
block|}
block|}
block|}
if|if
condition|(
name|masterThread
operator|!=
literal|null
operator|&&
name|masterThread
operator|.
name|isAlive
argument_list|()
condition|)
block|{
try|try
block|{
name|masterThread
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
block|}
comment|/**    * Shut down HBase cluster started by calling    * {@link #startMaster(Configuration)} and then    * {@link #startRegionServers(Configuration, int)};    * @param masterThread    * @param regionServerThreads    */
specifier|public
specifier|static
name|void
name|shutdown
parameter_list|(
specifier|final
name|MasterThread
name|masterThread
parameter_list|,
specifier|final
name|List
argument_list|<
name|RegionServerThread
argument_list|>
name|regionServerThreads
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Shutting down HBase Cluster"
argument_list|)
expr_stmt|;
comment|/** This is not needed.  Remove.     for(RegionServerThread hsr: regionServerThreads) {       hsr.getRegionServer().stop();     }     */
if|if
condition|(
name|masterThread
operator|!=
literal|null
condition|)
block|{
name|masterThread
operator|.
name|getMaster
argument_list|()
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
synchronized|synchronized
init|(
name|regionServerThreads
init|)
block|{
if|if
condition|(
name|regionServerThreads
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Thread
name|t
range|:
name|regionServerThreads
control|)
block|{
if|if
condition|(
name|t
operator|.
name|isAlive
argument_list|()
condition|)
block|{
try|try
block|{
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
block|}
block|}
block|}
if|if
condition|(
name|masterThread
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|masterThread
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Shutdown "
operator|+
operator|(
operator|(
name|masterThread
operator|!=
literal|null
operator|)
condition|?
name|masterThread
operator|.
name|getName
argument_list|()
else|:
literal|"0 masters"
operator|)
operator|+
literal|" "
operator|+
operator|(
operator|(
name|regionServerThreads
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|regionServerThreads
operator|.
name|size
argument_list|()
operator|)
operator|+
literal|" region server(s)"
argument_list|)
expr_stmt|;
block|}
name|void
name|shutdown
parameter_list|()
block|{
name|MiniHBaseCluster
operator|.
name|shutdown
argument_list|(
name|this
operator|.
name|masterThread
argument_list|,
name|this
operator|.
name|regionThreads
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|cluster
operator|!=
literal|null
condition|)
block|{
name|FileSystem
name|fs
init|=
name|cluster
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Shutting down Mini DFS cluster"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
if|if
condition|(
name|fs
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Shutting down FileSystem"
argument_list|)
expr_stmt|;
name|fs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"shutdown"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// Delete all DFS files
if|if
condition|(
name|deleteOnExit
condition|)
block|{
name|deleteFile
argument_list|(
operator|new
name|File
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
name|StaticTestEnvironment
operator|.
name|TEST_DIRECTORY_KEY
argument_list|)
argument_list|,
literal|"dfs"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|void
name|deleteFile
parameter_list|(
name|File
name|f
parameter_list|)
block|{
if|if
condition|(
name|f
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
name|File
index|[]
name|children
init|=
name|f
operator|.
name|listFiles
argument_list|()
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
name|children
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|deleteFile
argument_list|(
name|children
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|f
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

