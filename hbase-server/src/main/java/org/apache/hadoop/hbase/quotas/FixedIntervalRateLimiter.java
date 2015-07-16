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
name|hadoop
operator|.
name|hbase
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
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * With this limiter resources will be refilled only after a fixed interval of time.  */
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
name|FixedIntervalRateLimiter
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
name|now
operator|<
name|nextRefillTime
condition|)
block|{
return|return
literal|0
return|;
block|}
name|nextRefillTime
operator|=
name|now
operator|+
name|super
operator|.
name|getTimeUnitInMillis
argument_list|()
expr_stmt|;
return|return
name|limit
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
specifier|final
name|long
name|now
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
specifier|final
name|long
name|refillTime
init|=
name|nextRefillTime
decl_stmt|;
return|return
name|refillTime
operator|-
name|now
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

