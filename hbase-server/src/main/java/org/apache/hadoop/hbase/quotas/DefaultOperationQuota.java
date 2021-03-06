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
name|quotas
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
name|java
operator|.
name|util
operator|.
name|List
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
name|client
operator|.
name|Mutation
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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|DefaultOperationQuota
implements|implements
name|OperationQuota
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
name|DefaultOperationQuota
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|List
argument_list|<
name|QuotaLimiter
argument_list|>
name|limiters
decl_stmt|;
specifier|private
specifier|final
name|long
name|writeCapacityUnit
decl_stmt|;
specifier|private
specifier|final
name|long
name|readCapacityUnit
decl_stmt|;
comment|// the available read/write quota size in bytes
specifier|protected
name|long
name|writeAvailable
init|=
literal|0
decl_stmt|;
specifier|protected
name|long
name|readAvailable
init|=
literal|0
decl_stmt|;
comment|// estimated quota
specifier|protected
name|long
name|writeConsumed
init|=
literal|0
decl_stmt|;
specifier|protected
name|long
name|readConsumed
init|=
literal|0
decl_stmt|;
specifier|protected
name|long
name|writeCapacityUnitConsumed
init|=
literal|0
decl_stmt|;
specifier|protected
name|long
name|readCapacityUnitConsumed
init|=
literal|0
decl_stmt|;
comment|// real consumed quota
specifier|private
specifier|final
name|long
index|[]
name|operationSize
decl_stmt|;
comment|// difference between estimated quota and real consumed quota used in close method
comment|// to adjust quota amount. Also used by ExceedOperationQuota which is a subclass
comment|// of DefaultOperationQuota
specifier|protected
name|long
name|writeDiff
init|=
literal|0
decl_stmt|;
specifier|protected
name|long
name|readDiff
init|=
literal|0
decl_stmt|;
specifier|protected
name|long
name|writeCapacityUnitDiff
init|=
literal|0
decl_stmt|;
specifier|protected
name|long
name|readCapacityUnitDiff
init|=
literal|0
decl_stmt|;
specifier|public
name|DefaultOperationQuota
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|QuotaLimiter
modifier|...
name|limiters
parameter_list|)
block|{
name|this
argument_list|(
name|conf
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|limiters
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * NOTE: The order matters. It should be something like [user, table, namespace, global]    */
specifier|public
name|DefaultOperationQuota
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|List
argument_list|<
name|QuotaLimiter
argument_list|>
name|limiters
parameter_list|)
block|{
name|this
operator|.
name|writeCapacityUnit
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|QuotaUtil
operator|.
name|WRITE_CAPACITY_UNIT_CONF_KEY
argument_list|,
name|QuotaUtil
operator|.
name|DEFAULT_WRITE_CAPACITY_UNIT
argument_list|)
expr_stmt|;
name|this
operator|.
name|readCapacityUnit
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|QuotaUtil
operator|.
name|READ_CAPACITY_UNIT_CONF_KEY
argument_list|,
name|QuotaUtil
operator|.
name|DEFAULT_READ_CAPACITY_UNIT
argument_list|)
expr_stmt|;
name|this
operator|.
name|limiters
operator|=
name|limiters
expr_stmt|;
name|int
name|size
init|=
name|OperationType
operator|.
name|values
argument_list|()
operator|.
name|length
decl_stmt|;
name|operationSize
operator|=
operator|new
name|long
index|[
name|size
index|]
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
name|size
condition|;
operator|++
name|i
control|)
block|{
name|operationSize
index|[
name|i
index|]
operator|=
literal|0
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkQuota
parameter_list|(
name|int
name|numWrites
parameter_list|,
name|int
name|numReads
parameter_list|,
name|int
name|numScans
parameter_list|)
throws|throws
name|RpcThrottlingException
block|{
name|updateEstimateConsumeQuota
argument_list|(
name|numWrites
argument_list|,
name|numReads
argument_list|,
name|numScans
argument_list|)
expr_stmt|;
name|writeAvailable
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
name|readAvailable
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
for|for
control|(
specifier|final
name|QuotaLimiter
name|limiter
range|:
name|limiters
control|)
block|{
if|if
condition|(
name|limiter
operator|.
name|isBypass
argument_list|()
condition|)
continue|continue;
name|limiter
operator|.
name|checkQuota
argument_list|(
name|numWrites
argument_list|,
name|writeConsumed
argument_list|,
name|numReads
operator|+
name|numScans
argument_list|,
name|readConsumed
argument_list|,
name|writeCapacityUnitConsumed
argument_list|,
name|readCapacityUnitConsumed
argument_list|)
expr_stmt|;
name|readAvailable
operator|=
name|Math
operator|.
name|min
argument_list|(
name|readAvailable
argument_list|,
name|limiter
operator|.
name|getReadAvailable
argument_list|()
argument_list|)
expr_stmt|;
name|writeAvailable
operator|=
name|Math
operator|.
name|min
argument_list|(
name|writeAvailable
argument_list|,
name|limiter
operator|.
name|getWriteAvailable
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
specifier|final
name|QuotaLimiter
name|limiter
range|:
name|limiters
control|)
block|{
name|limiter
operator|.
name|grabQuota
argument_list|(
name|numWrites
argument_list|,
name|writeConsumed
argument_list|,
name|numReads
operator|+
name|numScans
argument_list|,
name|readConsumed
argument_list|,
name|writeCapacityUnitConsumed
argument_list|,
name|readCapacityUnitConsumed
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// Adjust the quota consumed for the specified operation
name|writeDiff
operator|=
name|operationSize
index|[
name|OperationType
operator|.
name|MUTATE
operator|.
name|ordinal
argument_list|()
index|]
operator|-
name|writeConsumed
expr_stmt|;
name|readDiff
operator|=
name|operationSize
index|[
name|OperationType
operator|.
name|GET
operator|.
name|ordinal
argument_list|()
index|]
operator|+
name|operationSize
index|[
name|OperationType
operator|.
name|SCAN
operator|.
name|ordinal
argument_list|()
index|]
operator|-
name|readConsumed
expr_stmt|;
name|writeCapacityUnitDiff
operator|=
name|calculateWriteCapacityUnitDiff
argument_list|(
name|operationSize
index|[
name|OperationType
operator|.
name|MUTATE
operator|.
name|ordinal
argument_list|()
index|]
argument_list|,
name|writeConsumed
argument_list|)
expr_stmt|;
name|readCapacityUnitDiff
operator|=
name|calculateReadCapacityUnitDiff
argument_list|(
name|operationSize
index|[
name|OperationType
operator|.
name|GET
operator|.
name|ordinal
argument_list|()
index|]
operator|+
name|operationSize
index|[
name|OperationType
operator|.
name|SCAN
operator|.
name|ordinal
argument_list|()
index|]
argument_list|,
name|readConsumed
argument_list|)
expr_stmt|;
for|for
control|(
specifier|final
name|QuotaLimiter
name|limiter
range|:
name|limiters
control|)
block|{
if|if
condition|(
name|writeDiff
operator|!=
literal|0
condition|)
block|{
name|limiter
operator|.
name|consumeWrite
argument_list|(
name|writeDiff
argument_list|,
name|writeCapacityUnitDiff
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|readDiff
operator|!=
literal|0
condition|)
block|{
name|limiter
operator|.
name|consumeRead
argument_list|(
name|readDiff
argument_list|,
name|readCapacityUnitDiff
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getReadAvailable
parameter_list|()
block|{
return|return
name|readAvailable
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getWriteAvailable
parameter_list|()
block|{
return|return
name|writeAvailable
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addGetResult
parameter_list|(
specifier|final
name|Result
name|result
parameter_list|)
block|{
name|operationSize
index|[
name|OperationType
operator|.
name|GET
operator|.
name|ordinal
argument_list|()
index|]
operator|+=
name|QuotaUtil
operator|.
name|calculateResultSize
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addScanResult
parameter_list|(
specifier|final
name|List
argument_list|<
name|Result
argument_list|>
name|results
parameter_list|)
block|{
name|operationSize
index|[
name|OperationType
operator|.
name|SCAN
operator|.
name|ordinal
argument_list|()
index|]
operator|+=
name|QuotaUtil
operator|.
name|calculateResultSize
argument_list|(
name|results
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addMutation
parameter_list|(
specifier|final
name|Mutation
name|mutation
parameter_list|)
block|{
name|operationSize
index|[
name|OperationType
operator|.
name|MUTATE
operator|.
name|ordinal
argument_list|()
index|]
operator|+=
name|QuotaUtil
operator|.
name|calculateMutationSize
argument_list|(
name|mutation
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update estimate quota(read/write size/capacityUnits) which will be consumed    * @param numWrites the number of write requests    * @param numReads the number of read requests    * @param numScans the number of scan requests    */
specifier|protected
name|void
name|updateEstimateConsumeQuota
parameter_list|(
name|int
name|numWrites
parameter_list|,
name|int
name|numReads
parameter_list|,
name|int
name|numScans
parameter_list|)
block|{
name|writeConsumed
operator|=
name|estimateConsume
argument_list|(
name|OperationType
operator|.
name|MUTATE
argument_list|,
name|numWrites
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|readConsumed
operator|=
name|estimateConsume
argument_list|(
name|OperationType
operator|.
name|GET
argument_list|,
name|numReads
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|readConsumed
operator|+=
name|estimateConsume
argument_list|(
name|OperationType
operator|.
name|SCAN
argument_list|,
name|numScans
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|writeCapacityUnitConsumed
operator|=
name|calculateWriteCapacityUnit
argument_list|(
name|writeConsumed
argument_list|)
expr_stmt|;
name|readCapacityUnitConsumed
operator|=
name|calculateReadCapacityUnit
argument_list|(
name|readConsumed
argument_list|)
expr_stmt|;
block|}
specifier|private
name|long
name|estimateConsume
parameter_list|(
specifier|final
name|OperationType
name|type
parameter_list|,
name|int
name|numReqs
parameter_list|,
name|long
name|avgSize
parameter_list|)
block|{
if|if
condition|(
name|numReqs
operator|>
literal|0
condition|)
block|{
return|return
name|avgSize
operator|*
name|numReqs
return|;
block|}
return|return
literal|0
return|;
block|}
specifier|private
name|long
name|calculateWriteCapacityUnit
parameter_list|(
specifier|final
name|long
name|size
parameter_list|)
block|{
return|return
operator|(
name|long
operator|)
name|Math
operator|.
name|ceil
argument_list|(
name|size
operator|*
literal|1.0
operator|/
name|this
operator|.
name|writeCapacityUnit
argument_list|)
return|;
block|}
specifier|private
name|long
name|calculateReadCapacityUnit
parameter_list|(
specifier|final
name|long
name|size
parameter_list|)
block|{
return|return
operator|(
name|long
operator|)
name|Math
operator|.
name|ceil
argument_list|(
name|size
operator|*
literal|1.0
operator|/
name|this
operator|.
name|readCapacityUnit
argument_list|)
return|;
block|}
specifier|private
name|long
name|calculateWriteCapacityUnitDiff
parameter_list|(
specifier|final
name|long
name|actualSize
parameter_list|,
specifier|final
name|long
name|estimateSize
parameter_list|)
block|{
return|return
name|calculateWriteCapacityUnit
argument_list|(
name|actualSize
argument_list|)
operator|-
name|calculateWriteCapacityUnit
argument_list|(
name|estimateSize
argument_list|)
return|;
block|}
specifier|private
name|long
name|calculateReadCapacityUnitDiff
parameter_list|(
specifier|final
name|long
name|actualSize
parameter_list|,
specifier|final
name|long
name|estimateSize
parameter_list|)
block|{
return|return
name|calculateReadCapacityUnit
argument_list|(
name|actualSize
argument_list|)
operator|-
name|calculateReadCapacityUnit
argument_list|(
name|estimateSize
argument_list|)
return|;
block|}
block|}
end_class

end_unit

