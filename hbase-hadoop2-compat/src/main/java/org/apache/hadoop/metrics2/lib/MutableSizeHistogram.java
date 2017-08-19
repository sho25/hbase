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
name|metrics2
operator|.
name|MetricsInfo
import|;
end_import

begin_comment
comment|/**  * Extended histogram implementation with counters for metric size ranges.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MutableSizeHistogram
extends|extends
name|MutableRangeHistogram
block|{
specifier|private
specifier|final
specifier|static
name|String
name|RANGE_TYPE
init|=
literal|"SizeRangeCount"
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|long
index|[]
name|RANGES
init|=
block|{
literal|10
block|,
literal|100
block|,
literal|1000
block|,
literal|10000
block|,
literal|100000
block|,
literal|1000000
block|,
literal|10000000
block|,
literal|100000000
block|}
decl_stmt|;
specifier|public
name|MutableSizeHistogram
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
name|MutableSizeHistogram
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
name|RANGES
index|[
name|RANGES
operator|.
name|length
operator|-
literal|2
index|]
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MutableSizeHistogram
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
annotation|@
name|Override
specifier|public
name|String
name|getRangeType
parameter_list|()
block|{
return|return
name|RANGE_TYPE
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
index|[]
name|getRanges
parameter_list|()
block|{
return|return
name|RANGES
return|;
block|}
block|}
end_class

end_unit

