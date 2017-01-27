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
name|HBaseTestingUtility
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
name|regionserver
operator|.
name|BloomType
import|;
end_import

begin_comment
comment|/**  * Action that tries to adjust the bloom filter setting on all the columns of a  * table  */
end_comment

begin_class
specifier|public
class|class
name|ChangeBloomFilterAction
extends|extends
name|Action
block|{
specifier|private
specifier|final
name|long
name|sleepTime
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|public
name|ChangeBloomFilterAction
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|this
argument_list|(
operator|-
literal|1
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ChangeBloomFilterAction
parameter_list|(
name|int
name|sleepTime
parameter_list|,
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|sleepTime
operator|=
name|sleepTime
expr_stmt|;
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
name|perform
parameter_list|()
throws|throws
name|Exception
block|{
name|Random
name|random
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
name|HBaseTestingUtility
name|util
init|=
name|context
operator|.
name|getHBaseIntegrationTestingUtility
argument_list|()
decl_stmt|;
name|Admin
name|admin
init|=
name|util
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Performing action: Change bloom filter on all columns of table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
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
specifier|final
name|BloomType
index|[]
name|bloomArray
init|=
name|BloomType
operator|.
name|values
argument_list|()
decl_stmt|;
specifier|final
name|int
name|bloomArraySize
init|=
name|bloomArray
operator|.
name|length
decl_stmt|;
for|for
control|(
name|HColumnDescriptor
name|descriptor
range|:
name|columnDescriptors
control|)
block|{
name|int
name|bloomFilterIndex
init|=
name|random
operator|.
name|nextInt
argument_list|(
name|bloomArraySize
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Performing action: About to set bloom filter type to "
operator|+
name|bloomArray
index|[
name|bloomFilterIndex
index|]
operator|+
literal|" on column "
operator|+
name|descriptor
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|" of table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|descriptor
operator|.
name|setBloomFilterType
argument_list|(
name|bloomArray
index|[
name|bloomFilterIndex
index|]
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Performing action: Just set bloom filter type to "
operator|+
name|bloomArray
index|[
name|bloomFilterIndex
index|]
operator|+
literal|" on column "
operator|+
name|descriptor
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|" of table "
operator|+
name|tableName
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

