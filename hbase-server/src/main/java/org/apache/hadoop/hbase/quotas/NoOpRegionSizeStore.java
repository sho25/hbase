begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|quotas
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
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
name|client
operator|.
name|RegionInfo
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
comment|/**  * A {@link RegionSizeStore} implementation that stores nothing.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|NoOpRegionSizeStore
implements|implements
name|RegionSizeStore
block|{
specifier|private
specifier|static
specifier|final
name|NoOpRegionSizeStore
name|INSTANCE
init|=
operator|new
name|NoOpRegionSizeStore
argument_list|()
decl_stmt|;
specifier|private
name|NoOpRegionSizeStore
parameter_list|()
block|{}
specifier|public
specifier|static
name|NoOpRegionSizeStore
name|getInstance
parameter_list|()
block|{
return|return
name|INSTANCE
return|;
block|}
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|RegionInfo
argument_list|,
name|RegionSize
argument_list|>
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|RegionSize
name|getRegionSize
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|put
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|,
name|long
name|size
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|void
name|incrementRegionSize
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|,
name|long
name|delta
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|RegionSize
name|remove
parameter_list|(
name|RegionInfo
name|regionInfo
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clear
parameter_list|()
block|{}
block|}
end_class

end_unit

