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
name|executor
operator|.
name|EventHandler
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
name|EventHandler
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
name|protobuf
operator|.
name|generated
operator|.
name|ZooKeeperProtos
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
name|EnvironmentEdgeManager
import|;
end_import

begin_import
import|import
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
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * Current state of a region in transition.  Holds state of a region as it moves through the  * steps that take it from offline to open, etc.  Used by regionserver, master, and zk packages.  * Encapsulates protobuf serialization/deserialization so we don't leak generated pb outside this  * class.  Create an instance using {@link #createRegionTransition(EventType, byte[], ServerName)}.  *<p>Immutable  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionTransition
block|{
specifier|private
specifier|final
name|ZooKeeperProtos
operator|.
name|RegionTransition
name|rt
decl_stmt|;
comment|/**    * Shutdown constructor    */
specifier|private
name|RegionTransition
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|RegionTransition
parameter_list|(
specifier|final
name|ZooKeeperProtos
operator|.
name|RegionTransition
name|rt
parameter_list|)
block|{
name|this
operator|.
name|rt
operator|=
name|rt
expr_stmt|;
block|}
specifier|public
name|EventHandler
operator|.
name|EventType
name|getEventType
parameter_list|()
block|{
return|return
name|EventHandler
operator|.
name|EventType
operator|.
name|get
argument_list|(
name|this
operator|.
name|rt
operator|.
name|getEventTypeCode
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|this
operator|.
name|rt
operator|.
name|getOriginServerName
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|long
name|getCreateTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|rt
operator|.
name|getCreateTime
argument_list|()
return|;
block|}
comment|/**    * @return Full region name    */
specifier|public
name|byte
index|[]
name|getRegionName
parameter_list|()
block|{
return|return
name|this
operator|.
name|rt
operator|.
name|getRegionName
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
specifier|public
name|byte
index|[]
name|getPayload
parameter_list|()
block|{
return|return
name|this
operator|.
name|rt
operator|.
name|getPayload
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|byte
index|[]
name|payload
init|=
name|getPayload
argument_list|()
decl_stmt|;
return|return
literal|"region="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|getRegionName
argument_list|()
argument_list|)
operator|+
literal|", state="
operator|+
name|getEventType
argument_list|()
operator|+
literal|", servername="
operator|+
name|getServerName
argument_list|()
operator|+
literal|", createTime="
operator|+
name|this
operator|.
name|getCreateTime
argument_list|()
operator|+
literal|", payload.length="
operator|+
operator|(
name|payload
operator|==
literal|null
condition|?
literal|0
else|:
name|payload
operator|.
name|length
operator|)
return|;
block|}
comment|/**    * @param type    * @param regionName    * @param sn    * @return a serialized pb {@link RegionTransition}    * @see #parseRegionTransition(byte[])    */
specifier|public
specifier|static
name|RegionTransition
name|createRegionTransition
parameter_list|(
specifier|final
name|EventType
name|type
parameter_list|,
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|ServerName
name|sn
parameter_list|)
block|{
return|return
name|createRegionTransition
argument_list|(
name|type
argument_list|,
name|regionName
argument_list|,
name|sn
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * @param type    * @param regionName    * @param sn    * @param payload May be null    * @return a serialized pb {@link RegionTransition}    * @see #parseRegionTransition(byte[])    */
specifier|public
specifier|static
name|RegionTransition
name|createRegionTransition
parameter_list|(
specifier|final
name|EventType
name|type
parameter_list|,
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|ServerName
name|sn
parameter_list|,
specifier|final
name|byte
index|[]
name|payload
parameter_list|)
block|{
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
name|ServerName
name|pbsn
init|=
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
name|ServerName
operator|.
name|newBuilder
argument_list|()
operator|.
name|setHostName
argument_list|(
name|sn
operator|.
name|getHostname
argument_list|()
argument_list|)
operator|.
name|setPort
argument_list|(
name|sn
operator|.
name|getPort
argument_list|()
argument_list|)
operator|.
name|setStartCode
argument_list|(
name|sn
operator|.
name|getStartcode
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ZooKeeperProtos
operator|.
name|RegionTransition
operator|.
name|Builder
name|builder
init|=
name|ZooKeeperProtos
operator|.
name|RegionTransition
operator|.
name|newBuilder
argument_list|()
operator|.
name|setEventTypeCode
argument_list|(
name|type
operator|.
name|getCode
argument_list|()
argument_list|)
operator|.
name|setRegionName
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|regionName
argument_list|)
argument_list|)
operator|.
name|setOriginServerName
argument_list|(
name|pbsn
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setCreateTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|payload
operator|!=
literal|null
condition|)
name|builder
operator|.
name|setPayload
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|payload
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|RegionTransition
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param data Serialized date to parse.    * @return A RegionTransition instance made of the passed<code>data</code>    * @throws DeserializationException     * @see #toByteArray()    */
specifier|public
specifier|static
name|RegionTransition
name|parseFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|DeserializationException
block|{
name|ProtobufUtil
operator|.
name|expectPBMagicPrefix
argument_list|(
name|data
argument_list|)
expr_stmt|;
try|try
block|{
name|int
name|prefixLen
init|=
name|ProtobufUtil
operator|.
name|lengthOfPBMagic
argument_list|()
decl_stmt|;
name|ZooKeeperProtos
operator|.
name|RegionTransition
name|rt
init|=
name|ZooKeeperProtos
operator|.
name|RegionTransition
operator|.
name|newBuilder
argument_list|()
operator|.
name|mergeFrom
argument_list|(
name|data
argument_list|,
name|prefixLen
argument_list|,
name|data
operator|.
name|length
operator|-
name|prefixLen
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
operator|new
name|RegionTransition
argument_list|(
name|rt
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * @return This instance serialized into a byte array    * @see #parseFrom(byte[])    */
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
return|return
name|ProtobufUtil
operator|.
name|prependPBMagic
argument_list|(
name|this
operator|.
name|rt
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

