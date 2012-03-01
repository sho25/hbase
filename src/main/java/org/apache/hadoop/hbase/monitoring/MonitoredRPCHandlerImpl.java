begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|monitoring
package|;
end_package

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
name|client
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
name|io
operator|.
name|WritableWithSize
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
name|io
operator|.
name|Writable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ObjectMapper
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
name|HashMap
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

begin_comment
comment|/**  * A MonitoredTask implementation designed for use with RPC Handlers   * handling frequent, short duration tasks. String concatenations and object   * allocations are avoided in methods that will be hit by every RPC call.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MonitoredRPCHandlerImpl
extends|extends
name|MonitoredTaskImpl
implements|implements
name|MonitoredRPCHandler
block|{
specifier|private
name|String
name|clientAddress
decl_stmt|;
specifier|private
name|int
name|remotePort
decl_stmt|;
specifier|private
name|long
name|rpcQueueTime
decl_stmt|;
specifier|private
name|long
name|rpcStartTime
decl_stmt|;
specifier|private
name|String
name|methodName
init|=
literal|""
decl_stmt|;
specifier|private
name|Object
index|[]
name|params
init|=
block|{}
decl_stmt|;
specifier|private
name|Writable
name|packet
decl_stmt|;
specifier|public
name|MonitoredRPCHandlerImpl
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// in this implementation, WAITING indicates that the handler is not
comment|// actively servicing an RPC call.
name|setState
argument_list|(
name|State
operator|.
name|WAITING
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|MonitoredRPCHandlerImpl
name|clone
parameter_list|()
block|{
return|return
operator|(
name|MonitoredRPCHandlerImpl
operator|)
name|super
operator|.
name|clone
argument_list|()
return|;
block|}
comment|/**    * Gets the status of this handler; if it is currently servicing an RPC,     * this status will include the RPC information.    * @return a String describing the current status.    */
annotation|@
name|Override
specifier|public
name|String
name|getStatus
parameter_list|()
block|{
if|if
condition|(
name|getState
argument_list|()
operator|!=
name|State
operator|.
name|RUNNING
condition|)
block|{
return|return
name|super
operator|.
name|getStatus
argument_list|()
return|;
block|}
return|return
name|super
operator|.
name|getStatus
argument_list|()
operator|+
literal|" from "
operator|+
name|getClient
argument_list|()
operator|+
literal|": "
operator|+
name|getRPC
argument_list|()
return|;
block|}
comment|/**    * Accesses the queue time for the currently running RPC on the     * monitored Handler.    * @return the queue timestamp or -1 if there is no RPC currently running.    */
specifier|public
name|long
name|getRPCQueueTime
parameter_list|()
block|{
if|if
condition|(
name|getState
argument_list|()
operator|!=
name|State
operator|.
name|RUNNING
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
name|rpcQueueTime
return|;
block|}
comment|/**    * Accesses the start time for the currently running RPC on the     * monitored Handler.    * @return the start timestamp or -1 if there is no RPC currently running.    */
specifier|public
name|long
name|getRPCStartTime
parameter_list|()
block|{
if|if
condition|(
name|getState
argument_list|()
operator|!=
name|State
operator|.
name|RUNNING
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
name|rpcStartTime
return|;
block|}
comment|/**    * Produces a string representation of the method currently being serviced    * by this Handler.    * @return a string representing the method call without parameters    */
specifier|public
name|String
name|getRPC
parameter_list|()
block|{
return|return
name|getRPC
argument_list|(
literal|false
argument_list|)
return|;
block|}
comment|/**    * Produces a string representation of the method currently being serviced    * by this Handler.    * @param withParams toggle inclusion of parameters in the RPC String    * @return A human-readable string representation of the method call.    */
specifier|public
specifier|synchronized
name|String
name|getRPC
parameter_list|(
name|boolean
name|withParams
parameter_list|)
block|{
if|if
condition|(
name|getState
argument_list|()
operator|!=
name|State
operator|.
name|RUNNING
condition|)
block|{
comment|// no RPC is currently running
return|return
literal|""
return|;
block|}
name|StringBuilder
name|buffer
init|=
operator|new
name|StringBuilder
argument_list|(
literal|256
argument_list|)
decl_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|methodName
argument_list|)
expr_stmt|;
if|if
condition|(
name|withParams
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|"("
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|params
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|!=
literal|0
condition|)
name|buffer
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|params
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
block|}
return|return
name|buffer
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Produces a string representation of the method currently being serviced    * by this Handler.    * @return A human-readable string representation of the method call.    */
specifier|public
name|long
name|getRPCPacketLength
parameter_list|()
block|{
if|if
condition|(
name|getState
argument_list|()
operator|!=
name|State
operator|.
name|RUNNING
operator|||
name|packet
operator|==
literal|null
condition|)
block|{
comment|// no RPC is currently running, or we don't have an RPC's packet info
return|return
operator|-
literal|1L
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|packet
operator|instanceof
name|WritableWithSize
operator|)
condition|)
block|{
comment|// the packet passed to us doesn't expose size information
return|return
operator|-
literal|1L
return|;
block|}
return|return
operator|(
operator|(
name|WritableWithSize
operator|)
name|packet
operator|)
operator|.
name|getWritableSize
argument_list|()
return|;
block|}
comment|/**    * If an RPC call is currently running, produces a String representation of     * the connection from which it was received.    * @return A human-readable string representation of the address and port     *  of the client.    */
specifier|public
name|String
name|getClient
parameter_list|()
block|{
return|return
name|clientAddress
operator|+
literal|":"
operator|+
name|remotePort
return|;
block|}
comment|/**    * Indicates to the client whether this task is monitoring a currently active     * RPC call.    * @return true if the monitored handler is currently servicing an RPC call.    */
specifier|public
name|boolean
name|isRPCRunning
parameter_list|()
block|{
return|return
name|getState
argument_list|()
operator|==
name|State
operator|.
name|RUNNING
return|;
block|}
comment|/**    * Indicates to the client whether this task is monitoring a currently active     * RPC call to a database command. (as defined by     * o.a.h.h.client.Operation)    * @return true if the monitored handler is currently servicing an RPC call    * to a database command.    */
specifier|public
name|boolean
name|isOperationRunning
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isRPCRunning
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|Object
name|param
range|:
name|params
control|)
block|{
if|if
condition|(
name|param
operator|instanceof
name|Operation
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Tells this instance that it is monitoring a new RPC call.    * @param methodName The name of the method that will be called by the RPC.    * @param params The parameters that will be passed to the indicated method.    */
specifier|public
specifier|synchronized
name|void
name|setRPC
parameter_list|(
name|String
name|methodName
parameter_list|,
name|Object
index|[]
name|params
parameter_list|,
name|long
name|queueTime
parameter_list|)
block|{
name|this
operator|.
name|methodName
operator|=
name|methodName
expr_stmt|;
name|this
operator|.
name|params
operator|=
name|params
expr_stmt|;
name|this
operator|.
name|rpcStartTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|this
operator|.
name|rpcQueueTime
operator|=
name|queueTime
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|State
operator|.
name|RUNNING
expr_stmt|;
block|}
comment|/**    * Gives this instance a reference to the Writable received by the RPC, so     * that it can later compute its size if asked for it.    * @param param The Writable received by the RPC for this call    */
specifier|public
name|void
name|setRPCPacket
parameter_list|(
name|Writable
name|param
parameter_list|)
block|{
name|this
operator|.
name|packet
operator|=
name|param
expr_stmt|;
block|}
comment|/**    * Registers current handler client details.    * @param clientAddress the address of the current client    * @param remotePort the port from which the client connected    */
specifier|public
name|void
name|setConnection
parameter_list|(
name|String
name|clientAddress
parameter_list|,
name|int
name|remotePort
parameter_list|)
block|{
name|this
operator|.
name|clientAddress
operator|=
name|clientAddress
expr_stmt|;
name|this
operator|.
name|remotePort
operator|=
name|remotePort
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|markComplete
parameter_list|(
name|String
name|status
parameter_list|)
block|{
name|super
operator|.
name|markComplete
argument_list|(
name|status
argument_list|)
expr_stmt|;
name|this
operator|.
name|params
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|packet
operator|=
literal|null
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|toMap
parameter_list|()
block|{
comment|// only include RPC info if the Handler is actively servicing an RPC call
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
name|super
operator|.
name|toMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|getState
argument_list|()
operator|!=
name|State
operator|.
name|RUNNING
condition|)
block|{
return|return
name|map
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|rpcJSON
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|ArrayList
name|paramList
init|=
operator|new
name|ArrayList
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"rpcCall"
argument_list|,
name|rpcJSON
argument_list|)
expr_stmt|;
name|rpcJSON
operator|.
name|put
argument_list|(
literal|"queuetimems"
argument_list|,
name|getRPCQueueTime
argument_list|()
argument_list|)
expr_stmt|;
name|rpcJSON
operator|.
name|put
argument_list|(
literal|"starttimems"
argument_list|,
name|getRPCStartTime
argument_list|()
argument_list|)
expr_stmt|;
name|rpcJSON
operator|.
name|put
argument_list|(
literal|"clientaddress"
argument_list|,
name|clientAddress
argument_list|)
expr_stmt|;
name|rpcJSON
operator|.
name|put
argument_list|(
literal|"remoteport"
argument_list|,
name|remotePort
argument_list|)
expr_stmt|;
name|rpcJSON
operator|.
name|put
argument_list|(
literal|"packetlength"
argument_list|,
name|getRPCPacketLength
argument_list|()
argument_list|)
expr_stmt|;
name|rpcJSON
operator|.
name|put
argument_list|(
literal|"method"
argument_list|,
name|methodName
argument_list|)
expr_stmt|;
name|rpcJSON
operator|.
name|put
argument_list|(
literal|"params"
argument_list|,
name|paramList
argument_list|)
expr_stmt|;
for|for
control|(
name|Object
name|param
range|:
name|params
control|)
block|{
if|if
condition|(
name|param
operator|instanceof
name|byte
index|[]
condition|)
block|{
name|paramList
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toStringBinary
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|param
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|param
operator|instanceof
name|Operation
condition|)
block|{
name|paramList
operator|.
name|add
argument_list|(
operator|(
operator|(
name|Operation
operator|)
name|param
operator|)
operator|.
name|toMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|paramList
operator|.
name|add
argument_list|(
name|param
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|map
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|getState
argument_list|()
operator|!=
name|State
operator|.
name|RUNNING
condition|)
block|{
return|return
name|super
operator|.
name|toString
argument_list|()
return|;
block|}
return|return
name|super
operator|.
name|toString
argument_list|()
operator|+
literal|", rpcMethod="
operator|+
name|getRPC
argument_list|()
return|;
block|}
block|}
end_class

end_unit

