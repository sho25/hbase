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
operator|.
name|balancer
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
name|RegionLoad
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
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Wrapper class for the few fields required by the {@link StochasticLoadBalancer}  * from the full {@link RegionLoad}.  */
end_comment

begin_class
annotation|@
name|InterfaceStability
operator|.
name|Evolving
class|class
name|BalancerRegionLoad
block|{
specifier|private
specifier|final
name|long
name|readRequestsCount
decl_stmt|;
specifier|private
specifier|final
name|long
name|writeRequestsCount
decl_stmt|;
specifier|private
specifier|final
name|int
name|memStoreSizeMB
decl_stmt|;
specifier|private
specifier|final
name|int
name|storefileSizeMB
decl_stmt|;
name|BalancerRegionLoad
parameter_list|(
name|RegionLoad
name|regionLoad
parameter_list|)
block|{
name|readRequestsCount
operator|=
name|regionLoad
operator|.
name|getReadRequestsCount
argument_list|()
expr_stmt|;
name|writeRequestsCount
operator|=
name|regionLoad
operator|.
name|getWriteRequestsCount
argument_list|()
expr_stmt|;
name|memStoreSizeMB
operator|=
name|regionLoad
operator|.
name|getMemStoreSizeMB
argument_list|()
expr_stmt|;
name|storefileSizeMB
operator|=
name|regionLoad
operator|.
name|getStorefileSizeMB
argument_list|()
expr_stmt|;
block|}
specifier|public
name|long
name|getReadRequestsCount
parameter_list|()
block|{
return|return
name|readRequestsCount
return|;
block|}
specifier|public
name|long
name|getWriteRequestsCount
parameter_list|()
block|{
return|return
name|writeRequestsCount
return|;
block|}
specifier|public
name|int
name|getMemStoreSizeMB
parameter_list|()
block|{
return|return
name|memStoreSizeMB
return|;
block|}
specifier|public
name|int
name|getStorefileSizeMB
parameter_list|()
block|{
return|return
name|storefileSizeMB
return|;
block|}
block|}
end_class

end_unit

