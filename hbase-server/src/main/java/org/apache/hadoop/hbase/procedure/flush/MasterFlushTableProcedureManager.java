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
name|procedure
operator|.
name|flush
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
name|HashMap
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
name|Map
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
name|HBaseInterfaceAudience
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
name|TableName
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
name|MetaTableAccessor
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
name|master
operator|.
name|MasterCoprocessorHost
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
name|master
operator|.
name|MetricsMaster
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
name|MasterProcedureManager
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
name|procedure
operator|.
name|ProcedureCoordinatorRpcs
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
name|ZKProcedureCoordinatorRpcs
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
name|HBaseProtos
operator|.
name|ProcedureDescription
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
name|hadoop
operator|.
name|hbase
operator|.
name|zookeeper
operator|.
name|MetaTableLocator
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
name|collect
operator|.
name|Lists
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|MasterFlushTableProcedureManager
extends|extends
name|MasterProcedureManager
block|{
specifier|public
specifier|static
specifier|final
name|String
name|FLUSH_TABLE_PROCEDURE_SIGNATURE
init|=
literal|"flush-table-proc"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FLUSH_TIMEOUT_MILLIS_KEY
init|=
literal|"hbase.flush.master.timeoutMillis"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|FLUSH_TIMEOUT_MILLIS_DEFAULT
init|=
literal|60000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FLUSH_WAKE_MILLIS_KEY
init|=
literal|"hbase.flush.master.wakeMillis"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|FLUSH_WAKE_MILLIS_DEFAULT
init|=
literal|500
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FLUSH_PROC_POOL_THREADS_KEY
init|=
literal|"hbase.flush.procedure.master.threads"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|FLUSH_PROC_POOL_THREADS_DEFAULT
init|=
literal|1
decl_stmt|;
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
name|MasterFlushTableProcedureManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|MasterServices
name|master
decl_stmt|;
specifier|private
name|ProcedureCoordinator
name|coordinator
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|TableName
argument_list|,
name|Procedure
argument_list|>
name|procMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|stopped
decl_stmt|;
specifier|public
name|MasterFlushTableProcedureManager
parameter_list|()
block|{}
empty_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"stop: "
operator|+
name|why
argument_list|)
expr_stmt|;
name|this
operator|.
name|stopped
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|this
operator|.
name|stopped
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|MasterServices
name|master
parameter_list|,
name|MetricsMaster
name|metricsMaster
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
throws|,
name|UnsupportedOperationException
block|{
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
comment|// get the configuration for the coordinator
name|Configuration
name|conf
init|=
name|master
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|long
name|wakeFrequency
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|FLUSH_WAKE_MILLIS_KEY
argument_list|,
name|FLUSH_WAKE_MILLIS_DEFAULT
argument_list|)
decl_stmt|;
name|long
name|timeoutMillis
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|FLUSH_TIMEOUT_MILLIS_KEY
argument_list|,
name|FLUSH_TIMEOUT_MILLIS_DEFAULT
argument_list|)
decl_stmt|;
name|int
name|threads
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|FLUSH_PROC_POOL_THREADS_KEY
argument_list|,
name|FLUSH_PROC_POOL_THREADS_DEFAULT
argument_list|)
decl_stmt|;
comment|// setup the procedure coordinator
name|String
name|name
init|=
name|master
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|ThreadPoolExecutor
name|tpool
init|=
name|ProcedureCoordinator
operator|.
name|defaultPool
argument_list|(
name|name
argument_list|,
name|threads
argument_list|)
decl_stmt|;
name|ProcedureCoordinatorRpcs
name|comms
init|=
operator|new
name|ZKProcedureCoordinatorRpcs
argument_list|(
name|master
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|getProcedureSignature
argument_list|()
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|this
operator|.
name|coordinator
operator|=
operator|new
name|ProcedureCoordinator
argument_list|(
name|comms
argument_list|,
name|tpool
argument_list|,
name|timeoutMillis
argument_list|,
name|wakeFrequency
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getProcedureSignature
parameter_list|()
block|{
return|return
name|FLUSH_TABLE_PROCEDURE_SIGNATURE
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|execProcedure
parameter_list|(
name|ProcedureDescription
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|desc
operator|.
name|getInstance
argument_list|()
argument_list|)
decl_stmt|;
comment|// call pre coproc hook
name|MasterCoprocessorHost
name|cpHost
init|=
name|master
operator|.
name|getMasterCoprocessorHost
argument_list|()
decl_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|cpHost
operator|.
name|preTableFlush
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
comment|// Get the list of region servers that host the online regions for table.
comment|// We use the procedure instance name to carry the table name from the client.
comment|// It is possible that regions may move after we get the region server list.
comment|// Each region server will get its own online regions for the table.
comment|// We may still miss regions that need to be flushed.
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
decl_stmt|;
if|if
condition|(
name|TableName
operator|.
name|META_TABLE_NAME
operator|.
name|equals
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|regionsAndLocations
operator|=
operator|new
name|MetaTableLocator
argument_list|()
operator|.
name|getMetaRegionsAndLocations
argument_list|(
name|master
operator|.
name|getZooKeeper
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|regionsAndLocations
operator|=
name|MetaTableAccessor
operator|.
name|getTableRegionsAndLocations
argument_list|(
name|master
operator|.
name|getConnection
argument_list|()
argument_list|,
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|regionServers
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|regionsAndLocations
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
name|regionsAndLocations
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
name|ForeignExceptionDispatcher
name|monitor
init|=
operator|new
name|ForeignExceptionDispatcher
argument_list|(
name|desc
operator|.
name|getInstance
argument_list|()
argument_list|)
decl_stmt|;
comment|// Kick of the global procedure from the master coordinator to the region servers.
comment|// We rely on the existing Distributed Procedure framework to prevent any concurrent
comment|// procedure with the same name.
name|Procedure
name|proc
init|=
name|coordinator
operator|.
name|startProcedure
argument_list|(
name|monitor
argument_list|,
name|desc
operator|.
name|getInstance
argument_list|()
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|Lists
operator|.
name|newArrayList
argument_list|(
name|regionServers
argument_list|)
argument_list|)
decl_stmt|;
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
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
literal|"Failed to submit distributed procedure "
operator|+
name|desc
operator|.
name|getSignature
argument_list|()
operator|+
literal|" for '"
operator|+
name|desc
operator|.
name|getInstance
argument_list|()
operator|+
literal|"'. "
operator|+
literal|"Another flush procedure is running?"
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
name|IOException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
name|procMap
operator|.
name|put
argument_list|(
name|tableName
argument_list|,
name|proc
argument_list|)
expr_stmt|;
try|try
block|{
comment|// wait for the procedure to complete.  A timer thread is kicked off that should cancel this
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
literal|"Done waiting - exec procedure "
operator|+
name|desc
operator|.
name|getSignature
argument_list|()
operator|+
literal|" for '"
operator|+
name|desc
operator|.
name|getInstance
argument_list|()
operator|+
literal|"'"
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Master flush table procedure is successful!"
argument_list|)
expr_stmt|;
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
literal|"Interrupted while waiting for flush table procdure to finish"
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
name|ForeignException
name|ee
init|=
operator|new
name|ForeignException
argument_list|(
literal|"Exception while waiting for flush table procdure to finish"
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
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|isProcedureDone
parameter_list|(
name|ProcedureDescription
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Procedure instance name is the table name.
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|desc
operator|.
name|getInstance
argument_list|()
argument_list|)
decl_stmt|;
name|Procedure
name|proc
init|=
name|procMap
operator|.
name|get
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|proc
operator|==
literal|null
condition|)
block|{
comment|// The procedure has not even been started yet.
comment|// The client would request the procedure and call isProcedureDone().
comment|// The HBaseAdmin.execProcedure() wraps both request and isProcedureDone().
return|return
literal|false
return|;
block|}
comment|// We reply on the existing Distributed Procedure framework to give us the status.
return|return
name|proc
operator|.
name|isCompleted
argument_list|()
return|;
block|}
block|}
end_class

end_unit

