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
name|master
operator|.
name|snapshot
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
name|HashSet
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
name|Set
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
name|ThreadPoolExecutor
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
name|client
operator|.
name|RegionReplicaUtil
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
name|errorhandling
operator|.
name|ForeignException
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
name|errorhandling
operator|.
name|ForeignExceptionListener
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
name|errorhandling
operator|.
name|TimeoutExceptionInjector
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
name|MasterServices
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
name|generated
operator|.
name|HBaseProtos
operator|.
name|SnapshotDescription
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
name|snapshot
operator|.
name|ClientSnapshotDescriptionUtils
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
name|snapshot
operator|.
name|SnapshotDescriptionUtils
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
name|snapshot
operator|.
name|SnapshotManifest
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
name|hbase
operator|.
name|util
operator|.
name|ModifyRegionUtils
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
name|Pair
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
comment|/**  * Take a snapshot of a disabled table.  *<p>  * Table must exist when taking the snapshot, or results are undefined.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|DisabledTableSnapshotHandler
extends|extends
name|TakeSnapshotHandler
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
name|DisabledTableSnapshotHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|TimeoutExceptionInjector
name|timeoutInjector
decl_stmt|;
comment|/**    * @param snapshot descriptor of the snapshot to take    * @param masterServices master services provider    */
specifier|public
name|DisabledTableSnapshotHandler
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|MasterServices
name|masterServices
parameter_list|)
block|{
name|super
argument_list|(
name|snapshot
argument_list|,
name|masterServices
argument_list|)
expr_stmt|;
comment|// setup the timer
name|timeoutInjector
operator|=
name|getMasterTimerAndBindToMonitor
argument_list|(
name|snapshot
argument_list|,
name|conf
argument_list|,
name|monitor
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|DisabledTableSnapshotHandler
name|prepare
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|(
name|DisabledTableSnapshotHandler
operator|)
name|super
operator|.
name|prepare
argument_list|()
return|;
block|}
comment|// TODO consider parallelizing these operations since they are independent. Right now its just
comment|// easier to keep them serial though
annotation|@
name|Override
specifier|public
name|void
name|snapshotRegions
parameter_list|(
name|List
argument_list|<
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
argument_list|>
name|regionsAndLocations
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
try|try
block|{
name|timeoutInjector
operator|.
name|start
argument_list|()
expr_stmt|;
comment|// 1. get all the regions hosting this table.
comment|// extract each pair to separate lists
name|Set
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
operator|new
name|HashSet
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|p
range|:
name|regionsAndLocations
control|)
block|{
comment|// Don't include non-default regions
name|HRegionInfo
name|hri
init|=
name|p
operator|.
name|getFirst
argument_list|()
decl_stmt|;
if|if
condition|(
name|RegionReplicaUtil
operator|.
name|isDefaultReplica
argument_list|(
name|hri
argument_list|)
condition|)
block|{
name|regions
operator|.
name|add
argument_list|(
name|hri
argument_list|)
expr_stmt|;
block|}
block|}
comment|// 2. for each region, write all the info to disk
name|String
name|msg
init|=
literal|"Starting to write region info and WALs for regions for offline snapshot:"
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|status
operator|.
name|setStatus
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|ThreadPoolExecutor
name|exec
init|=
name|SnapshotManifest
operator|.
name|createExecutor
argument_list|(
name|conf
argument_list|,
literal|"DisabledTableSnapshot"
argument_list|)
decl_stmt|;
try|try
block|{
name|ModifyRegionUtils
operator|.
name|editRegions
argument_list|(
name|exec
argument_list|,
name|regions
argument_list|,
operator|new
name|ModifyRegionUtils
operator|.
name|RegionEditTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|editRegion
parameter_list|(
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|snapshotManifest
operator|.
name|addRegion
argument_list|(
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|snapshotTable
argument_list|)
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|exec
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// make sure we capture the exception to propagate back to the client later
name|String
name|reason
init|=
literal|"Failed snapshot "
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
operator|+
literal|" due to exception:"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
name|ForeignException
name|ee
init|=
operator|new
name|ForeignException
argument_list|(
name|reason
argument_list|,
name|e
argument_list|)
decl_stmt|;
name|monitor
operator|.
name|receive
argument_list|(
name|ee
argument_list|)
expr_stmt|;
name|status
operator|.
name|abort
argument_list|(
literal|"Snapshot of table: "
operator|+
name|snapshotTable
operator|+
literal|" failed because "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Marking snapshot"
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
operator|+
literal|" as finished."
argument_list|)
expr_stmt|;
comment|// 3. mark the timer as finished - even if we got an exception, we don't need to time the
comment|// operation any further
name|timeoutInjector
operator|.
name|complete
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Create a snapshot timer for the master which notifies the monitor when an error occurs    * @param snapshot snapshot to monitor    * @param conf configuration to use when getting the max snapshot life    * @param monitor monitor to notify when the snapshot life expires    * @return the timer to use update to signal the start and end of the snapshot    */
specifier|private
name|TimeoutExceptionInjector
name|getMasterTimerAndBindToMonitor
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|ForeignExceptionListener
name|monitor
parameter_list|)
block|{
name|long
name|maxTime
init|=
name|SnapshotDescriptionUtils
operator|.
name|getMaxMasterTimeout
argument_list|(
name|conf
argument_list|,
name|snapshot
operator|.
name|getType
argument_list|()
argument_list|,
name|SnapshotDescriptionUtils
operator|.
name|DEFAULT_MAX_WAIT_TIME
argument_list|)
decl_stmt|;
return|return
operator|new
name|TimeoutExceptionInjector
argument_list|(
name|monitor
argument_list|,
name|maxTime
argument_list|)
return|;
block|}
block|}
end_class

end_unit

