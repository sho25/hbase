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
name|regionserver
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
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
name|client
operator|.
name|Mutation
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
name|Put
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
name|RegionServerTests
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
name|SmallTests
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Pair
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
name|wal
operator|.
name|WALEdit
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
name|Test
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
name|RegionServerTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMiniBatchOperationInProgress
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
name|TestMiniBatchOperationInProgress
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testMiniBatchOperationInProgressMethods
parameter_list|()
block|{
name|Pair
argument_list|<
name|Mutation
argument_list|,
name|Integer
argument_list|>
index|[]
name|operations
init|=
operator|new
name|Pair
index|[
literal|10
index|]
decl_stmt|;
name|OperationStatus
index|[]
name|retCodeDetails
init|=
operator|new
name|OperationStatus
index|[
literal|10
index|]
decl_stmt|;
name|WALEdit
index|[]
name|walEditsFromCoprocessors
init|=
operator|new
name|WALEdit
index|[
literal|10
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|operations
index|[
name|i
index|]
operator|=
operator|new
name|Pair
argument_list|<>
argument_list|(
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|MiniBatchOperationInProgress
argument_list|<
name|Pair
argument_list|<
name|Mutation
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|miniBatch
init|=
operator|new
name|MiniBatchOperationInProgress
argument_list|<>
argument_list|(
name|operations
argument_list|,
name|retCodeDetails
argument_list|,
name|walEditsFromCoprocessors
argument_list|,
literal|0
argument_list|,
literal|5
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|miniBatch
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|,
name|miniBatch
operator|.
name|getOperation
argument_list|(
literal|0
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|2
argument_list|)
argument_list|,
name|miniBatch
operator|.
name|getOperation
argument_list|(
literal|2
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|4
argument_list|)
argument_list|,
name|miniBatch
operator|.
name|getOperation
argument_list|(
literal|4
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|miniBatch
operator|.
name|getOperation
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should throw Exception while accessing out of range"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArrayIndexOutOfBoundsException
name|e
parameter_list|)
block|{     }
name|miniBatch
operator|.
name|setOperationStatus
argument_list|(
literal|1
argument_list|,
name|OperationStatus
operator|.
name|FAILURE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|OperationStatus
operator|.
name|FAILURE
argument_list|,
name|retCodeDetails
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
try|try
block|{
name|miniBatch
operator|.
name|setOperationStatus
argument_list|(
literal|6
argument_list|,
name|OperationStatus
operator|.
name|FAILURE
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should throw Exception while accessing out of range"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArrayIndexOutOfBoundsException
name|e
parameter_list|)
block|{     }
try|try
block|{
name|miniBatch
operator|.
name|setWalEdit
argument_list|(
literal|5
argument_list|,
operator|new
name|WALEdit
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should throw Exception while accessing out of range"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArrayIndexOutOfBoundsException
name|e
parameter_list|)
block|{     }
name|miniBatch
operator|=
operator|new
name|MiniBatchOperationInProgress
argument_list|<>
argument_list|(
name|operations
argument_list|,
name|retCodeDetails
argument_list|,
name|walEditsFromCoprocessors
argument_list|,
literal|7
argument_list|,
literal|10
argument_list|,
literal|3
argument_list|)
expr_stmt|;
try|try
block|{
name|miniBatch
operator|.
name|setWalEdit
argument_list|(
operator|-
literal|1
argument_list|,
operator|new
name|WALEdit
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should throw Exception while accessing out of range"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArrayIndexOutOfBoundsException
name|e
parameter_list|)
block|{     }
try|try
block|{
name|miniBatch
operator|.
name|getOperation
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should throw Exception while accessing out of range"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArrayIndexOutOfBoundsException
name|e
parameter_list|)
block|{     }
try|try
block|{
name|miniBatch
operator|.
name|getOperation
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should throw Exception while accessing out of range"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArrayIndexOutOfBoundsException
name|e
parameter_list|)
block|{     }
try|try
block|{
name|miniBatch
operator|.
name|getOperationStatus
argument_list|(
literal|9
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should throw Exception while accessing out of range"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArrayIndexOutOfBoundsException
name|e
parameter_list|)
block|{     }
try|try
block|{
name|miniBatch
operator|.
name|setOperationStatus
argument_list|(
literal|3
argument_list|,
name|OperationStatus
operator|.
name|FAILURE
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Should throw Exception while accessing out of range"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ArrayIndexOutOfBoundsException
name|e
parameter_list|)
block|{     }
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|7
argument_list|)
argument_list|,
name|miniBatch
operator|.
name|getOperation
argument_list|(
literal|0
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|9
argument_list|)
argument_list|,
name|miniBatch
operator|.
name|getOperation
argument_list|(
literal|2
argument_list|)
operator|.
name|getFirst
argument_list|()
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|miniBatch
operator|.
name|setOperationStatus
argument_list|(
literal|1
argument_list|,
name|OperationStatus
operator|.
name|SUCCESS
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|OperationStatus
operator|.
name|SUCCESS
argument_list|,
name|retCodeDetails
index|[
literal|8
index|]
argument_list|)
expr_stmt|;
name|WALEdit
name|wal
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|miniBatch
operator|.
name|setWalEdit
argument_list|(
literal|0
argument_list|,
name|wal
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|wal
argument_list|,
name|walEditsFromCoprocessors
index|[
literal|7
index|]
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

