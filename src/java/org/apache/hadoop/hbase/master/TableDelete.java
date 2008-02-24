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
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|io
operator|.
name|BatchUpdate
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
name|regionserver
operator|.
name|HRegionInterface
import|;
end_import

begin_comment
comment|/**   * Instantiated to delete a table  * Note that it extends ChangeTableState, which takes care of disabling  * the table.  */
end_comment

begin_class
class|class
name|TableDelete
extends|extends
name|ChangeTableState
block|{
name|TableDelete
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|,
specifier|final
name|Text
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
argument_list|,
literal|false
argument_list|)
expr_stmt|;
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
comment|// For regions that are being served, mark them for deletion
for|for
control|(
name|HashSet
argument_list|<
name|HRegionInfo
argument_list|>
name|s
range|:
name|servedRegions
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|HRegionInfo
name|i
range|:
name|s
control|)
block|{
name|master
operator|.
name|regionManager
operator|.
name|markRegionForDeletion
argument_list|(
name|i
operator|.
name|getRegionName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Unserved regions we can delete now
for|for
control|(
name|HRegionInfo
name|i
range|:
name|unservedRegions
control|)
block|{
comment|// Delete the region
try|try
block|{
name|HRegion
operator|.
name|deleteRegion
argument_list|(
name|this
operator|.
name|master
operator|.
name|fs
argument_list|,
name|this
operator|.
name|master
operator|.
name|rootdir
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
name|i
operator|.
name|getRegionName
argument_list|()
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
name|super
operator|.
name|postProcessMeta
argument_list|(
name|m
argument_list|,
name|server
argument_list|)
expr_stmt|;
comment|// delete the table's folder from fs.
name|master
operator|.
name|fs
operator|.
name|delete
argument_list|(
operator|new
name|Path
argument_list|(
name|master
operator|.
name|rootdir
argument_list|,
name|tableName
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|updateRegionInfo
parameter_list|(
name|BatchUpdate
name|b
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|HRegionInfo
name|info
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ALL_META_COLUMNS
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// Be sure to clean all cells
name|b
operator|.
name|delete
argument_list|(
name|ALL_META_COLUMNS
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

