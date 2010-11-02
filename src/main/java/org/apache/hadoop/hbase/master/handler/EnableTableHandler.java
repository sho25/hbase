begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|handler
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
name|List
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
name|Server
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
name|TableNotFoundException
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
name|catalog
operator|.
name|CatalogTracker
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
name|catalog
operator|.
name|MetaReader
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
name|executor
operator|.
name|EventHandler
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
name|master
operator|.
name|AssignmentManager
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

begin_class
specifier|public
class|class
name|EnableTableHandler
extends|extends
name|EventHandler
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|EnableTableHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|String
name|tableNameStr
decl_stmt|;
specifier|private
specifier|final
name|AssignmentManager
name|assignmentManager
decl_stmt|;
specifier|private
specifier|final
name|CatalogTracker
name|ct
decl_stmt|;
specifier|public
name|EnableTableHandler
parameter_list|(
name|Server
name|server
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|CatalogTracker
name|catalogTracker
parameter_list|,
name|AssignmentManager
name|assignmentManager
parameter_list|)
throws|throws
name|TableNotFoundException
throws|,
name|IOException
block|{
name|super
argument_list|(
name|server
argument_list|,
name|EventType
operator|.
name|C_M_ENABLE_TABLE
argument_list|)
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|tableNameStr
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|this
operator|.
name|ct
operator|=
name|catalogTracker
expr_stmt|;
name|this
operator|.
name|assignmentManager
operator|=
name|assignmentManager
expr_stmt|;
comment|// Check if table exists
if|if
condition|(
operator|!
name|MetaReader
operator|.
name|tableExists
argument_list|(
name|catalogTracker
argument_list|,
name|this
operator|.
name|tableNameStr
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|TableNotFoundException
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|process
parameter_list|()
block|{
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Attemping to enable the table "
operator|+
name|this
operator|.
name|tableNameStr
argument_list|)
expr_stmt|;
name|handleEnableTable
argument_list|()
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
literal|"Error trying to enable the table "
operator|+
name|this
operator|.
name|tableNameStr
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|handleEnableTable
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|assignmentManager
operator|.
name|isTableDisabled
argument_list|(
name|this
operator|.
name|tableNameStr
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Table "
operator|+
name|tableNameStr
operator|+
literal|" is not disabled; skipping enable"
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Get the regions of this table
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|MetaReader
operator|.
name|getTableRegions
argument_list|(
name|this
operator|.
name|ct
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
comment|// Set the table as disabled so it doesn't get re-onlined
name|assignmentManager
operator|.
name|undisableTable
argument_list|(
name|this
operator|.
name|tableNameStr
argument_list|)
expr_stmt|;
comment|// Verify all regions of table are disabled
for|for
control|(
name|HRegionInfo
name|region
range|:
name|regions
control|)
block|{
name|assignmentManager
operator|.
name|assign
argument_list|(
name|region
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|// Wait on table's regions to clear region in transition.
for|for
control|(
name|HRegionInfo
name|region
range|:
name|regions
control|)
block|{
name|this
operator|.
name|assignmentManager
operator|.
name|waitOnRegionToClearRegionsInTransition
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

