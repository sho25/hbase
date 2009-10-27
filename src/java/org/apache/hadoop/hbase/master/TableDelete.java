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
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|fs
operator|.
name|Path
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
name|TableNotDisabledException
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
name|ipc
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**   * Instantiated to delete a table. Table must be offline.  */
end_comment

begin_class
class|class
name|TableDelete
extends|extends
name|TableOperation
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
name|TableDelete
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|master
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processScanItem
parameter_list|(
name|String
name|serverName
parameter_list|,
specifier|final
name|HRegionInfo
name|info
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|isEnabled
argument_list|(
name|info
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Region still enabled: "
operator|+
name|info
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|TableNotDisabledException
argument_list|(
name|tableName
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|postProcessMeta
parameter_list|(
name|MetaRegion
name|m
parameter_list|,
name|HRegionInterface
name|server
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|HRegionInfo
name|i
range|:
name|unservedRegions
control|)
block|{
if|if
condition|(
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|i
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
comment|// Don't delete regions that are not from our table.
continue|continue;
block|}
comment|// Delete the region
try|try
block|{
name|HRegion
operator|.
name|removeRegionFromMETA
argument_list|(
name|server
argument_list|,
name|m
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|i
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
name|HRegion
operator|.
name|deleteRegion
argument_list|(
name|this
operator|.
name|master
operator|.
name|getFileSystem
argument_list|()
argument_list|,
name|this
operator|.
name|master
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"failed to delete region "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|i
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|,
name|RemoteExceptionHandler
operator|.
name|checkIOException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// delete the table's folder from fs.
name|this
operator|.
name|master
operator|.
name|getFileSystem
argument_list|()
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|this
operator|.
name|master
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

