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
name|ServerName
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

begin_comment
comment|/**  * A {@link Result} with some statistics about the server/region status  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ResultStatsUtil
block|{
specifier|private
name|ResultStatsUtil
parameter_list|()
block|{
comment|//private ctor for util class
block|}
comment|/**    * Update the stats for the specified region if the result is an instance of {@link    * ResultStatsUtil}    *    * @param r object that contains the result and possibly the statistics about the region    * @param serverStats stats tracker to update from the result    * @param server server from which the result was obtained    * @param regionName full region name for the stats.    * @return the underlying {@link Result} if the passed result is an {@link    * ResultStatsUtil} or just returns the result;    */
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|updateStats
parameter_list|(
name|T
name|r
parameter_list|,
name|ServerStatisticTracker
name|serverStats
parameter_list|,
name|ServerName
name|server
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|r
operator|instanceof
name|Result
operator|)
condition|)
block|{
return|return
name|r
return|;
block|}
name|Result
name|result
init|=
operator|(
name|Result
operator|)
name|r
decl_stmt|;
comment|// early exit if there are no stats to collect
name|RegionLoadStats
name|stats
init|=
name|result
operator|.
name|getStats
argument_list|()
decl_stmt|;
if|if
condition|(
name|stats
operator|==
literal|null
condition|)
block|{
return|return
name|r
return|;
block|}
name|updateStats
argument_list|(
name|serverStats
argument_list|,
name|server
argument_list|,
name|regionName
argument_list|,
name|stats
argument_list|)
expr_stmt|;
return|return
name|r
return|;
block|}
specifier|public
specifier|static
name|void
name|updateStats
parameter_list|(
name|StatisticTrackable
name|tracker
parameter_list|,
name|ServerName
name|server
parameter_list|,
name|byte
index|[]
name|regionName
parameter_list|,
name|RegionLoadStats
name|stats
parameter_list|)
block|{
if|if
condition|(
name|regionName
operator|!=
literal|null
operator|&&
name|stats
operator|!=
literal|null
operator|&&
name|tracker
operator|!=
literal|null
condition|)
block|{
name|tracker
operator|.
name|updateRegionStats
argument_list|(
name|server
argument_list|,
name|regionName
argument_list|,
name|stats
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|updateStats
parameter_list|(
name|T
name|r
parameter_list|,
name|ServerStatisticTracker
name|stats
parameter_list|,
name|HRegionLocation
name|regionLocation
parameter_list|)
block|{
name|byte
index|[]
name|regionName
init|=
literal|null
decl_stmt|;
name|ServerName
name|server
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|regionLocation
operator|!=
literal|null
condition|)
block|{
name|server
operator|=
name|regionLocation
operator|.
name|getServerName
argument_list|()
expr_stmt|;
name|regionName
operator|=
name|regionLocation
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
expr_stmt|;
block|}
return|return
name|updateStats
argument_list|(
name|r
argument_list|,
name|stats
argument_list|,
name|server
argument_list|,
name|regionName
argument_list|)
return|;
block|}
block|}
end_class

end_unit

