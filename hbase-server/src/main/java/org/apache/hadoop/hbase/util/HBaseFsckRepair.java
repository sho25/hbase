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
operator|.
name|util
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
name|classification
operator|.
name|InterfaceStability
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
name|HConstants
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|ServerName
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
name|catalog
operator|.
name|MetaEditor
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
name|AdminProtocol
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
name|client
operator|.
name|HConnection
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
name|master
operator|.
name|RegionState
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|wal
operator|.
name|HLog
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
comment|/**  * This class contains helper methods that repair parts of hbase's filesystem  * contents.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|HBaseFsckRepair
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|HBaseFsckRepair
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Fix multiple assignment by doing silent closes on each RS hosting the region    * and then force ZK unassigned node to OFFLINE to trigger assignment by    * master.    *    * @param admin HBase admin used to undeploy    * @param region Region to undeploy    * @param servers list of Servers to undeploy from    */
specifier|public
specifier|static
name|void
name|fixMultiAssignment
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|HRegionInfo
name|region
parameter_list|,
name|List
argument_list|<
name|ServerName
argument_list|>
name|servers
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
throws|,
name|InterruptedException
block|{
name|HRegionInfo
name|actualRegion
init|=
operator|new
name|HRegionInfo
argument_list|(
name|region
argument_list|)
decl_stmt|;
comment|// Close region on the servers silently
for|for
control|(
name|ServerName
name|server
range|:
name|servers
control|)
block|{
name|closeRegionSilentlyAndWait
argument_list|(
name|admin
argument_list|,
name|server
argument_list|,
name|actualRegion
argument_list|)
expr_stmt|;
block|}
comment|// Force ZK node to OFFLINE so master assigns
name|forceOfflineInZK
argument_list|(
name|admin
argument_list|,
name|actualRegion
argument_list|)
expr_stmt|;
block|}
comment|/**    * Fix unassigned by creating/transition the unassigned ZK node for this    * region to OFFLINE state with a special flag to tell the master that this is    * a forced operation by HBCK.    *    * This assumes that info is in META.    *    * @param conf    * @param region    * @throws IOException    * @throws KeeperException    */
specifier|public
specifier|static
name|void
name|fixUnassigned
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
name|HRegionInfo
name|actualRegion
init|=
operator|new
name|HRegionInfo
argument_list|(
name|region
argument_list|)
decl_stmt|;
comment|// Force ZK node to OFFLINE so master assigns
name|forceOfflineInZK
argument_list|(
name|admin
argument_list|,
name|actualRegion
argument_list|)
expr_stmt|;
block|}
comment|/**    * In 0.90, this forces an HRI offline by setting the RegionTransitionData    * in ZK to have HBCK_CODE_NAME as the server.  This is a special case in    * the AssignmentManager that attempts an assign call by the master.    *    * @see org.apache.hadoop.hbase.master.AssignementManager#handleHBCK    *    * This doesn't seem to work properly in the updated version of 0.92+'s hbck    * so we use assign to force the region into transition.  This has the    * side-effect of requiring a HRegionInfo that considers regionId (timestamp)    * in comparators that is addressed by HBASE-5563.    */
specifier|private
specifier|static
name|void
name|forceOfflineInZK
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
specifier|final
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|ZooKeeperConnectionException
throws|,
name|KeeperException
throws|,
name|IOException
block|{
name|admin
operator|.
name|assign
argument_list|(
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/*    * Should we check all assignments or just not in RIT?    */
specifier|public
specifier|static
name|void
name|waitUntilAssigned
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|long
name|timeout
init|=
name|admin
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
literal|"hbase.hbck.assign.timeout"
argument_list|,
literal|120000
argument_list|)
decl_stmt|;
name|long
name|expiration
init|=
name|timeout
operator|+
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|expiration
condition|)
block|{
try|try
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|RegionState
argument_list|>
name|rits
init|=
name|admin
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getRegionsInTransition
argument_list|()
decl_stmt|;
if|if
condition|(
name|rits
operator|.
name|keySet
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|rits
operator|.
name|keySet
argument_list|()
operator|.
name|contains
argument_list|(
name|region
operator|.
name|getEncodedName
argument_list|()
argument_list|)
condition|)
block|{
comment|// yay! no longer RIT
return|return;
block|}
comment|// still in rit
name|LOG
operator|.
name|info
argument_list|(
literal|"Region still in transition, waiting for "
operator|+
literal|"it to become assigned: "
operator|+
name|region
argument_list|)
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
literal|"Exception when waiting for region to become assigned,"
operator|+
literal|" retrying"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Region "
operator|+
name|region
operator|+
literal|" failed to move out of "
operator|+
literal|"transition within timeout "
operator|+
name|timeout
operator|+
literal|"ms"
argument_list|)
throw|;
block|}
comment|/**    * Contacts a region server and waits up to hbase.hbck.close.timeout ms    * (default 120s) to close the region.  This bypasses the active hmaster.    */
specifier|public
specifier|static
name|void
name|closeRegionSilentlyAndWait
parameter_list|(
name|HBaseAdmin
name|admin
parameter_list|,
name|ServerName
name|server
parameter_list|,
name|HRegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|HConnection
name|connection
init|=
name|admin
operator|.
name|getConnection
argument_list|()
decl_stmt|;
name|AdminProtocol
name|rs
init|=
name|connection
operator|.
name|getAdmin
argument_list|(
name|server
operator|.
name|getHostname
argument_list|()
argument_list|,
name|server
operator|.
name|getPort
argument_list|()
argument_list|)
decl_stmt|;
name|ProtobufUtil
operator|.
name|closeRegion
argument_list|(
name|rs
argument_list|,
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|long
name|timeout
init|=
name|admin
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
literal|"hbase.hbck.close.timeout"
argument_list|,
literal|120000
argument_list|)
decl_stmt|;
name|long
name|expiration
init|=
name|timeout
operator|+
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
while|while
condition|(
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|expiration
condition|)
block|{
try|try
block|{
name|HRegionInfo
name|rsRegion
init|=
name|ProtobufUtil
operator|.
name|getRegionInfo
argument_list|(
name|rs
argument_list|,
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|rsRegion
operator|==
literal|null
condition|)
return|return;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
return|return;
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Region "
operator|+
name|region
operator|+
literal|" failed to close within"
operator|+
literal|" timeout "
operator|+
name|timeout
argument_list|)
throw|;
block|}
comment|/**    * Puts the specified HRegionInfo into META.    */
specifier|public
specifier|static
name|void
name|fixMetaHoleOnline
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HRegionInfo
name|hri
parameter_list|)
throws|throws
name|IOException
block|{
name|HTable
name|meta
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|MetaEditor
operator|.
name|addRegionToMeta
argument_list|(
name|meta
argument_list|,
name|hri
argument_list|)
expr_stmt|;
name|meta
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Creates, flushes, and closes a new region.    */
specifier|public
specifier|static
name|HRegion
name|createHDFSRegionDir
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HRegionInfo
name|hri
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Create HRegion
name|Path
name|root
init|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|hri
argument_list|,
name|root
argument_list|,
name|conf
argument_list|,
name|htd
argument_list|)
decl_stmt|;
name|HLog
name|hlog
init|=
name|region
operator|.
name|getLog
argument_list|()
decl_stmt|;
comment|// Close the new region to flush to disk. Close log file too.
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|hlog
operator|.
name|closeAndDelete
argument_list|()
expr_stmt|;
return|return
name|region
return|;
block|}
block|}
end_class

end_unit

