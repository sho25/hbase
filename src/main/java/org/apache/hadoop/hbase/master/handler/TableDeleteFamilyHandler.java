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
name|HTableDescriptor
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
name|InvalidFamilyOperationException
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
name|catalog
operator|.
name|MetaEditor
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
name|MasterFileSystem
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
name|MasterServices
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
comment|/**  * Handles adding a new family to an existing table.  */
end_comment

begin_class
specifier|public
class|class
name|TableDeleteFamilyHandler
extends|extends
name|TableEventHandler
block|{
specifier|private
specifier|final
name|byte
index|[]
name|familyName
decl_stmt|;
specifier|public
name|TableDeleteFamilyHandler
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
name|familyName
parameter_list|,
name|Server
name|server
parameter_list|,
specifier|final
name|MasterServices
name|masterServices
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|EventType
operator|.
name|C2M_ADD_FAMILY
argument_list|,
name|tableName
argument_list|,
name|server
argument_list|,
name|masterServices
argument_list|)
expr_stmt|;
name|this
operator|.
name|familyName
operator|=
name|familyName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|handleTableOperation
parameter_list|(
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|hris
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|htd
init|=
name|hris
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTableDesc
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|htd
operator|.
name|hasFamily
argument_list|(
name|familyName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|InvalidFamilyOperationException
argument_list|(
literal|"Family '"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|familyName
argument_list|)
operator|+
literal|"' does not exist so "
operator|+
literal|"cannot be deleted"
argument_list|)
throw|;
block|}
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|hris
control|)
block|{
comment|// Update the HTD
name|hri
operator|.
name|getTableDesc
argument_list|()
operator|.
name|removeFamily
argument_list|(
name|familyName
argument_list|)
expr_stmt|;
comment|// Update region in META
name|MetaEditor
operator|.
name|updateRegionInfo
argument_list|(
name|this
operator|.
name|server
operator|.
name|getCatalogTracker
argument_list|()
argument_list|,
name|hri
argument_list|)
expr_stmt|;
name|MasterFileSystem
name|mfs
init|=
name|this
operator|.
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
decl_stmt|;
comment|// Update region info in FS
name|mfs
operator|.
name|updateRegionInfo
argument_list|(
name|hri
argument_list|)
expr_stmt|;
comment|// Delete directory in FS
name|mfs
operator|.
name|deleteFamily
argument_list|(
name|hri
argument_list|,
name|familyName
argument_list|)
expr_stmt|;
comment|// Update region info in FS
name|this
operator|.
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|updateRegionInfo
argument_list|(
name|hri
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

