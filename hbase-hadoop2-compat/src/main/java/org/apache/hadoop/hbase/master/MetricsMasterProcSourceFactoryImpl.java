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
name|master
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
comment|/**  * Factory to create MetricsMasterProcSource when given a MetricsMasterWrapper  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsMasterProcSourceFactoryImpl
implements|implements
name|MetricsMasterProcSourceFactory
block|{
specifier|private
name|MetricsMasterProcSource
name|masterProcSource
decl_stmt|;
annotation|@
name|Override
specifier|public
specifier|synchronized
name|MetricsMasterProcSource
name|create
parameter_list|(
name|MetricsMasterWrapper
name|masterWrapper
parameter_list|)
block|{
if|if
condition|(
name|masterProcSource
operator|==
literal|null
condition|)
block|{
name|masterProcSource
operator|=
operator|new
name|MetricsMasterProcSourceImpl
argument_list|(
name|masterWrapper
argument_list|)
expr_stmt|;
block|}
return|return
name|masterProcSource
return|;
block|}
block|}
end_class

end_unit

