begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|io
operator|.
name|hfile
operator|.
name|bucket
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|LongAdder
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
name|io
operator|.
name|hfile
operator|.
name|CacheStats
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
comment|/**  * Class that implements cache metrics for bucket cache.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|BucketCacheStats
extends|extends
name|CacheStats
block|{
specifier|private
specifier|final
name|LongAdder
name|ioHitCount
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|LongAdder
name|ioHitTime
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|NANO_TIME
init|=
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toNanos
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|private
name|long
name|lastLogTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
name|BucketCacheStats
parameter_list|()
block|{
name|super
argument_list|(
literal|"BucketCache"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|super
operator|.
name|toString
argument_list|()
operator|+
literal|", ioHitsPerSecond="
operator|+
name|getIOHitsPerSecond
argument_list|()
operator|+
literal|", ioTimePerHit="
operator|+
name|getIOTimePerHit
argument_list|()
return|;
block|}
specifier|public
name|void
name|ioHit
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|ioHitCount
operator|.
name|increment
argument_list|()
expr_stmt|;
name|ioHitTime
operator|.
name|add
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|getIOHitsPerSecond
parameter_list|()
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
name|took
init|=
operator|(
name|now
operator|-
name|lastLogTime
operator|)
operator|/
literal|1000
decl_stmt|;
name|lastLogTime
operator|=
name|now
expr_stmt|;
return|return
name|took
operator|==
literal|0
condition|?
literal|0
else|:
name|ioHitCount
operator|.
name|sum
argument_list|()
operator|/
name|took
return|;
block|}
specifier|public
name|double
name|getIOTimePerHit
parameter_list|()
block|{
name|long
name|time
init|=
name|ioHitTime
operator|.
name|sum
argument_list|()
operator|/
name|NANO_TIME
decl_stmt|;
name|long
name|count
init|=
name|ioHitCount
operator|.
name|sum
argument_list|()
decl_stmt|;
return|return
operator|(
operator|(
name|float
operator|)
name|time
operator|/
operator|(
name|float
operator|)
name|count
operator|)
return|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|ioHitCount
operator|.
name|reset
argument_list|()
expr_stmt|;
name|ioHitTime
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

