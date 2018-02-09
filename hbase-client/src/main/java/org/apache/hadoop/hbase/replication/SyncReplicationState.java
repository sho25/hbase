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
name|replication
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|replication
operator|.
name|ReplicationPeerConfigUtil
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
name|InvalidProtocolBufferException
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
name|ReplicationProtos
import|;
end_import

begin_comment
comment|/**  * Used by synchronous replication. Indicate the state of the current cluster in a synchronous  * replication peer. The state may be one of {@link SyncReplicationState#ACTIVE},  * {@link SyncReplicationState#DOWNGRADE_ACTIVE} or {@link SyncReplicationState#STANDBY}.  *<p>  * For asynchronous replication, the state is {@link SyncReplicationState#NONE}.  */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
enum|enum
name|SyncReplicationState
block|{
name|NONE
argument_list|(
literal|0
argument_list|)
block|,
name|ACTIVE
argument_list|(
literal|1
argument_list|)
block|,
name|DOWNGRADE_ACTIVE
argument_list|(
literal|2
argument_list|)
block|,
name|STANDBY
argument_list|(
literal|3
argument_list|)
block|;
specifier|private
specifier|final
name|byte
name|value
decl_stmt|;
specifier|private
name|SyncReplicationState
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
operator|(
name|byte
operator|)
name|value
expr_stmt|;
block|}
specifier|public
specifier|static
name|SyncReplicationState
name|valueOf
parameter_list|(
name|int
name|value
parameter_list|)
block|{
switch|switch
condition|(
name|value
condition|)
block|{
case|case
literal|0
case|:
return|return
name|NONE
return|;
case|case
literal|1
case|:
return|return
name|ACTIVE
return|;
case|case
literal|2
case|:
return|return
name|DOWNGRADE_ACTIVE
return|;
case|case
literal|3
case|:
return|return
name|STANDBY
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown synchronous replication state "
operator|+
name|value
argument_list|)
throw|;
block|}
block|}
specifier|public
name|int
name|value
parameter_list|()
block|{
return|return
name|value
operator|&
literal|0xFF
return|;
block|}
specifier|public
specifier|static
name|byte
index|[]
name|toByteArray
parameter_list|(
name|SyncReplicationState
name|state
parameter_list|)
block|{
return|return
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|ReplicationPeerConfigUtil
operator|.
name|toSyncReplicationState
argument_list|(
name|state
argument_list|)
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|SyncReplicationState
name|parseFrom
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
throws|throws
name|InvalidProtocolBufferException
block|{
return|return
name|ReplicationPeerConfigUtil
operator|.
name|toSyncReplicationState
argument_list|(
name|ReplicationProtos
operator|.
name|SyncReplicationState
operator|.
name|parseFrom
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|bytes
argument_list|,
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
argument_list|,
name|bytes
operator|.
name|length
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
end_enum

end_unit

