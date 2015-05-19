begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|commons
operator|.
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|LargeTests
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
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|LargeTests
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
name|TestHTableMultiplexer
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestHTableMultiplexer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testFamily"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testQualifier"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|VALUE1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testValue1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|VALUE2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testValue2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|int
name|SLAVES
init|=
literal|3
decl_stmt|;
specifier|private
specifier|static
name|int
name|PER_REGIONSERVER_QUEUE_SIZE
init|=
literal|100000
decl_stmt|;
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
name|SLAVES
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws java.lang.Exception    */
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|checkExistence
parameter_list|(
name|HTable
name|htable
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|quality
parameter_list|)
throws|throws
name|Exception
block|{
comment|// verify that the Get returns the correct result
name|Result
name|r
decl_stmt|;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
name|int
name|nbTry
init|=
literal|0
decl_stmt|;
do|do
block|{
name|assertTrue
argument_list|(
literal|"Fail to get from "
operator|+
name|htable
operator|.
name|getName
argument_list|()
operator|+
literal|" after "
operator|+
name|nbTry
operator|+
literal|" tries"
argument_list|,
name|nbTry
operator|<
literal|50
argument_list|)
expr_stmt|;
name|nbTry
operator|++
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|r
operator|=
name|htable
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|r
operator|==
literal|null
operator|||
name|r
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
operator|==
literal|null
condition|)
do|;
name|assertEquals
argument_list|(
literal|"value"
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|VALUE1
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|r
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHTableMultiplexer
parameter_list|()
throws|throws
name|Exception
block|{
name|TableName
name|TABLE_1
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testHTableMultiplexer_1"
argument_list|)
decl_stmt|;
name|TableName
name|TABLE_2
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testHTableMultiplexer_2"
argument_list|)
decl_stmt|;
specifier|final
name|int
name|NUM_REGIONS
init|=
literal|10
decl_stmt|;
specifier|final
name|int
name|VERSION
init|=
literal|3
decl_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|failedPuts
decl_stmt|;
name|boolean
name|success
decl_stmt|;
name|HTableMultiplexer
name|multiplexer
init|=
operator|new
name|HTableMultiplexer
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|PER_REGIONSERVER_QUEUE_SIZE
argument_list|)
decl_stmt|;
name|HTable
name|htable1
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_1
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|,
name|VERSION
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaaaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzzzz"
argument_list|)
argument_list|,
name|NUM_REGIONS
argument_list|)
decl_stmt|;
name|HTable
name|htable2
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_2
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
argument_list|,
name|VERSION
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaaaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"zzzzz"
argument_list|)
argument_list|,
name|NUM_REGIONS
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|TABLE_1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitUntilAllRegionsAssigned
argument_list|(
name|TABLE_2
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|startRows
init|=
name|htable1
operator|.
name|getStartKeys
argument_list|()
decl_stmt|;
name|byte
index|[]
index|[]
name|endRows
init|=
name|htable1
operator|.
name|getEndKeys
argument_list|()
decl_stmt|;
comment|// SinglePut case
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_REGIONS
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|startRows
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|row
operator|==
literal|null
operator|||
name|row
operator|.
name|length
operator|<=
literal|0
condition|)
continue|continue;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE1
argument_list|)
decl_stmt|;
name|success
operator|=
name|multiplexer
operator|.
name|put
argument_list|(
name|TABLE_1
argument_list|,
name|put
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"multiplexer.put returns"
argument_list|,
name|success
argument_list|)
expr_stmt|;
name|put
operator|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE1
argument_list|)
expr_stmt|;
name|success
operator|=
name|multiplexer
operator|.
name|put
argument_list|(
name|TABLE_2
argument_list|,
name|put
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"multiplexer.put failed"
argument_list|,
name|success
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Put for "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|startRows
index|[
name|i
index|]
argument_list|)
operator|+
literal|" @ iteration "
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
argument_list|)
expr_stmt|;
comment|// verify that the Get returns the correct result
name|checkExistence
argument_list|(
name|htable1
argument_list|,
name|startRows
index|[
name|i
index|]
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
name|checkExistence
argument_list|(
name|htable2
argument_list|,
name|startRows
index|[
name|i
index|]
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
block|}
comment|// MultiPut case
name|List
argument_list|<
name|Put
argument_list|>
name|multiput
init|=
operator|new
name|ArrayList
argument_list|<
name|Put
argument_list|>
argument_list|()
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
name|NUM_REGIONS
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|endRows
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|row
operator|==
literal|null
operator|||
name|row
operator|.
name|length
operator|<=
literal|0
condition|)
continue|continue;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|VALUE2
argument_list|)
expr_stmt|;
name|multiput
operator|.
name|add
argument_list|(
name|put
argument_list|)
expr_stmt|;
block|}
name|failedPuts
operator|=
name|multiplexer
operator|.
name|put
argument_list|(
name|TABLE_1
argument_list|,
name|multiput
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|failedPuts
operator|==
literal|null
argument_list|)
expr_stmt|;
comment|// verify that the Get returns the correct result
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUM_REGIONS
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|endRows
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|row
operator|==
literal|null
operator|||
name|row
operator|.
name|length
operator|<=
literal|0
condition|)
continue|continue;
name|Get
name|get
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|get
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
name|Result
name|r
decl_stmt|;
name|int
name|nbTry
init|=
literal|0
decl_stmt|;
do|do
block|{
name|assertTrue
argument_list|(
name|nbTry
operator|++
operator|<
literal|50
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|r
operator|=
name|htable1
operator|.
name|get
argument_list|(
name|get
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|r
operator|==
literal|null
operator|||
name|r
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
operator|==
literal|null
operator|||
name|Bytes
operator|.
name|compareTo
argument_list|(
name|VALUE2
argument_list|,
name|r
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|)
argument_list|)
operator|!=
literal|0
condition|)
do|;
block|}
block|}
block|}
end_class

end_unit

