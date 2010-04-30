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
name|IOException
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
name|client
operator|.
name|HBaseAdmin
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
name|Bytes
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
comment|/**  * This class creates a single process HBase cluster. One thread is created for  * a master and one per region server.  *   * Call {@link #startup()} to start the cluster running and {@link #shutdown()}  * to close it all down. {@link #join} the cluster is you want to wait on  * shutdown completion.  *   *<p>Runs master on port 60000 by default.  Because we can't just kill the  * process -- not till HADOOP-1700 gets fixed and even then.... -- we need to  * be able to find the master with a remote client to run shutdown.  To use a  * port other than 60000, set the hbase.master to a value of 'local:PORT':  * that is 'local', not 'localhost', and the port number the master should use  * instead of 60000.  *   *<p>To make 'local' mode more responsive, make values such as  *<code>hbase.regionserver.msginterval</code>,  *<code>hbase.master.meta.thread.rescanfrequency</code>, and  *<code>hbase.server.thread.wakefrequency</code> a second or less.  */
end_comment

begin_class
specifier|public
class|class
name|LocalHBaseCluster
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
name|LocalHBaseCluster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HMaster
name|master
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
name|noRegionServers
argument_list|,
name|HMaster
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor.    * @param conf Configuration to use.  Post construction has the master's    * address.    * @param noRegionServers Count of regionservers to start.    * @param masterClass    * @throws IOException    */
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
name|noRegionServers
parameter_list|,
specifier|final
name|Class
name|masterClass
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
comment|// Create the master
name|this
operator|.
name|master
operator|=
name|HMaster
operator|.
name|constructMaster
argument_list|(
name|masterClass
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// Start the HRegionServers.  Always have region servers come up on
comment|// port '0' so there won't be clashes over default port as unit tests
comment|// start/stop ports at different times during the life of the test.
name|conf
operator|.
name|set
argument_list|(
name|REGIONSERVER_PORT
argument_list|,
literal|"0"
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionThreads
operator|=
operator|new
name|CopyOnWriteArrayList
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
argument_list|()
expr_stmt|;
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
name|HRegionServer
operator|.
name|class
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
name|noRegionServers
condition|;
name|i
operator|++
control|)
block|{
name|addRegionServer
argument_list|(
name|i
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
name|this
operator|.
name|regionThreads
operator|.
name|size
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|addRegionServer
parameter_list|(
specifier|final
name|int
name|index
parameter_list|)
throws|throws
name|IOException
block|{
name|JVMClusterUtil
operator|.
name|RegionServerThread
name|rst
init|=
name|JVMClusterUtil
operator|.
name|createRegionServerThread
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|this
operator|.
name|regionServerClass
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
comment|/**    * @return the HMaster thread    */
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
comment|/**    * Wait for the specified region server to stop    * Removes this thread from list of running threads.    * @param serverNumber    * @return Name of region server that just went down.    */
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
name|remove
argument_list|(
name|serverNumber
argument_list|)
decl_stmt|;
while|while
condition|(
name|regionServerThread
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
name|regionServerThread
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getHServerInfo
argument_list|()
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
block|}
return|return
name|regionServerThread
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
if|if
condition|(
name|this
operator|.
name|master
operator|!=
literal|null
operator|&&
name|this
operator|.
name|master
operator|.
name|isAlive
argument_list|()
condition|)
block|{
try|try
block|{
name|this
operator|.
name|master
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
comment|/**    * Start the cluster.    */
specifier|public
name|void
name|startup
parameter_list|()
block|{
name|JVMClusterUtil
operator|.
name|startup
argument_list|(
name|this
operator|.
name|master
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
name|master
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
name|String
name|mode
init|=
name|c
operator|.
name|get
argument_list|(
name|CLUSTER_DISTRIBUTED
argument_list|)
decl_stmt|;
return|return
name|mode
operator|==
literal|null
operator|||
name|mode
operator|.
name|equals
argument_list|(
name|CLUSTER_IS_LOCAL
argument_list|)
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
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
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
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

