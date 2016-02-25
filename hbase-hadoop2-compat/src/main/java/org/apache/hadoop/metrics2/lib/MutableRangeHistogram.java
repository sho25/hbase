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
name|metrics2
operator|.
name|lib
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
name|util
operator|.
name|FastLongHistogram
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
name|metrics2
operator|.
name|MetricHistogram
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
name|metrics2
operator|.
name|MetricsInfo
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
name|metrics2
operator|.
name|MetricsRecordBuilder
import|;
end_import

begin_comment
comment|/**  * Extended histogram implementation with metric range counters.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|MutableRangeHistogram
extends|extends
name|MutableHistogram
implements|implements
name|MetricHistogram
block|{
specifier|public
name|MutableRangeHistogram
parameter_list|(
name|MetricsInfo
name|info
parameter_list|)
block|{
name|this
argument_list|(
name|info
operator|.
name|name
argument_list|()
argument_list|,
name|info
operator|.
name|description
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MutableRangeHistogram
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|description
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|description
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
operator|<<
literal|2
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MutableRangeHistogram
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|description
parameter_list|,
name|long
name|expectedMax
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|description
argument_list|,
name|expectedMax
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns the type of range histogram size or time     */
specifier|public
specifier|abstract
name|String
name|getRangeType
parameter_list|()
function_decl|;
comment|/**    * Returns the ranges to be counted     */
specifier|public
specifier|abstract
name|long
index|[]
name|getRanges
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|snapshot
parameter_list|(
name|MetricsRecordBuilder
name|metricsRecordBuilder
parameter_list|,
name|boolean
name|all
parameter_list|)
block|{
comment|// Get a reference to the old histogram.
name|FastLongHistogram
name|histo
init|=
name|histogram
operator|.
name|reset
argument_list|()
decl_stmt|;
if|if
condition|(
name|histo
operator|!=
literal|null
condition|)
block|{
name|updateSnapshotMetrics
argument_list|(
name|metricsRecordBuilder
argument_list|,
name|histo
argument_list|)
expr_stmt|;
name|updateSnapshotRangeMetrics
argument_list|(
name|metricsRecordBuilder
argument_list|,
name|histo
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|updateSnapshotRangeMetrics
parameter_list|(
name|MetricsRecordBuilder
name|metricsRecordBuilder
parameter_list|,
name|FastLongHistogram
name|histogram
parameter_list|)
block|{
name|long
name|priorRange
init|=
literal|0
decl_stmt|;
name|long
name|cumNum
init|=
literal|0
decl_stmt|;
specifier|final
name|long
index|[]
name|ranges
init|=
name|getRanges
argument_list|()
decl_stmt|;
specifier|final
name|String
name|rangeType
init|=
name|getRangeType
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ranges
operator|.
name|length
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|long
name|val
init|=
name|histogram
operator|.
name|getNumAtOrBelow
argument_list|(
name|ranges
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|-
name|cumNum
operator|>
literal|0
condition|)
block|{
name|metricsRecordBuilder
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
operator|+
literal|"_"
operator|+
name|rangeType
operator|+
literal|"_"
operator|+
name|priorRange
operator|+
literal|"-"
operator|+
name|ranges
index|[
name|i
index|]
argument_list|,
name|desc
argument_list|)
argument_list|,
name|val
operator|-
name|cumNum
argument_list|)
expr_stmt|;
block|}
name|priorRange
operator|=
name|ranges
index|[
name|i
index|]
expr_stmt|;
name|cumNum
operator|=
name|val
expr_stmt|;
block|}
name|long
name|val
init|=
name|histogram
operator|.
name|getCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|val
operator|-
name|cumNum
operator|>
literal|0
condition|)
block|{
name|metricsRecordBuilder
operator|.
name|addCounter
argument_list|(
name|Interns
operator|.
name|info
argument_list|(
name|name
operator|+
literal|"_"
operator|+
name|rangeType
operator|+
literal|"_"
operator|+
name|ranges
index|[
name|ranges
operator|.
name|length
operator|-
literal|1
index|]
operator|+
literal|"-inf"
argument_list|,
name|desc
argument_list|)
argument_list|,
name|val
operator|-
name|cumNum
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

