begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|executor
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|Writables
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

begin_comment
comment|/**  * Data serialized into ZooKeeper for region transitions.  */
end_comment

begin_class
specifier|public
class|class
name|RegionTransitionData
implements|implements
name|Writable
block|{
comment|/**    * Type of transition event (offline, opening, opened, closing, closed).    * Required.    */
specifier|private
name|EventType
name|eventType
decl_stmt|;
comment|/** Region being transitioned.  Required. */
specifier|private
name|byte
index|[]
name|regionName
decl_stmt|;
comment|/** Server event originated from.  Optional. */
specifier|private
name|ServerName
name|origin
decl_stmt|;
comment|/** Time the event was created.  Required but automatically set. */
specifier|private
name|long
name|stamp
decl_stmt|;
specifier|private
name|byte
index|[]
name|payload
decl_stmt|;
comment|/**    * Writable constructor.  Do not use directly.    */
specifier|public
name|RegionTransitionData
parameter_list|()
block|{}
comment|/**    * Construct data for a new region transition event with the specified event    * type and region name.    *    *<p>Used when the server name is not known (the master is setting it).  This    * happens during cluster startup or during failure scenarios.  When    * processing a failed regionserver, the master assigns the regions from that    * server to other servers though the region was never 'closed'.  During    * master failover, the new master may have regions stuck in transition    * without a destination so may have to set regions offline and generate a new    * assignment.    *    *<p>Since only the master uses this constructor, the type should always be    * {@link EventType#M_ZK_REGION_OFFLINE}.    *    * @param eventType type of event    * @param regionName name of region as per<code>HRegionInfo#getRegionName()</code>    */
specifier|public
name|RegionTransitionData
parameter_list|(
name|EventType
name|eventType
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|)
block|{
name|this
argument_list|(
name|eventType
argument_list|,
name|regionName
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct data for a new region transition event with the specified event    * type, region name, and server name.    *    *<p>Used when the server name is known (a regionserver is setting it).    *    *<p>Valid types for this constructor are {@link EventType#M_ZK_REGION_CLOSING},    * {@link EventType#RS_ZK_REGION_CLOSED}, {@link EventType#RS_ZK_REGION_OPENING},    * {@link EventType#RS_ZK_REGION_SPLITTING},    * and {@link EventType#RS_ZK_REGION_OPENED}.    *    * @param eventType type of event    * @param regionName name of region as per<code>HRegionInfo#getRegionName()</code>    * @param origin Originating {@link ServerName}    */
specifier|public
name|RegionTransitionData
parameter_list|(
name|EventType
name|eventType
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|ServerName
name|origin
parameter_list|)
block|{
name|this
argument_list|(
name|eventType
argument_list|,
name|regionName
argument_list|,
name|origin
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct data for a new region transition event with the specified event    * type, region name, and server name.    *    *<p>Used when the server name is known (a regionserver is setting it).    *    *<p>Valid types for this constructor are {@link EventType#RS_ZK_REGION_SPLIT}    * since SPLIT is only type that currently carries a payload.    *    * @param eventType type of event    * @param regionName name of region as per<code>HRegionInfo#getRegionName()</code>    * @param serverName Originating {@link ServerName}    * @param payload Payload examples include the daughters involved in a    * {@link EventType#RS_ZK_REGION_SPLIT}. Can be null    */
specifier|public
name|RegionTransitionData
parameter_list|(
name|EventType
name|eventType
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|ServerName
name|serverName
parameter_list|,
specifier|final
name|byte
index|[]
name|payload
parameter_list|)
block|{
name|this
operator|.
name|eventType
operator|=
name|eventType
expr_stmt|;
name|this
operator|.
name|stamp
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|this
operator|.
name|regionName
operator|=
name|regionName
expr_stmt|;
name|this
operator|.
name|origin
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|payload
operator|=
name|payload
expr_stmt|;
block|}
comment|/**    * Gets the type of region transition event.    *    *<p>One of:    *<ul>    *<li>{@link EventType#M_ZK_REGION_OFFLINE}    *<li>{@link EventType#M_ZK_REGION_CLOSING}    *<li>{@link EventType#RS_ZK_REGION_CLOSED}    *<li>{@link EventType#RS_ZK_REGION_OPENING}    *<li>{@link EventType#RS_ZK_REGION_OPENED}    *<li>{@link EventType#RS_ZK_REGION_SPLITTING}    *<li>{@link EventType#RS_ZK_REGION_SPLIT}    *</ul>    * @return type of region transition event    */
specifier|public
name|EventType
name|getEventType
parameter_list|()
block|{
return|return
name|eventType
return|;
block|}
comment|/**    * Gets the name of the region being transitioned.    *    *<p>Region name is required so this never returns null.    * @return region name, the result of a call to<code>HRegionInfo#getRegionName()</code>    */
specifier|public
name|byte
index|[]
name|getRegionName
parameter_list|()
block|{
return|return
name|regionName
return|;
block|}
comment|/**    * Gets the server the event originated from.  If null, this event originated    * from the master.    *    * @return server name of originating regionserver, or null if from master    */
specifier|public
name|ServerName
name|getOrigin
parameter_list|()
block|{
return|return
name|origin
return|;
block|}
comment|/**    * Gets the timestamp when this event was created.    *    * @return stamp event was created    */
specifier|public
name|long
name|getStamp
parameter_list|()
block|{
return|return
name|stamp
return|;
block|}
comment|/**    * @return Payload if any.    */
specifier|public
name|byte
index|[]
name|getPayload
parameter_list|()
block|{
return|return
name|this
operator|.
name|payload
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
comment|// the event type byte
name|eventType
operator|=
name|EventType
operator|.
name|values
argument_list|()
index|[
name|in
operator|.
name|readShort
argument_list|()
index|]
expr_stmt|;
comment|// the timestamp
name|stamp
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
comment|// the encoded name of the region being transitioned
name|regionName
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
comment|// remaining fields are optional so prefixed with boolean
comment|// the name of the regionserver sending the data
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|byte
index|[]
name|versionedBytes
init|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|this
operator|.
name|origin
operator|=
name|ServerName
operator|.
name|parseVersionedServerName
argument_list|(
name|versionedBytes
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|this
operator|.
name|payload
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeShort
argument_list|(
name|eventType
operator|.
name|ordinal
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|regionName
argument_list|)
expr_stmt|;
comment|// remaining fields are optional so prefixed with boolean
name|out
operator|.
name|writeBoolean
argument_list|(
name|this
operator|.
name|origin
operator|!=
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|origin
operator|!=
literal|null
condition|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|origin
operator|.
name|getVersionedBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|this
operator|.
name|payload
operator|!=
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|payload
operator|!=
literal|null
condition|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|payload
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get the bytes for this instance.  Throws a {@link RuntimeException} if    * there is an error deserializing this instance because it represents a code    * bug.    * @return binary representation of this instance    */
specifier|public
name|byte
index|[]
name|getBytes
parameter_list|()
block|{
try|try
block|{
return|return
name|Writables
operator|.
name|getBytes
argument_list|(
name|this
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Get an instance from bytes.  Throws a {@link RuntimeException} if    * there is an error serializing this instance from bytes because it    * represents a code bug.    * @param bytes binary representation of this instance    * @return instance of this class    */
specifier|public
specifier|static
name|RegionTransitionData
name|fromBytes
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
try|try
block|{
name|RegionTransitionData
name|data
init|=
operator|new
name|RegionTransitionData
argument_list|()
decl_stmt|;
name|Writables
operator|.
name|getWritable
argument_list|(
name|bytes
argument_list|,
name|data
argument_list|)
expr_stmt|;
return|return
name|data
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"region="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|regionName
argument_list|)
operator|+
literal|", origin="
operator|+
name|this
operator|.
name|origin
operator|+
literal|", state="
operator|+
name|eventType
return|;
block|}
block|}
end_class

end_unit

