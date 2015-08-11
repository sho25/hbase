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
name|ConstantSizeRegionSplitPolicy
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
name|DisabledRegionSplitPolicy
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
name|IncreasingToUpperBoundRegionSplitPolicy
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

begin_class
specifier|public
class|class
name|ChangeSplitPolicyAction
extends|extends
name|Action
block|{
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|String
index|[]
name|possiblePolicies
decl_stmt|;
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
specifier|public
name|ChangeSplitPolicyAction
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
name|possiblePolicies
operator|=
operator|new
name|String
index|[]
block|{
name|IncreasingToUpperBoundRegionSplitPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
block|,
name|ConstantSizeRegionSplitPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
block|,
name|DisabledRegionSplitPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
block|}
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
name|perform
parameter_list|()
throws|throws
name|Exception
block|{
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
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Performing action: Change split policy of table "
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
name|String
name|chosenPolicy
init|=
name|possiblePolicies
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|possiblePolicies
operator|.
name|length
argument_list|)
index|]
decl_stmt|;
name|tableDescriptor
operator|.
name|setRegionSplitPolicyClassName
argument_list|(
name|chosenPolicy
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Changing "
operator|+
name|tableName
operator|+
literal|" split policy to "
operator|+
name|chosenPolicy
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

