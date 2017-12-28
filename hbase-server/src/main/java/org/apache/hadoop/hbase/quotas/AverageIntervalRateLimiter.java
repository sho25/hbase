begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license  * agreements. See the NOTICE file distributed with this work for additional information regarding  * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License. You may obtain a  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable  * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  * for the specific language governing permissions and limitations under the License.  */
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
name|util
operator|.
name|EnvironmentEdgeManager
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

begin_comment
comment|/**  * This limiter will refill resources at every TimeUnit/resources interval. For example: For a  * limiter configured with 10resources/second, then 1 resource will be refilled after every 100ms  * (1sec/10resources)  */
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
name|AverageIntervalRateLimiter
extends|extends
name|RateLimiter
block|{
specifier|private
name|long
name|nextRefillTime
init|=
operator|-
literal|1L
decl_stmt|;
annotation|@
name|Override
specifier|public
name|long
name|refill
parameter_list|(
name|long
name|limit
parameter_list|)
block|{
specifier|final
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
if|if
condition|(
name|nextRefillTime
operator|==
operator|-
literal|1
condition|)
block|{
comment|// Till now no resource has been consumed.
name|nextRefillTime
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
return|return
name|limit
return|;
block|}
name|long
name|timeInterval
init|=
name|now
operator|-
name|nextRefillTime
decl_stmt|;
name|long
name|delta
init|=
literal|0
decl_stmt|;
name|long
name|timeUnitInMillis
init|=
name|super
operator|.
name|getTimeUnitInMillis
argument_list|()
decl_stmt|;
if|if
condition|(
name|timeInterval
operator|>=
name|timeUnitInMillis
condition|)
block|{
name|delta
operator|=
name|limit
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|timeInterval
operator|>
literal|0
condition|)
block|{
name|double
name|r
init|=
operator|(
operator|(
name|double
operator|)
name|timeInterval
operator|/
operator|(
name|double
operator|)
name|timeUnitInMillis
operator|)
operator|*
name|limit
decl_stmt|;
name|delta
operator|=
operator|(
name|long
operator|)
name|r
expr_stmt|;
block|}
if|if
condition|(
name|delta
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|nextRefillTime
operator|=
name|now
expr_stmt|;
block|}
return|return
name|delta
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getWaitInterval
parameter_list|(
name|long
name|limit
parameter_list|,
name|long
name|available
parameter_list|,
name|long
name|amount
parameter_list|)
block|{
if|if
condition|(
name|nextRefillTime
operator|==
operator|-
literal|1
condition|)
block|{
return|return
literal|0
return|;
block|}
name|double
name|r
init|=
operator|(
call|(
name|double
call|)
argument_list|(
name|amount
operator|-
name|available
argument_list|)
operator|)
operator|*
name|super
operator|.
name|getTimeUnitInMillis
argument_list|()
operator|/
name|limit
decl_stmt|;
return|return
operator|(
name|long
operator|)
name|r
return|;
block|}
comment|// This method is for strictly testing purpose only
annotation|@
name|VisibleForTesting
annotation|@
name|Override
specifier|public
name|void
name|setNextRefillTime
parameter_list|(
name|long
name|nextRefillTime
parameter_list|)
block|{
name|this
operator|.
name|nextRefillTime
operator|=
name|nextRefillTime
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
annotation|@
name|Override
specifier|public
name|long
name|getNextRefillTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|nextRefillTime
return|;
block|}
block|}
end_class

end_unit

