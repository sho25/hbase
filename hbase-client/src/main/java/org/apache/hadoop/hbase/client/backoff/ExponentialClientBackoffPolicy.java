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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_comment
comment|/**  * Simple exponential backoff policy on for the client that uses a  percent^4 times the  * max backoff to generate the backoff time.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|ExponentialClientBackoffPolicy
implements|implements
name|ClientBackoffPolicy
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
name|ExponentialClientBackoffPolicy
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|ONE_MINUTE
init|=
literal|60
operator|*
literal|1000
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_MAX_BACKOFF
init|=
literal|5
operator|*
name|ONE_MINUTE
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAX_BACKOFF_KEY
init|=
literal|"hbase.client.exponential-backoff.max"
decl_stmt|;
specifier|private
name|long
name|maxBackoff
decl_stmt|;
specifier|private
name|float
name|heapOccupancyLowWatermark
decl_stmt|;
specifier|private
name|float
name|heapOccupancyHighWatermark
decl_stmt|;
specifier|public
name|ExponentialClientBackoffPolicy
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|maxBackoff
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|MAX_BACKOFF_KEY
argument_list|,
name|DEFAULT_MAX_BACKOFF
argument_list|)
expr_stmt|;
name|this
operator|.
name|heapOccupancyLowWatermark
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|HConstants
operator|.
name|HEAP_OCCUPANCY_LOW_WATERMARK_KEY
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HEAP_OCCUPANCY_LOW_WATERMARK
argument_list|)
expr_stmt|;
name|this
operator|.
name|heapOccupancyHighWatermark
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|HConstants
operator|.
name|HEAP_OCCUPANCY_HIGH_WATERMARK_KEY
argument_list|,
name|HConstants
operator|.
name|DEFAULT_HEAP_OCCUPANCY_HIGH_WATERMARK
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getBackoffTime
parameter_list|(
name|ServerName
name|serverName
parameter_list|,
name|byte
index|[]
name|region
parameter_list|,
name|ServerStatistics
name|stats
parameter_list|)
block|{
comment|// no stats for the server yet, so don't backoff
if|if
condition|(
name|stats
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
name|ServerStatistics
operator|.
name|RegionStatistics
name|regionStats
init|=
name|stats
operator|.
name|getStatsForRegion
argument_list|(
name|region
argument_list|)
decl_stmt|;
comment|// no stats for the region yet - don't backoff
if|if
condition|(
name|regionStats
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
comment|// Factor in memstore load
name|double
name|percent
init|=
name|regionStats
operator|.
name|getMemstoreLoadPercent
argument_list|()
operator|/
literal|100.0
decl_stmt|;
comment|// Factor in heap occupancy
name|float
name|heapOccupancy
init|=
name|regionStats
operator|.
name|getHeapOccupancyPercent
argument_list|()
operator|/
literal|100.0f
decl_stmt|;
comment|// Factor in compaction pressure, 1.0 means heavy compaction pressure
name|float
name|compactionPressure
init|=
name|regionStats
operator|.
name|getCompactionPressure
argument_list|()
operator|/
literal|100.0f
decl_stmt|;
if|if
condition|(
name|heapOccupancy
operator|>=
name|heapOccupancyLowWatermark
condition|)
block|{
comment|// If we are higher than the high watermark, we are already applying max
comment|// backoff and cannot scale more (see scale() below)
if|if
condition|(
name|heapOccupancy
operator|>
name|heapOccupancyHighWatermark
condition|)
block|{
name|heapOccupancy
operator|=
name|heapOccupancyHighWatermark
expr_stmt|;
block|}
name|percent
operator|=
name|Math
operator|.
name|max
argument_list|(
name|percent
argument_list|,
name|scale
argument_list|(
name|heapOccupancy
argument_list|,
name|heapOccupancyLowWatermark
argument_list|,
name|heapOccupancyHighWatermark
argument_list|,
literal|0.1
argument_list|,
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|percent
operator|=
name|Math
operator|.
name|max
argument_list|(
name|percent
argument_list|,
name|compactionPressure
argument_list|)
expr_stmt|;
comment|// square the percent as a value less than 1. Closer we move to 100 percent,
comment|// the percent moves to 1, but squaring causes the exponential curve
name|double
name|multiplier
init|=
name|Math
operator|.
name|pow
argument_list|(
name|percent
argument_list|,
literal|4.0
argument_list|)
decl_stmt|;
if|if
condition|(
name|multiplier
operator|>
literal|1
condition|)
block|{
name|multiplier
operator|=
literal|1
expr_stmt|;
block|}
return|return
call|(
name|long
call|)
argument_list|(
name|multiplier
operator|*
name|maxBackoff
argument_list|)
return|;
block|}
comment|/** Scale valueIn in the range [baseMin,baseMax] to the range [limitMin,limitMax] */
specifier|private
specifier|static
name|double
name|scale
parameter_list|(
name|double
name|valueIn
parameter_list|,
name|double
name|baseMin
parameter_list|,
name|double
name|baseMax
parameter_list|,
name|double
name|limitMin
parameter_list|,
name|double
name|limitMax
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|baseMin
operator|<=
name|baseMax
argument_list|,
literal|"Illegal source range [%s,%s]"
argument_list|,
name|baseMin
argument_list|,
name|baseMax
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|limitMin
operator|<=
name|limitMax
argument_list|,
literal|"Illegal target range [%s,%s]"
argument_list|,
name|limitMin
argument_list|,
name|limitMax
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|valueIn
operator|>=
name|baseMin
operator|&&
name|valueIn
operator|<=
name|baseMax
argument_list|,
literal|"Value %s must be within the range [%s,%s]"
argument_list|,
name|valueIn
argument_list|,
name|baseMin
argument_list|,
name|baseMax
argument_list|)
expr_stmt|;
return|return
operator|(
operator|(
name|limitMax
operator|-
name|limitMin
operator|)
operator|*
operator|(
name|valueIn
operator|-
name|baseMin
operator|)
operator|/
operator|(
name|baseMax
operator|-
name|baseMin
operator|)
operator|)
operator|+
name|limitMin
return|;
block|}
block|}
end_class

end_unit

