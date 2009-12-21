begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|net
operator|.
name|BindException
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
name|master
operator|.
name|HMaster
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
name|regionserver
operator|.
name|HRegionServer
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
name|regionserver
operator|.
name|HRegion
import|;
end_import

begin_comment
comment|/**  * This class creates a single process HBase cluster. One thread is run for  * each server started.  Pass how many instances of a RegionServer you want  * running in your cluster-in-a-single-jvm.  Its modeled on MiniDFSCluster.  * Uses {@link LocalHBaseCluster}.  Will run on top of whatever the currently  * configured FileSystem.  */
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
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
name|HBaseConfiguration
name|conf
decl_stmt|;
specifier|public
name|LocalHBaseCluster
name|hbaseCluster
decl_stmt|;
comment|/**    * Start a MiniHBaseCluster.     * @param conf HBaseConfiguration to be used for cluster    * @param numRegionServers initial number of region servers to start.    * @throws IOException    */
specifier|public
name|MiniHBaseCluster
parameter_list|(
name|HBaseConfiguration
name|conf
parameter_list|,
name|int
name|numRegionServers
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
name|init
argument_list|(
name|numRegionServers
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
comment|// start up a LocalHBaseCluster
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|hbaseCluster
operator|=
operator|new
name|LocalHBaseCluster
argument_list|(
name|conf
argument_list|,
name|nRegionNodes
argument_list|)
expr_stmt|;
name|hbaseCluster
operator|.
name|startup
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BindException
name|e
parameter_list|)
block|{
comment|//this port is already in use. try to use another (for multiple testing)
name|int
name|port
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|MASTER_PORT
argument_list|,
name|DEFAULT_MASTER_PORT
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed binding Master to port: "
operator|+
name|port
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|port
operator|++
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|MASTER_PORT
argument_list|,
name|port
argument_list|)
expr_stmt|;
continue|continue;
block|}
break|break;
block|}
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
comment|/**    * Starts a region server thread running    *    * @throws IOException    * @return Name of regionserver started.    */
specifier|public
name|String
name|startRegionServer
parameter_list|()
throws|throws
name|IOException
block|{
name|LocalHBaseCluster
operator|.
name|RegionServerThread
name|t
init|=
name|this
operator|.
name|hbaseCluster
operator|.
name|addRegionServer
argument_list|()
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|t
operator|.
name|waitForServerOnline
argument_list|()
expr_stmt|;
return|return
name|t
operator|.
name|getName
argument_list|()
return|;
block|}
comment|/**    * @return Returns the rpc address actually used by the master server, because    * the supplied port is not necessarily the actual port used.    */
specifier|public
name|HServerAddress
name|getHMasterAddress
parameter_list|()
block|{
return|return
name|this
operator|.
name|hbaseCluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterAddress
argument_list|()
return|;
block|}
comment|/**    * @return the HMaster    */
specifier|public
name|HMaster
name|getMaster
parameter_list|()
block|{
return|return
name|this
operator|.
name|hbaseCluster
operator|.
name|getMaster
argument_list|()
return|;
block|}
comment|/**    * Cause a region server to exit without cleaning up    *    * @param serverNumber  Used as index into a list.    */
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
name|getRegionServer
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
literal|"Aborting "
operator|+
name|server
operator|.
name|getHServerInfo
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|server
operator|.
name|abort
argument_list|()
expr_stmt|;
block|}
comment|/**    * Shut down the specified region server cleanly    *    * @param serverNumber  Used as index into a list.    * @return the region server that was stopped    */
specifier|public
name|LocalHBaseCluster
operator|.
name|RegionServerThread
name|stopRegionServer
parameter_list|(
name|int
name|serverNumber
parameter_list|)
block|{
return|return
name|stopRegionServer
argument_list|(
name|serverNumber
argument_list|,
literal|true
argument_list|)
return|;
block|}
comment|/**    * Shut down the specified region server cleanly    *    * @param serverNumber  Used as index into a list.    * @param shutdownFS True is we are to shutdown the filesystem as part of this    * regionserver's shutdown.  Usually we do but you do not want to do this if    * you are running multiple regionservers in a test and you shut down one    * before end of the test.    * @return the region server that was stopped    */
specifier|public
name|LocalHBaseCluster
operator|.
name|RegionServerThread
name|stopRegionServer
parameter_list|(
name|int
name|serverNumber
parameter_list|,
specifier|final
name|boolean
name|shutdownFS
parameter_list|)
block|{
name|LocalHBaseCluster
operator|.
name|RegionServerThread
name|server
init|=
name|hbaseCluster
operator|.
name|getRegionServers
argument_list|()
operator|.
name|get
argument_list|(
name|serverNumber
argument_list|)
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
if|if
condition|(
operator|!
name|shutdownFS
condition|)
block|{
comment|// Stop the running of the hdfs shutdown thread in tests.
name|server
operator|.
name|getRegionServer
argument_list|()
operator|.
name|setShutdownHDFS
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|server
operator|.
name|getRegionServer
argument_list|()
operator|.
name|stop
argument_list|()
expr_stmt|;
return|return
name|server
return|;
block|}
comment|/**    * Wait for the specified region server to stop    * Removes this thread from list of running threads.    * @param serverNumber    * @return Name of region server that just went down.    */
specifier|public
name|String
name|waitOnRegionServer
parameter_list|(
specifier|final
name|int
name|serverNumber
parameter_list|)
block|{
return|return
name|this
operator|.
name|hbaseCluster
operator|.
name|waitOnRegionServer
argument_list|(
name|serverNumber
argument_list|)
return|;
block|}
comment|/**    * Wait for Mini HBase Cluster to shut down.    */
specifier|public
name|void
name|join
parameter_list|()
block|{
name|this
operator|.
name|hbaseCluster
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
comment|/**    * Shut down the mini HBase cluster    * @throws IOException     */
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|hbaseCluster
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|hbaseCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|HConnectionManager
operator|.
name|deleteAllConnections
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Call flushCache on all regions on all participating regionservers.    * @throws IOException    */
specifier|public
name|void
name|flushcache
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|LocalHBaseCluster
operator|.
name|RegionServerThread
name|t
range|:
name|this
operator|.
name|hbaseCluster
operator|.
name|getRegionServers
argument_list|()
control|)
block|{
for|for
control|(
name|HRegion
name|r
range|:
name|t
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getOnlineRegions
argument_list|()
control|)
block|{
name|r
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @return List of region server threads.    */
specifier|public
name|List
argument_list|<
name|LocalHBaseCluster
operator|.
name|RegionServerThread
argument_list|>
name|getRegionThreads
parameter_list|()
block|{
return|return
name|this
operator|.
name|hbaseCluster
operator|.
name|getRegionServers
argument_list|()
return|;
block|}
comment|/**    * Grab a numbered region server of your choice.    * @param serverNumber    * @return region server    */
specifier|public
name|HRegionServer
name|getRegionServer
parameter_list|(
name|int
name|serverNumber
parameter_list|)
block|{
return|return
name|hbaseCluster
operator|.
name|getRegionServer
argument_list|(
name|serverNumber
argument_list|)
return|;
block|}
block|}
end_class

end_unit

