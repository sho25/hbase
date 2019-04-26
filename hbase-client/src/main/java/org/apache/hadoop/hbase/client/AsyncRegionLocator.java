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
name|util
operator|.
name|FutureUtils
operator|.
name|addListener
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
name|Supplier
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
name|RegionLocations
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
name|FutureUtils
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
name|hbase
operator|.
name|thirdparty
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
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
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
name|AsyncConnectionImpl
name|conn
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
name|conn
operator|=
name|conn
expr_stmt|;
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
parameter_list|<
name|T
parameter_list|>
name|CompletableFuture
argument_list|<
name|T
argument_list|>
name|withTimeout
parameter_list|(
name|CompletableFuture
argument_list|<
name|T
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
name|FutureUtils
operator|.
name|addListener
argument_list|(
name|future
argument_list|,
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
expr_stmt|;
return|return
name|future
return|;
block|}
specifier|private
name|boolean
name|isMeta
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
return|return
name|TableName
operator|.
name|isMetaTableName
argument_list|(
name|tableName
argument_list|)
return|;
block|}
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
name|getRegionLocations
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
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
name|future
init|=
name|isMeta
argument_list|(
name|tableName
argument_list|)
condition|?
name|metaRegionLocator
operator|.
name|getRegionLocations
argument_list|(
name|RegionReplicaUtil
operator|.
name|DEFAULT_REPLICA_ID
argument_list|,
name|reload
argument_list|)
else|:
name|nonMetaRegionLocator
operator|.
name|getRegionLocations
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|RegionReplicaUtil
operator|.
name|DEFAULT_REPLICA_ID
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
literal|"ms) waiting for region locations for "
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
name|int
name|replicaId
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
operator|new
name|CompletableFuture
argument_list|<>
argument_list|()
decl_stmt|;
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
name|locsFuture
init|=
name|isMeta
argument_list|(
name|tableName
argument_list|)
condition|?
name|metaRegionLocator
operator|.
name|getRegionLocations
argument_list|(
name|replicaId
argument_list|,
name|reload
argument_list|)
else|:
name|nonMetaRegionLocator
operator|.
name|getRegionLocations
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|replicaId
argument_list|,
name|type
argument_list|,
name|reload
argument_list|)
decl_stmt|;
name|addListener
argument_list|(
name|locsFuture
argument_list|,
parameter_list|(
name|locs
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
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
name|error
argument_list|)
expr_stmt|;
return|return;
block|}
name|HRegionLocation
name|loc
init|=
name|locs
operator|.
name|getRegionLocation
argument_list|(
name|replicaId
argument_list|)
decl_stmt|;
if|if
condition|(
name|loc
operator|==
literal|null
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|RegionOfflineException
argument_list|(
literal|"No location for "
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
literal|"', locateType="
operator|+
name|type
operator|+
literal|", replicaId="
operator|+
name|replicaId
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|loc
operator|.
name|getServerName
argument_list|()
operator|==
literal|null
condition|)
block|{
name|future
operator|.
name|completeExceptionally
argument_list|(
operator|new
name|RegionOfflineException
argument_list|(
literal|"No server address listed for region '"
operator|+
name|loc
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
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
literal|"', locateType="
operator|+
name|type
operator|+
literal|", replicaId="
operator|+
name|replicaId
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|future
operator|.
name|complete
argument_list|(
name|loc
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
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
literal|"', replicaId="
operator|+
name|replicaId
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
name|int
name|replicaId
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
name|replicaId
argument_list|,
name|type
argument_list|,
literal|false
argument_list|,
name|timeoutNs
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
return|return
name|getRegionLocation
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|RegionReplicaUtil
operator|.
name|DEFAULT_REPLICA_ID
argument_list|,
name|type
argument_list|,
name|reload
argument_list|,
name|timeoutNs
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
name|void
name|updateCachedLocationOnError
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
name|getRegion
argument_list|()
operator|.
name|isMetaRegion
argument_list|()
condition|)
block|{
name|metaRegionLocator
operator|.
name|updateCachedLocationOnError
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
name|updateCachedLocationOnError
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"Clear meta cache for {}"
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
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
name|void
name|clearCache
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Clear meta cache for {}"
argument_list|,
name|serverName
argument_list|)
expr_stmt|;
name|metaRegionLocator
operator|.
name|clearCache
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|nonMetaRegionLocator
operator|.
name|clearCache
argument_list|(
name|serverName
argument_list|)
expr_stmt|;
name|conn
operator|.
name|getConnectionMetrics
argument_list|()
operator|.
name|ifPresent
argument_list|(
name|MetricsConnection
operator|::
name|incrMetaCacheNumClearServer
argument_list|)
expr_stmt|;
block|}
name|void
name|clearCache
parameter_list|()
block|{
name|metaRegionLocator
operator|.
name|clearCache
argument_list|()
expr_stmt|;
name|nonMetaRegionLocator
operator|.
name|clearCache
argument_list|()
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
name|AsyncNonMetaRegionLocator
name|getNonMetaRegionLocator
parameter_list|()
block|{
return|return
name|nonMetaRegionLocator
return|;
block|}
comment|// only used for testing whether we have cached the location for a region.
annotation|@
name|VisibleForTesting
name|RegionLocations
name|getRegionLocationInCache
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
block|{
if|if
condition|(
name|TableName
operator|.
name|isMetaTableName
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
return|return
name|metaRegionLocator
operator|.
name|getRegionLocationInCache
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|nonMetaRegionLocator
operator|.
name|getRegionLocationInCache
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|)
return|;
block|}
block|}
comment|// only used for testing whether we have cached the location for a table.
annotation|@
name|VisibleForTesting
name|int
name|getNumberOfCachedRegionLocations
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
if|if
condition|(
name|TableName
operator|.
name|isMetaTableName
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
return|return
name|metaRegionLocator
operator|.
name|getNumberOfCachedRegionLocations
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|nonMetaRegionLocator
operator|.
name|getNumberOfCachedRegionLocations
argument_list|(
name|tableName
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

