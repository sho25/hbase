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
name|hbase
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
name|procedure
operator|.
name|Procedure
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
name|procedure
operator|.
name|ProcedureCoordinator
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
name|HBaseSnapshotException
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Handle the master side of taking a snapshot of an online table, regardless of snapshot type.  * Uses a {@link Procedure} to run the snapshot across all the involved region servers.  * @see ProcedureCoordinator  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|EnabledTableSnapshotHandler
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
name|EnabledTableSnapshotHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ProcedureCoordinator
name|coordinator
decl_stmt|;
specifier|public
name|EnabledTableSnapshotHandler
parameter_list|(
name|SnapshotDescription
name|snapshot
parameter_list|,
name|MasterServices
name|master
parameter_list|,
specifier|final
name|SnapshotManager
name|manager
parameter_list|)
block|{
name|super
argument_list|(
name|snapshot
argument_list|,
name|master
argument_list|)
expr_stmt|;
name|this
operator|.
name|coordinator
operator|=
name|manager
operator|.
name|getCoordinator
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|EnabledTableSnapshotHandler
name|prepare
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|(
name|EnabledTableSnapshotHandler
operator|)
name|super
operator|.
name|prepare
argument_list|()
return|;
block|}
comment|// TODO consider switching over to using regionnames, rather than server names. This would allow
comment|// regions to migrate during a snapshot, and then be involved when they are ready. Still want to
comment|// enforce a snapshot time constraints, but lets us be potentially a bit more robust.
comment|/**    * This method kicks off a snapshot procedure.  Other than that it hangs around for various    * phases to complete.    */
annotation|@
name|Override
specifier|protected
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
name|regions
parameter_list|)
throws|throws
name|HBaseSnapshotException
throws|,
name|IOException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|regionServers
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|regions
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|region
range|:
name|regions
control|)
block|{
if|if
condition|(
name|region
operator|!=
literal|null
operator|&&
name|region
operator|.
name|getFirst
argument_list|()
operator|!=
literal|null
operator|&&
name|region
operator|.
name|getSecond
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|HRegionInfo
name|hri
init|=
name|region
operator|.
name|getFirst
argument_list|()
decl_stmt|;
if|if
condition|(
name|hri
operator|.
name|isOffline
argument_list|()
operator|&&
operator|(
name|hri
operator|.
name|isSplit
argument_list|()
operator|||
name|hri
operator|.
name|isSplitParent
argument_list|()
operator|)
condition|)
continue|continue;
name|regionServers
operator|.
name|add
argument_list|(
name|region
operator|.
name|getSecond
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// start the snapshot on the RS
name|Procedure
name|proc
init|=
name|coordinator
operator|.
name|startProcedure
argument_list|(
name|this
operator|.
name|monitor
argument_list|,
name|this
operator|.
name|snapshot
operator|.
name|getName
argument_list|()
argument_list|,
name|this
operator|.
name|snapshot
operator|.
name|toByteArray
argument_list|()
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|regionServers
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|proc
operator|==
literal|null
condition|)
block|{
name|String
name|msg
init|=
literal|"Failed to submit distributed procedure for snapshot '"
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|"'"
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|HBaseSnapshotException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
try|try
block|{
comment|// wait for the snapshot to complete.  A timer thread is kicked off that should cancel this
comment|// if it takes too long.
name|proc
operator|.
name|waitForCompleted
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Done waiting - online snapshot for "
operator|+
name|this
operator|.
name|snapshot
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Take the offline regions as disabled
for|for
control|(
name|Pair
argument_list|<
name|HRegionInfo
argument_list|,
name|ServerName
argument_list|>
name|region
range|:
name|regions
control|)
block|{
name|HRegionInfo
name|regionInfo
init|=
name|region
operator|.
name|getFirst
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionInfo
operator|.
name|isOffline
argument_list|()
operator|&&
operator|(
name|regionInfo
operator|.
name|isSplit
argument_list|()
operator|||
name|regionInfo
operator|.
name|isSplitParent
argument_list|()
operator|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Take disabled snapshot of offline region="
operator|+
name|regionInfo
argument_list|)
expr_stmt|;
name|snapshotDisabledRegion
argument_list|(
name|regionInfo
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|ForeignException
name|ee
init|=
operator|new
name|ForeignException
argument_list|(
literal|"Interrupted while waiting for snapshot to finish"
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
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ForeignException
name|e
parameter_list|)
block|{
name|monitor
operator|.
name|receive
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

