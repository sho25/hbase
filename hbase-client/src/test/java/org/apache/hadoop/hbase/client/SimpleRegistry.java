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
name|DoNotRetryIOException
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
name|HConstants
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
name|HRegionLocation
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
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|FutureUtils
import|;
end_import

begin_comment
comment|/**  * Simple cluster registry inserted in place of our usual zookeeper based one.  */
end_comment

begin_class
class|class
name|SimpleRegistry
extends|extends
name|DoNothingAsyncRegistry
block|{
specifier|private
specifier|final
name|ServerName
name|metaHost
decl_stmt|;
specifier|volatile
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|META_HOST_CONFIG_NAME
init|=
literal|"hbase.client.simple-registry.meta.host"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_META_HOST
init|=
literal|"meta.example.org.16010,12345"
decl_stmt|;
specifier|public
specifier|static
name|void
name|setMetaHost
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ServerName
name|metaHost
parameter_list|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|META_HOST_CONFIG_NAME
argument_list|,
name|metaHost
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|SimpleRegistry
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|metaHost
operator|=
name|ServerName
operator|.
name|valueOf
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|META_HOST_CONFIG_NAME
argument_list|,
name|DEFAULT_META_HOST
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
if|if
condition|(
name|closed
condition|)
block|{
return|return
name|FutureUtils
operator|.
name|failedFuture
argument_list|(
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Client already closed"
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|CompletableFuture
operator|.
name|completedFuture
argument_list|(
operator|new
name|RegionLocations
argument_list|(
operator|new
name|HRegionLocation
argument_list|(
name|RegionInfoBuilder
operator|.
name|FIRST_META_REGIONINFO
argument_list|,
name|metaHost
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
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
if|if
condition|(
name|closed
condition|)
block|{
return|return
name|FutureUtils
operator|.
name|failedFuture
argument_list|(
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Client already closed"
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|CompletableFuture
operator|.
name|completedFuture
argument_list|(
name|HConstants
operator|.
name|CLUSTER_ID_DEFAULT
argument_list|)
return|;
block|}
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
if|if
condition|(
name|closed
condition|)
block|{
return|return
name|FutureUtils
operator|.
name|failedFuture
argument_list|(
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Client already closed"
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|CompletableFuture
operator|.
name|completedFuture
argument_list|(
literal|1
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|closed
operator|=
literal|true
expr_stmt|;
block|}
block|}
end_class

end_unit

