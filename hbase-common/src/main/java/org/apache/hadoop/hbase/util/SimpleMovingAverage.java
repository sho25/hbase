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
comment|/**  * SMA measure the overall average execution time of a specific method.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SimpleMovingAverage
extends|extends
name|MovingAverage
block|{
specifier|private
name|double
name|averageTime
init|=
literal|0.0
decl_stmt|;
specifier|protected
name|long
name|count
init|=
literal|0
decl_stmt|;
specifier|public
name|SimpleMovingAverage
parameter_list|(
name|String
name|label
parameter_list|)
block|{
name|super
argument_list|(
name|label
argument_list|)
expr_stmt|;
name|this
operator|.
name|averageTime
operator|=
literal|0.0
expr_stmt|;
name|this
operator|.
name|count
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateMostRecentTime
parameter_list|(
name|long
name|elapsed
parameter_list|)
block|{
name|averageTime
operator|+=
operator|(
name|elapsed
operator|-
name|averageTime
operator|)
operator|/
operator|(
operator|++
name|count
operator|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getAverageTime
parameter_list|()
block|{
return|return
name|averageTime
return|;
block|}
block|}
end_class

end_unit
