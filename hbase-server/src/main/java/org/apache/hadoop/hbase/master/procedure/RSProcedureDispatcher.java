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
name|procedure
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
name|lang
operator|.
name|Thread
operator|.
name|UncaughtExceptionHandler
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
name|TimeUnit
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
name|CallQueueTooBigException
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
name|DoNotRetryIOException
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
name|RegionInfo
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
name|ServerListener
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
name|procedure2
operator|.
name|RemoteProcedureDispatcher
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
name|RegionServerAbortedException
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
name|RegionServerStoppedException
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
name|EnvironmentEdgeManager
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ArrayListMultimap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ServiceException
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
name|shaded
operator|.
name|protobuf
operator|.
name|RequestConverter
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
name|AdminProtos
operator|.
name|AdminService
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
name|AdminProtos
operator|.
name|CloseRegionRequest
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
name|AdminProtos
operator|.
name|ExecuteProceduresRequest
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
name|AdminProtos
operator|.
name|ExecuteProceduresResponse
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
name|AdminProtos
operator|.
name|OpenRegionRequest
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
name|AdminProtos
operator|.
name|RemoteProcedureRequest
import|;
end_import

begin_comment
comment|/**  * A remote procecdure dispatcher for regionservers.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RSProcedureDispatcher
extends|extends
name|RemoteProcedureDispatcher
argument_list|<
name|MasterProcedureEnv
argument_list|,
name|ServerName
argument_list|>
implements|implements
name|ServerListener
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
name|RSProcedureDispatcher
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|RS_RPC_STARTUP_WAIT_TIME_CONF_KEY
init|=
literal|"hbase.regionserver.rpc.startup.waittime"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_RS_RPC_STARTUP_WAIT_TIME
init|=
literal|60000
decl_stmt|;
specifier|protected
specifier|final
name|MasterServices
name|master
decl_stmt|;
specifier|private
specifier|final
name|long
name|rsStartupWaitTime
decl_stmt|;
specifier|private
name|MasterProcedureEnv
name|procedureEnv
decl_stmt|;
specifier|public
name|RSProcedureDispatcher
parameter_list|(
specifier|final
name|MasterServices
name|master
parameter_list|)
block|{
name|super
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|this
operator|.
name|rsStartupWaitTime
operator|=
name|master
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getLong
argument_list|(
name|RS_RPC_STARTUP_WAIT_TIME_CONF_KEY
argument_list|,
name|DEFAULT_RS_RPC_STARTUP_WAIT_TIME
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|UncaughtExceptionHandler
name|getUncaughtExceptionHandler
parameter_list|()
block|{
return|return
operator|new
name|UncaughtExceptionHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|uncaughtException
parameter_list|(
name|Thread
name|t
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unexpected error caught, this may cause the procedure to hang forever"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|start
parameter_list|()
block|{
if|if
condition|(
operator|!
name|super
operator|.
name|start
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|registerListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|procedureEnv
operator|=
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
expr_stmt|;
for|for
control|(
name|ServerName
name|serverName
range|:
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
control|)
block|{
name|addNode
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|stop
parameter_list|()
block|{
if|if
condition|(
operator|!
name|super
operator|.
name|stop
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|unregisterListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|remoteDispatch
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|Set
argument_list|<
name|RemoteProcedure
argument_list|>
name|remoteProcedures
parameter_list|)
block|{
if|if
condition|(
operator|!
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|isServerOnline
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
comment|// fail fast
name|submitTask
argument_list|(
operator|new
name|DeadRSRemoteCall
argument_list|(
name|serverName
argument_list|,
name|remoteProcedures
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|submitTask
argument_list|(
operator|new
name|ExecuteProceduresRemoteCall
argument_list|(
name|serverName
argument_list|,
name|remoteProcedures
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|abortPendingOperations
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|Set
argument_list|<
name|RemoteProcedure
argument_list|>
name|operations
parameter_list|)
block|{
comment|// TODO: Replace with a ServerNotOnlineException()
specifier|final
name|IOException
name|e
init|=
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"server not online "
operator|+
name|serverName
argument_list|)
decl_stmt|;
for|for
control|(
name|RemoteProcedure
name|proc
range|:
name|operations
control|)
block|{
name|proc
operator|.
name|remoteCallFailed
argument_list|(
name|procedureEnv
argument_list|,
name|serverName
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|serverAdded
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
name|addNode
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|serverRemoved
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
name|removeNode
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
block|}
specifier|private
interface|interface
name|RemoteProcedureResolver
block|{
name|void
name|dispatchOpenRequests
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|List
argument_list|<
name|RegionOpenOperation
argument_list|>
name|operations
parameter_list|)
function_decl|;
name|void
name|dispatchCloseRequests
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|List
argument_list|<
name|RegionCloseOperation
argument_list|>
name|operations
parameter_list|)
function_decl|;
name|void
name|dispatchServerOperations
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|List
argument_list|<
name|ServerOperation
argument_list|>
name|operations
parameter_list|)
function_decl|;
block|}
comment|/**    * Fetches {@link org.apache.hadoop.hbase.procedure2.RemoteProcedureDispatcher.RemoteOperation}s    * from the given {@code remoteProcedures} and groups them by class of the returned operation.    * Then {@code resolver} is used to dispatch {@link RegionOpenOperation}s and    * {@link RegionCloseOperation}s.    * @param serverName RegionServer to which the remote operations are sent    * @param operations Remote procedures which are dispatched to the given server    * @param resolver Used to dispatch remote procedures to given server.    */
specifier|public
name|void
name|splitAndResolveOperation
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|Set
argument_list|<
name|RemoteProcedure
argument_list|>
name|operations
parameter_list|,
name|RemoteProcedureResolver
name|resolver
parameter_list|)
block|{
name|MasterProcedureEnv
name|env
init|=
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
decl_stmt|;
name|ArrayListMultimap
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|,
name|RemoteOperation
argument_list|>
name|reqsByType
init|=
name|buildAndGroupRequestByType
argument_list|(
name|env
argument_list|,
name|serverName
argument_list|,
name|operations
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|RegionOpenOperation
argument_list|>
name|openOps
init|=
name|fetchType
argument_list|(
name|reqsByType
argument_list|,
name|RegionOpenOperation
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|openOps
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|resolver
operator|.
name|dispatchOpenRequests
argument_list|(
name|env
argument_list|,
name|openOps
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|RegionCloseOperation
argument_list|>
name|closeOps
init|=
name|fetchType
argument_list|(
name|reqsByType
argument_list|,
name|RegionCloseOperation
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|closeOps
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|resolver
operator|.
name|dispatchCloseRequests
argument_list|(
name|env
argument_list|,
name|closeOps
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|ServerOperation
argument_list|>
name|refreshOps
init|=
name|fetchType
argument_list|(
name|reqsByType
argument_list|,
name|ServerOperation
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|refreshOps
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|resolver
operator|.
name|dispatchServerOperations
argument_list|(
name|env
argument_list|,
name|refreshOps
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|reqsByType
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"unknown request type in the queue: "
operator|+
name|reqsByType
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|DeadRSRemoteCall
extends|extends
name|ExecuteProceduresRemoteCall
block|{
specifier|public
name|DeadRSRemoteCall
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|Set
argument_list|<
name|RemoteProcedure
argument_list|>
name|remoteProcedures
parameter_list|)
block|{
name|super
argument_list|(
name|serverName
argument_list|,
name|remoteProcedures
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
name|remoteCallFailed
argument_list|(
name|procedureEnv
argument_list|,
operator|new
name|RegionServerStoppedException
argument_list|(
literal|"Server "
operator|+
name|getServerName
argument_list|()
operator|+
literal|" is not online"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// ==========================================================================
comment|//  Compatibility calls
comment|// ==========================================================================
specifier|protected
class|class
name|ExecuteProceduresRemoteCall
implements|implements
name|RemoteProcedureResolver
implements|,
name|Runnable
block|{
specifier|private
specifier|final
name|ServerName
name|serverName
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|RemoteProcedure
argument_list|>
name|remoteProcedures
decl_stmt|;
specifier|private
name|int
name|numberOfAttemptsSoFar
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|maxWaitTime
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|ExecuteProceduresRequest
operator|.
name|Builder
name|request
init|=
literal|null
decl_stmt|;
specifier|public
name|ExecuteProceduresRemoteCall
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|Set
argument_list|<
name|RemoteProcedure
argument_list|>
name|remoteProcedures
parameter_list|)
block|{
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|remoteProcedures
operator|=
name|remoteProcedures
expr_stmt|;
block|}
specifier|private
name|AdminService
operator|.
name|BlockingInterface
name|getRsAdmin
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|AdminService
operator|.
name|BlockingInterface
name|admin
init|=
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getRsAdmin
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
if|if
condition|(
name|admin
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Attempting to send OPEN RPC to server "
operator|+
name|getServerName
argument_list|()
operator|+
literal|" failed because no RPC connection found to this server"
argument_list|)
throw|;
block|}
return|return
name|admin
return|;
block|}
specifier|protected
specifier|final
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|serverName
return|;
block|}
specifier|private
name|boolean
name|scheduleForRetry
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"request to {} failed, try={}"
argument_list|,
name|serverName
argument_list|,
name|numberOfAttemptsSoFar
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// Should we wait a little before retrying? If the server is starting it's yes.
if|if
condition|(
name|e
operator|instanceof
name|ServerNotRunningYetException
condition|)
block|{
name|long
name|remainingTime
init|=
name|getMaxWaitTime
argument_list|()
operator|-
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
if|if
condition|(
name|remainingTime
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"waiting a little before trying on the same server={},"
operator|+
literal|" try={}, can wait up to {}ms"
argument_list|,
name|serverName
argument_list|,
name|numberOfAttemptsSoFar
argument_list|,
name|remainingTime
argument_list|)
expr_stmt|;
name|numberOfAttemptsSoFar
operator|++
expr_stmt|;
name|submitTask
argument_list|(
name|this
argument_list|,
literal|100
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
name|LOG
operator|.
name|warn
argument_list|(
literal|"server {} is not up for a while; try a new one"
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|e
operator|instanceof
name|DoNotRetryIOException
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"server {} tells us do not retry due to {}, try={}, give up"
argument_list|,
name|serverName
argument_list|,
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|numberOfAttemptsSoFar
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// this exception is thrown in the rpc framework, where we can make sure that the call has not
comment|// been executed yet, so it is safe to mark it as fail. Especially for open a region, we'd
comment|// better choose another region server
comment|// notice that, it is safe to quit only if this is the first time we send request to region
comment|// server. Maybe the region server has accept our request the first time, and then there is a
comment|// network error which prevents we receive the response, and the second time we hit a
comment|// CallQueueTooBigException, obviously it is not safe to quit here, otherwise it may lead to a
comment|// double assign...
if|if
condition|(
name|e
operator|instanceof
name|CallQueueTooBigException
operator|&&
name|numberOfAttemptsSoFar
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"request to {} failed due to {}, try={}, this usually because"
operator|+
literal|" server is overloaded, give up"
argument_list|,
name|serverName
argument_list|,
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|numberOfAttemptsSoFar
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
comment|// Always retry for other exception types if the region server is not dead yet.
if|if
condition|(
operator|!
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|isServerOnline
argument_list|(
name|serverName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"request to {} failed due to {}, try={}, and the server is dead, give up"
argument_list|,
name|serverName
argument_list|,
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|numberOfAttemptsSoFar
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|e
operator|instanceof
name|RegionServerAbortedException
operator|||
name|e
operator|instanceof
name|RegionServerStoppedException
condition|)
block|{
comment|// A better way is to return true here to let the upper layer quit, and then schedule a
comment|// background task to check whether the region server is dead. And if it is dead, call
comment|// remoteCallFailed to tell the upper layer. Keep retrying here does not lead to incorrect
comment|// result, but waste some resources.
name|LOG
operator|.
name|warn
argument_list|(
literal|"server {} is aborted or stopped, for safety we still need to"
operator|+
literal|" wait until it is fully dead, try={}"
argument_list|,
name|serverName
argument_list|,
name|numberOfAttemptsSoFar
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"request to server {} failed due to {}, try={}, retrying..."
argument_list|,
name|serverName
argument_list|,
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|numberOfAttemptsSoFar
argument_list|)
expr_stmt|;
block|}
name|numberOfAttemptsSoFar
operator|++
expr_stmt|;
name|submitTask
argument_list|(
name|this
argument_list|,
literal|100
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|private
name|long
name|getMaxWaitTime
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|maxWaitTime
operator|<
literal|0
condition|)
block|{
comment|// This is the max attempts, not retries, so it should be at least 1.
name|this
operator|.
name|maxWaitTime
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
operator|+
name|rsStartupWaitTime
expr_stmt|;
block|}
return|return
name|this
operator|.
name|maxWaitTime
return|;
block|}
specifier|private
name|IOException
name|unwrapException
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|RemoteException
condition|)
block|{
name|e
operator|=
operator|(
operator|(
name|RemoteException
operator|)
name|e
operator|)
operator|.
name|unwrapRemoteException
argument_list|()
expr_stmt|;
block|}
return|return
name|e
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|request
operator|=
name|ExecuteProceduresRequest
operator|.
name|newBuilder
argument_list|()
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Building request with operations count="
operator|+
name|remoteProcedures
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|splitAndResolveOperation
argument_list|(
name|getServerName
argument_list|()
argument_list|,
name|remoteProcedures
argument_list|,
name|this
argument_list|)
expr_stmt|;
try|try
block|{
name|sendRequest
argument_list|(
name|getServerName
argument_list|()
argument_list|,
name|request
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|=
name|unwrapException
argument_list|(
name|e
argument_list|)
expr_stmt|;
comment|// TODO: In the future some operation may want to bail out early.
comment|// TODO: How many times should we retry (use numberOfAttemptsSoFar)
if|if
condition|(
operator|!
name|scheduleForRetry
argument_list|(
name|e
argument_list|)
condition|)
block|{
name|remoteCallFailed
argument_list|(
name|procedureEnv
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|dispatchOpenRequests
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|List
argument_list|<
name|RegionOpenOperation
argument_list|>
name|operations
parameter_list|)
block|{
name|request
operator|.
name|addOpenRegion
argument_list|(
name|buildOpenRegionRequest
argument_list|(
name|env
argument_list|,
name|getServerName
argument_list|()
argument_list|,
name|operations
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|dispatchCloseRequests
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|List
argument_list|<
name|RegionCloseOperation
argument_list|>
name|operations
parameter_list|)
block|{
for|for
control|(
name|RegionCloseOperation
name|op
range|:
name|operations
control|)
block|{
name|request
operator|.
name|addCloseRegion
argument_list|(
name|op
operator|.
name|buildCloseRegionRequest
argument_list|(
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|dispatchServerOperations
parameter_list|(
name|MasterProcedureEnv
name|env
parameter_list|,
name|List
argument_list|<
name|ServerOperation
argument_list|>
name|operations
parameter_list|)
block|{
name|operations
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|o
lambda|->
name|o
operator|.
name|buildRequest
argument_list|()
argument_list|)
operator|.
name|forEachOrdered
argument_list|(
name|request
operator|::
name|addProc
argument_list|)
expr_stmt|;
block|}
comment|// will be overridden in test.
annotation|@
name|VisibleForTesting
specifier|protected
name|ExecuteProceduresResponse
name|sendRequest
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|ExecuteProceduresRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|getRsAdmin
argument_list|()
operator|.
name|executeProcedures
argument_list|(
literal|null
argument_list|,
name|request
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ServiceException
name|se
parameter_list|)
block|{
throw|throw
name|ProtobufUtil
operator|.
name|getRemoteException
argument_list|(
name|se
argument_list|)
throw|;
block|}
block|}
specifier|protected
specifier|final
name|void
name|remoteCallFailed
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|IOException
name|e
parameter_list|)
block|{
for|for
control|(
name|RemoteProcedure
name|proc
range|:
name|remoteProcedures
control|)
block|{
name|proc
operator|.
name|remoteCallFailed
argument_list|(
name|env
argument_list|,
name|getServerName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|OpenRegionRequest
name|buildOpenRegionRequest
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|,
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|List
argument_list|<
name|RegionOpenOperation
argument_list|>
name|operations
parameter_list|)
block|{
specifier|final
name|OpenRegionRequest
operator|.
name|Builder
name|builder
init|=
name|OpenRegionRequest
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setServerStartCode
argument_list|(
name|serverName
operator|.
name|getStartcode
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setMasterSystemTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionOpenOperation
name|op
range|:
name|operations
control|)
block|{
name|builder
operator|.
name|addOpenInfo
argument_list|(
name|op
operator|.
name|buildRegionOpenInfoRequest
argument_list|(
name|env
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|// ==========================================================================
comment|//  RPC Messages
comment|//  - ServerOperation: refreshConfig, grant, revoke, ... (TODO)
comment|//  - RegionOperation: open, close, flush, snapshot, ...
comment|// ==========================================================================
specifier|public
specifier|static
specifier|final
class|class
name|ServerOperation
extends|extends
name|RemoteOperation
block|{
specifier|private
specifier|final
name|long
name|procId
decl_stmt|;
specifier|private
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|rsProcClass
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|rsProcData
decl_stmt|;
specifier|public
name|ServerOperation
parameter_list|(
name|RemoteProcedure
name|remoteProcedure
parameter_list|,
name|long
name|procId
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|rsProcClass
parameter_list|,
name|byte
index|[]
name|rsProcData
parameter_list|)
block|{
name|super
argument_list|(
name|remoteProcedure
argument_list|)
expr_stmt|;
name|this
operator|.
name|procId
operator|=
name|procId
expr_stmt|;
name|this
operator|.
name|rsProcClass
operator|=
name|rsProcClass
expr_stmt|;
name|this
operator|.
name|rsProcData
operator|=
name|rsProcData
expr_stmt|;
block|}
specifier|public
name|RemoteProcedureRequest
name|buildRequest
parameter_list|()
block|{
return|return
name|RemoteProcedureRequest
operator|.
name|newBuilder
argument_list|()
operator|.
name|setProcId
argument_list|(
name|procId
argument_list|)
operator|.
name|setProcClass
argument_list|(
name|rsProcClass
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|setProcData
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|rsProcData
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
specifier|public
specifier|static
specifier|abstract
class|class
name|RegionOperation
extends|extends
name|RemoteOperation
block|{
specifier|protected
specifier|final
name|RegionInfo
name|regionInfo
decl_stmt|;
specifier|protected
specifier|final
name|long
name|procId
decl_stmt|;
specifier|protected
name|RegionOperation
parameter_list|(
name|RemoteProcedure
name|remoteProcedure
parameter_list|,
name|RegionInfo
name|regionInfo
parameter_list|,
name|long
name|procId
parameter_list|)
block|{
name|super
argument_list|(
name|remoteProcedure
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
name|this
operator|.
name|procId
operator|=
name|procId
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
class|class
name|RegionOpenOperation
extends|extends
name|RegionOperation
block|{
specifier|public
name|RegionOpenOperation
parameter_list|(
name|RemoteProcedure
name|remoteProcedure
parameter_list|,
name|RegionInfo
name|regionInfo
parameter_list|,
name|long
name|procId
parameter_list|)
block|{
name|super
argument_list|(
name|remoteProcedure
argument_list|,
name|regionInfo
argument_list|,
name|procId
argument_list|)
expr_stmt|;
block|}
specifier|public
name|OpenRegionRequest
operator|.
name|RegionOpenInfo
name|buildRegionOpenInfoRequest
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
block|{
return|return
name|RequestConverter
operator|.
name|buildRegionOpenInfo
argument_list|(
name|regionInfo
argument_list|,
name|env
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getFavoredNodes
argument_list|(
name|regionInfo
argument_list|)
argument_list|,
name|procId
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
class|class
name|RegionCloseOperation
extends|extends
name|RegionOperation
block|{
specifier|private
specifier|final
name|ServerName
name|destinationServer
decl_stmt|;
specifier|public
name|RegionCloseOperation
parameter_list|(
name|RemoteProcedure
name|remoteProcedure
parameter_list|,
name|RegionInfo
name|regionInfo
parameter_list|,
name|long
name|procId
parameter_list|,
name|ServerName
name|destinationServer
parameter_list|)
block|{
name|super
argument_list|(
name|remoteProcedure
argument_list|,
name|regionInfo
argument_list|,
name|procId
argument_list|)
expr_stmt|;
name|this
operator|.
name|destinationServer
operator|=
name|destinationServer
expr_stmt|;
block|}
specifier|public
name|ServerName
name|getDestinationServer
parameter_list|()
block|{
return|return
name|destinationServer
return|;
block|}
specifier|public
name|CloseRegionRequest
name|buildCloseRegionRequest
parameter_list|(
specifier|final
name|ServerName
name|serverName
parameter_list|)
block|{
return|return
name|ProtobufUtil
operator|.
name|buildCloseRegionRequest
argument_list|(
name|serverName
argument_list|,
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|getDestinationServer
argument_list|()
argument_list|,
name|procId
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

