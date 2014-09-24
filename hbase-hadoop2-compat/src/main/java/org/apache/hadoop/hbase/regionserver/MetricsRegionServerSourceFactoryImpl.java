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
name|regionserver
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

begin_comment
comment|/**  * Factory to create MetricsRegionServerSource when given a  MetricsRegionServerWrapper  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsRegionServerSourceFactoryImpl
implements|implements
name|MetricsRegionServerSourceFactory
block|{
specifier|public
specifier|static
enum|enum
name|FactoryStorage
block|{
name|INSTANCE
block|;
specifier|private
name|Object
name|aggLock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|private
name|MetricsRegionAggregateSourceImpl
name|aggImpl
decl_stmt|;
block|}
specifier|private
specifier|synchronized
name|MetricsRegionAggregateSourceImpl
name|getAggregate
parameter_list|()
block|{
synchronized|synchronized
init|(
name|FactoryStorage
operator|.
name|INSTANCE
operator|.
name|aggLock
init|)
block|{
if|if
condition|(
name|FactoryStorage
operator|.
name|INSTANCE
operator|.
name|aggImpl
operator|==
literal|null
condition|)
block|{
name|FactoryStorage
operator|.
name|INSTANCE
operator|.
name|aggImpl
operator|=
operator|new
name|MetricsRegionAggregateSourceImpl
argument_list|()
expr_stmt|;
block|}
return|return
name|FactoryStorage
operator|.
name|INSTANCE
operator|.
name|aggImpl
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|MetricsRegionServerSource
name|createServer
parameter_list|(
name|MetricsRegionServerWrapper
name|regionServerWrapper
parameter_list|)
block|{
return|return
operator|new
name|MetricsRegionServerSourceImpl
argument_list|(
name|regionServerWrapper
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|MetricsRegionSource
name|createRegion
parameter_list|(
name|MetricsRegionWrapper
name|wrapper
parameter_list|)
block|{
return|return
operator|new
name|MetricsRegionSourceImpl
argument_list|(
name|wrapper
argument_list|,
name|getAggregate
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

