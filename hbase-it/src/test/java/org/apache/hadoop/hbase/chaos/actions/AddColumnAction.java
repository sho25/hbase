begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|chaos
operator|.
name|actions
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
name|lang3
operator|.
name|RandomStringUtils
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
name|HColumnDescriptor
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
name|client
operator|.
name|Admin
import|;
end_import

begin_comment
comment|/**  * Action the adds a column family to a table.  */
end_comment

begin_class
specifier|public
class|class
name|AddColumnAction
extends|extends
name|Action
block|{
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|Admin
name|admin
decl_stmt|;
specifier|public
name|AddColumnAction
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|ActionContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|init
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|admin
operator|=
name|context
operator|.
name|getHBaseIntegrationTestingUtility
argument_list|()
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|perform
parameter_list|()
throws|throws
name|Exception
block|{
name|HTableDescriptor
name|tableDescriptor
init|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|columnDescriptor
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|columnDescriptor
operator|==
literal|null
operator|||
name|tableDescriptor
operator|.
name|getFamily
argument_list|(
name|columnDescriptor
operator|.
name|getName
argument_list|()
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|columnDescriptor
operator|=
operator|new
name|HColumnDescriptor
argument_list|(
name|RandomStringUtils
operator|.
name|randomAlphabetic
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Don't try the modify if we're stopping
if|if
condition|(
name|context
operator|.
name|isStopping
argument_list|()
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Performing action: Adding "
operator|+
name|columnDescriptor
operator|+
literal|" to "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|tableDescriptor
operator|.
name|addFamily
argument_list|(
name|columnDescriptor
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyTable
argument_list|(
name|tableName
argument_list|,
name|tableDescriptor
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

