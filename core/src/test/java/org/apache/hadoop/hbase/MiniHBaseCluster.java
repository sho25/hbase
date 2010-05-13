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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|HRegion
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
name|util
operator|.
name|JVMClusterUtil
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
name|Threads
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
name|DistributedFileSystem
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
name|io
operator|.
name|MapWritable
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
name|security
operator|.
name|UnixUserGroupInformation
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
name|security
operator|.
name|UserGroupInformation
import|;
end_import

begin_comment
comment|/**  * This class creates a single process HBase cluster.  * each server.  The master uses the 'default' FileSystem.  The RegionServers,  * if we are running on DistributedFilesystem, create a FileSystem instance  * each and will close down their instance on the way out.  */
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
comment|// Cache this.  For some reason only works first time I get it.  TODO: Figure
comment|// out why.
specifier|private
specifier|final
specifier|static
name|UserGroupInformation
name|UGI
decl_stmt|;
static|static
block|{
name|UGI
operator|=
name|UserGroupInformation
operator|.
name|getCurrentUGI
argument_list|()
expr_stmt|;
block|}
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|LocalHBaseCluster
name|hbaseCluster
decl_stmt|;
comment|/**    * Start a MiniHBaseCluster.    * @param conf Configuration to be used for cluster    * @param numRegionServers initial number of region servers to start.    * @throws IOException    */
specifier|public
name|MiniHBaseCluster
parameter_list|(
name|Configuration
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
comment|/**    * Override Master so can add inject behaviors testing.    */
specifier|public
specifier|static
class|class
name|MiniHBaseClusterMaster
extends|extends
name|HMaster
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|HServerInfo
argument_list|,
name|List
argument_list|<
name|HMsg
argument_list|>
argument_list|>
name|messages
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|HServerInfo
argument_list|,
name|List
argument_list|<
name|HMsg
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
specifier|public
name|MiniHBaseClusterMaster
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**      * Add a message to send to a regionserver next time it checks in.      * @param hsi RegionServer's HServerInfo.      * @param msg Message to add.      */
name|void
name|addMessage
parameter_list|(
specifier|final
name|HServerInfo
name|hsi
parameter_list|,
name|HMsg
name|msg
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|messages
init|)
block|{
name|List
argument_list|<
name|HMsg
argument_list|>
name|hmsgs
init|=
name|this
operator|.
name|messages
operator|.
name|get
argument_list|(
name|hsi
argument_list|)
decl_stmt|;
if|if
condition|(
name|hmsgs
operator|==
literal|null
condition|)
block|{
name|hmsgs
operator|=
operator|new
name|ArrayList
argument_list|<
name|HMsg
argument_list|>
argument_list|()
expr_stmt|;
name|this
operator|.
name|messages
operator|.
name|put
argument_list|(
name|hsi
argument_list|,
name|hmsgs
argument_list|)
expr_stmt|;
block|}
name|hmsgs
operator|.
name|add
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|HMsg
index|[]
name|adornRegionServerAnswer
parameter_list|(
specifier|final
name|HServerInfo
name|hsi
parameter_list|,
specifier|final
name|HMsg
index|[]
name|msgs
parameter_list|)
block|{
name|HMsg
index|[]
name|answerMsgs
init|=
name|msgs
decl_stmt|;
synchronized|synchronized
init|(
name|this
operator|.
name|messages
init|)
block|{
name|List
argument_list|<
name|HMsg
argument_list|>
name|hmsgs
init|=
name|this
operator|.
name|messages
operator|.
name|get
argument_list|(
name|hsi
argument_list|)
decl_stmt|;
if|if
condition|(
name|hmsgs
operator|!=
literal|null
operator|&&
operator|!
name|hmsgs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|int
name|size
init|=
name|answerMsgs
operator|.
name|length
decl_stmt|;
name|HMsg
index|[]
name|newAnswerMsgs
init|=
operator|new
name|HMsg
index|[
name|size
operator|+
name|hmsgs
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|answerMsgs
argument_list|,
literal|0
argument_list|,
name|newAnswerMsgs
argument_list|,
literal|0
argument_list|,
name|answerMsgs
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|hmsgs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|newAnswerMsgs
index|[
name|answerMsgs
operator|.
name|length
operator|+
name|i
index|]
operator|=
name|hmsgs
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
name|answerMsgs
operator|=
name|newAnswerMsgs
expr_stmt|;
name|hmsgs
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|super
operator|.
name|adornRegionServerAnswer
argument_list|(
name|hsi
argument_list|,
name|answerMsgs
argument_list|)
return|;
block|}
block|}
comment|/**    * Subclass so can get at protected methods (none at moment).  Also, creates    * a FileSystem instance per instantiation.  Adds a shutdown own FileSystem    * on the way out. Shuts down own Filesystem only, not All filesystems as     * the FileSystem system exit hook does.    */
specifier|public
specifier|static
class|class
name|MiniHBaseClusterRegionServer
extends|extends
name|HRegionServer
block|{
specifier|private
specifier|static
name|int
name|index
init|=
literal|0
decl_stmt|;
specifier|private
name|Thread
name|shutdownThread
init|=
literal|null
decl_stmt|;
specifier|public
name|MiniHBaseClusterRegionServer
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|setDifferentUser
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/*      * @param c      * @param currentfs We return this if we did not make a new one.      * @param uniqueName Same name used to help identify the created fs.      * @return A new fs instance if we are up on DistributeFileSystem.      * @throws IOException      */
specifier|private
specifier|static
name|Configuration
name|setDifferentUser
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|currentfs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|currentfs
operator|instanceof
name|DistributedFileSystem
operator|)
condition|)
return|return
name|c
return|;
comment|// Else distributed filesystem.  Make a new instance per daemon.  Below
comment|// code is taken from the AppendTestUtil over in hdfs.
name|Configuration
name|c2
init|=
operator|new
name|Configuration
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|String
name|username
init|=
name|UGI
operator|.
name|getUserName
argument_list|()
operator|+
literal|".hrs."
operator|+
name|index
operator|++
decl_stmt|;
name|UnixUserGroupInformation
operator|.
name|saveToConf
argument_list|(
name|c2
argument_list|,
name|UnixUserGroupInformation
operator|.
name|UGI_PROPERTY_NAME
argument_list|,
operator|new
name|UnixUserGroupInformation
argument_list|(
name|username
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"supergroup"
block|}
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|c2
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|init
parameter_list|(
name|MapWritable
name|c
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|init
argument_list|(
name|c
argument_list|)
expr_stmt|;
comment|// Run this thread to shutdown our filesystem on way out.
name|this
operator|.
name|shutdownThread
operator|=
operator|new
name|SingleFileSystemShutdownThread
argument_list|(
name|getFileSystem
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|super
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
comment|// Run this on the way out.
if|if
condition|(
name|this
operator|.
name|shutdownThread
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|shutdownThread
operator|.
name|start
argument_list|()
expr_stmt|;
name|Threads
operator|.
name|shutdown
argument_list|(
name|this
operator|.
name|shutdownThread
argument_list|,
literal|30000
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|kill
parameter_list|()
block|{
name|super
operator|.
name|kill
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Alternate shutdown hook.    * Just shuts down the passed fs, not all as default filesystem hook does.    */
specifier|static
class|class
name|SingleFileSystemShutdownThread
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|FileSystem
name|fs
decl_stmt|;
name|SingleFileSystemShutdownThread
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|)
block|{
name|super
argument_list|(
literal|"Shutdown of "
operator|+
name|fs
argument_list|)
expr_stmt|;
name|this
operator|.
name|fs
operator|=
name|fs
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Hook closing fs="
operator|+
name|this
operator|.
name|fs
argument_list|)
expr_stmt|;
name|this
operator|.
name|fs
operator|.
name|close
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
literal|"Running hook"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
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
argument_list|,
name|MiniHBaseCluster
operator|.
name|MiniHBaseClusterMaster
operator|.
name|class
argument_list|,
name|MiniHBaseCluster
operator|.
name|MiniHBaseClusterRegionServer
operator|.
name|class
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
comment|/**    * Starts a region server thread running    *    * @throws IOException    * @return New RegionServerThread    */
specifier|public
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|startRegionServer
parameter_list|()
throws|throws
name|IOException
block|{
name|JVMClusterUtil
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
comment|/**    * Cause a region server to exit doing basic clean up only on its way out.    * @param serverNumber  Used as index into a list.    */
specifier|public
name|String
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Aborting "
operator|+
name|server
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
return|return
name|server
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Shut down the specified region server cleanly    *    * @param serverNumber  Used as index into a list.    * @return the region server that was stopped    */
specifier|public
name|JVMClusterUtil
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
name|JVMClusterUtil
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
name|JVMClusterUtil
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
comment|/**    * Wait for the specified region server to stop. Removes this thread from list    * of running threads.    * @param serverNumber    * @return Name of region server that just went down.    */
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
comment|/**    * Shut down the mini HBase cluster    * @throws IOException    */
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
name|JVMClusterUtil
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
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
name|getRegionServerThreads
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
comment|/**    * @return List of live region server threads (skips the aborted and the killed)    */
specifier|public
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
name|getLiveRegionServerThreads
parameter_list|()
block|{
return|return
name|this
operator|.
name|hbaseCluster
operator|.
name|getLiveRegionServers
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
comment|/**    * @return Index into List of {@link MiniHBaseCluster#getRegionServerThreads()}    * of HRS carrying .META.  Returns -1 if none found.    */
specifier|public
name|int
name|getServerWithMeta
parameter_list|()
block|{
name|int
name|index
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|rst
range|:
name|getRegionServerThreads
argument_list|()
control|)
block|{
name|HRegionServer
name|hrs
init|=
name|rst
operator|.
name|getRegionServer
argument_list|()
decl_stmt|;
name|HRegion
name|metaRegion
init|=
name|hrs
operator|.
name|getOnlineRegion
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|metaRegion
operator|!=
literal|null
condition|)
block|{
name|index
operator|=
name|count
expr_stmt|;
break|break;
block|}
name|count
operator|++
expr_stmt|;
block|}
return|return
name|index
return|;
block|}
comment|/**    * Add a message to include in the responses send a regionserver when it    * checks back in.    * @param serverNumber Which server to send it to.    * @param msg The MESSAGE    * @throws IOException    */
specifier|public
name|void
name|addMessageToSendRegionServer
parameter_list|(
specifier|final
name|int
name|serverNumber
parameter_list|,
specifier|final
name|HMsg
name|msg
parameter_list|)
throws|throws
name|IOException
block|{
name|MiniHBaseClusterRegionServer
name|hrs
init|=
operator|(
name|MiniHBaseClusterRegionServer
operator|)
name|getRegionServer
argument_list|(
name|serverNumber
argument_list|)
decl_stmt|;
name|addMessageToSendRegionServer
argument_list|(
name|hrs
argument_list|,
name|msg
argument_list|)
expr_stmt|;
block|}
comment|/**    * Add a message to include in the responses send a regionserver when it    * checks back in.    * @param hrs Which region server.    * @param msg The MESSAGE    * @throws IOException    */
specifier|public
name|void
name|addMessageToSendRegionServer
parameter_list|(
specifier|final
name|MiniHBaseClusterRegionServer
name|hrs
parameter_list|,
specifier|final
name|HMsg
name|msg
parameter_list|)
throws|throws
name|IOException
block|{
operator|(
operator|(
name|MiniHBaseClusterMaster
operator|)
name|getMaster
argument_list|()
operator|)
operator|.
name|addMessage
argument_list|(
name|hrs
operator|.
name|getHServerInfo
argument_list|()
argument_list|,
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

