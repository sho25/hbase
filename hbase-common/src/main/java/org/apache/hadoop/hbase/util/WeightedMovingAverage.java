begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
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

begin_comment
comment|/**  * Different from SMA {@link SimpleMovingAverage}, WeightedMovingAverage gives each data different  * weight. And it is based on {@link WindowMovingAverage}, such that it only focus on the last N.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|WeightedMovingAverage
extends|extends
name|WindowMovingAverage
block|{
specifier|private
name|int
index|[]
name|coefficient
decl_stmt|;
specifier|private
name|int
name|denominator
decl_stmt|;
specifier|public
name|WeightedMovingAverage
parameter_list|()
block|{
name|this
argument_list|(
name|DEFAULT_SIZE
argument_list|)
expr_stmt|;
block|}
specifier|public
name|WeightedMovingAverage
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|super
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|int
name|length
init|=
name|getNumberOfStatistics
argument_list|()
decl_stmt|;
name|denominator
operator|=
name|length
operator|*
operator|(
name|length
operator|+
literal|1
operator|)
operator|/
literal|2
expr_stmt|;
name|coefficient
operator|=
operator|new
name|int
index|[
name|length
index|]
expr_stmt|;
comment|// E.g. default size is 5, coefficient should be [1, 2, 3, 4, 5]
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
name|coefficient
index|[
name|i
index|]
operator|=
name|i
operator|+
literal|1
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|double
name|getAverageTime
parameter_list|()
block|{
if|if
condition|(
operator|!
name|enoughStatistics
argument_list|()
condition|)
block|{
return|return
name|super
operator|.
name|getAverageTime
argument_list|()
return|;
block|}
comment|// only we get enough statistics, then start WMA.
name|double
name|average
init|=
literal|0.0
decl_stmt|;
name|int
name|coIndex
init|=
literal|0
decl_stmt|;
name|int
name|length
init|=
name|getNumberOfStatistics
argument_list|()
decl_stmt|;
comment|// tmIndex, it points to the oldest data.
for|for
control|(
name|int
name|tmIndex
init|=
operator|(
name|getMostRecentPosistion
argument_list|()
operator|+
literal|1
operator|)
operator|%
name|length
init|;
name|coIndex
operator|<
name|length
condition|;
name|coIndex
operator|++
operator|,
name|tmIndex
operator|=
operator|(
operator|++
name|tmIndex
operator|)
operator|%
name|length
control|)
block|{
comment|// start the multiplication from oldest to newest
name|average
operator|+=
name|coefficient
index|[
name|coIndex
index|]
operator|*
name|getStatisticsAtIndex
argument_list|(
name|tmIndex
argument_list|)
expr_stmt|;
block|}
return|return
name|average
operator|/
name|denominator
return|;
block|}
block|}
end_class

end_unit

