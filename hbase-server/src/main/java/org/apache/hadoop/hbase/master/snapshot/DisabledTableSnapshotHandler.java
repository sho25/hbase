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
name|snapshot
operator|.
name|CopyRecoveredEditsTask
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
name|ReferenceRegionHFilesTask
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
name|TableInfoCopyTask
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
name|TakeSnapshotUtils
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
comment|/**    * @param snapshot descriptor of the snapshot to take    * @param server parent server    * @param masterServices master services provider    * @throws IOException on unexpected error    */
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
throws|throws
name|IOException
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
name|TakeSnapshotUtils
operator|.
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
name|String
argument_list|>
name|serverNames
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
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
name|regions
operator|.
name|add
argument_list|(
name|p
operator|.
name|getFirst
argument_list|()
argument_list|)
expr_stmt|;
name|serverNames
operator|.
name|add
argument_list|(
name|p
operator|.
name|getSecond
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// 2. for each region, write all the info to disk
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting to write region info and WALs for regions for offline snapshot:"
operator|+
name|snapshot
argument_list|)
expr_stmt|;
for|for
control|(
name|HRegionInfo
name|regionInfo
range|:
name|regions
control|)
block|{
comment|// 2.1 copy the regionInfo files to the snapshot
name|Path
name|snapshotRegionDir
init|=
name|TakeSnapshotUtils
operator|.
name|getRegionSnapshotDirectory
argument_list|(
name|snapshot
argument_list|,
name|rootDir
argument_list|,
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|HRegion
operator|.
name|writeRegioninfoOnFilesystem
argument_list|(
name|regionInfo
argument_list|,
name|snapshotRegionDir
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|)
expr_stmt|;
comment|// check for error for each region
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
comment|// 2.2 for each region, copy over its recovered.edits directory
name|Path
name|regionDir
init|=
name|HRegion
operator|.
name|getRegionDir
argument_list|(
name|rootDir
argument_list|,
name|regionInfo
argument_list|)
decl_stmt|;
operator|new
name|CopyRecoveredEditsTask
argument_list|(
name|snapshot
argument_list|,
name|monitor
argument_list|,
name|fs
argument_list|,
name|regionDir
argument_list|,
name|snapshotRegionDir
argument_list|)
operator|.
name|call
argument_list|()
expr_stmt|;
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
comment|// 2.3 reference all the files in the region
operator|new
name|ReferenceRegionHFilesTask
argument_list|(
name|snapshot
argument_list|,
name|monitor
argument_list|,
name|regionDir
argument_list|,
name|fs
argument_list|,
name|snapshotRegionDir
argument_list|)
operator|.
name|call
argument_list|()
expr_stmt|;
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
block|}
comment|// 3. write the table info to disk
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting to copy tableinfo for offline snapshot:\n"
operator|+
name|snapshot
argument_list|)
expr_stmt|;
name|TableInfoCopyTask
name|tableInfo
init|=
operator|new
name|TableInfoCopyTask
argument_list|(
name|this
operator|.
name|monitor
argument_list|,
name|snapshot
argument_list|,
name|fs
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|tableInfo
operator|.
name|call
argument_list|()
expr_stmt|;
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
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
literal|"Failed due to exception:"
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
block|}
finally|finally
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Marking snapshot"
operator|+
name|this
operator|.
name|snapshot
operator|+
literal|" as finished."
argument_list|)
expr_stmt|;
comment|// 6. mark the timer as finished - even if we got an exception, we don't need to time the
comment|// operation any further
name|timeoutInjector
operator|.
name|complete
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

