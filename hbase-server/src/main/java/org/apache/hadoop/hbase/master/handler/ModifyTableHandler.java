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
name|MasterServices
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ModifyTableHandler
extends|extends
name|TableEventHandler
block|{
specifier|private
specifier|final
name|HTableDescriptor
name|htd
decl_stmt|;
specifier|public
name|ModifyTableHandler
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|HTableDescriptor
name|htd
parameter_list|,
specifier|final
name|Server
name|server
parameter_list|,
specifier|final
name|MasterServices
name|masterServices
parameter_list|)
block|{
name|super
argument_list|(
name|EventType
operator|.
name|C_M_MODIFY_TABLE
argument_list|,
name|tableName
argument_list|,
name|server
argument_list|,
name|masterServices
argument_list|)
expr_stmt|;
comment|// This is the new schema we are going to write out as this modification.
name|this
operator|.
name|htd
operator|=
name|htd
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
comment|// Check table exists.
name|getTableDescriptor
argument_list|()
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
name|preModifyTableHandler
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|this
operator|.
name|htd
argument_list|)
expr_stmt|;
block|}
comment|// Update descriptor
name|this
operator|.
name|masterServices
operator|.
name|getTableDescriptors
argument_list|()
operator|.
name|add
argument_list|(
name|this
operator|.
name|htd
argument_list|)
expr_stmt|;
if|if
condition|(
name|cpHost
operator|!=
literal|null
condition|)
block|{
name|cpHost
operator|.
name|postModifyTableHandler
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|this
operator|.
name|htd
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
name|tableNameStr
return|;
block|}
block|}
end_class

end_unit

