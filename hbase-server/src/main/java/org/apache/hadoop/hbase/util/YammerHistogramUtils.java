begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Histogram
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Reservoir
import|;
end_import

begin_import
import|import
name|com
operator|.
name|codahale
operator|.
name|metrics
operator|.
name|Snapshot
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|DecimalFormat
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
comment|/** Utility functions for working with Yammer Metrics. */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|YammerHistogramUtils
block|{
comment|// not for public consumption
specifier|private
name|YammerHistogramUtils
parameter_list|()
block|{}
comment|/**    * Used formatting doubles so only two places after decimal point.    */
specifier|private
specifier|static
name|DecimalFormat
name|DOUBLE_FORMAT
init|=
operator|new
name|DecimalFormat
argument_list|(
literal|"#0.00"
argument_list|)
decl_stmt|;
comment|/**    * Create a new {@link com.codahale.metrics.Histogram} instance. These constructors are    * not public in 2.2.0, so we use reflection to find them.    */
specifier|public
specifier|static
name|Histogram
name|newHistogram
parameter_list|(
name|Reservoir
name|sample
parameter_list|)
block|{
try|try
block|{
name|Constructor
argument_list|<
name|?
argument_list|>
name|ctor
init|=
name|Histogram
operator|.
name|class
operator|.
name|getDeclaredConstructor
argument_list|(
name|Reservoir
operator|.
name|class
argument_list|)
decl_stmt|;
name|ctor
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
operator|(
name|Histogram
operator|)
name|ctor
operator|.
name|newInstance
argument_list|(
name|sample
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|/** @return an abbreviated summary of {@code hist}. */
specifier|public
specifier|static
name|String
name|getShortHistogramReport
parameter_list|(
specifier|final
name|Histogram
name|hist
parameter_list|)
block|{
name|Snapshot
name|sn
init|=
name|hist
operator|.
name|getSnapshot
argument_list|()
decl_stmt|;
return|return
literal|"mean="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getMean
argument_list|()
argument_list|)
operator|+
literal|", min="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getMin
argument_list|()
argument_list|)
operator|+
literal|", max="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getMax
argument_list|()
argument_list|)
operator|+
literal|", stdDev="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getStdDev
argument_list|()
argument_list|)
operator|+
literal|", 95th="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|get95thPercentile
argument_list|()
argument_list|)
operator|+
literal|", 99th="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|get99thPercentile
argument_list|()
argument_list|)
return|;
block|}
comment|/** @return a summary of {@code hist}. */
specifier|public
specifier|static
name|String
name|getHistogramReport
parameter_list|(
specifier|final
name|Histogram
name|hist
parameter_list|)
block|{
name|Snapshot
name|sn
init|=
name|hist
operator|.
name|getSnapshot
argument_list|()
decl_stmt|;
return|return
literal|"mean="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getMean
argument_list|()
argument_list|)
operator|+
literal|", min="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getMin
argument_list|()
argument_list|)
operator|+
literal|", max="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getMax
argument_list|()
argument_list|)
operator|+
literal|", stdDev="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getStdDev
argument_list|()
argument_list|)
operator|+
literal|", 50th="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getMedian
argument_list|()
argument_list|)
operator|+
literal|", 75th="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|get75thPercentile
argument_list|()
argument_list|)
operator|+
literal|", 95th="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|get95thPercentile
argument_list|()
argument_list|)
operator|+
literal|", 99th="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|get99thPercentile
argument_list|()
argument_list|)
operator|+
literal|", 99.9th="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|get999thPercentile
argument_list|()
argument_list|)
operator|+
literal|", 99.99th="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getValue
argument_list|(
literal|0.9999
argument_list|)
argument_list|)
operator|+
literal|", 99.999th="
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getValue
argument_list|(
literal|0.99999
argument_list|)
argument_list|)
return|;
block|}
comment|/** @return pretty summary of {@code hist}. */
specifier|public
specifier|static
name|String
name|getPrettyHistogramReport
parameter_list|(
specifier|final
name|Histogram
name|h
parameter_list|)
block|{
name|Snapshot
name|sn
init|=
name|h
operator|.
name|getSnapshot
argument_list|()
decl_stmt|;
return|return
literal|"Mean      = "
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getMean
argument_list|()
argument_list|)
operator|+
literal|"\n"
operator|+
literal|"Min       = "
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getMin
argument_list|()
argument_list|)
operator|+
literal|"\n"
operator|+
literal|"Max       = "
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getMax
argument_list|()
argument_list|)
operator|+
literal|"\n"
operator|+
literal|"StdDev    = "
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getStdDev
argument_list|()
argument_list|)
operator|+
literal|"\n"
operator|+
literal|"50th      = "
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getMedian
argument_list|()
argument_list|)
operator|+
literal|"\n"
operator|+
literal|"75th      = "
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|get75thPercentile
argument_list|()
argument_list|)
operator|+
literal|"\n"
operator|+
literal|"95th      = "
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|get95thPercentile
argument_list|()
argument_list|)
operator|+
literal|"\n"
operator|+
literal|"99th      = "
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|get99thPercentile
argument_list|()
argument_list|)
operator|+
literal|"\n"
operator|+
literal|"99.9th    = "
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|get999thPercentile
argument_list|()
argument_list|)
operator|+
literal|"\n"
operator|+
literal|"99.99th   = "
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getValue
argument_list|(
literal|0.9999
argument_list|)
argument_list|)
operator|+
literal|"\n"
operator|+
literal|"99.999th  = "
operator|+
name|DOUBLE_FORMAT
operator|.
name|format
argument_list|(
name|sn
operator|.
name|getValue
argument_list|(
literal|0.99999
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

