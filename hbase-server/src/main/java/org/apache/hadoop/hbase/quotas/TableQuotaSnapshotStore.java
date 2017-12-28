begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|quotas
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
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
name|locks
operator|.
name|ReentrantReadWriteLock
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
name|locks
operator|.
name|ReentrantReadWriteLock
operator|.
name|ReadLock
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
name|locks
operator|.
name|ReentrantReadWriteLock
operator|.
name|WriteLock
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
name|Cell
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
name|CellScanner
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
name|client
operator|.
name|Connection
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
name|client
operator|.
name|Result
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
name|ResultScanner
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
name|Scan
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
name|Table
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
name|quotas
operator|.
name|SpaceQuotaSnapshot
operator|.
name|SpaceQuotaStatus
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
name|base
operator|.
name|Predicate
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
name|Iterables
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
name|QuotaProtos
operator|.
name|Quotas
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
name|QuotaProtos
operator|.
name|SpaceQuota
import|;
end_import

begin_comment
comment|/**  * {@link QuotaSnapshotStore} for tables.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TableQuotaSnapshotStore
implements|implements
name|QuotaSnapshotStore
argument_list|<
name|TableName
argument_list|>
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
name|TableQuotaSnapshotStore
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ReentrantReadWriteLock
name|lock
init|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ReadLock
name|rlock
init|=
name|lock
operator|.
name|readLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|WriteLock
name|wlock
init|=
name|lock
operator|.
name|writeLock
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Connection
name|conn
decl_stmt|;
specifier|private
specifier|final
name|QuotaObserverChore
name|chore
decl_stmt|;
specifier|private
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
name|regionUsage
decl_stmt|;
specifier|public
name|TableQuotaSnapshotStore
parameter_list|(
name|Connection
name|conn
parameter_list|,
name|QuotaObserverChore
name|chore
parameter_list|,
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
name|regionUsage
parameter_list|)
block|{
name|this
operator|.
name|conn
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|conn
argument_list|)
expr_stmt|;
name|this
operator|.
name|chore
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|chore
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionUsage
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|regionUsage
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|SpaceQuota
name|getSpaceQuota
parameter_list|(
name|TableName
name|subject
parameter_list|)
throws|throws
name|IOException
block|{
name|Quotas
name|quotas
init|=
name|getQuotaForTable
argument_list|(
name|subject
argument_list|)
decl_stmt|;
if|if
condition|(
name|quotas
operator|!=
literal|null
operator|&&
name|quotas
operator|.
name|hasSpace
argument_list|()
condition|)
block|{
return|return
name|quotas
operator|.
name|getSpace
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Fetches the table quota. Visible for mocking/testing.    */
name|Quotas
name|getQuotaForTable
parameter_list|(
name|TableName
name|table
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|QuotaTableUtil
operator|.
name|getTableQuota
argument_list|(
name|conn
argument_list|,
name|table
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SpaceQuotaSnapshot
name|getCurrentState
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
comment|// Defer the "current state" to the chore
return|return
name|chore
operator|.
name|getTableQuotaSnapshot
argument_list|(
name|table
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|SpaceQuotaSnapshot
name|getTargetState
parameter_list|(
name|TableName
name|table
parameter_list|,
name|SpaceQuota
name|spaceQuota
parameter_list|)
throws|throws
name|IOException
block|{
name|rlock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
specifier|final
name|long
name|sizeLimitInBytes
init|=
name|spaceQuota
operator|.
name|getSoftLimit
argument_list|()
decl_stmt|;
name|long
name|sum
init|=
literal|0L
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|filterBySubject
argument_list|(
name|table
argument_list|)
control|)
block|{
name|sum
operator|+=
name|entry
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
comment|// Add in the size for any snapshots against this table
name|sum
operator|+=
name|getSnapshotSizesForTable
argument_list|(
name|table
argument_list|)
expr_stmt|;
comment|// Observance is defined as the size of the table being less than the limit
name|SpaceQuotaStatus
name|status
init|=
name|sum
operator|<=
name|sizeLimitInBytes
condition|?
name|SpaceQuotaStatus
operator|.
name|notInViolation
argument_list|()
else|:
operator|new
name|SpaceQuotaStatus
argument_list|(
name|ProtobufUtil
operator|.
name|toViolationPolicy
argument_list|(
name|spaceQuota
operator|.
name|getViolationPolicy
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|SpaceQuotaSnapshot
argument_list|(
name|status
argument_list|,
name|sum
argument_list|,
name|sizeLimitInBytes
argument_list|)
return|;
block|}
finally|finally
block|{
name|rlock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Fetches any serialized snapshot sizes from the quota table for the {@code tn} provided. Any    * malformed records are skipped with a warning printed out.    */
name|long
name|getSnapshotSizesForTable
parameter_list|(
name|TableName
name|tn
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Table
name|quotaTable
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|QuotaTableUtil
operator|.
name|QUOTA_TABLE_NAME
argument_list|)
init|)
block|{
name|Scan
name|s
init|=
name|QuotaTableUtil
operator|.
name|createScanForSpaceSnapshotSizes
argument_list|(
name|tn
argument_list|)
decl_stmt|;
name|ResultScanner
name|rs
init|=
name|quotaTable
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
try|try
block|{
name|long
name|size
init|=
literal|0L
decl_stmt|;
comment|// Should just be a single row (for our table)
for|for
control|(
name|Result
name|result
range|:
name|rs
control|)
block|{
comment|// May have multiple columns, one for each snapshot
name|CellScanner
name|cs
init|=
name|result
operator|.
name|cellScanner
argument_list|()
decl_stmt|;
while|while
condition|(
name|cs
operator|.
name|advance
argument_list|()
condition|)
block|{
name|Cell
name|current
init|=
name|cs
operator|.
name|current
argument_list|()
decl_stmt|;
try|try
block|{
name|long
name|snapshotSize
init|=
name|QuotaTableUtil
operator|.
name|parseSnapshotSize
argument_list|(
name|current
argument_list|)
decl_stmt|;
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
literal|"Saw snapshot size of "
operator|+
name|snapshotSize
operator|+
literal|" for "
operator|+
name|current
argument_list|)
expr_stmt|;
block|}
name|size
operator|+=
name|snapshotSize
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to parse snapshot size from cell: "
operator|+
name|current
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|size
return|;
block|}
finally|finally
block|{
if|if
condition|(
literal|null
operator|!=
name|rs
condition|)
block|{
name|rs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|Iterable
argument_list|<
name|Entry
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
argument_list|>
name|filterBySubject
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
name|rlock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
return|return
name|Iterables
operator|.
name|filter
argument_list|(
name|regionUsage
operator|.
name|entrySet
argument_list|()
argument_list|,
operator|new
name|Predicate
argument_list|<
name|Entry
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Entry
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
name|input
parameter_list|)
block|{
return|return
name|table
operator|.
name|equals
argument_list|(
name|input
operator|.
name|getKey
argument_list|()
operator|.
name|getTable
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
finally|finally
block|{
name|rlock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setCurrentState
parameter_list|(
name|TableName
name|table
parameter_list|,
name|SpaceQuotaSnapshot
name|snapshot
parameter_list|)
block|{
comment|// Defer the "current state" to the chore
name|this
operator|.
name|chore
operator|.
name|setTableQuotaSnapshot
argument_list|(
name|table
argument_list|,
name|snapshot
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setRegionUsage
parameter_list|(
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|Long
argument_list|>
name|regionUsage
parameter_list|)
block|{
name|wlock
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|this
operator|.
name|regionUsage
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|regionUsage
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|wlock
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

