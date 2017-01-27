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
name|java
operator|.
name|util
operator|.
name|Random
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
name|encoding
operator|.
name|DataBlockEncoding
import|;
end_import

begin_comment
comment|/**  * Action that changes the encoding on a column family from a list of tables.  */
end_comment

begin_class
specifier|public
class|class
name|ChangeEncodingAction
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
specifier|private
name|Random
name|random
decl_stmt|;
specifier|public
name|ChangeEncodingAction
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
name|this
operator|.
name|random
operator|=
operator|new
name|Random
argument_list|()
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
index|[]
name|columnDescriptors
init|=
name|tableDescriptor
operator|.
name|getColumnFamilies
argument_list|()
decl_stmt|;
if|if
condition|(
name|columnDescriptors
operator|==
literal|null
operator|||
name|columnDescriptors
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|LOG
operator|.
name|debug
argument_list|(
literal|"Performing action: Changing encodings on "
operator|+
name|tableName
argument_list|)
expr_stmt|;
comment|// possible DataBlockEncoding id's
name|int
index|[]
name|possibleIds
init|=
block|{
literal|0
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|,
literal|6
block|}
decl_stmt|;
for|for
control|(
name|HColumnDescriptor
name|descriptor
range|:
name|columnDescriptors
control|)
block|{
name|short
name|id
init|=
operator|(
name|short
operator|)
name|possibleIds
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|possibleIds
operator|.
name|length
argument_list|)
index|]
decl_stmt|;
name|descriptor
operator|.
name|setDataBlockEncoding
argument_list|(
name|DataBlockEncoding
operator|.
name|getEncodingById
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Set encoding of column family "
operator|+
name|descriptor
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|" to: "
operator|+
name|descriptor
operator|.
name|getDataBlockEncoding
argument_list|()
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

