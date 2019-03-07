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
name|client
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
name|HBaseClassTestRule
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
name|testclassification
operator|.
name|ClientTests
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
name|testclassification
operator|.
name|MediumTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestSnapshotWithAcl
extends|extends
name|SnapshotWithAclTestBase
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestSnapshotWithAcl
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|snapshot
parameter_list|(
name|String
name|snapshotName
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|snapshot
argument_list|(
name|snapshotName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|cloneSnapshot
parameter_list|(
name|String
name|snapshotName
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|boolean
name|restoreAcl
parameter_list|)
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName
argument_list|,
name|tableName
argument_list|,
name|restoreAcl
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|restoreSnapshot
parameter_list|(
name|String
name|snapshotName
parameter_list|,
name|boolean
name|restoreAcl
parameter_list|)
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|restoreSnapshot
argument_list|(
name|snapshotName
argument_list|,
literal|false
argument_list|,
name|restoreAcl
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

