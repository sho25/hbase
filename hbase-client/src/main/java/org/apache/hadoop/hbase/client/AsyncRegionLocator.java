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
name|client
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
operator|.
name|META_TABLE_NAME
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|exceptions
operator|.
name|ClientExceptionsUtil
operator|.
name|findException
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|exceptions
operator|.
name|ClientExceptionsUtil
operator|.
name|isMetaClearingException
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
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|HashedWheelTimer
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
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|Timeout
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
name|CompletableFuture
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
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
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
name|HRegionLocation
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
name|hadoop
operator|.
name|hbase
operator|.
name|exceptions
operator|.
name|RegionMovedException
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
name|exceptions
operator|.
name|TimeoutIOException
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

begin_comment
comment|/**  * The asynchronous region locator.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|AsyncRegionLocator
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
name|AsyncRegionLocator
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HashedWheelTimer
name|retryTimer
decl_stmt|;
specifier|private
specifier|final
name|AsyncMetaRegionLocator
name|metaRegionLocator
decl_stmt|;
specifier|private
specifier|final
name|AsyncNonMetaRegionLocator
name|nonMetaRegionLocator
decl_stmt|;
name|AsyncRegionLocator
parameter_list|(
name|AsyncConnectionImpl
name|conn
parameter_list|,
name|HashedWheelTimer
name|retryTimer
parameter_list|)
block|{
name|this
operator|.
name|metaRegionLocator
operator|=
operator|new
name|AsyncMetaRegionLocator
argument_list|(
name|conn
operator|.
name|registry
argument_list|)
expr_stmt|;
name|this
operator|.
name|nonMetaRegionLocator
operator|=
operator|new
name|AsyncNonMetaRegionLocator
argument_list|(
name|conn
argument_list|)
expr_stmt|;
name|this
operator|.
name|retryTimer
operator|=
name|retryTimer
expr_stmt|;
block|}
specifier|private
name|CompletableFuture
argument_list|<
name|HRegionLocation
argument_list|>
name|withTimeout
parameter_list|(
name|CompletableFuture
argument_list|<
name|HRegionLocation
argument_list|>
name|future
parameter_list|,
name|long
name|timeoutNs
parameter_list|,
name|Supplier
argument_list|<
name|String
argument_list|>
name|timeoutMsg
parameter_list|)
block|{
if|if
condition|(
name|future
operator|.
name|isDone
argument_list|()
operator|||
name|timeoutNs
operator|<=
literal|0
condition|)
block|{
return|return
name|future
return|;
block|}
name|Timeout
name|timeoutTask
init|=
name|retryTimer
operator|.
name|newTimeout
argument_list|(
name|t
lambda|->
block|{
if|if
condition|(
name|future
operator|.
name|isDone
argument_list|()
condition|)
block|{
return|return;
block|}
name|future
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|TimeoutIOException
argument_list|(
name|timeoutMsg
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|timeoutNs
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
decl_stmt|;
return|return
name|future
operator|.
name|whenComplete
argument_list|(
parameter_list|(
name|loc
parameter_list|,
name|error
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|error
operator|!=
literal|null
operator|&&
name|error
operator|.
name|getClass
argument_list|()
operator|!=
name|TimeoutIOException
operator|.
name|class
condition|)
block|{
comment|// cancel timeout task if we are not completed by it.
name|timeoutTask
operator|.
name|cancel
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
return|;
block|}
name|CompletableFuture
argument_list|<
name|HRegionLocation
argument_list|>
name|getRegionLocation
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|RegionLocateType
name|type
parameter_list|,
name|boolean
name|reload
parameter_list|,
name|long
name|timeoutNs
parameter_list|)
block|{
comment|// meta region can not be split right now so we always call the same method.
comment|// Change it later if the meta table can have more than one regions.
name|CompletableFuture
argument_list|<
name|HRegionLocation
argument_list|>
name|future
init|=
name|tableName
operator|.
name|equals
argument_list|(
name|META_TABLE_NAME
argument_list|)
condition|?
name|metaRegionLocator
operator|.
name|getRegionLocation
argument_list|(
name|reload
argument_list|)
else|:
name|nonMetaRegionLocator
operator|.
name|getRegionLocation
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|type
argument_list|,
name|reload
argument_list|)
decl_stmt|;
return|return
name|withTimeout
argument_list|(
name|future
argument_list|,
name|timeoutNs
argument_list|,
parameter_list|()
lambda|->
literal|"Timeout("
operator|+
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|timeoutNs
argument_list|)
operator|+
literal|"ms) waiting for region location for "
operator|+
name|tableName
operator|+
literal|", row='"
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|row
argument_list|)
operator|+
literal|"'"
argument_list|)
return|;
block|}
name|CompletableFuture
argument_list|<
name|HRegionLocation
argument_list|>
name|getRegionLocation
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|RegionLocateType
name|type
parameter_list|,
name|long
name|timeoutNs
parameter_list|)
block|{
return|return
name|getRegionLocation
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|type
argument_list|,
literal|false
argument_list|,
name|timeoutNs
argument_list|)
return|;
block|}
specifier|static
name|boolean
name|canUpdate
parameter_list|(
name|HRegionLocation
name|loc
parameter_list|,
name|HRegionLocation
name|oldLoc
parameter_list|)
block|{
comment|// Do not need to update if no such location, or the location is newer, or the location is not
comment|// same with us
return|return
name|oldLoc
operator|!=
literal|null
operator|&&
name|oldLoc
operator|.
name|getSeqNum
argument_list|()
operator|<=
name|loc
operator|.
name|getSeqNum
argument_list|()
operator|&&
name|oldLoc
operator|.
name|getServerName
argument_list|()
operator|.
name|equals
argument_list|(
name|loc
operator|.
name|getServerName
argument_list|()
argument_list|)
return|;
block|}
specifier|static
name|void
name|updateCachedLocation
parameter_list|(
name|HRegionLocation
name|loc
parameter_list|,
name|Throwable
name|exception
parameter_list|,
name|Function
argument_list|<
name|HRegionLocation
argument_list|,
name|HRegionLocation
argument_list|>
name|cachedLocationSupplier
parameter_list|,
name|Consumer
argument_list|<
name|HRegionLocation
argument_list|>
name|addToCache
parameter_list|,
name|Consumer
argument_list|<
name|HRegionLocation
argument_list|>
name|removeFromCache
parameter_list|)
block|{
name|HRegionLocation
name|oldLoc
init|=
name|cachedLocationSupplier
operator|.
name|apply
argument_list|(
name|loc
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Try updating "
operator|+
name|loc
operator|+
literal|", the old value is "
operator|+
name|oldLoc
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|canUpdate
argument_list|(
name|loc
argument_list|,
name|oldLoc
argument_list|)
condition|)
block|{
return|return;
block|}
name|Throwable
name|cause
init|=
name|findException
argument_list|(
name|exception
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"The actual exception when updating "
operator|+
name|loc
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cause
operator|==
literal|null
operator|||
operator|!
name|isMetaClearingException
argument_list|(
name|cause
argument_list|)
condition|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Will not update "
operator|+
name|loc
operator|+
literal|" because the exception is null or not the one we care about"
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
if|if
condition|(
name|cause
operator|instanceof
name|RegionMovedException
condition|)
block|{
name|RegionMovedException
name|rme
init|=
operator|(
name|RegionMovedException
operator|)
name|cause
decl_stmt|;
name|HRegionLocation
name|newLoc
init|=
operator|new
name|HRegionLocation
argument_list|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|rme
operator|.
name|getServerName
argument_list|()
argument_list|,
name|rme
operator|.
name|getLocationSeqNum
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Try updating "
operator|+
name|loc
operator|+
literal|" with the new location "
operator|+
name|newLoc
operator|+
literal|" constructed by "
operator|+
name|rme
argument_list|)
expr_stmt|;
block|}
name|addToCache
operator|.
name|accept
argument_list|(
name|newLoc
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Try removing "
operator|+
name|loc
operator|+
literal|" from cache"
argument_list|)
expr_stmt|;
block|}
name|removeFromCache
operator|.
name|accept
argument_list|(
name|loc
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|updateCachedLocation
parameter_list|(
name|HRegionLocation
name|loc
parameter_list|,
name|Throwable
name|exception
parameter_list|)
block|{
if|if
condition|(
name|loc
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|isMetaTable
argument_list|()
condition|)
block|{
name|metaRegionLocator
operator|.
name|updateCachedLocation
argument_list|(
name|loc
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|nonMetaRegionLocator
operator|.
name|updateCachedLocation
argument_list|(
name|loc
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
block|}
name|void
name|clearCache
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Clear meta cache for "
operator|+
name|tableName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tableName
operator|.
name|equals
argument_list|(
name|META_TABLE_NAME
argument_list|)
condition|)
block|{
name|metaRegionLocator
operator|.
name|clearCache
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|nonMetaRegionLocator
operator|.
name|clearCache
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

