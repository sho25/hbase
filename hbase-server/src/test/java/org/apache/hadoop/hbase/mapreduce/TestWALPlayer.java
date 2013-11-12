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
name|PrintStream
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
name|fs
operator|.
name|Path
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
name|CellUtil
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
name|HConstants
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
name|KeyValue
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
name|io
operator|.
name|ImmutableBytesWritable
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
name|mapreduce
operator|.
name|WALPlayer
operator|.
name|HLogKeyValueMapper
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
name|HLog
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
name|HLogKey
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
name|Mapper
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
name|Mapper
operator|.
name|Context
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
name|mockito
operator|.
name|invocation
operator|.
name|InvocationOnMock
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|stubbing
operator|.
name|Answer
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Basic test for the WALPlayer M/R tool  */
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
name|TestWALPlayer
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
argument_list|()
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
name|testWALPlayer
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
literal|"testWALPlayer1"
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
literal|"testWALPlayer2"
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
name|COLUMN2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c2"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
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
comment|// put a row into the first table
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|ROW
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
name|p
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN2
argument_list|,
name|COLUMN2
argument_list|)
expr_stmt|;
name|t1
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// delete one column
name|Delete
name|d
init|=
operator|new
name|Delete
argument_list|(
name|ROW
argument_list|)
decl_stmt|;
name|d
operator|.
name|deleteColumns
argument_list|(
name|FAMILY
argument_list|,
name|COLUMN1
argument_list|)
expr_stmt|;
name|t1
operator|.
name|delete
argument_list|(
name|d
argument_list|)
expr_stmt|;
comment|// replay the WAL, map table 1 to table 2
name|HLog
name|log
init|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
operator|.
name|getWAL
argument_list|()
decl_stmt|;
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|String
name|walInputDir
init|=
operator|new
name|Path
argument_list|(
name|cluster
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
argument_list|,
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
name|Configuration
name|configuration
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|WALPlayer
name|player
init|=
operator|new
name|WALPlayer
argument_list|(
name|configuration
argument_list|)
decl_stmt|;
name|String
name|optionName
init|=
literal|"_test_.name"
decl_stmt|;
name|configuration
operator|.
name|set
argument_list|(
name|optionName
argument_list|,
literal|"1000"
argument_list|)
expr_stmt|;
name|player
operator|.
name|setupTime
argument_list|(
name|configuration
argument_list|,
name|optionName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1000
argument_list|,
name|configuration
operator|.
name|getLong
argument_list|(
name|optionName
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|player
operator|.
name|run
argument_list|(
operator|new
name|String
index|[]
block|{
name|walInputDir
block|,
name|Bytes
operator|.
name|toString
argument_list|(
name|TABLENAME1
argument_list|)
block|,
name|Bytes
operator|.
name|toString
argument_list|(
name|TABLENAME2
argument_list|)
block|}
argument_list|)
argument_list|)
expr_stmt|;
comment|// verify the WAL was player into table 2
name|Get
name|g
init|=
operator|new
name|Get
argument_list|(
name|ROW
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
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|r
operator|.
name|rawCells
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|COLUMN2
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test HLogKeyValueMapper setup and map    */
annotation|@
name|Test
specifier|public
name|void
name|testHLogKeyValueMapper
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|configuration
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|configuration
operator|.
name|set
argument_list|(
name|WALPlayer
operator|.
name|TABLES_KEY
argument_list|,
literal|"table"
argument_list|)
expr_stmt|;
name|HLogKeyValueMapper
name|mapper
init|=
operator|new
name|HLogKeyValueMapper
argument_list|()
decl_stmt|;
name|HLogKey
name|key
init|=
name|mock
argument_list|(
name|HLogKey
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|key
operator|.
name|getTablename
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"table"
argument_list|)
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Mapper
argument_list|<
name|HLogKey
argument_list|,
name|WALEdit
argument_list|,
name|ImmutableBytesWritable
argument_list|,
name|KeyValue
argument_list|>
operator|.
name|Context
name|context
init|=
name|mock
argument_list|(
name|Context
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|context
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|configuration
argument_list|)
expr_stmt|;
name|WALEdit
name|value
init|=
name|mock
argument_list|(
name|WALEdit
operator|.
name|class
argument_list|)
decl_stmt|;
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|KeyValue
name|kv1
init|=
name|mock
argument_list|(
name|KeyValue
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|kv1
operator|.
name|getFamily
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|kv1
operator|.
name|getRow
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
argument_list|)
argument_list|)
expr_stmt|;
name|values
operator|.
name|add
argument_list|(
name|kv1
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|value
operator|.
name|getKeyValues
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|values
argument_list|)
expr_stmt|;
name|mapper
operator|.
name|setup
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|doAnswer
argument_list|(
operator|new
name|Answer
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|answer
parameter_list|(
name|InvocationOnMock
name|invocation
parameter_list|)
throws|throws
name|Throwable
block|{
name|ImmutableBytesWritable
name|writer
init|=
operator|(
name|ImmutableBytesWritable
operator|)
name|invocation
operator|.
name|getArguments
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|KeyValue
name|key
init|=
operator|(
name|KeyValue
operator|)
name|invocation
operator|.
name|getArguments
argument_list|()
index|[
literal|1
index|]
decl_stmt|;
name|assertEquals
argument_list|(
literal|"row"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|writer
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"row"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|key
operator|.
name|getRow
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
operator|.
name|when
argument_list|(
name|context
argument_list|)
operator|.
name|write
argument_list|(
name|any
argument_list|(
name|ImmutableBytesWritable
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|KeyValue
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|mapper
operator|.
name|map
argument_list|(
name|key
argument_list|,
name|value
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test main method    */
annotation|@
name|Test
specifier|public
name|void
name|testMainMethod
parameter_list|()
throws|throws
name|Exception
block|{
name|PrintStream
name|oldPrintStream
init|=
name|System
operator|.
name|err
decl_stmt|;
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
name|ByteArrayOutputStream
name|data
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|String
index|[]
name|args
init|=
block|{}
decl_stmt|;
name|System
operator|.
name|setErr
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|System
operator|.
name|setErr
argument_list|(
operator|new
name|PrintStream
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|WALPlayer
operator|.
name|main
argument_list|(
name|args
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should be SecurityException"
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
operator|-
literal|1
argument_list|,
name|newSecurityManager
operator|.
name|getExitCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"ERROR: Wrong number of arguments:"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Usage: WALPlayer [options]<wal inputdir>"
operator|+
literal|"<tables> [<tableMappings>]"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|data
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
literal|"-Dhlog.bulk.output=/path/for/output"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|System
operator|.
name|setErr
argument_list|(
name|oldPrintStream
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
block|}
block|}
end_class

end_unit

