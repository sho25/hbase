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
name|hadoop
operator|.
name|hbase
operator|.
name|HConstants
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
name|client
operator|.
name|backoff
operator|.
name|ServerStatistics
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
name|ClientProtos
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
name|ConcurrentHashMap
import|;
end_import

begin_comment
comment|/**  * Tracks the statistics for multiple regions  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ServerStatisticTracker
implements|implements
name|StatisticTrackable
block|{
specifier|private
specifier|final
name|ConcurrentHashMap
argument_list|<
name|ServerName
argument_list|,
name|ServerStatistics
argument_list|>
name|stats
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|ServerName
argument_list|,
name|ServerStatistics
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|updateRegionStats
parameter_list|(
name|ServerName
name|server
parameter_list|,
name|byte
index|[]
name|region
parameter_list|,
name|ClientProtos
operator|.
name|RegionLoadStats
name|currentStats
parameter_list|)
block|{
name|ServerStatistics
name|stat
init|=
name|stats
operator|.
name|get
argument_list|(
name|server
argument_list|)
decl_stmt|;
if|if
condition|(
name|stat
operator|==
literal|null
condition|)
block|{
name|stat
operator|=
name|stats
operator|.
name|get
argument_list|(
name|server
argument_list|)
expr_stmt|;
comment|// We don't have stats for that server yet, so we need to make an entry.
comment|// If we race with another thread it's a harmless unnecessary allocation.
if|if
condition|(
name|stat
operator|==
literal|null
condition|)
block|{
name|stat
operator|=
operator|new
name|ServerStatistics
argument_list|()
expr_stmt|;
name|ServerStatistics
name|old
init|=
name|stats
operator|.
name|putIfAbsent
argument_list|(
name|server
argument_list|,
name|stat
argument_list|)
decl_stmt|;
if|if
condition|(
name|old
operator|!=
literal|null
condition|)
block|{
name|stat
operator|=
name|old
expr_stmt|;
block|}
block|}
block|}
name|stat
operator|.
name|update
argument_list|(
name|region
argument_list|,
name|currentStats
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ServerStatistics
name|getStats
parameter_list|(
name|ServerName
name|server
parameter_list|)
block|{
return|return
name|this
operator|.
name|stats
operator|.
name|get
argument_list|(
name|server
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|ServerStatisticTracker
name|create
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
operator|!
name|conf
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|ENABLE_CLIENT_BACKPRESSURE
argument_list|,
name|HConstants
operator|.
name|DEFAULT_ENABLE_CLIENT_BACKPRESSURE
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|ServerStatisticTracker
argument_list|()
return|;
block|}
annotation|@
name|VisibleForTesting
name|ServerStatistics
name|getServerStatsForTesting
parameter_list|(
name|ServerName
name|server
parameter_list|)
block|{
return|return
name|stats
operator|.
name|get
argument_list|(
name|server
argument_list|)
return|;
block|}
block|}
end_class

end_unit

