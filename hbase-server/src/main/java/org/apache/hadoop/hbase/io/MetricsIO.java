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
name|io
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
name|CompatibilitySingletonFactory
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
name|regionserver
operator|.
name|MetricsRegionServerSourceFactory
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_class
specifier|public
class|class
name|MetricsIO
block|{
specifier|private
specifier|final
name|MetricsIOSource
name|source
decl_stmt|;
specifier|private
specifier|final
name|MetricsIOWrapper
name|wrapper
decl_stmt|;
specifier|public
name|MetricsIO
parameter_list|(
name|MetricsIOWrapper
name|wrapper
parameter_list|)
block|{
name|this
argument_list|(
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsRegionServerSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|createIO
argument_list|(
name|wrapper
argument_list|)
argument_list|,
name|wrapper
argument_list|)
expr_stmt|;
block|}
name|MetricsIO
parameter_list|(
name|MetricsIOSource
name|source
parameter_list|,
name|MetricsIOWrapper
name|wrapper
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|wrapper
operator|=
name|wrapper
expr_stmt|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|MetricsIOSource
name|getMetricsSource
parameter_list|()
block|{
return|return
name|source
return|;
block|}
annotation|@
name|VisibleForTesting
specifier|public
name|MetricsIOWrapper
name|getWrapper
parameter_list|()
block|{
return|return
name|wrapper
return|;
block|}
specifier|public
name|void
name|updateFsReadTime
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|source
operator|.
name|updateFsReadTime
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateFsPreadTime
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|source
operator|.
name|updateFsPReadTime
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateFsWriteTime
parameter_list|(
name|long
name|t
parameter_list|)
block|{
name|source
operator|.
name|updateFsWriteTime
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

