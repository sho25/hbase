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
name|concurrent
operator|.
name|LinkedBlockingQueue
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
name|PleaseHoldException
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
name|ConnectionUtils
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
name|ipc
operator|.
name|ServerNotRunningYetException
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
name|ForeignExceptionUtil
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|TextFormat
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
name|RegionServerStatusProtos
operator|.
name|RemoteProcedureResult
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
name|RegionServerStatusProtos
operator|.
name|ReportProcedureDoneRequest
import|;
end_import

begin_comment
comment|/**  * A thread which calls {@code reportProcedureDone} to tell master the result of a remote procedure.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|RemoteProcedureResultReporter
extends|extends
name|Thread
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
name|RemoteProcedureResultReporter
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Time to pause if master says 'please hold'. Make configurable if needed.
specifier|private
specifier|static
specifier|final
name|int
name|INIT_PAUSE_TIME_MS
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_BATCH
init|=
literal|100
decl_stmt|;
specifier|private
specifier|final
name|HRegionServer
name|server
decl_stmt|;
specifier|private
specifier|final
name|LinkedBlockingQueue
argument_list|<
name|RemoteProcedureResult
argument_list|>
name|results
init|=
operator|new
name|LinkedBlockingQueue
argument_list|<>
argument_list|()
decl_stmt|;
specifier|public
name|RemoteProcedureResultReporter
parameter_list|(
name|HRegionServer
name|server
parameter_list|)
block|{
name|this
operator|.
name|server
operator|=
name|server
expr_stmt|;
block|}
specifier|public
name|void
name|complete
parameter_list|(
name|long
name|procId
parameter_list|,
name|Throwable
name|error
parameter_list|)
block|{
name|RemoteProcedureResult
operator|.
name|Builder
name|builder
init|=
name|RemoteProcedureResult
operator|.
name|newBuilder
argument_list|()
operator|.
name|setProcId
argument_list|(
name|procId
argument_list|)
decl_stmt|;
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setStatus
argument_list|(
name|RemoteProcedureResult
operator|.
name|Status
operator|.
name|ERROR
argument_list|)
operator|.
name|setError
argument_list|(
name|ForeignExceptionUtil
operator|.
name|toProtoForeignException
argument_list|(
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|error
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|setStatus
argument_list|(
name|RemoteProcedureResult
operator|.
name|Status
operator|.
name|SUCCESS
argument_list|)
expr_stmt|;
block|}
name|results
operator|.
name|add
argument_list|(
name|builder
operator|.
name|build
argument_list|()
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
name|ReportProcedureDoneRequest
operator|.
name|Builder
name|builder
init|=
name|ReportProcedureDoneRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|int
name|tries
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|!
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
if|if
condition|(
name|builder
operator|.
name|getResultCount
argument_list|()
operator|==
literal|0
condition|)
block|{
try|try
block|{
name|builder
operator|.
name|addResult
argument_list|(
name|results
operator|.
name|take
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
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
continue|continue;
block|}
block|}
while|while
condition|(
name|builder
operator|.
name|getResultCount
argument_list|()
operator|<
name|MAX_BATCH
condition|)
block|{
name|RemoteProcedureResult
name|result
init|=
name|results
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|builder
operator|.
name|addResult
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
name|ReportProcedureDoneRequest
name|request
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|server
operator|.
name|reportProcedureDone
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|clear
argument_list|()
expr_stmt|;
name|tries
operator|=
literal|0
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|boolean
name|pause
init|=
name|e
operator|instanceof
name|ServerNotRunningYetException
operator|||
name|e
operator|instanceof
name|PleaseHoldException
decl_stmt|;
name|long
name|pauseTime
decl_stmt|;
if|if
condition|(
name|pause
condition|)
block|{
comment|// Do backoff else we flood the Master with requests.
name|pauseTime
operator|=
name|ConnectionUtils
operator|.
name|getPauseTime
argument_list|(
name|INIT_PAUSE_TIME_MS
argument_list|,
name|tries
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|pauseTime
operator|=
name|INIT_PAUSE_TIME_MS
expr_stmt|;
comment|// Reset.
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Failed report procedure "
operator|+
name|TextFormat
operator|.
name|shortDebugString
argument_list|(
name|request
argument_list|)
operator|+
literal|"; retry (#"
operator|+
name|tries
operator|+
literal|")"
operator|+
operator|(
name|pause
condition|?
literal|" after "
operator|+
name|pauseTime
operator|+
literal|"ms delay (Master is coming online...)."
else|:
literal|" immediately."
operator|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
name|pauseTime
argument_list|)
expr_stmt|;
name|tries
operator|++
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

