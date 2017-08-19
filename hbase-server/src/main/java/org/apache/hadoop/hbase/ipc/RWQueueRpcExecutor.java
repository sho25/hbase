begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**   * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ipc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|BlockingQueue
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
name|HBaseInterfaceAudience
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
name|ClientProtos
operator|.
name|Action
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
name|ClientProtos
operator|.
name|MultiRequest
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
name|ClientProtos
operator|.
name|MutateRequest
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
name|generated
operator|.
name|RegionServerStatusProtos
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
name|ClientProtos
operator|.
name|RegionAction
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
name|ClientProtos
operator|.
name|ScanRequest
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
name|RPCProtos
operator|.
name|RequestHeader
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
name|Message
import|;
end_import

begin_comment
comment|/**  * RPC Executor that uses different queues for reads and writes.  * With the options to use different queues/executors for gets and scans.  * Each handler has its own queue and there is no stealing.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
name|HBaseInterfaceAudience
operator|.
name|COPROC
block|,
name|HBaseInterfaceAudience
operator|.
name|PHOENIX
block|}
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|RWQueueRpcExecutor
extends|extends
name|RpcExecutor
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
name|RWQueueRpcExecutor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CALL_QUEUE_READ_SHARE_CONF_KEY
init|=
literal|"hbase.ipc.server.callqueue.read.ratio"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CALL_QUEUE_SCAN_SHARE_CONF_KEY
init|=
literal|"hbase.ipc.server.callqueue.scan.ratio"
decl_stmt|;
specifier|private
specifier|final
name|QueueBalancer
name|writeBalancer
decl_stmt|;
specifier|private
specifier|final
name|QueueBalancer
name|readBalancer
decl_stmt|;
specifier|private
specifier|final
name|QueueBalancer
name|scanBalancer
decl_stmt|;
specifier|private
specifier|final
name|int
name|writeHandlersCount
decl_stmt|;
specifier|private
specifier|final
name|int
name|readHandlersCount
decl_stmt|;
specifier|private
specifier|final
name|int
name|scanHandlersCount
decl_stmt|;
specifier|private
specifier|final
name|int
name|numWriteQueues
decl_stmt|;
specifier|private
specifier|final
name|int
name|numReadQueues
decl_stmt|;
specifier|private
specifier|final
name|int
name|numScanQueues
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|activeWriteHandlerCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|activeReadHandlerCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicInteger
name|activeScanHandlerCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|public
name|RWQueueRpcExecutor
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
specifier|final
name|int
name|handlerCount
parameter_list|,
specifier|final
name|int
name|maxQueueLength
parameter_list|,
specifier|final
name|PriorityFunction
name|priority
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|Abortable
name|abortable
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|handlerCount
argument_list|,
name|maxQueueLength
argument_list|,
name|priority
argument_list|,
name|conf
argument_list|,
name|abortable
argument_list|)
expr_stmt|;
name|float
name|callqReadShare
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|CALL_QUEUE_READ_SHARE_CONF_KEY
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|float
name|callqScanShare
init|=
name|conf
operator|.
name|getFloat
argument_list|(
name|CALL_QUEUE_SCAN_SHARE_CONF_KEY
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|numWriteQueues
operator|=
name|calcNumWriters
argument_list|(
name|this
operator|.
name|numCallQueues
argument_list|,
name|callqReadShare
argument_list|)
expr_stmt|;
name|writeHandlersCount
operator|=
name|Math
operator|.
name|max
argument_list|(
name|numWriteQueues
argument_list|,
name|calcNumWriters
argument_list|(
name|handlerCount
argument_list|,
name|callqReadShare
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|readQueues
init|=
name|calcNumReaders
argument_list|(
name|this
operator|.
name|numCallQueues
argument_list|,
name|callqReadShare
argument_list|)
decl_stmt|;
name|int
name|readHandlers
init|=
name|Math
operator|.
name|max
argument_list|(
name|readQueues
argument_list|,
name|calcNumReaders
argument_list|(
name|handlerCount
argument_list|,
name|callqReadShare
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|scanQueues
init|=
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
operator|(
name|int
operator|)
name|Math
operator|.
name|floor
argument_list|(
name|readQueues
operator|*
name|callqScanShare
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|scanHandlers
init|=
name|Math
operator|.
name|max
argument_list|(
literal|0
argument_list|,
operator|(
name|int
operator|)
name|Math
operator|.
name|floor
argument_list|(
name|readHandlers
operator|*
name|callqScanShare
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|readQueues
operator|-
name|scanQueues
operator|)
operator|>
literal|0
condition|)
block|{
name|readQueues
operator|-=
name|scanQueues
expr_stmt|;
name|readHandlers
operator|-=
name|scanHandlers
expr_stmt|;
block|}
else|else
block|{
name|scanQueues
operator|=
literal|0
expr_stmt|;
name|scanHandlers
operator|=
literal|0
expr_stmt|;
block|}
name|numReadQueues
operator|=
name|readQueues
expr_stmt|;
name|readHandlersCount
operator|=
name|readHandlers
expr_stmt|;
name|numScanQueues
operator|=
name|scanQueues
expr_stmt|;
name|scanHandlersCount
operator|=
name|scanHandlers
expr_stmt|;
name|this
operator|.
name|writeBalancer
operator|=
name|getBalancer
argument_list|(
name|numWriteQueues
argument_list|)
expr_stmt|;
name|this
operator|.
name|readBalancer
operator|=
name|getBalancer
argument_list|(
name|numReadQueues
argument_list|)
expr_stmt|;
name|this
operator|.
name|scanBalancer
operator|=
name|numScanQueues
operator|>
literal|0
condition|?
name|getBalancer
argument_list|(
name|numScanQueues
argument_list|)
else|:
literal|null
expr_stmt|;
name|initializeQueues
argument_list|(
name|numWriteQueues
argument_list|)
expr_stmt|;
name|initializeQueues
argument_list|(
name|numReadQueues
argument_list|)
expr_stmt|;
name|initializeQueues
argument_list|(
name|numScanQueues
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|getName
argument_list|()
operator|+
literal|" writeQueues="
operator|+
name|numWriteQueues
operator|+
literal|" writeHandlers="
operator|+
name|writeHandlersCount
operator|+
literal|" readQueues="
operator|+
name|numReadQueues
operator|+
literal|" readHandlers="
operator|+
name|readHandlersCount
operator|+
literal|" scanQueues="
operator|+
name|numScanQueues
operator|+
literal|" scanHandlers="
operator|+
name|scanHandlersCount
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|computeNumCallQueues
parameter_list|(
specifier|final
name|int
name|handlerCount
parameter_list|,
specifier|final
name|float
name|callQueuesHandlersFactor
parameter_list|)
block|{
comment|// at least 1 read queue and 1 write queue
return|return
name|Math
operator|.
name|max
argument_list|(
literal|2
argument_list|,
operator|(
name|int
operator|)
name|Math
operator|.
name|round
argument_list|(
name|handlerCount
operator|*
name|callQueuesHandlersFactor
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|startHandlers
parameter_list|(
specifier|final
name|int
name|port
parameter_list|)
block|{
name|startHandlers
argument_list|(
literal|".write"
argument_list|,
name|writeHandlersCount
argument_list|,
name|queues
argument_list|,
literal|0
argument_list|,
name|numWriteQueues
argument_list|,
name|port
argument_list|,
name|activeWriteHandlerCount
argument_list|)
expr_stmt|;
name|startHandlers
argument_list|(
literal|".read"
argument_list|,
name|readHandlersCount
argument_list|,
name|queues
argument_list|,
name|numWriteQueues
argument_list|,
name|numReadQueues
argument_list|,
name|port
argument_list|,
name|activeReadHandlerCount
argument_list|)
expr_stmt|;
if|if
condition|(
name|numScanQueues
operator|>
literal|0
condition|)
block|{
name|startHandlers
argument_list|(
literal|".scan"
argument_list|,
name|scanHandlersCount
argument_list|,
name|queues
argument_list|,
name|numWriteQueues
operator|+
name|numReadQueues
argument_list|,
name|numScanQueues
argument_list|,
name|port
argument_list|,
name|activeScanHandlerCount
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|dispatch
parameter_list|(
specifier|final
name|CallRunner
name|callTask
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|RpcCall
name|call
init|=
name|callTask
operator|.
name|getRpcCall
argument_list|()
decl_stmt|;
name|int
name|queueIndex
decl_stmt|;
if|if
condition|(
name|isWriteRequest
argument_list|(
name|call
operator|.
name|getHeader
argument_list|()
argument_list|,
name|call
operator|.
name|getParam
argument_list|()
argument_list|)
condition|)
block|{
name|queueIndex
operator|=
name|writeBalancer
operator|.
name|getNextQueue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|numScanQueues
operator|>
literal|0
operator|&&
name|isScanRequest
argument_list|(
name|call
operator|.
name|getHeader
argument_list|()
argument_list|,
name|call
operator|.
name|getParam
argument_list|()
argument_list|)
condition|)
block|{
name|queueIndex
operator|=
name|numWriteQueues
operator|+
name|numReadQueues
operator|+
name|scanBalancer
operator|.
name|getNextQueue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|queueIndex
operator|=
name|numWriteQueues
operator|+
name|readBalancer
operator|.
name|getNextQueue
argument_list|()
expr_stmt|;
block|}
name|BlockingQueue
argument_list|<
name|CallRunner
argument_list|>
name|queue
init|=
name|queues
operator|.
name|get
argument_list|(
name|queueIndex
argument_list|)
decl_stmt|;
if|if
condition|(
name|queue
operator|.
name|size
argument_list|()
operator|>=
name|currentQueueLimit
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|queue
operator|.
name|offer
argument_list|(
name|callTask
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getWriteQueueLength
parameter_list|()
block|{
name|int
name|length
init|=
literal|0
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
name|numWriteQueues
condition|;
name|i
operator|++
control|)
block|{
name|length
operator|+=
name|queues
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getReadQueueLength
parameter_list|()
block|{
name|int
name|length
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|numWriteQueues
init|;
name|i
operator|<
operator|(
name|numWriteQueues
operator|+
name|numReadQueues
operator|)
condition|;
name|i
operator|++
control|)
block|{
name|length
operator|+=
name|queues
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getScanQueueLength
parameter_list|()
block|{
name|int
name|length
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|numWriteQueues
operator|+
name|numReadQueues
init|;
name|i
operator|<
operator|(
name|numWriteQueues
operator|+
name|numReadQueues
operator|+
name|numScanQueues
operator|)
condition|;
name|i
operator|++
control|)
block|{
name|length
operator|+=
name|queues
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
return|return
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getActiveHandlerCount
parameter_list|()
block|{
return|return
name|activeWriteHandlerCount
operator|.
name|get
argument_list|()
operator|+
name|activeReadHandlerCount
operator|.
name|get
argument_list|()
operator|+
name|activeScanHandlerCount
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getActiveWriteHandlerCount
parameter_list|()
block|{
return|return
name|activeWriteHandlerCount
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getActiveReadHandlerCount
parameter_list|()
block|{
return|return
name|activeReadHandlerCount
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getActiveScanHandlerCount
parameter_list|()
block|{
return|return
name|activeScanHandlerCount
operator|.
name|get
argument_list|()
return|;
block|}
specifier|private
name|boolean
name|isWriteRequest
parameter_list|(
specifier|final
name|RequestHeader
name|header
parameter_list|,
specifier|final
name|Message
name|param
parameter_list|)
block|{
comment|// TODO: Is there a better way to do this?
if|if
condition|(
name|param
operator|instanceof
name|MultiRequest
condition|)
block|{
name|MultiRequest
name|multi
init|=
operator|(
name|MultiRequest
operator|)
name|param
decl_stmt|;
for|for
control|(
name|RegionAction
name|regionAction
range|:
name|multi
operator|.
name|getRegionActionList
argument_list|()
control|)
block|{
for|for
control|(
name|Action
name|action
range|:
name|regionAction
operator|.
name|getActionList
argument_list|()
control|)
block|{
if|if
condition|(
name|action
operator|.
name|hasMutation
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|param
operator|instanceof
name|MutateRequest
condition|)
block|{
return|return
literal|true
return|;
block|}
comment|// Below here are methods for master. It's a pretty brittle version of this.
comment|// Not sure that master actually needs a read/write queue since 90% of requests to
comment|// master are writing to status or changing the meta table.
comment|// All other read requests are admin generated and can be processed whenever.
comment|// However changing that would require a pretty drastic change and should be done for
comment|// the next major release and not as a fix for HBASE-14239
if|if
condition|(
name|param
operator|instanceof
name|RegionServerStatusProtos
operator|.
name|ReportRegionStateTransitionRequest
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|param
operator|instanceof
name|RegionServerStatusProtos
operator|.
name|RegionServerStartupRequest
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|param
operator|instanceof
name|RegionServerStatusProtos
operator|.
name|RegionServerReportRequest
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
specifier|private
name|boolean
name|isScanRequest
parameter_list|(
specifier|final
name|RequestHeader
name|header
parameter_list|,
specifier|final
name|Message
name|param
parameter_list|)
block|{
if|if
condition|(
name|param
operator|instanceof
name|ScanRequest
condition|)
block|{
comment|// The first scan request will be executed as a "short read"
name|ScanRequest
name|request
init|=
operator|(
name|ScanRequest
operator|)
name|param
decl_stmt|;
return|return
name|request
operator|.
name|hasScannerId
argument_list|()
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/*    * Calculate the number of writers based on the "total count" and the read share.    * You'll get at least one writer.    */
specifier|private
specifier|static
name|int
name|calcNumWriters
parameter_list|(
specifier|final
name|int
name|count
parameter_list|,
specifier|final
name|float
name|readShare
parameter_list|)
block|{
return|return
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|count
operator|-
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
operator|(
name|int
operator|)
name|Math
operator|.
name|round
argument_list|(
name|count
operator|*
name|readShare
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/*    * Calculate the number of readers based on the "total count" and the read share.    * You'll get at least one reader.    */
specifier|private
specifier|static
name|int
name|calcNumReaders
parameter_list|(
specifier|final
name|int
name|count
parameter_list|,
specifier|final
name|float
name|readShare
parameter_list|)
block|{
return|return
name|count
operator|-
name|calcNumWriters
argument_list|(
name|count
argument_list|,
name|readShare
argument_list|)
return|;
block|}
block|}
end_class

end_unit

