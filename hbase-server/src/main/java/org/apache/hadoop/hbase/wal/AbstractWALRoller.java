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
name|wal
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
name|java
operator|.
name|net
operator|.
name|ConnectException
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
name|Iterator
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
operator|.
name|Entry
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMap
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
name|Abortable
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
name|regionserver
operator|.
name|wal
operator|.
name|AbstractFSWAL
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
name|FailedLogCloseException
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
name|WALActionsListener
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
name|WALClosedException
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
name|HasThread
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
name|ipc
operator|.
name|RemoteException
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
comment|/**  * Runs periodically to determine if the WAL should be rolled.  *<p/>  * NOTE: This class extends Thread rather than Chore because the sleep time can be interrupted when  * there is something to do, rather than the Chore sleep time which is invariant.  *<p/>  * The {@link #scheduleFlush(String)} is abstract here, as sometimes we hold a region without a  * region server but we still want to roll its WAL.  *<p/>  * TODO: change to a pool of threads  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractWALRoller
parameter_list|<
name|T
extends|extends
name|Abortable
parameter_list|>
extends|extends
name|HasThread
implements|implements
name|Closeable
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
name|AbstractWALRoller
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|WAL_ROLL_PERIOD_KEY
init|=
literal|"hbase.regionserver.logroll.period"
decl_stmt|;
specifier|protected
specifier|final
name|ConcurrentMap
argument_list|<
name|WAL
argument_list|,
name|Boolean
argument_list|>
name|walNeedsRoll
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|T
name|abortable
decl_stmt|;
specifier|private
specifier|volatile
name|long
name|lastRollTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// Period to roll log.
specifier|private
specifier|final
name|long
name|rollPeriod
decl_stmt|;
specifier|private
specifier|final
name|int
name|threadWakeFrequency
decl_stmt|;
comment|// The interval to check low replication on hlog's pipeline
specifier|private
name|long
name|checkLowReplicationInterval
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|running
init|=
literal|true
decl_stmt|;
specifier|public
name|void
name|addWAL
parameter_list|(
name|WAL
name|wal
parameter_list|)
block|{
comment|// check without lock first
if|if
condition|(
name|walNeedsRoll
operator|.
name|containsKey
argument_list|(
name|wal
argument_list|)
condition|)
block|{
return|return;
block|}
comment|// this is to avoid race between addWAL and requestRollAll.
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|walNeedsRoll
operator|.
name|putIfAbsent
argument_list|(
name|wal
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
operator|==
literal|null
condition|)
block|{
name|wal
operator|.
name|registerWALActionsListener
argument_list|(
operator|new
name|WALActionsListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|logRollRequested
parameter_list|(
name|WALActionsListener
operator|.
name|RollRequestReason
name|reason
parameter_list|)
block|{
comment|// TODO logs will contend with each other here, replace with e.g. DelayedQueue
synchronized|synchronized
init|(
name|AbstractWALRoller
operator|.
name|this
init|)
block|{
name|walNeedsRoll
operator|.
name|put
argument_list|(
name|wal
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
name|AbstractWALRoller
operator|.
name|this
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|requestRollAll
parameter_list|()
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
name|List
argument_list|<
name|WAL
argument_list|>
name|wals
init|=
operator|new
name|ArrayList
argument_list|<
name|WAL
argument_list|>
argument_list|(
name|walNeedsRoll
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|WAL
name|wal
range|:
name|wals
control|)
block|{
name|walNeedsRoll
operator|.
name|put
argument_list|(
name|wal
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
block|}
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
specifier|protected
name|AbstractWALRoller
parameter_list|(
name|String
name|name
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|T
name|abortable
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|abortable
operator|=
name|abortable
expr_stmt|;
name|this
operator|.
name|rollPeriod
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|WAL_ROLL_PERIOD_KEY
argument_list|,
literal|3600000
argument_list|)
expr_stmt|;
name|this
operator|.
name|threadWakeFrequency
operator|=
name|conf
operator|.
name|getInt
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|this
operator|.
name|checkLowReplicationInterval
operator|=
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.regionserver.hlog.check.lowreplication.interval"
argument_list|,
literal|30
operator|*
literal|1000
argument_list|)
expr_stmt|;
block|}
comment|/**    * we need to check low replication in period, see HBASE-18132    */
specifier|private
name|void
name|checkLowReplication
parameter_list|(
name|long
name|now
parameter_list|)
block|{
try|try
block|{
for|for
control|(
name|Entry
argument_list|<
name|WAL
argument_list|,
name|Boolean
argument_list|>
name|entry
range|:
name|walNeedsRoll
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|WAL
name|wal
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|boolean
name|needRollAlready
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|needRollAlready
operator|||
operator|!
operator|(
name|wal
operator|instanceof
name|AbstractFSWAL
operator|)
condition|)
block|{
continue|continue;
block|}
operator|(
operator|(
name|AbstractFSWAL
argument_list|<
name|?
argument_list|>
operator|)
name|wal
operator|)
operator|.
name|checkLogLowReplication
argument_list|(
name|checkLowReplicationInterval
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed checking low replication"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|abort
parameter_list|(
name|String
name|reason
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
comment|// close all WALs before calling abort on RS.
comment|// This is because AsyncFSWAL replies on us for rolling a new writer to make progress, and if we
comment|// failed, AsyncFSWAL may be stuck, so we need to close it to let the upper layer know that it
comment|// is already broken.
for|for
control|(
name|WAL
name|wal
range|:
name|walNeedsRoll
operator|.
name|keySet
argument_list|()
control|)
block|{
comment|// shutdown rather than close here since we are going to abort the RS and the wals need to be
comment|// split when recovery
try|try
block|{
name|wal
operator|.
name|shutdown
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
literal|"Failed to shutdown wal"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|abortable
operator|.
name|abort
argument_list|(
name|reason
argument_list|,
name|cause
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
while|while
condition|(
name|running
condition|)
block|{
name|boolean
name|periodic
init|=
literal|false
decl_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|checkLowReplication
argument_list|(
name|now
argument_list|)
expr_stmt|;
name|periodic
operator|=
operator|(
name|now
operator|-
name|this
operator|.
name|lastRollTime
operator|)
operator|>
name|this
operator|.
name|rollPeriod
expr_stmt|;
if|if
condition|(
name|periodic
condition|)
block|{
comment|// Time for periodic roll, fall through
name|LOG
operator|.
name|debug
argument_list|(
literal|"WAL roll period {} ms elapsed"
argument_list|,
name|this
operator|.
name|rollPeriod
argument_list|)
expr_stmt|;
block|}
else|else
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|walNeedsRoll
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|Boolean
operator|::
name|booleanValue
argument_list|)
condition|)
block|{
comment|// WAL roll requested, fall through
name|LOG
operator|.
name|debug
argument_list|(
literal|"WAL roll requested"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|wait
argument_list|(
name|this
operator|.
name|threadWakeFrequency
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// restore the interrupt state
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
comment|// goto the beginning to check whether again whether we should fall through to roll
comment|// several WALs, and also check whether we should quit.
continue|continue;
block|}
block|}
block|}
try|try
block|{
name|this
operator|.
name|lastRollTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|WAL
argument_list|,
name|Boolean
argument_list|>
argument_list|>
name|iter
init|=
name|walNeedsRoll
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|iter
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Entry
argument_list|<
name|WAL
argument_list|,
name|Boolean
argument_list|>
name|entry
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
name|WAL
name|wal
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
comment|// reset the flag in front to avoid missing roll request before we return from rollWriter.
name|walNeedsRoll
operator|.
name|put
argument_list|(
name|wal
argument_list|,
name|Boolean
operator|.
name|FALSE
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|regionsToFlush
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Force the roll if the logroll.period is elapsed or if a roll was requested.
comment|// The returned value is an array of actual region names.
name|regionsToFlush
operator|=
name|wal
operator|.
name|rollWriter
argument_list|(
name|periodic
operator|||
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|WALClosedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"WAL has been closed. Skipping rolling of writer and just remove it"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|iter
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|regionsToFlush
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|byte
index|[]
name|r
range|:
name|regionsToFlush
control|)
block|{
name|scheduleFlush
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|r
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|afterRoll
argument_list|(
name|wal
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|FailedLogCloseException
decl||
name|ConnectException
name|e
parameter_list|)
block|{
name|abort
argument_list|(
literal|"Failed log close in log roller"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
comment|// Abort if we get here. We probably won't recover an IOE. HBASE-1132
name|abort
argument_list|(
literal|"IOE in log roller"
argument_list|,
name|ex
operator|instanceof
name|RemoteException
condition|?
operator|(
operator|(
name|RemoteException
operator|)
name|ex
operator|)
operator|.
name|unwrapRemoteException
argument_list|()
else|:
name|ex
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Log rolling failed"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|abort
argument_list|(
literal|"Log rolling failed"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"LogRoller exiting."
argument_list|)
expr_stmt|;
block|}
comment|/**    * Called after we finish rolling the give {@code wal}.    */
specifier|protected
name|void
name|afterRoll
parameter_list|(
name|WAL
name|wal
parameter_list|)
block|{   }
comment|/**    * @param encodedRegionName Encoded name of region to flush.    */
specifier|protected
specifier|abstract
name|void
name|scheduleFlush
parameter_list|(
name|String
name|encodedRegionName
parameter_list|)
function_decl|;
specifier|private
name|boolean
name|isWaiting
parameter_list|()
block|{
name|Thread
operator|.
name|State
name|state
init|=
name|getThread
argument_list|()
operator|.
name|getState
argument_list|()
decl_stmt|;
return|return
name|state
operator|==
name|Thread
operator|.
name|State
operator|.
name|WAITING
operator|||
name|state
operator|==
name|Thread
operator|.
name|State
operator|.
name|TIMED_WAITING
return|;
block|}
comment|/**    * @return true if all WAL roll finished    */
specifier|public
name|boolean
name|walRollFinished
parameter_list|()
block|{
return|return
name|walNeedsRoll
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|allMatch
argument_list|(
name|needRoll
lambda|->
operator|!
name|needRoll
argument_list|)
operator|&&
name|isWaiting
argument_list|()
return|;
block|}
comment|/**    * Wait until all wals have been rolled after calling {@link #requestRollAll()}.    */
specifier|public
name|void
name|waitUntilWalRollFinished
parameter_list|()
throws|throws
name|InterruptedException
block|{
while|while
condition|(
operator|!
name|walRollFinished
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|running
operator|=
literal|false
expr_stmt|;
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

