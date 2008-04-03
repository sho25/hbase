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
name|regionserver
operator|.
name|HRegion
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
name|RemoteExceptionHandler
import|;
end_import

begin_comment
comment|/**  * ProcessRegionClose is the way we do post-processing on a closed region. We  * only spawn one of these asynchronous tasks when the region needs to be   * either offlined or deleted. We used to create one of these tasks whenever  * a region was closed, but since closing a region that isn't being offlined  * or deleted doesn't actually require post processing, it's no longer   * necessary.  */
end_comment

begin_class
class|class
name|ProcessRegionClose
extends|extends
name|ProcessRegionStatusChange
block|{
specifier|private
name|boolean
name|offlineRegion
decl_stmt|;
specifier|private
name|boolean
name|deleteRegion
decl_stmt|;
comment|/**   * @param master   * @param regionInfo Region to operate on   * @param offlineRegion if true, set the region to offline in meta   * @param deleteRegion if true, delete the region row from meta and then   * delete the region files from disk.   */
specifier|public
name|ProcessRegionClose
parameter_list|(
name|HMaster
name|master
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|boolean
name|offlineRegion
parameter_list|,
name|boolean
name|deleteRegion
parameter_list|)
block|{
name|super
argument_list|(
name|master
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
name|this
operator|.
name|offlineRegion
operator|=
name|offlineRegion
expr_stmt|;
name|this
operator|.
name|deleteRegion
operator|=
name|deleteRegion
expr_stmt|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"ProcessRegionClose of "
operator|+
name|this
operator|.
name|regionInfo
operator|.
name|getRegionName
argument_list|()
operator|+
literal|", "
operator|+
name|this
operator|.
name|offlineRegion
operator|+
literal|", "
operator|+
name|this
operator|.
name|deleteRegion
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|process
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|tries
init|=
literal|0
init|;
name|tries
operator|<
name|numRetries
condition|;
name|tries
operator|++
control|)
block|{
if|if
condition|(
name|master
operator|.
name|closed
operator|.
name|get
argument_list|()
condition|)
block|{
return|return
literal|true
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"region closed: "
operator|+
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Mark the Region as unavailable in the appropriate meta table
if|if
condition|(
operator|!
name|metaRegionAvailable
argument_list|()
condition|)
block|{
comment|// We can't proceed unless the meta region we are going to update
comment|// is online. metaRegionAvailable() has put this operation on the
comment|// delayedToDoQueue, so return true so the operation is not put
comment|// back on the toDoQueue
return|return
literal|true
return|;
block|}
try|try
block|{
if|if
condition|(
name|deleteRegion
condition|)
block|{
name|HRegion
operator|.
name|removeRegionFromMETA
argument_list|(
name|getMetaServer
argument_list|()
argument_list|,
name|metaRegionName
argument_list|,
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|offlineRegion
condition|)
block|{
comment|// offline the region in meta and then note that we've offlined the
comment|// region.
name|HRegion
operator|.
name|offlineRegionInMETA
argument_list|(
name|getMetaServer
argument_list|()
argument_list|,
name|metaRegionName
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
name|master
operator|.
name|regionManager
operator|.
name|regionOfflined
argument_list|(
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|tries
operator|==
name|numRetries
operator|-
literal|1
condition|)
block|{
throw|throw
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
continue|continue;
block|}
block|}
comment|// now that meta is updated, if we need to delete the region's files, now's
comment|// the time.
if|if
condition|(
name|deleteRegion
condition|)
block|{
try|try
block|{
name|HRegion
operator|.
name|deleteRegion
argument_list|(
name|master
operator|.
name|fs
argument_list|,
name|master
operator|.
name|rootdir
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|=
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|error
argument_list|(
literal|"failed delete region "
operator|+
name|regionInfo
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

