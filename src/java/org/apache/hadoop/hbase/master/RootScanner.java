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
name|RemoteExceptionHandler
import|;
end_import

begin_comment
comment|/** Scanner for the<code>ROOT</code> HRegion. */
end_comment

begin_class
class|class
name|RootScanner
extends|extends
name|BaseScanner
block|{
comment|/**    * Constructor    *     * @param master    * @param regionManager    */
specifier|public
name|RootScanner
parameter_list|(
name|HMaster
name|master
parameter_list|,
name|RegionManager
name|regionManager
parameter_list|)
block|{
name|super
argument_list|(
name|master
argument_list|,
name|regionManager
argument_list|,
literal|true
argument_list|,
name|master
operator|.
name|metaRescanInterval
argument_list|,
name|master
operator|.
name|closed
argument_list|)
expr_stmt|;
block|}
comment|// Don't retry if we get an error while scanning. Errors are most often
comment|// caused by the server going away. Wait until next rescan interval when
comment|// things should be back to normal
specifier|private
name|boolean
name|scanRoot
parameter_list|()
block|{
name|boolean
name|scanSuccessful
init|=
literal|false
decl_stmt|;
name|master
operator|.
name|waitForRootRegionLocation
argument_list|()
expr_stmt|;
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
name|scanSuccessful
return|;
block|}
try|try
block|{
comment|// Don't interrupt us while we're working
synchronized|synchronized
init|(
name|scannerLock
init|)
block|{
if|if
condition|(
name|master
operator|.
name|getRootRegionLocation
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|scanRegion
argument_list|(
operator|new
name|MetaRegion
argument_list|(
name|master
operator|.
name|getRootRegionLocation
argument_list|()
argument_list|,
name|HRegionInfo
operator|.
name|ROOT_REGIONINFO
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|scanSuccessful
operator|=
literal|true
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
name|warn
argument_list|(
literal|"Scan ROOT region"
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// Make sure the file system is still available
name|master
operator|.
name|checkFileSystem
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// If for some reason we get some other kind of exception,
comment|// at least log it rather than go out silently.
name|LOG
operator|.
name|error
argument_list|(
literal|"Unexpected exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|scanSuccessful
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|initialScan
parameter_list|()
block|{
name|initialScanComplete
operator|=
name|scanRoot
argument_list|()
expr_stmt|;
return|return
name|initialScanComplete
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|maintenanceScan
parameter_list|()
block|{
name|scanRoot
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

