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
name|mapreduce
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
name|MiniHBaseCluster
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
name|HTable
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
name|LauncherSecurityManager
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
name|mapreduce
operator|.
name|Job
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
name|util
operator|.
name|GenericOptionsParser
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
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|io
operator|.
name|PrintStream
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
name|*
import|;
end_import

begin_comment
comment|/**  * Basic test for the CopyTable M/R tool  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestCopyTable
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|MiniHBaseCluster
name|cluster
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY_A_STRING
init|=
literal|"a"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|FAMILY_B_STRING
init|=
literal|"b"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_A
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY_A_STRING
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY_B
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY_B_STRING
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|cluster
operator|=
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniMapReduceCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniMapReduceCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Simple end-to-end test    * @throws Exception    */
annotation|@
name|Test
specifier|public
name|void
name|testCopyTable
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|byte
index|[]
name|TABLENAME1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testCopyTable1"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|TABLENAME2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testCopyTable2"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|COLUMN1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c1"
argument_list|)
decl_stmt|;
name|HTable
name|t1
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLENAME1
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|HTable
name|t2
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLENAME2
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
comment|// put rows into the first table
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
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN1
argument_list|,
name|COLUMN1
argument_list|)
expr_stmt|;
name|t1
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
block|}
name|CopyTable
name|copy
init|=
operator|new
name|CopyTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|copy
operator|.
name|run
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"--new.name="
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|TABLENAME2
argument_list|)
block|,
name|Bytes
operator|.
name|toString
argument_list|(
name|TABLENAME1
argument_list|)
block|}
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify the data was copied into table 2
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
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|t2
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|r
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
name|COLUMN1
argument_list|,
name|r
operator|.
name|raw
argument_list|()
index|[
literal|0
index|]
operator|.
name|getQualifier
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|t1
operator|.
name|close
argument_list|()
expr_stmt|;
name|t2
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLENAME1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLENAME2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStartStopRow
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|byte
index|[]
name|TABLENAME1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testStartStopRow1"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|TABLENAME2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testStartStopRow2"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|COLUMN1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c1"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|ROW0
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row0"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|ROW1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|ROW2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
decl_stmt|;
name|HTable
name|t1
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLENAME1
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
name|HTable
name|t2
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLENAME2
argument_list|,
name|FAMILY
argument_list|)
decl_stmt|;
comment|// put rows into the first table
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|ROW0
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN1
argument_list|,
name|COLUMN1
argument_list|)
expr_stmt|;
name|t1
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|ROW1
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN1
argument_list|,
name|COLUMN1
argument_list|)
expr_stmt|;
name|t1
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|ROW2
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN1
argument_list|,
name|COLUMN1
argument_list|)
expr_stmt|;
name|t1
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|CopyTable
name|copy
init|=
operator|new
name|CopyTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|copy
operator|.
name|run
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"--new.name="
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|TABLENAME2
argument_list|)
block|,
literal|"--startrow=row1"
block|,
literal|"--stoprow=row2"
block|,
name|Bytes
operator|.
name|toString
argument_list|(
name|TABLENAME1
argument_list|)
block|}
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify the data was copied into table 2
comment|// row1 exist, row0, row2 do not exist
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|ROW1
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|t2
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|r
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
name|COLUMN1
argument_list|,
name|r
operator|.
name|raw
argument_list|()
index|[
literal|0
index|]
operator|.
name|getQualifier
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|ROW0
argument_list|)
expr_stmt|;
name|r
operator|=
name|t2
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|g
operator|=
operator|new
name|Get
argument_list|(
name|ROW2
argument_list|)
expr_stmt|;
name|r
operator|=
name|t2
operator|.
name|get
argument_list|(
name|g
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|t1
operator|.
name|close
argument_list|()
expr_stmt|;
name|t2
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLENAME1
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLENAME2
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test copy of table from sourceTable to targetTable all rows from family a    */
annotation|@
name|Test
specifier|public
name|void
name|testRenameFamily
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|sourceTable
init|=
literal|"sourceTable"
decl_stmt|;
name|String
name|targetTable
init|=
literal|"targetTable"
decl_stmt|;
name|byte
index|[]
index|[]
name|families
init|=
block|{
name|FAMILY_A
block|,
name|FAMILY_B
block|}
decl_stmt|;
name|HTable
name|t
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|sourceTable
argument_list|)
argument_list|,
name|families
argument_list|)
decl_stmt|;
name|HTable
name|t2
init|=
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|targetTable
argument_list|)
argument_list|,
name|families
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|ROW1
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Data11"
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Data12"
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Data13"
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|p
operator|=
operator|new
name|Put
argument_list|(
name|ROW2
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Dat21"
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Data22"
argument_list|)
argument_list|)
expr_stmt|;
name|p
operator|.
name|add
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"Data23"
argument_list|)
argument_list|)
expr_stmt|;
name|t
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|long
name|currentTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|String
index|[]
name|args
init|=
operator|new
name|String
index|[]
block|{
literal|"--new.name="
operator|+
name|targetTable
block|,
literal|"--families=a:b"
block|,
literal|"--all.cells"
block|,
literal|"--starttime="
operator|+
operator|(
name|currentTime
operator|-
literal|100000
operator|)
block|,
literal|"--endtime="
operator|+
operator|(
name|currentTime
operator|+
literal|100000
operator|)
block|,
literal|"--versions=1"
block|,
name|sourceTable
block|}
decl_stmt|;
name|assertNull
argument_list|(
name|t2
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW1
argument_list|)
argument_list|)
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|clean
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|runCopy
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|t2
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW1
argument_list|)
argument_list|)
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|Result
name|res
init|=
name|t2
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW1
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|b1
init|=
name|res
operator|.
name|getValue
argument_list|(
name|FAMILY_B
argument_list|,
name|QUALIFIER
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Data13"
argument_list|,
operator|new
name|String
argument_list|(
name|b1
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|t2
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW2
argument_list|)
argument_list|)
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|res
operator|=
name|t2
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW2
argument_list|)
argument_list|)
expr_stmt|;
name|b1
operator|=
name|res
operator|.
name|getValue
argument_list|(
name|FAMILY_A
argument_list|,
name|QUALIFIER
argument_list|)
expr_stmt|;
comment|// Data from the family of B is not copied
name|assertNull
argument_list|(
name|b1
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test main method of CopyTable.    */
annotation|@
name|Test
specifier|public
name|void
name|testMainMethod
parameter_list|()
throws|throws
name|Exception
block|{
name|String
index|[]
name|emptyArgs
init|=
block|{
literal|"-h"
block|}
decl_stmt|;
name|PrintStream
name|oldWriter
init|=
name|System
operator|.
name|err
decl_stmt|;
name|ByteArrayOutputStream
name|data
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|PrintStream
name|writer
init|=
operator|new
name|PrintStream
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|System
operator|.
name|setErr
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|SecurityManager
name|SECURITY_MANAGER
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
name|LauncherSecurityManager
name|newSecurityManager
init|=
operator|new
name|LauncherSecurityManager
argument_list|()
decl_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
name|newSecurityManager
argument_list|)
expr_stmt|;
try|try
block|{
name|CopyTable
operator|.
name|main
argument_list|(
name|emptyArgs
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should be exit"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|newSecurityManager
operator|.
name|getExitCode
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|System
operator|.
name|setErr
argument_list|(
name|oldWriter
argument_list|)
expr_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
name|SECURITY_MANAGER
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"rs.class"
argument_list|)
argument_list|)
expr_stmt|;
comment|// should print usage information
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Usage:"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|boolean
name|runCopy
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
block|{
name|GenericOptionsParser
name|opts
init|=
operator|new
name|GenericOptionsParser
argument_list|(
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|Configuration
name|configuration
init|=
name|opts
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|args
operator|=
name|opts
operator|.
name|getRemainingArgs
argument_list|()
expr_stmt|;
name|clean
argument_list|()
expr_stmt|;
name|Job
name|job
init|=
name|CopyTable
operator|.
name|createSubmittableJob
argument_list|(
name|configuration
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return
name|job
operator|.
name|isSuccessful
argument_list|()
return|;
block|}
specifier|private
name|void
name|clean
parameter_list|()
block|{
name|CopyTable
operator|.
name|startTime
operator|=
literal|0
expr_stmt|;
name|CopyTable
operator|.
name|endTime
operator|=
literal|0
expr_stmt|;
name|CopyTable
operator|.
name|versions
operator|=
operator|-
literal|1
expr_stmt|;
name|CopyTable
operator|.
name|tableName
operator|=
literal|null
expr_stmt|;
name|CopyTable
operator|.
name|startRow
operator|=
literal|null
expr_stmt|;
name|CopyTable
operator|.
name|stopRow
operator|=
literal|null
expr_stmt|;
name|CopyTable
operator|.
name|newTableName
operator|=
literal|null
expr_stmt|;
name|CopyTable
operator|.
name|peerAddress
operator|=
literal|null
expr_stmt|;
name|CopyTable
operator|.
name|families
operator|=
literal|null
expr_stmt|;
name|CopyTable
operator|.
name|allCells
operator|=
literal|false
expr_stmt|;
block|}
block|}
end_class

end_unit

