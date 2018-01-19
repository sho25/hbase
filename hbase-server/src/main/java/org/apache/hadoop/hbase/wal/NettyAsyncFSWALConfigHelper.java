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
name|wal
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|util
operator|.
name|Pair
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|Channel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|EventLoopGroup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|nio
operator|.
name|NioEventLoopGroup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|socket
operator|.
name|nio
operator|.
name|NioSocketChannel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|concurrent
operator|.
name|DefaultThreadFactory
import|;
end_import

begin_comment
comment|/**  * Helper class for passing netty event loop config to {@link AsyncFSWALProvider}.  * @since 2.0.0  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|NettyAsyncFSWALConfigHelper
block|{
specifier|private
specifier|static
specifier|final
name|String
name|EVENT_LOOP_CONFIG
init|=
literal|"hbase.wal.async.event-loop.config"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|CONFIG_NAME
init|=
literal|"global-event-loop"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Pair
argument_list|<
name|EventLoopGroup
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
argument_list|>
argument_list|>
name|EVENT_LOOP_CONFIG_MAP
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * Set the EventLoopGroup and channel class for {@code AsyncFSWALProvider}.    */
specifier|public
specifier|static
name|void
name|setEventLoopConfig
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|EventLoopGroup
name|group
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
name|channelClass
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|group
argument_list|,
literal|"group is null"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|channelClass
argument_list|,
literal|"channel class is null"
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|EVENT_LOOP_CONFIG
argument_list|,
name|CONFIG_NAME
argument_list|)
expr_stmt|;
name|EVENT_LOOP_CONFIG_MAP
operator|.
name|put
argument_list|(
name|CONFIG_NAME
argument_list|,
name|Pair
operator|.
expr|<
name|EventLoopGroup
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
operator|>
name|newPair
argument_list|(
name|group
argument_list|,
name|channelClass
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|static
name|Pair
argument_list|<
name|EventLoopGroup
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
argument_list|>
name|getEventLoopConfig
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|name
init|=
name|conf
operator|.
name|get
argument_list|(
name|EVENT_LOOP_CONFIG
argument_list|)
decl_stmt|;
if|if
condition|(
name|StringUtils
operator|.
name|isBlank
argument_list|(
name|name
argument_list|)
condition|)
block|{
comment|// create new event loop group if config is empty
return|return
name|Pair
operator|.
expr|<
name|EventLoopGroup
operator|,
name|Class
argument_list|<
name|?
extends|extends
name|Channel
argument_list|>
operator|>
name|newPair
argument_list|(
operator|new
name|NioEventLoopGroup
argument_list|(
literal|0
argument_list|,
operator|new
name|DefaultThreadFactory
argument_list|(
literal|"AsyncFSWAL"
argument_list|,
literal|true
argument_list|,
name|Thread
operator|.
name|MAX_PRIORITY
argument_list|)
argument_list|)
argument_list|,
name|NioSocketChannel
operator|.
name|class
argument_list|)
return|;
block|}
return|return
name|EVENT_LOOP_CONFIG_MAP
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
specifier|private
name|NettyAsyncFSWALConfigHelper
parameter_list|()
block|{}
block|}
end_class

end_unit

