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
name|snapshot
operator|.
name|SnapshotTestingUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|RestoreSnapshotFromClientAfterTruncateTestBase
extends|extends
name|RestoreSnapshotFromClientTestBase
block|{
annotation|@
name|Test
specifier|public
name|void
name|testRestoreSnapshotAfterTruncate
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|getValidMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|SnapshotTestingUtils
operator|.
name|createTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableName
argument_list|,
name|getNumReplicas
argument_list|()
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|loadData
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableName
argument_list|,
literal|500
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|int
name|numOfRows
init|=
literal|0
decl_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|numOfRows
operator|=
name|countRows
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
comment|// take snapshot
name|admin
operator|.
name|snapshot
argument_list|(
literal|"snap"
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|truncateTable
argument_list|(
name|tableName
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|restoreSnapshot
argument_list|(
literal|"snap"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|verifyRowCount
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableName
argument_list|,
name|numOfRows
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|verifyReplicasCameOnline
argument_list|(
name|tableName
argument_list|,
name|admin
argument_list|,
name|getNumReplicas
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

