begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|HRegionInfo
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
name|HRegionInterface
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
name|HServerAddress
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
name|io
operator|.
name|Text
import|;
end_import

begin_comment
comment|/**  * Abstract class that performs common operations for   * @see #ProcessRegionClose and @see #ProcessRegionOpen  */
end_comment

begin_class
specifier|abstract
class|class
name|ProcessRegionStatusChange
extends|extends
name|RegionServerOperation
block|{
specifier|protected
specifier|final
name|boolean
name|isMetaTable
decl_stmt|;
specifier|protected
specifier|final
name|HRegionInfo
name|regionInfo
decl_stmt|;
specifier|private
name|MetaRegion
name|metaRegion
decl_stmt|;
specifier|protected
name|Text
name|metaRegionName
decl_stmt|;
comment|/**    * @param regionInfo    */
specifier|public
name|ProcessRegionStatusChange
parameter_list|(
name|HMaster
name|master
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
name|super
argument_list|(
name|master
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
expr_stmt|;
name|this
operator|.
name|isMetaTable
operator|=
name|regionInfo
operator|.
name|isMetaTable
argument_list|()
expr_stmt|;
name|this
operator|.
name|metaRegion
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|metaRegionName
operator|=
literal|null
expr_stmt|;
block|}
specifier|protected
name|boolean
name|metaRegionAvailable
parameter_list|()
block|{
name|boolean
name|available
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|isMetaTable
condition|)
block|{
comment|// This operation is for the meta table
if|if
condition|(
operator|!
name|rootAvailable
argument_list|()
condition|)
block|{
comment|// But we can't proceed unless the root region is available
name|available
operator|=
literal|false
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
operator|!
name|master
operator|.
name|rootScanned
operator|||
operator|!
name|metaTableAvailable
argument_list|()
condition|)
block|{
comment|// The root region has not been scanned or the meta table is not
comment|// available so we can't proceed.
comment|// Put the operation on the delayedToDoQueue
name|requeue
argument_list|()
expr_stmt|;
name|available
operator|=
literal|false
expr_stmt|;
block|}
block|}
return|return
name|available
return|;
block|}
specifier|protected
name|HRegionInterface
name|getMetaServer
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|isMetaTable
condition|)
block|{
name|this
operator|.
name|metaRegionName
operator|=
name|HRegionInfo
operator|.
name|rootRegionInfo
operator|.
name|getRegionName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|this
operator|.
name|metaRegion
operator|==
literal|null
condition|)
block|{
synchronized|synchronized
init|(
name|master
operator|.
name|onlineMetaRegions
init|)
block|{
name|metaRegion
operator|=
name|master
operator|.
name|onlineMetaRegions
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|?
name|master
operator|.
name|onlineMetaRegions
operator|.
name|get
argument_list|(
name|master
operator|.
name|onlineMetaRegions
operator|.
name|firstKey
argument_list|()
argument_list|)
else|:
name|master
operator|.
name|onlineMetaRegions
operator|.
name|containsKey
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
condition|?
name|master
operator|.
name|onlineMetaRegions
operator|.
name|get
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
else|:
name|master
operator|.
name|onlineMetaRegions
operator|.
name|get
argument_list|(
name|master
operator|.
name|onlineMetaRegions
operator|.
name|headMap
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
operator|.
name|lastKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|metaRegionName
operator|=
name|metaRegion
operator|.
name|getRegionName
argument_list|()
expr_stmt|;
block|}
block|}
name|HServerAddress
name|server
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|isMetaTable
condition|)
block|{
name|server
operator|=
name|master
operator|.
name|rootRegionLocation
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|server
operator|=
name|metaRegion
operator|.
name|getServer
argument_list|()
expr_stmt|;
block|}
return|return
name|master
operator|.
name|connection
operator|.
name|getHRegionConnection
argument_list|(
name|server
argument_list|)
return|;
block|}
block|}
end_class

end_unit

