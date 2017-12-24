begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|metrics
operator|.
name|impl
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
name|metrics
operator|.
name|Histogram
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
name|metrics
operator|.
name|Snapshot
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
comment|/**  * Custom histogram implementation based on FastLongHistogram. Dropwizard-based histograms are  * slow compared to this implementation, so we are using our implementation here.  * See HBASE-15222.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HistogramImpl
implements|implements
name|Histogram
block|{
comment|// Double buffer the two FastLongHistograms.
comment|// As they are reset they learn how the buckets should be spaced
comment|// So keep two around and use them
specifier|protected
specifier|final
name|FastLongHistogram
name|histogram
decl_stmt|;
specifier|private
specifier|final
name|CounterImpl
name|counter
decl_stmt|;
specifier|public
name|HistogramImpl
parameter_list|()
block|{
name|this
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
operator|<<
literal|2
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HistogramImpl
parameter_list|(
name|long
name|maxExpected
parameter_list|)
block|{
name|this
argument_list|(
name|FastLongHistogram
operator|.
name|DEFAULT_NBINS
argument_list|,
literal|1
argument_list|,
name|maxExpected
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HistogramImpl
parameter_list|(
name|int
name|numBins
parameter_list|,
name|long
name|min
parameter_list|,
name|long
name|maxExpected
parameter_list|)
block|{
name|this
operator|.
name|counter
operator|=
operator|new
name|CounterImpl
argument_list|()
expr_stmt|;
name|this
operator|.
name|histogram
operator|=
operator|new
name|FastLongHistogram
argument_list|(
name|numBins
argument_list|,
name|min
argument_list|,
name|maxExpected
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|HistogramImpl
parameter_list|(
name|CounterImpl
name|counter
parameter_list|,
name|FastLongHistogram
name|histogram
parameter_list|)
block|{
name|this
operator|.
name|counter
operator|=
name|counter
expr_stmt|;
name|this
operator|.
name|histogram
operator|=
name|histogram
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|update
parameter_list|(
name|int
name|value
parameter_list|)
block|{
name|counter
operator|.
name|increment
argument_list|()
expr_stmt|;
name|histogram
operator|.
name|add
argument_list|(
name|value
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|update
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|counter
operator|.
name|increment
argument_list|()
expr_stmt|;
name|histogram
operator|.
name|add
argument_list|(
name|value
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|public
name|long
name|getCount
parameter_list|()
block|{
return|return
name|counter
operator|.
name|getCount
argument_list|()
return|;
block|}
specifier|public
name|long
name|getMax
parameter_list|()
block|{
return|return
name|this
operator|.
name|histogram
operator|.
name|getMax
argument_list|()
return|;
block|}
specifier|public
name|Snapshot
name|snapshot
parameter_list|()
block|{
return|return
name|histogram
operator|.
name|snapshotAndReset
argument_list|()
return|;
block|}
block|}
end_class

end_unit

