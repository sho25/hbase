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
name|Closeable
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
name|Configurable
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
name|RegionInfoBuilder
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

begin_comment
comment|/**  * This class defines methods that can help with managing HBase clusters  * from unit tests and system tests. There are 3 types of cluster deployments:  *<ul>  *<li><b>MiniHBaseCluster:</b> each server is run in the same JVM in separate threads,  * used by unit tests</li>  *<li><b>DistributedHBaseCluster:</b> the cluster is pre-deployed, system and integration tests can  * interact with the cluster.</li>  *<li><b>ProcessBasedLocalHBaseCluster:</b> each server is deployed locally but in separate  * JVMs.</li>  *</ul>  *<p>  * HBaseCluster unifies the way tests interact with the cluster, so that the same test can  * be run against a mini-cluster during unit test execution, or a distributed cluster having  * tens/hundreds of nodes during execution of integration tests.  *  *<p>  * HBaseCluster exposes client-side public interfaces to tests, so that tests does not assume  * running in a particular mode. Not all the tests are suitable to be run on an actual cluster,  * and some tests will still need to mock stuff and introspect internal state. For those use  * cases from unit tests, or if more control is needed, you can use the subclasses directly.  * In that sense, this class does not abstract away<strong>every</strong> interface that  * MiniHBaseCluster or DistributedHBaseCluster provide.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|HBaseCluster
implements|implements
name|Closeable
implements|,
name|Configurable
block|{
comment|// Log is being used in DistributedHBaseCluster class, hence keeping it as package scope
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|HBaseCluster
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
comment|/** the status of the cluster before we begin */
specifier|protected
name|ClusterMetrics
name|initialClusterStatus
decl_stmt|;
comment|/**    * Construct an HBaseCluster    * @param conf Configuration to be used for cluster    */
specifier|public
name|HBaseCluster
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
comment|/**    * Returns a ClusterMetrics for this HBase cluster.    * @see #getInitialClusterMetrics()    */
specifier|public
specifier|abstract
name|ClusterMetrics
name|getClusterMetrics
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Returns a ClusterStatus for this HBase cluster as observed at the    * starting of the HBaseCluster    */
specifier|public
name|ClusterMetrics
name|getInitialClusterMetrics
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|initialClusterStatus
return|;
block|}
comment|/**    * Starts a new region server on the given hostname or if this is a mini/local cluster,    * starts a region server locally.    * @param hostname the hostname to start the regionserver on    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|startRegionServer
parameter_list|(
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Kills the region server process if this is a distributed cluster, otherwise    * this causes the region server to exit doing basic clean up only.    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|killRegionServer
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Keeping track of killed servers and being able to check if a particular server was killed makes    * it possible to do fault tolerance testing for dead servers in a deterministic way. A concrete    * example of such case is - killing servers and waiting for all regions of a particular table    * to be assigned. We can check for server column in META table and that its value is not one    * of the killed servers.    */
specifier|public
specifier|abstract
name|boolean
name|isKilledRS
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
function_decl|;
comment|/**    * Stops the given region server, by attempting a gradual stop.    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|stopRegionServer
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Wait for the specified region server to join the cluster    * @throws IOException if something goes wrong or timeout occurs    */
specifier|public
name|void
name|waitForRegionServerToStart
parameter_list|(
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
operator|<
name|timeout
condition|)
block|{
for|for
control|(
name|ServerName
name|server
range|:
name|getClusterMetrics
argument_list|()
operator|.
name|getLiveServerMetrics
argument_list|()
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|server
operator|.
name|getHostname
argument_list|()
operator|.
name|equals
argument_list|(
name|hostname
argument_list|)
operator|&&
name|server
operator|.
name|getPort
argument_list|()
operator|==
name|port
condition|)
block|{
return|return;
block|}
block|}
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"did timeout "
operator|+
name|timeout
operator|+
literal|"ms waiting for region server to start: "
operator|+
name|hostname
argument_list|)
throw|;
block|}
comment|/**    * Wait for the specified region server to stop the thread / process.    * @throws IOException if something goes wrong or timeout occurs    */
specifier|public
specifier|abstract
name|void
name|waitForRegionServerToStop
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Suspend the region server    * @param serverName the hostname to suspend the regionserver on    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|suspendRegionServer
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Resume the region server    * @param serverName the hostname to resume the regionserver on    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|resumeRegionServer
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Starts a new zookeeper node on the given hostname or if this is a mini/local cluster,    * silently logs warning message.    * @param hostname the hostname to start the regionserver on    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|startZkNode
parameter_list|(
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Kills the zookeeper node process if this is a distributed cluster, otherwise,    * this causes master to exit doing basic clean up only.    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|killZkNode
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Stops the region zookeeper if this is a distributed cluster, otherwise    * silently logs warning message.    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|stopZkNode
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Wait for the specified zookeeper node to join the cluster    * @throws IOException if something goes wrong or timeout occurs    */
specifier|public
specifier|abstract
name|void
name|waitForZkNodeToStart
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Wait for the specified zookeeper node to stop the thread / process.    * @throws IOException if something goes wrong or timeout occurs    */
specifier|public
specifier|abstract
name|void
name|waitForZkNodeToStop
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Starts a new datanode on the given hostname or if this is a mini/local cluster,    * silently logs warning message.    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|startDataNode
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Kills the datanode process if this is a distributed cluster, otherwise,    * this causes master to exit doing basic clean up only.    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|killDataNode
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Stops the datanode if this is a distributed cluster, otherwise    * silently logs warning message.    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|stopDataNode
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Wait for the specified datanode to join the cluster    * @throws IOException if something goes wrong or timeout occurs    */
specifier|public
specifier|abstract
name|void
name|waitForDataNodeToStart
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Wait for the specified datanode to stop the thread / process.    * @throws IOException if something goes wrong or timeout occurs    */
specifier|public
specifier|abstract
name|void
name|waitForDataNodeToStop
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Starts a new namenode on the given hostname or if this is a mini/local cluster, silently logs    * warning message.    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|startNameNode
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Kills the namenode process if this is a distributed cluster, otherwise, this causes master to    * exit doing basic clean up only.    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|killNameNode
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Stops the namenode if this is a distributed cluster, otherwise silently logs warning message.    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|stopNameNode
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Wait for the specified namenode to join the cluster    * @throws IOException if something goes wrong or timeout occurs    */
specifier|public
specifier|abstract
name|void
name|waitForNameNodeToStart
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Wait for the specified namenode to stop    * @throws IOException if something goes wrong or timeout occurs    */
specifier|public
specifier|abstract
name|void
name|waitForNameNodeToStop
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Starts a new master on the given hostname or if this is a mini/local cluster,    * starts a master locally.    * @param hostname the hostname to start the master on    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|startMaster
parameter_list|(
name|String
name|hostname
parameter_list|,
name|int
name|port
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Kills the master process if this is a distributed cluster, otherwise,    * this causes master to exit doing basic clean up only.    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|killMaster
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Stops the given master, by attempting a gradual stop.    * @throws IOException if something goes wrong    */
specifier|public
specifier|abstract
name|void
name|stopMaster
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Wait for the specified master to stop the thread / process.    * @throws IOException if something goes wrong or timeout occurs    */
specifier|public
specifier|abstract
name|void
name|waitForMasterToStop
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Blocks until there is an active master and that master has completed    * initialization.    *    * @return true if an active master becomes available.  false if there are no    *         masters left.    * @throws IOException if something goes wrong or timeout occurs    */
specifier|public
name|boolean
name|waitForActiveAndReadyMaster
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|waitForActiveAndReadyMaster
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
comment|/**    * Blocks until there is an active master and that master has completed    * initialization.    * @param timeout the timeout limit in ms    * @return true if an active master becomes available.  false if there are no    *         masters left.    */
specifier|public
specifier|abstract
name|boolean
name|waitForActiveAndReadyMaster
parameter_list|(
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Wait for HBase Cluster to shut down.    */
specifier|public
specifier|abstract
name|void
name|waitUntilShutDown
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Shut down the HBase cluster    */
specifier|public
specifier|abstract
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Restores the cluster to it's initial state if this is a real cluster,    * otherwise does nothing.    * This is a best effort restore. If the servers are not reachable, or insufficient    * permissions, etc. restoration might be partial.    * @return whether restoration is complete    */
specifier|public
name|boolean
name|restoreInitialStatus
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|restoreClusterMetrics
argument_list|(
name|getInitialClusterMetrics
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Restores the cluster to given state if this is a real cluster,    * otherwise does nothing.    * This is a best effort restore. If the servers are not reachable, or insufficient    * permissions, etc. restoration might be partial.    * @return whether restoration is complete    */
specifier|public
name|boolean
name|restoreClusterMetrics
parameter_list|(
name|ClusterMetrics
name|desiredStatus
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|true
return|;
block|}
comment|/**    * Get the ServerName of region server serving the first hbase:meta region    */
specifier|public
name|ServerName
name|getServerHoldingMeta
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|getServerHoldingRegion
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|,
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getRegionName
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Get the ServerName of region server serving the specified region    * @param regionName Name of the region in bytes    * @param tn Table name that has the region.    * @return ServerName that hosts the region or null    */
specifier|public
specifier|abstract
name|ServerName
name|getServerHoldingRegion
parameter_list|(
specifier|final
name|TableName
name|tn
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return whether we are interacting with a distributed cluster as opposed to an    * in-process mini/local cluster.    */
specifier|public
name|boolean
name|isDistributedCluster
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
comment|/**    * Closes all the resources held open for this cluster. Note that this call does not shutdown    * the cluster.    * @see #shutdown()    */
annotation|@
name|Override
specifier|public
specifier|abstract
name|void
name|close
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Wait for the namenode.    *    * @throws InterruptedException    */
specifier|public
name|void
name|waitForNamenodeAvailable
parameter_list|()
throws|throws
name|InterruptedException
block|{   }
specifier|public
name|void
name|waitForDatanodesRegistered
parameter_list|(
name|int
name|nbDN
parameter_list|)
throws|throws
name|Exception
block|{   }
block|}
end_class

end_unit

