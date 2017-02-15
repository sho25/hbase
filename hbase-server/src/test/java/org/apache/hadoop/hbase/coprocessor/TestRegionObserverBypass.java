begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
package|;
end_package

begin_import
import|import static
name|junit
operator|.
name|framework
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|Map
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
name|conf
operator|.
name|Configuration
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
name|Cell
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
name|HBaseConfiguration
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
name|Delete
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
name|Get
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
name|client
operator|.
name|Result
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
name|Durability
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
name|Table
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
name|wal
operator|.
name|WALEdit
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
name|CoprocessorTests
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
name|EnvironmentEdgeManager
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
name|EnvironmentEdgeManagerTestHelper
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
name|IncrementingEnvironmentEdge
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
name|Before
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
name|CoprocessorTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestRegionObserverBypass
block|{
specifier|private
specifier|static
name|HBaseTestingUtility
name|util
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|dummy
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"dummy"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|row3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r3"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|test
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
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
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|USER_REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|TestCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|util
operator|=
operator|new
name|HBaseTestingUtility
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|util
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
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
name|util
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|Admin
name|admin
init|=
name|util
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
if|if
condition|(
name|admin
operator|.
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|util
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|dummy
block|,
name|test
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**    * do a single put that is bypassed by a RegionObserver    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testSimple
parameter_list|()
throws|throws
name|Exception
block|{
name|Table
name|t
init|=
name|util
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
comment|// before HBASE-4331, this would throw an exception
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test various multiput operations.    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testMulti
parameter_list|()
throws|throws
name|Exception
block|{
comment|//ensure that server time increments every time we do an operation, otherwise
comment|//previous deletes will eclipse successive puts having the same timestamp
name|EnvironmentEdgeManagerTestHelper
operator|.
name|injectEdge
argument_list|(
operator|new
name|IncrementingEnvironmentEdge
argument_list|()
argument_list|)
expr_stmt|;
name|Table
name|t
init|=
name|util
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Put
argument_list|>
name|puts
init|=
operator|new
name|ArrayList
argument_list|<
name|Put
argument_list|>
argument_list|()
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|dummy
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row2
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row3
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// before HBASE-4331, this would throw an exception
name|t
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row2
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row3
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|puts
operator|.
name|clear
argument_list|()
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row2
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row3
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// before HBASE-4331, this would throw an exception
name|t
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row2
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row3
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|puts
operator|.
name|clear
argument_list|()
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row2
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row3
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|dummy
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// this worked fine even before HBASE-4331
name|t
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row2
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row3
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|puts
operator|.
name|clear
argument_list|()
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|dummy
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row2
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row3
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|dummy
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// this worked fine even before HBASE-4331
name|t
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row2
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row3
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|puts
operator|.
name|clear
argument_list|()
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row1
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row2
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|dummy
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|row3
argument_list|)
expr_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|test
argument_list|,
name|dummy
argument_list|,
name|dummy
argument_list|)
expr_stmt|;
name|puts
operator|.
name|add
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// before HBASE-4331, this would throw an exception
name|t
operator|.
name|put
argument_list|(
name|puts
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row2
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|checkRowAndDelete
argument_list|(
name|t
argument_list|,
name|row3
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|t
operator|.
name|close
argument_list|()
expr_stmt|;
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|checkRowAndDelete
parameter_list|(
name|Table
name|t
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|int
name|count
parameter_list|)
throws|throws
name|IOException
block|{
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|t
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|count
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|row
argument_list|)
decl_stmt|;
name|t
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|TestCoprocessor
implements|implements
name|RegionObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|prePut
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|,
specifier|final
name|Put
name|put
parameter_list|,
specifier|final
name|WALEdit
name|edit
parameter_list|,
specifier|final
name|Durability
name|durability
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Cell
argument_list|>
argument_list|>
name|familyMap
init|=
name|put
operator|.
name|getFamilyCellMap
argument_list|()
decl_stmt|;
if|if
condition|(
name|familyMap
operator|.
name|containsKey
argument_list|(
name|test
argument_list|)
condition|)
block|{
name|e
operator|.
name|bypass
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

