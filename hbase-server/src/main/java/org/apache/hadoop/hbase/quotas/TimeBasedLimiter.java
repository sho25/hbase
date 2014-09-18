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
name|classification
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
name|protobuf
operator|.
name|generated
operator|.
name|QuotaProtos
operator|.
name|Throttle
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
name|QuotaProtos
operator|.
name|TimedQuota
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
name|OperationQuota
operator|.
name|AvgOperationSize
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
name|OperationQuota
operator|.
name|OperationType
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

begin_comment
comment|/**  * Simple time based limiter that checks the quota Throttle  */
end_comment

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
name|TimeBasedLimiter
implements|implements
name|QuotaLimiter
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
name|TimeBasedLimiter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|long
name|writeLastTs
init|=
literal|0
decl_stmt|;
specifier|private
name|long
name|readLastTs
init|=
literal|0
decl_stmt|;
specifier|private
name|RateLimiter
name|reqsLimiter
init|=
operator|new
name|RateLimiter
argument_list|()
decl_stmt|;
specifier|private
name|RateLimiter
name|reqSizeLimiter
init|=
operator|new
name|RateLimiter
argument_list|()
decl_stmt|;
specifier|private
name|RateLimiter
name|writeReqsLimiter
init|=
operator|new
name|RateLimiter
argument_list|()
decl_stmt|;
specifier|private
name|RateLimiter
name|writeSizeLimiter
init|=
operator|new
name|RateLimiter
argument_list|()
decl_stmt|;
specifier|private
name|RateLimiter
name|readReqsLimiter
init|=
operator|new
name|RateLimiter
argument_list|()
decl_stmt|;
specifier|private
name|RateLimiter
name|readSizeLimiter
init|=
operator|new
name|RateLimiter
argument_list|()
decl_stmt|;
specifier|private
name|AvgOperationSize
name|avgOpSize
init|=
operator|new
name|AvgOperationSize
argument_list|()
decl_stmt|;
specifier|private
name|TimeBasedLimiter
parameter_list|()
block|{   }
specifier|static
name|QuotaLimiter
name|fromThrottle
parameter_list|(
specifier|final
name|Throttle
name|throttle
parameter_list|)
block|{
name|TimeBasedLimiter
name|limiter
init|=
operator|new
name|TimeBasedLimiter
argument_list|()
decl_stmt|;
name|boolean
name|isBypass
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|throttle
operator|.
name|hasReqNum
argument_list|()
condition|)
block|{
name|setFromTimedQuota
argument_list|(
name|limiter
operator|.
name|reqsLimiter
argument_list|,
name|throttle
operator|.
name|getReqNum
argument_list|()
argument_list|)
expr_stmt|;
name|isBypass
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|throttle
operator|.
name|hasReqSize
argument_list|()
condition|)
block|{
name|setFromTimedQuota
argument_list|(
name|limiter
operator|.
name|reqSizeLimiter
argument_list|,
name|throttle
operator|.
name|getReqSize
argument_list|()
argument_list|)
expr_stmt|;
name|isBypass
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|throttle
operator|.
name|hasWriteNum
argument_list|()
condition|)
block|{
name|setFromTimedQuota
argument_list|(
name|limiter
operator|.
name|writeReqsLimiter
argument_list|,
name|throttle
operator|.
name|getWriteNum
argument_list|()
argument_list|)
expr_stmt|;
name|isBypass
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|throttle
operator|.
name|hasWriteSize
argument_list|()
condition|)
block|{
name|setFromTimedQuota
argument_list|(
name|limiter
operator|.
name|writeSizeLimiter
argument_list|,
name|throttle
operator|.
name|getWriteSize
argument_list|()
argument_list|)
expr_stmt|;
name|isBypass
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|throttle
operator|.
name|hasReadNum
argument_list|()
condition|)
block|{
name|setFromTimedQuota
argument_list|(
name|limiter
operator|.
name|readReqsLimiter
argument_list|,
name|throttle
operator|.
name|getReadNum
argument_list|()
argument_list|)
expr_stmt|;
name|isBypass
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|throttle
operator|.
name|hasReadSize
argument_list|()
condition|)
block|{
name|setFromTimedQuota
argument_list|(
name|limiter
operator|.
name|readSizeLimiter
argument_list|,
name|throttle
operator|.
name|getReadSize
argument_list|()
argument_list|)
expr_stmt|;
name|isBypass
operator|=
literal|false
expr_stmt|;
block|}
return|return
name|isBypass
condition|?
name|NoopQuotaLimiter
operator|.
name|get
argument_list|()
else|:
name|limiter
return|;
block|}
specifier|public
name|void
name|update
parameter_list|(
specifier|final
name|TimeBasedLimiter
name|other
parameter_list|)
block|{
name|reqsLimiter
operator|.
name|update
argument_list|(
name|other
operator|.
name|reqsLimiter
argument_list|)
expr_stmt|;
name|reqSizeLimiter
operator|.
name|update
argument_list|(
name|other
operator|.
name|reqSizeLimiter
argument_list|)
expr_stmt|;
name|writeReqsLimiter
operator|.
name|update
argument_list|(
name|other
operator|.
name|writeReqsLimiter
argument_list|)
expr_stmt|;
name|writeSizeLimiter
operator|.
name|update
argument_list|(
name|other
operator|.
name|writeSizeLimiter
argument_list|)
expr_stmt|;
name|readReqsLimiter
operator|.
name|update
argument_list|(
name|other
operator|.
name|readReqsLimiter
argument_list|)
expr_stmt|;
name|readSizeLimiter
operator|.
name|update
argument_list|(
name|other
operator|.
name|readSizeLimiter
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|setFromTimedQuota
parameter_list|(
specifier|final
name|RateLimiter
name|limiter
parameter_list|,
specifier|final
name|TimedQuota
name|timedQuota
parameter_list|)
block|{
name|limiter
operator|.
name|set
argument_list|(
name|timedQuota
operator|.
name|getSoftLimit
argument_list|()
argument_list|,
name|ProtobufUtil
operator|.
name|toTimeUnit
argument_list|(
name|timedQuota
operator|.
name|getTimeUnit
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|checkQuota
parameter_list|(
name|long
name|writeSize
parameter_list|,
name|long
name|readSize
parameter_list|)
throws|throws
name|ThrottlingException
block|{
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|long
name|lastTs
init|=
name|Math
operator|.
name|max
argument_list|(
name|readLastTs
argument_list|,
name|writeLastTs
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|reqsLimiter
operator|.
name|canExecute
argument_list|(
name|now
argument_list|,
name|lastTs
argument_list|)
condition|)
block|{
name|ThrottlingException
operator|.
name|throwNumRequestsExceeded
argument_list|(
name|reqsLimiter
operator|.
name|waitInterval
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|reqSizeLimiter
operator|.
name|canExecute
argument_list|(
name|now
argument_list|,
name|lastTs
argument_list|,
name|writeSize
operator|+
name|readSize
argument_list|)
condition|)
block|{
name|ThrottlingException
operator|.
name|throwNumRequestsExceeded
argument_list|(
name|reqSizeLimiter
operator|.
name|waitInterval
argument_list|(
name|writeSize
operator|+
name|readSize
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|writeSize
operator|>
literal|0
condition|)
block|{
if|if
condition|(
operator|!
name|writeReqsLimiter
operator|.
name|canExecute
argument_list|(
name|now
argument_list|,
name|writeLastTs
argument_list|)
condition|)
block|{
name|ThrottlingException
operator|.
name|throwNumWriteRequestsExceeded
argument_list|(
name|writeReqsLimiter
operator|.
name|waitInterval
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|writeSizeLimiter
operator|.
name|canExecute
argument_list|(
name|now
argument_list|,
name|writeLastTs
argument_list|,
name|writeSize
argument_list|)
condition|)
block|{
name|ThrottlingException
operator|.
name|throwWriteSizeExceeded
argument_list|(
name|writeSizeLimiter
operator|.
name|waitInterval
argument_list|(
name|writeSize
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|readSize
operator|>
literal|0
condition|)
block|{
if|if
condition|(
operator|!
name|readReqsLimiter
operator|.
name|canExecute
argument_list|(
name|now
argument_list|,
name|readLastTs
argument_list|)
condition|)
block|{
name|ThrottlingException
operator|.
name|throwNumReadRequestsExceeded
argument_list|(
name|readReqsLimiter
operator|.
name|waitInterval
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|readSizeLimiter
operator|.
name|canExecute
argument_list|(
name|now
argument_list|,
name|readLastTs
argument_list|,
name|readSize
argument_list|)
condition|)
block|{
name|ThrottlingException
operator|.
name|throwReadSizeExceeded
argument_list|(
name|readSizeLimiter
operator|.
name|waitInterval
argument_list|(
name|readSize
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|grabQuota
parameter_list|(
name|long
name|writeSize
parameter_list|,
name|long
name|readSize
parameter_list|)
block|{
assert|assert
name|writeSize
operator|!=
literal|0
operator|||
name|readSize
operator|!=
literal|0
assert|;
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|reqsLimiter
operator|.
name|consume
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|reqSizeLimiter
operator|.
name|consume
argument_list|(
name|writeSize
operator|+
name|readSize
argument_list|)
expr_stmt|;
if|if
condition|(
name|writeSize
operator|>
literal|0
condition|)
block|{
name|writeReqsLimiter
operator|.
name|consume
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|writeSizeLimiter
operator|.
name|consume
argument_list|(
name|writeSize
argument_list|)
expr_stmt|;
name|writeLastTs
operator|=
name|now
expr_stmt|;
block|}
if|if
condition|(
name|readSize
operator|>
literal|0
condition|)
block|{
name|readReqsLimiter
operator|.
name|consume
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|readSizeLimiter
operator|.
name|consume
argument_list|(
name|readSize
argument_list|)
expr_stmt|;
name|readLastTs
operator|=
name|now
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|consumeWrite
parameter_list|(
specifier|final
name|long
name|size
parameter_list|)
block|{
name|reqSizeLimiter
operator|.
name|consume
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|writeSizeLimiter
operator|.
name|consume
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|consumeRead
parameter_list|(
specifier|final
name|long
name|size
parameter_list|)
block|{
name|reqSizeLimiter
operator|.
name|consume
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|readSizeLimiter
operator|.
name|consume
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isBypass
parameter_list|()
block|{
return|return
literal|false
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
name|writeSizeLimiter
operator|.
name|getAvailable
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getReadAvailable
parameter_list|()
block|{
return|return
name|readSizeLimiter
operator|.
name|getAvailable
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addOperationSize
parameter_list|(
name|OperationType
name|type
parameter_list|,
name|long
name|size
parameter_list|)
block|{
name|avgOpSize
operator|.
name|addOperationSize
argument_list|(
name|type
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getAvgOperationSize
parameter_list|(
name|OperationType
name|type
parameter_list|)
block|{
return|return
name|avgOpSize
operator|.
name|getAvgOperationSize
argument_list|(
name|type
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"TimeBasedLimiter("
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|reqsLimiter
operator|.
name|isBypass
argument_list|()
condition|)
name|builder
operator|.
name|append
argument_list|(
literal|"reqs="
operator|+
name|reqsLimiter
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|reqSizeLimiter
operator|.
name|isBypass
argument_list|()
condition|)
name|builder
operator|.
name|append
argument_list|(
literal|" resSize="
operator|+
name|reqSizeLimiter
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|writeReqsLimiter
operator|.
name|isBypass
argument_list|()
condition|)
name|builder
operator|.
name|append
argument_list|(
literal|" writeReqs="
operator|+
name|writeReqsLimiter
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|writeSizeLimiter
operator|.
name|isBypass
argument_list|()
condition|)
name|builder
operator|.
name|append
argument_list|(
literal|" writeSize="
operator|+
name|writeSizeLimiter
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|readReqsLimiter
operator|.
name|isBypass
argument_list|()
condition|)
name|builder
operator|.
name|append
argument_list|(
literal|" readReqs="
operator|+
name|readReqsLimiter
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|readSizeLimiter
operator|.
name|isBypass
argument_list|()
condition|)
name|builder
operator|.
name|append
argument_list|(
literal|" readSize="
operator|+
name|readSizeLimiter
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

