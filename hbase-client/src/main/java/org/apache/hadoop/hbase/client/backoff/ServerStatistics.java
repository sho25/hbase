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
operator|.
name|backoff
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
name|hadoop
operator|.
name|hbase
operator|.
name|client
operator|.
name|RegionLoadStats
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
name|TreeMap
import|;
end_import

begin_comment
comment|/**  * Track the statistics for a single region  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ServerStatistics
block|{
specifier|private
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|RegionStatistics
argument_list|>
name|stats
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
comment|/**    * Good enough attempt. Last writer wins. It doesn't really matter which one gets to update,    * as something gets set    * @param region    * @param currentStats    */
specifier|public
name|void
name|update
parameter_list|(
name|byte
index|[]
name|region
parameter_list|,
name|RegionLoadStats
name|currentStats
parameter_list|)
block|{
name|RegionStatistics
name|regionStat
init|=
name|this
operator|.
name|stats
operator|.
name|get
argument_list|(
name|region
argument_list|)
decl_stmt|;
if|if
condition|(
name|regionStat
operator|==
literal|null
condition|)
block|{
name|regionStat
operator|=
operator|new
name|RegionStatistics
argument_list|()
expr_stmt|;
name|this
operator|.
name|stats
operator|.
name|put
argument_list|(
name|region
argument_list|,
name|regionStat
argument_list|)
expr_stmt|;
block|}
name|regionStat
operator|.
name|update
argument_list|(
name|currentStats
argument_list|)
expr_stmt|;
block|}
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
name|RegionStatistics
name|getStatsForRegion
parameter_list|(
name|byte
index|[]
name|regionName
parameter_list|)
block|{
return|return
name|stats
operator|.
name|get
argument_list|(
name|regionName
argument_list|)
return|;
block|}
specifier|public
specifier|static
class|class
name|RegionStatistics
block|{
specifier|private
name|int
name|memstoreLoad
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|heapOccupancy
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|compactionPressure
init|=
literal|0
decl_stmt|;
specifier|public
name|void
name|update
parameter_list|(
name|RegionLoadStats
name|currentStats
parameter_list|)
block|{
name|this
operator|.
name|memstoreLoad
operator|=
name|currentStats
operator|.
name|getMemstoreLoad
argument_list|()
expr_stmt|;
name|this
operator|.
name|heapOccupancy
operator|=
name|currentStats
operator|.
name|getHeapOccupancy
argument_list|()
expr_stmt|;
name|this
operator|.
name|compactionPressure
operator|=
name|currentStats
operator|.
name|getCompactionPressure
argument_list|()
expr_stmt|;
block|}
specifier|public
name|int
name|getMemstoreLoadPercent
parameter_list|()
block|{
return|return
name|this
operator|.
name|memstoreLoad
return|;
block|}
specifier|public
name|int
name|getHeapOccupancyPercent
parameter_list|()
block|{
return|return
name|this
operator|.
name|heapOccupancy
return|;
block|}
specifier|public
name|int
name|getCompactionPressure
parameter_list|()
block|{
return|return
name|compactionPressure
return|;
block|}
block|}
block|}
end_class

end_unit

