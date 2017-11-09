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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CompletableFuture
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
name|conf
operator|.
name|Configuration
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
name|RegionLocations
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
name|ServerName
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
comment|/**  * Registry that does nothing. Otherwise, default Registry wants zookeeper up and running.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|DoNothingAsyncRegistry
implements|implements
name|AsyncRegistry
block|{
specifier|public
name|DoNothingAsyncRegistry
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|RegionLocations
argument_list|>
name|getMetaRegionLocation
parameter_list|()
block|{
return|return
name|CompletableFuture
operator|.
name|completedFuture
argument_list|(
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|String
argument_list|>
name|getClusterId
parameter_list|()
block|{
return|return
name|CompletableFuture
operator|.
name|completedFuture
argument_list|(
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Integer
argument_list|>
name|getCurrentNrHRS
parameter_list|()
block|{
return|return
name|CompletableFuture
operator|.
name|completedFuture
argument_list|(
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|ServerName
argument_list|>
name|getMasterAddress
parameter_list|()
block|{
return|return
name|CompletableFuture
operator|.
name|completedFuture
argument_list|(
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompletableFuture
argument_list|<
name|Integer
argument_list|>
name|getMasterInfoPort
parameter_list|()
block|{
return|return
name|CompletableFuture
operator|.
name|completedFuture
argument_list|(
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{   }
block|}
end_class

end_unit
