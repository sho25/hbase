begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|PrivilegedExceptionAction
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
name|Collections
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|client
operator|.
name|Admin
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
name|Connection
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
name|ConnectionFactory
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
name|security
operator|.
name|User
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
operator|.
name|RegionServerThread
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CopyOnWriteArrayList
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
name|util
operator|.
name|JVMClusterUtil
import|;
end_import

begin_comment
comment|/**  * This class creates a single process HBase cluster. One thread is created for  * a master and one per region server.  *  * Call {@link #startup()} to start the cluster running and {@link #shutdown()}  * to close it all down. {@link #join} the cluster is you want to wait on  * shutdown completion.  *  *<p>Runs master on port 16000 by default.  Because we can't just kill the  * process -- not till HADOOP-1700 gets fixed and even then.... -- we need to  * be able to find the master with a remote client to run shutdown.  To use a  * port other than 16000, set the hbase.master to a value of 'local:PORT':  * that is 'local', not 'localhost', and the port number the master should use  * instead of 16000.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|LocalHBaseCluster
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|LocalHBaseCluster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|MasterThread
argument_list|>
name|masterThreads
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
name|regionThreads
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|DEFAULT_NO
init|=
literal|1
decl_stmt|;
comment|/** local mode */
specifier|public
specifier|static
specifier|final
name|String
name|LOCAL
init|=
literal|"local"
decl_stmt|;
comment|/** 'local:' */
specifier|public
specifier|static
specifier|final
name|String
name|LOCAL_COLON
init|=
name|LOCAL
operator|+
literal|":"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ASSIGN_RANDOM_PORTS
init|=
literal|"hbase.localcluster.assign.random.ports"
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|HMaster
argument_list|>
name|masterClass
decl_stmt|;
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|HRegionServer
argument_list|>
name|regionServerClass
decl_stmt|;
comment|/**    * Constructor.    * @param conf    * @throws IOException    */
specifier|public
name|LocalHBaseCluster
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|DEFAULT_NO
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor.    * @param conf Configuration to use.  Post construction has the master's    * address.    * @param noRegionServers Count of regionservers to start.    * @throws IOException    */
specifier|public
name|LocalHBaseCluster
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|int
name|noRegionServers
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|conf
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
name|noRegionServers
argument_list|,
name|getMasterImplementation
argument_list|(
name|conf
argument_list|)
argument_list|,
name|getRegionServerImplementation
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor.    * @param conf Configuration to use.  Post construction has the active master    * address.    * @param noMasters Count of masters to start.    * @param noRegionServers Count of regionservers to start.    * @throws IOException    */
specifier|public
name|LocalHBaseCluster
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|int
name|noMasters
parameter_list|,
specifier|final
name|int
name|noRegionServers
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|noMasters
argument_list|,
literal|0
argument_list|,
name|noRegionServers
argument_list|,
name|getMasterImplementation
argument_list|(
name|conf
argument_list|)
argument_list|,
name|getRegionServerImplementation
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|HRegionServer
argument_list|>
name|getRegionServerImplementation
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HRegionServer
argument_list|>
operator|)
name|conf
operator|.
name|getClass
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_IMPL
argument_list|,
name|HRegionServer
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|private
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|HMaster
argument_list|>
name|getMasterImplementation
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HMaster
argument_list|>
operator|)
name|conf
operator|.
name|getClass
argument_list|(
name|HConstants
operator|.
name|MASTER_IMPL
argument_list|,
name|HMaster
operator|.
name|class
argument_list|)
return|;
block|}
specifier|public
name|LocalHBaseCluster
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|int
name|noMasters
parameter_list|,
specifier|final
name|int
name|noRegionServers
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|HMaster
argument_list|>
name|masterClass
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|HRegionServer
argument_list|>
name|regionServerClass
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|noMasters
argument_list|,
literal|0
argument_list|,
name|noRegionServers
argument_list|,
name|masterClass
argument_list|,
name|regionServerClass
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor.    * @param conf Configuration to use.  Post construction has the master's    * address.    * @param noMasters Count of masters to start.    * @param noRegionServers Count of regionservers to start.    * @param masterClass    * @param regionServerClass    * @throws IOException    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|LocalHBaseCluster
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|int
name|noMasters
parameter_list|,
specifier|final
name|int
name|noAlwaysStandByMasters
parameter_list|,
specifier|final
name|int
name|noRegionServers
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|HMaster
argument_list|>
name|masterClass
parameter_list|,
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|HRegionServer
argument_list|>
name|regionServerClass
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
comment|// When active, if a port selection is default then we switch to random
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|ASSIGN_RANDOM_PORTS
argument_list|,
literal|false
argument_list|)
condition|)
block|{
if|if
condition|(
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|MASTER_PORT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_MASTER_PORT
argument_list|)
operator|==
name|HConstants
operator|.
name|DEFAULT_MASTER_PORT
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Setting Master Port to random."
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|MASTER_PORT
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|REGIONSERVER_PORT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_REGIONSERVER_PORT
argument_list|)
operator|==
name|HConstants
operator|.
name|DEFAULT_REGIONSERVER_PORT
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Setting RegionServer Port to random."
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|REGIONSERVER_PORT
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
block|}
comment|// treat info ports special; expressly don't change '-1' (keep off)
comment|// in case we make that the default behavior.
if|if
condition|(
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|REGIONSERVER_INFO_PORT
argument_list|,
literal|0
argument_list|)
operator|!=
operator|-
literal|1
operator|&&
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|REGIONSERVER_INFO_PORT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_REGIONSERVER_INFOPORT
argument_list|)
operator|==
name|HConstants
operator|.
name|DEFAULT_REGIONSERVER_INFOPORT
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Setting RS InfoServer Port to random."
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|REGIONSERVER_INFO_PORT
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|MASTER_INFO_PORT
argument_list|,
literal|0
argument_list|)
operator|!=
operator|-
literal|1
operator|&&
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|MASTER_INFO_PORT
argument_list|,
name|HConstants
operator|.
name|DEFAULT_MASTER_INFOPORT
argument_list|)
operator|==
name|HConstants
operator|.
name|DEFAULT_MASTER_INFOPORT
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Setting Master InfoServer Port to random."
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|MASTER_INFO_PORT
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|masterClass
operator|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HMaster
argument_list|>
operator|)
name|conf
operator|.
name|getClass
argument_list|(
name|HConstants
operator|.
name|MASTER_IMPL
argument_list|,
name|masterClass
argument_list|)
expr_stmt|;
comment|// Start the HMasters.
name|int
name|i
decl_stmt|;
for|for
control|(
name|i
operator|=
literal|0
init|;
name|i
operator|<
name|noMasters
condition|;
name|i
operator|++
control|)
block|{
name|addMaster
argument_list|(
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|noAlwaysStandByMasters
condition|;
name|j
operator|++
control|)
block|{
name|Configuration
name|c
init|=
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|c
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|MASTER_IMPL
argument_list|,
literal|"org.apache.hadoop.hbase.master.AlwaysStandByHMaster"
argument_list|)
expr_stmt|;
name|addMaster
argument_list|(
name|c
argument_list|,
name|i
operator|+
name|j
argument_list|)
expr_stmt|;
block|}
comment|// Start the HRegionServers.
name|this
operator|.
name|regionServerClass
operator|=
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HRegionServer
argument_list|>
operator|)
name|conf
operator|.
name|getClass
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_IMPL
argument_list|,
name|regionServerClass
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|noRegionServers
condition|;
name|j
operator|++
control|)
block|{
name|addRegionServer
argument_list|(
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
argument_list|,
name|j
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|addRegionServer
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|addRegionServer
argument_list|(
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
argument_list|,
name|this
operator|.
name|regionThreads
operator|.
name|size
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|addRegionServer
parameter_list|(
name|Configuration
name|config
parameter_list|,
specifier|final
name|int
name|index
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Create each regionserver with its own Configuration instance so each has
comment|// its Connection instance rather than share (see HBASE_INSTANCES down in
comment|// the guts of ConnectionManager).
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|rst
init|=
name|JVMClusterUtil
operator|.
name|createRegionServerThread
argument_list|(
name|config
argument_list|,
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HRegionServer
argument_list|>
operator|)
name|conf
operator|.
name|getClass
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_IMPL
argument_list|,
name|this
operator|.
name|regionServerClass
argument_list|)
argument_list|,
name|index
argument_list|)
decl_stmt|;
name|this
operator|.
name|regionThreads
operator|.
name|add
argument_list|(
name|rst
argument_list|)
expr_stmt|;
return|return
name|rst
return|;
block|}
specifier|public
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|addRegionServer
parameter_list|(
specifier|final
name|Configuration
name|config
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
name|User
name|user
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|user
operator|.
name|runAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|run
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|addRegionServer
argument_list|(
name|config
argument_list|,
name|index
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
specifier|public
name|JVMClusterUtil
operator|.
name|MasterThread
name|addMaster
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|addMaster
argument_list|(
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
argument_list|,
name|this
operator|.
name|masterThreads
operator|.
name|size
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|JVMClusterUtil
operator|.
name|MasterThread
name|addMaster
parameter_list|(
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
comment|// Create each master with its own Configuration instance so each has
comment|// its Connection instance rather than share (see HBASE_INSTANCES down in
comment|// the guts of ConnectionManager.
name|JVMClusterUtil
operator|.
name|MasterThread
name|mt
init|=
name|JVMClusterUtil
operator|.
name|createMasterThread
argument_list|(
name|c
argument_list|,
operator|(
name|Class
argument_list|<
name|?
extends|extends
name|HMaster
argument_list|>
operator|)
name|c
operator|.
name|getClass
argument_list|(
name|HConstants
operator|.
name|MASTER_IMPL
argument_list|,
name|this
operator|.
name|masterClass
argument_list|)
argument_list|,
name|index
argument_list|)
decl_stmt|;
name|this
operator|.
name|masterThreads
operator|.
name|add
argument_list|(
name|mt
argument_list|)
expr_stmt|;
comment|// Refresh the master address config.
name|List
argument_list|<
name|String
argument_list|>
name|masterHostPorts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|getMasters
argument_list|()
operator|.
name|forEach
argument_list|(
name|masterThread
lambda|->
name|masterHostPorts
operator|.
name|add
argument_list|(
name|masterThread
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|.
name|getAddress
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|MASTER_ADDRS_KEY
argument_list|,
name|String
operator|.
name|join
argument_list|(
literal|","
argument_list|,
name|masterHostPorts
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|mt
return|;
block|}
specifier|public
name|JVMClusterUtil
operator|.
name|MasterThread
name|addMaster
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|,
specifier|final
name|int
name|index
parameter_list|,
name|User
name|user
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
return|return
name|user
operator|.
name|runAs
argument_list|(
operator|new
name|PrivilegedExceptionAction
argument_list|<
name|JVMClusterUtil
operator|.
name|MasterThread
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|JVMClusterUtil
operator|.
name|MasterThread
name|run
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|addMaster
argument_list|(
name|c
argument_list|,
name|index
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
comment|/**    * @param serverNumber    * @return region server    */
specifier|public
name|HRegionServer
name|getRegionServer
parameter_list|(
name|int
name|serverNumber
parameter_list|)
block|{
return|return
name|regionThreads
operator|.
name|get
argument_list|(
name|serverNumber
argument_list|)
operator|.
name|getRegionServer
argument_list|()
return|;
block|}
comment|/**    * @return Read-only list of region server threads.    */
specifier|public
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
name|getRegionServers
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|this
operator|.
name|regionThreads
argument_list|)
return|;
block|}
comment|/**    * @return List of running servers (Some servers may have been killed or    * aborted during lifetime of cluster; these servers are not included in this    * list).    */
specifier|public
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
name|getLiveRegionServers
parameter_list|()
block|{
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
name|liveServers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|RegionServerThread
argument_list|>
name|list
init|=
name|getRegionServers
argument_list|()
decl_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|rst
range|:
name|list
control|)
block|{
if|if
condition|(
name|rst
operator|.
name|isAlive
argument_list|()
condition|)
name|liveServers
operator|.
name|add
argument_list|(
name|rst
argument_list|)
expr_stmt|;
else|else
name|LOG
operator|.
name|info
argument_list|(
literal|"Not alive "
operator|+
name|rst
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|liveServers
return|;
block|}
comment|/**    * @return the Configuration used by this LocalHBaseCluster    */
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|this
operator|.
name|conf
return|;
block|}
comment|/**    * Wait for the specified region server to stop. Removes this thread from list of running threads.    * @return Name of region server that just went down.    */
specifier|public
name|String
name|waitOnRegionServer
parameter_list|(
name|int
name|serverNumber
parameter_list|)
block|{
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|regionServerThread
init|=
name|this
operator|.
name|regionThreads
operator|.
name|get
argument_list|(
name|serverNumber
argument_list|)
decl_stmt|;
return|return
name|waitOnRegionServer
argument_list|(
name|regionServerThread
argument_list|)
return|;
block|}
comment|/**    * Wait for the specified region server to stop. Removes this thread from list of running threads.    * @return Name of region server that just went down.    */
specifier|public
name|String
name|waitOnRegionServer
parameter_list|(
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|rst
parameter_list|)
block|{
while|while
condition|(
name|rst
operator|.
name|isAlive
argument_list|()
condition|)
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting on "
operator|+
name|rst
operator|.
name|getRegionServer
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|rst
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Interrupted while waiting for {} to finish. Retrying join"
argument_list|,
name|rst
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
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
name|regionThreads
operator|.
name|remove
argument_list|(
name|rst
argument_list|)
expr_stmt|;
return|return
name|rst
operator|.
name|getName
argument_list|()
return|;
block|}
comment|/**    * @return the HMaster thread    */
specifier|public
name|HMaster
name|getMaster
parameter_list|(
name|int
name|serverNumber
parameter_list|)
block|{
return|return
name|masterThreads
operator|.
name|get
argument_list|(
name|serverNumber
argument_list|)
operator|.
name|getMaster
argument_list|()
return|;
block|}
comment|/**    * Gets the current active master, if available.  If no active master, returns    * null.    * @return the HMaster for the active master    */
specifier|public
name|HMaster
name|getActiveMaster
parameter_list|()
block|{
for|for
control|(
name|JVMClusterUtil
operator|.
name|MasterThread
name|mt
range|:
name|masterThreads
control|)
block|{
comment|// Ensure that the current active master is not stopped.
comment|// We don't want to return a stopping master as an active master.
if|if
condition|(
name|mt
operator|.
name|getMaster
argument_list|()
operator|.
name|isActiveMaster
argument_list|()
operator|&&
operator|!
name|mt
operator|.
name|getMaster
argument_list|()
operator|.
name|isStopped
argument_list|()
condition|)
block|{
return|return
name|mt
operator|.
name|getMaster
argument_list|()
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * @return Read-only list of master threads.    */
specifier|public
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|MasterThread
argument_list|>
name|getMasters
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|this
operator|.
name|masterThreads
argument_list|)
return|;
block|}
comment|/**    * @return List of running master servers (Some servers may have been killed    * or aborted during lifetime of cluster; these servers are not included in    * this list).    */
specifier|public
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|MasterThread
argument_list|>
name|getLiveMasters
parameter_list|()
block|{
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|MasterThread
argument_list|>
name|liveServers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|MasterThread
argument_list|>
name|list
init|=
name|getMasters
argument_list|()
decl_stmt|;
for|for
control|(
name|JVMClusterUtil
operator|.
name|MasterThread
name|mt
range|:
name|list
control|)
block|{
if|if
condition|(
name|mt
operator|.
name|isAlive
argument_list|()
condition|)
block|{
name|liveServers
operator|.
name|add
argument_list|(
name|mt
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|liveServers
return|;
block|}
comment|/**    * Wait for the specified master to stop. Removes this thread from list of running threads.    * @return Name of master that just went down.    */
specifier|public
name|String
name|waitOnMaster
parameter_list|(
name|int
name|serverNumber
parameter_list|)
block|{
name|JVMClusterUtil
operator|.
name|MasterThread
name|masterThread
init|=
name|this
operator|.
name|masterThreads
operator|.
name|get
argument_list|(
name|serverNumber
argument_list|)
decl_stmt|;
return|return
name|waitOnMaster
argument_list|(
name|masterThread
argument_list|)
return|;
block|}
comment|/**    * Wait for the specified master to stop. Removes this thread from list of running threads.    * @return Name of master that just went down.    */
specifier|public
name|String
name|waitOnMaster
parameter_list|(
name|JVMClusterUtil
operator|.
name|MasterThread
name|masterThread
parameter_list|)
block|{
while|while
condition|(
name|masterThread
operator|.
name|isAlive
argument_list|()
condition|)
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting on "
operator|+
name|masterThread
operator|.
name|getMaster
argument_list|()
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
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
name|LOG
operator|.
name|error
argument_list|(
literal|"Interrupted while waiting for {} to finish. Retrying join"
argument_list|,
name|masterThread
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
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
name|masterThreads
operator|.
name|remove
argument_list|(
name|masterThread
argument_list|)
expr_stmt|;
return|return
name|masterThread
operator|.
name|getName
argument_list|()
return|;
block|}
comment|/**    * Wait for Mini HBase Cluster to shut down.    * Presumes you've already called {@link #shutdown()}.    */
specifier|public
name|void
name|join
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|regionThreads
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
name|Threads
operator|.
name|threadDumpingIsAlive
argument_list|(
name|t
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
name|debug
argument_list|(
literal|"Interrupted"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|this
operator|.
name|masterThreads
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
name|masterThreads
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
name|Threads
operator|.
name|threadDumpingIsAlive
argument_list|(
name|t
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
name|debug
argument_list|(
literal|"Interrupted"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|/**    * Start the cluster.    */
specifier|public
name|void
name|startup
parameter_list|()
throws|throws
name|IOException
block|{
name|JVMClusterUtil
operator|.
name|startup
argument_list|(
name|this
operator|.
name|masterThreads
argument_list|,
name|this
operator|.
name|regionThreads
argument_list|)
expr_stmt|;
block|}
comment|/**    * Shut down the mini HBase cluster    */
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|JVMClusterUtil
operator|.
name|shutdown
argument_list|(
name|this
operator|.
name|masterThreads
argument_list|,
name|this
operator|.
name|regionThreads
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param c Configuration to check.    * @return True if a 'local' address in hbase.master value.    */
specifier|public
specifier|static
name|boolean
name|isLocal
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
block|{
name|boolean
name|mode
init|=
name|c
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|CLUSTER_DISTRIBUTED
argument_list|,
name|HConstants
operator|.
name|DEFAULT_CLUSTER_DISTRIBUTED
argument_list|)
decl_stmt|;
return|return
operator|(
name|mode
operator|==
name|HConstants
operator|.
name|CLUSTER_IS_LOCAL
operator|)
return|;
block|}
comment|/**    * Test things basically work.    * @param args    * @throws IOException    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|LocalHBaseCluster
name|cluster
init|=
operator|new
name|LocalHBaseCluster
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|cluster
operator|.
name|startup
argument_list|()
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Admin
name|admin
init|=
name|connection
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
try|try
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|cluster
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

