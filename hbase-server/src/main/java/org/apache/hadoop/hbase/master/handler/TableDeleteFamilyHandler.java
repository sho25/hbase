begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|classification
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
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
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
name|executor
operator|.
name|EventType
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
name|HMaster
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
name|MasterCoprocessorHost
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
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TableDeleteFamilyHandler
extends|extends
name|TableEventHandler
block|{
specifier|private
name|byte
index|[]
name|familyName
decl_stmt|;
specifier|public
name|TableDeleteFamilyHandler
parameter_list|(
name|TableName
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
name|C_M_DELETE_FAMILY
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
name|prepareWithTableLock
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|prepareWithTableLock
argument_list|()
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
name|getTableDescriptor
argument_list|()
decl_stmt|;
name|this
operator|.
name|familyName
operator|=
name|hasColumnFamily
argument_list|(
name|htd
argument_list|,
name|familyName
argument_list|)
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
name|MasterCoprocessorHost
name|cpHost
init|=
operator|(
operator|(
name|HMaster
operator|)
name|this
operator|.
name|server
operator|)
operator|.
name|getCoprocessorHost
argument_list|()
decl_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|cpHost
operator|.
name|preDeleteColumnHandler
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|this
operator|.
name|familyName
argument_list|)
expr_stmt|;
block|}
comment|// Update table descriptor
name|this
operator|.
name|masterServices
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|deleteColumn
argument_list|(
name|tableName
argument_list|,
name|familyName
argument_list|)
expr_stmt|;
comment|// Remove the column family from the file system
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
for|for
control|(
name|HRegionInfo
name|hri
range|:
name|hris
control|)
block|{
comment|// Delete the family directory in FS for all the regions one by one
name|mfs
operator|.
name|deleteFamilyFromFS
argument_list|(
name|hri
argument_list|,
name|familyName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|cpHost
operator|.
name|postDeleteColumnHandler
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|this
operator|.
name|familyName
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|name
init|=
literal|"UnknownServerName"
decl_stmt|;
if|if
condition|(
name|server
operator|!=
literal|null
operator|&&
name|server
operator|.
name|getServerName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|name
operator|=
name|server
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
name|String
name|family
init|=
literal|"UnknownFamily"
decl_stmt|;
if|if
condition|(
name|familyName
operator|!=
literal|null
condition|)
block|{
name|family
operator|=
name|Bytes
operator|.
name|toString
argument_list|(
name|familyName
argument_list|)
expr_stmt|;
block|}
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"-"
operator|+
name|name
operator|+
literal|"-"
operator|+
name|getSeqid
argument_list|()
operator|+
literal|"-"
operator|+
name|tableName
operator|+
literal|"-"
operator|+
name|family
return|;
block|}
block|}
end_class

end_unit

