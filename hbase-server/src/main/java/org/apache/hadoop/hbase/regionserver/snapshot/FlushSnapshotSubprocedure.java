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
name|regionserver
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
name|List
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
name|Callable
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
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|hbase
operator|.
name|client
operator|.
name|IsolationLevel
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
name|ForeignExceptionDispatcher
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
name|ProcedureMember
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
name|Subprocedure
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
name|HRegion
operator|.
name|FlushResult
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
name|Region
operator|.
name|Operation
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
name|snapshot
operator|.
name|RegionServerSnapshotManager
operator|.
name|SnapshotSubprocedurePool
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|SnapshotProtos
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

begin_comment
comment|/**  * This online snapshot implementation uses the distributed procedure framework to force a  * store flush and then records the hfiles.  Its enter stage does nothing.  Its leave stage then  * flushes the memstore, builds the region server's snapshot manifest from its hfiles list, and  * copies .regioninfos into the snapshot working directory.  At the master side, there is an atomic  * rename of the working dir into the proper snapshot directory.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Unstable
specifier|public
class|class
name|FlushSnapshotSubprocedure
extends|extends
name|Subprocedure
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
name|FlushSnapshotSubprocedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
decl_stmt|;
specifier|private
specifier|final
name|SnapshotDescription
name|snapshot
decl_stmt|;
specifier|private
specifier|final
name|SnapshotSubprocedurePool
name|taskManager
decl_stmt|;
specifier|private
name|boolean
name|snapshotSkipFlush
init|=
literal|false
decl_stmt|;
comment|// the maximum number of attempts we flush
specifier|final
specifier|static
name|int
name|MAX_RETRIES
init|=
literal|3
decl_stmt|;
specifier|public
name|FlushSnapshotSubprocedure
parameter_list|(
name|ProcedureMember
name|member
parameter_list|,
name|ForeignExceptionDispatcher
name|errorListener
parameter_list|,
name|long
name|wakeFrequency
parameter_list|,
name|long
name|timeout
parameter_list|,
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
parameter_list|,
name|SnapshotDescription
name|snapshot
parameter_list|,
name|SnapshotSubprocedurePool
name|taskManager
parameter_list|)
block|{
name|super
argument_list|(
name|member
argument_list|,
name|snapshot
operator|.
name|getName
argument_list|()
argument_list|,
name|errorListener
argument_list|,
name|wakeFrequency
argument_list|,
name|timeout
argument_list|)
expr_stmt|;
name|this
operator|.
name|snapshot
operator|=
name|snapshot
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|snapshot
operator|.
name|getType
argument_list|()
operator|==
name|SnapshotDescription
operator|.
name|Type
operator|.
name|SKIPFLUSH
condition|)
block|{
name|snapshotSkipFlush
operator|=
literal|true
expr_stmt|;
block|}
name|this
operator|.
name|regions
operator|=
name|regions
expr_stmt|;
name|this
operator|.
name|taskManager
operator|=
name|taskManager
expr_stmt|;
block|}
comment|/**    * Callable for adding files to snapshot manifest working dir.  Ready for multithreading.    */
specifier|public
specifier|static
class|class
name|RegionSnapshotTask
implements|implements
name|Callable
argument_list|<
name|Void
argument_list|>
block|{
specifier|private
name|HRegion
name|region
decl_stmt|;
specifier|private
name|boolean
name|skipFlush
decl_stmt|;
specifier|private
name|ForeignExceptionDispatcher
name|monitor
decl_stmt|;
specifier|private
name|SnapshotDescription
name|snapshotDesc
decl_stmt|;
specifier|public
name|RegionSnapshotTask
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|SnapshotDescription
name|snapshotDesc
parameter_list|,
name|boolean
name|skipFlush
parameter_list|,
name|ForeignExceptionDispatcher
name|monitor
parameter_list|)
block|{
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
name|this
operator|.
name|skipFlush
operator|=
name|skipFlush
expr_stmt|;
name|this
operator|.
name|monitor
operator|=
name|monitor
expr_stmt|;
name|this
operator|.
name|snapshotDesc
operator|=
name|snapshotDesc
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Taking the region read lock prevents the individual region from being closed while a
comment|// snapshot is in progress.  This is helpful but not sufficient for preventing races with
comment|// snapshots that involve multiple regions and regionservers.  It is still possible to have
comment|// an interleaving such that globally regions are missing, so we still need the verification
comment|// step.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Starting snapshot operation on "
operator|+
name|region
argument_list|)
expr_stmt|;
name|region
operator|.
name|startRegionOperation
argument_list|(
name|Operation
operator|.
name|SNAPSHOT
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|skipFlush
condition|)
block|{
comment|/*          * This is to take an online-snapshot without force a coordinated flush to prevent pause          * The snapshot type is defined inside the snapshot description. FlushSnapshotSubprocedure          * should be renamed to distributedSnapshotSubprocedure, and the flush() behavior can be          * turned on/off based on the flush type.          * To minimized the code change, class name is not changed.          */
name|LOG
operator|.
name|debug
argument_list|(
literal|"take snapshot without flush memstore first"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Flush Snapshotting region "
operator|+
name|region
operator|.
name|toString
argument_list|()
operator|+
literal|" started..."
argument_list|)
expr_stmt|;
name|boolean
name|succeeded
init|=
literal|false
decl_stmt|;
name|long
name|readPt
init|=
name|region
operator|.
name|getReadPoint
argument_list|(
name|IsolationLevel
operator|.
name|READ_COMMITTED
argument_list|)
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
name|MAX_RETRIES
condition|;
name|i
operator|++
control|)
block|{
name|FlushResult
name|res
init|=
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|.
name|getResult
argument_list|()
operator|==
name|FlushResult
operator|.
name|Result
operator|.
name|CANNOT_FLUSH
condition|)
block|{
comment|// CANNOT_FLUSH may mean that a flush is already on-going
comment|// we need to wait for that flush to complete
name|region
operator|.
name|waitForFlushes
argument_list|()
expr_stmt|;
if|if
condition|(
name|region
operator|.
name|getMaxFlushedSeqId
argument_list|()
operator|>=
name|readPt
condition|)
block|{
comment|// writes at the start of the snapshot have been persisted
name|succeeded
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
else|else
block|{
name|succeeded
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|succeeded
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to complete flush after "
operator|+
name|MAX_RETRIES
operator|+
literal|" attempts"
argument_list|)
throw|;
block|}
block|}
name|region
operator|.
name|addRegionToSnapshot
argument_list|(
name|snapshotDesc
argument_list|,
name|monitor
argument_list|)
expr_stmt|;
if|if
condition|(
name|skipFlush
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"... SkipFlush Snapshotting region "
operator|+
name|region
operator|.
name|toString
argument_list|()
operator|+
literal|" completed."
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"... Flush Snapshotting region "
operator|+
name|region
operator|.
name|toString
argument_list|()
operator|+
literal|" completed."
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Closing snapshot operation on "
operator|+
name|region
argument_list|)
expr_stmt|;
name|region
operator|.
name|closeRegionOperation
argument_list|(
name|Operation
operator|.
name|SNAPSHOT
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|void
name|flushSnapshot
parameter_list|()
throws|throws
name|ForeignException
block|{
if|if
condition|(
name|regions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// No regions on this RS, we are basically done.
return|return;
block|}
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
comment|// assert that the taskManager is empty.
if|if
condition|(
name|taskManager
operator|.
name|hasTasks
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Attempting to take snapshot "
operator|+
name|ClientSnapshotDescriptionUtils
operator|.
name|toString
argument_list|(
name|snapshot
argument_list|)
operator|+
literal|" but we currently have outstanding tasks"
argument_list|)
throw|;
block|}
comment|// Add all hfiles already existing in region.
for|for
control|(
name|HRegion
name|region
range|:
name|regions
control|)
block|{
comment|// submit one task per region for parallelize by region.
name|taskManager
operator|.
name|submitTask
argument_list|(
operator|new
name|RegionSnapshotTask
argument_list|(
name|region
argument_list|,
name|snapshot
argument_list|,
name|snapshotSkipFlush
argument_list|,
name|monitor
argument_list|)
argument_list|)
expr_stmt|;
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
block|}
comment|// wait for everything to complete.
name|LOG
operator|.
name|debug
argument_list|(
literal|"Flush Snapshot Tasks submitted for "
operator|+
name|regions
operator|.
name|size
argument_list|()
operator|+
literal|" regions"
argument_list|)
expr_stmt|;
try|try
block|{
name|taskManager
operator|.
name|waitForOutstandingTasks
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
literal|"got interrupted exception for "
operator|+
name|getMemberName
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ForeignException
argument_list|(
name|getMemberName
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * do nothing, core of snapshot is executed in {@link #insideBarrier} step.    */
annotation|@
name|Override
specifier|public
name|void
name|acquireBarrier
parameter_list|()
throws|throws
name|ForeignException
block|{
comment|// NO OP
block|}
comment|/**    * do a flush snapshot of every region on this rs from the target table.    */
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|insideBarrier
parameter_list|()
throws|throws
name|ForeignException
block|{
name|flushSnapshot
argument_list|()
expr_stmt|;
return|return
operator|new
name|byte
index|[
literal|0
index|]
return|;
block|}
comment|/**    * Cancel threads if they haven't finished.    */
annotation|@
name|Override
specifier|public
name|void
name|cleanup
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Aborting all online FLUSH snapshot subprocedure task threads for '"
operator|+
name|snapshot
operator|.
name|getName
argument_list|()
operator|+
literal|"' due to error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
try|try
block|{
name|taskManager
operator|.
name|cancelTasks
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e1
parameter_list|)
block|{
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
comment|/**    * Hooray!    */
specifier|public
name|void
name|releaseBarrier
parameter_list|()
block|{
comment|// NO OP
block|}
block|}
end_class

end_unit

