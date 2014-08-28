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
name|master
operator|.
name|handler
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
name|io
operator|.
name|InterruptedIOException
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
name|Server
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
name|executor
operator|.
name|EventType
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
name|AssignmentManager
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
name|DeadServer
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
name|ZooKeeperProtos
operator|.
name|SplitLogTask
operator|.
name|RecoveryMode
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
name|zookeeper
operator|.
name|KeeperException
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
name|annotations
operator|.
name|VisibleForTesting
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
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_comment
comment|/**  * Shutdown handler for the server hosting<code>hbase:meta</code>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetaServerShutdownHandler
extends|extends
name|ServerShutdownHandler
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
name|MetaServerShutdownHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|AtomicInteger
name|eventExceptionCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
annotation|@
name|VisibleForTesting
specifier|static
specifier|final
name|int
name|SHOW_STRACKTRACE_FREQUENCY
init|=
literal|100
decl_stmt|;
specifier|public
name|MetaServerShutdownHandler
parameter_list|(
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|MasterServices
name|services
parameter_list|,
specifier|final
name|DeadServer
name|deadServers
parameter_list|,
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|services
argument_list|,
name|deadServers
argument_list|,
name|serverName
argument_list|,
name|EventType
operator|.
name|M_META_SERVER_SHUTDOWN
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|gotException
init|=
literal|true
decl_stmt|;
try|try
block|{
name|AssignmentManager
name|am
init|=
name|this
operator|.
name|services
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|setLogRecoveryMode
argument_list|()
expr_stmt|;
name|boolean
name|distributedLogReplay
init|=
operator|(
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getLogRecoveryMode
argument_list|()
operator|==
name|RecoveryMode
operator|.
name|LOG_REPLAY
operator|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|this
operator|.
name|shouldSplitHlog
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Splitting hbase:meta logs for "
operator|+
name|serverName
argument_list|)
expr_stmt|;
if|if
condition|(
name|distributedLogReplay
condition|)
block|{
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
name|regions
operator|.
name|add
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
expr_stmt|;
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|prepareLogReplay
argument_list|(
name|serverName
argument_list|,
name|regions
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|splitMetaLog
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|logSplit
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|this
operator|.
name|services
operator|.
name|getExecutorService
argument_list|()
operator|.
name|submit
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|deadServers
operator|.
name|add
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"failed log splitting for "
operator|+
name|serverName
operator|+
literal|", will retry"
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
comment|// Assign meta if we were carrying it.
comment|// Check again: region may be assigned to other where because of RIT
comment|// timeout
if|if
condition|(
name|am
operator|.
name|isCarryingMeta
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Server "
operator|+
name|serverName
operator|+
literal|" was carrying META. Trying to assign."
argument_list|)
expr_stmt|;
name|verifyAndAssignMetaWithRetries
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"META has been assigned to otherwhere, skip assigning."
argument_list|)
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|this
operator|.
name|shouldSplitHlog
operator|&&
name|distributedLogReplay
condition|)
block|{
if|if
condition|(
operator|!
name|am
operator|.
name|waitOnRegionToClearRegionsInTransition
argument_list|(
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|regionAssignmentWaitTimeout
argument_list|)
condition|)
block|{
comment|// Wait here is to avoid log replay hits current dead server and incur a RPC timeout
comment|// when replay happens before region assignment completes.
name|LOG
operator|.
name|warn
argument_list|(
literal|"Region "
operator|+
name|HRegionInfo
operator|.
name|FIRST_META_REGIONINFO
operator|.
name|getEncodedName
argument_list|()
operator|+
literal|" didn't complete assignment in time"
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|services
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|splitMetaLog
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
if|if
condition|(
name|ex
operator|instanceof
name|IOException
condition|)
block|{
name|this
operator|.
name|services
operator|.
name|getExecutorService
argument_list|()
operator|.
name|submit
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|deadServers
operator|.
name|add
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"failed log splitting for "
operator|+
name|serverName
operator|+
literal|", will retry"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
name|gotException
operator|=
literal|false
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|gotException
condition|)
block|{
comment|// If we had an exception, this.deadServers.finish will be skipped in super.process()
name|this
operator|.
name|deadServers
operator|.
name|finish
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
block|}
name|super
operator|.
name|process
argument_list|()
expr_stmt|;
comment|// Clear this counter on successful handling.
name|this
operator|.
name|eventExceptionCount
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
name|boolean
name|isCarryingMeta
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|/**    * Before assign the hbase:meta region, ensure it haven't    *  been assigned by other place    *<p>    * Under some scenarios, the hbase:meta region can be opened twice, so it seemed online    * in two regionserver at the same time.    * If the hbase:meta region has been assigned, so the operation can be canceled.    * @throws InterruptedException    * @throws IOException    * @throws KeeperException    */
specifier|private
name|void
name|verifyAndAssignMeta
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|IOException
throws|,
name|KeeperException
block|{
name|long
name|timeout
init|=
name|this
operator|.
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
literal|"hbase.catalog.verification.timeout"
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|server
operator|.
name|getMetaTableLocator
argument_list|()
operator|.
name|verifyMetaRegionLocation
argument_list|(
name|server
operator|.
name|getShortCircuitConnection
argument_list|()
argument_list|,
name|this
operator|.
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|timeout
argument_list|)
condition|)
block|{
name|this
operator|.
name|services
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|assignMeta
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|serverName
operator|.
name|equals
argument_list|(
name|server
operator|.
name|getMetaTableLocator
argument_list|()
operator|.
name|getMetaRegionLocation
argument_list|(
name|this
operator|.
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"hbase:meta is onlined on the dead server "
operator|+
name|serverName
argument_list|)
throw|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Skip assigning hbase:meta, because it is online on the "
operator|+
name|server
operator|.
name|getMetaTableLocator
argument_list|()
operator|.
name|getMetaRegionLocation
argument_list|(
name|this
operator|.
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Failed many times, shutdown processing    * @throws IOException    */
specifier|private
name|void
name|verifyAndAssignMetaWithRetries
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|iTimes
init|=
name|this
operator|.
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.catalog.verification.retries"
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|long
name|waitTime
init|=
name|this
operator|.
name|server
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
literal|"hbase.catalog.verification.timeout"
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|int
name|iFlag
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
try|try
block|{
name|verifyAndAssignMeta
argument_list|()
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
name|this
operator|.
name|server
operator|.
name|abort
argument_list|(
literal|"In server shutdown processing, assigning meta"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Aborting"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|iFlag
operator|>=
name|iTimes
condition|)
block|{
name|this
operator|.
name|server
operator|.
name|abort
argument_list|(
literal|"verifyAndAssignMeta failed after"
operator|+
name|iTimes
operator|+
literal|" times retries, aborting"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Aborting"
argument_list|,
name|e
argument_list|)
throw|;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|waitTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e1
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Interrupted when is the thread sleep"
argument_list|,
name|e1
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
throw|throw
operator|(
name|InterruptedIOException
operator|)
operator|new
name|InterruptedIOException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e1
argument_list|)
throw|;
block|}
name|iFlag
operator|++
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|handleException
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|int
name|count
init|=
name|eventExceptionCount
operator|.
name|getAndIncrement
argument_list|()
decl_stmt|;
if|if
condition|(
name|count
operator|<
literal|0
condition|)
name|count
operator|=
name|eventExceptionCount
operator|.
name|getAndSet
argument_list|(
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|count
operator|>
name|SHOW_STRACKTRACE_FREQUENCY
condition|)
block|{
comment|// Too frequent, let's slow reporting
name|Threads
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|count
operator|%
name|SHOW_STRACKTRACE_FREQUENCY
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught "
operator|+
name|eventType
operator|+
literal|", count="
operator|+
name|this
operator|.
name|eventExceptionCount
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Caught "
operator|+
name|eventType
operator|+
literal|", count="
operator|+
name|this
operator|.
name|eventExceptionCount
operator|+
literal|"; "
operator|+
name|t
operator|.
name|getMessage
argument_list|()
operator|+
literal|"; stack trace shows every "
operator|+
name|SHOW_STRACKTRACE_FREQUENCY
operator|+
literal|"th time."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

