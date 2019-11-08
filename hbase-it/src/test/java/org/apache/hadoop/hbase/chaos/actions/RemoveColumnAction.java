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
name|java
operator|.
name|util
operator|.
name|Set
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
name|client
operator|.
name|ColumnFamilyDescriptor
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
name|TableDescriptor
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
name|TableDescriptorBuilder
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Action that removes a column family.  */
end_comment

begin_class
specifier|public
class|class
name|RemoveColumnAction
extends|extends
name|Action
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|RemoveColumnAction
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|protectedColumns
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
name|RemoveColumnAction
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|protectedColumns
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
name|protectedColumns
operator|=
name|protectedColumns
expr_stmt|;
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
name|TableDescriptor
name|tableDescriptor
init|=
name|admin
operator|.
name|getDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|ColumnFamilyDescriptor
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
operator|.
name|length
operator|<=
operator|(
name|protectedColumns
operator|==
literal|null
condition|?
literal|1
else|:
name|protectedColumns
operator|.
name|size
argument_list|()
operator|)
condition|)
block|{
return|return;
block|}
name|int
name|index
init|=
name|random
operator|.
name|nextInt
argument_list|(
name|columnDescriptors
operator|.
name|length
argument_list|)
decl_stmt|;
while|while
condition|(
name|protectedColumns
operator|!=
literal|null
operator|&&
name|protectedColumns
operator|.
name|contains
argument_list|(
name|columnDescriptors
index|[
name|index
index|]
operator|.
name|getNameAsString
argument_list|()
argument_list|)
condition|)
block|{
name|index
operator|=
name|random
operator|.
name|nextInt
argument_list|(
name|columnDescriptors
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
name|byte
index|[]
name|colDescName
init|=
name|columnDescriptors
index|[
name|index
index|]
operator|.
name|getName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Performing action: Removing "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|colDescName
argument_list|)
operator|+
literal|" from "
operator|+
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
expr_stmt|;
name|TableDescriptorBuilder
name|builder
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableDescriptor
argument_list|)
decl_stmt|;
name|builder
operator|.
name|removeColumnFamily
argument_list|(
name|colDescName
argument_list|)
expr_stmt|;
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
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

