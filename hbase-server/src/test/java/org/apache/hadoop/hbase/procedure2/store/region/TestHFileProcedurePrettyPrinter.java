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
name|procedure2
operator|.
name|store
operator|.
name|region
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
name|assertNotEquals
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
name|BufferedReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
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
name|InputStreamReader
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
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|lang3
operator|.
name|mutable
operator|.
name|MutableLong
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
name|FileSystem
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
name|RegionInfo
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
name|hfile
operator|.
name|HFile
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
name|MasterTests
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
name|CommonFSUtils
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
name|ToolRunner
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestHFileProcedurePrettyPrinter
extends|extends
name|RegionProcedureStoreTestBase
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
name|TestHFileProcedurePrettyPrinter
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|TestHFileProcedurePrettyPrinter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|checkOutput
parameter_list|(
name|BufferedReader
name|reader
parameter_list|,
name|MutableLong
name|putCount
parameter_list|,
name|MutableLong
name|deleteCount
parameter_list|,
name|MutableLong
name|markDeletedCount
parameter_list|)
throws|throws
name|IOException
block|{
name|putCount
operator|.
name|setValue
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|deleteCount
operator|.
name|setValue
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|markDeletedCount
operator|.
name|setValue
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fileScanned
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
init|;
condition|;
control|)
block|{
name|String
name|line
init|=
name|reader
operator|.
name|readLine
argument_list|()
decl_stmt|;
if|if
condition|(
name|line
operator|==
literal|null
condition|)
block|{
return|return
name|fileScanned
return|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|line
argument_list|)
expr_stmt|;
if|if
condition|(
name|line
operator|.
name|contains
argument_list|(
literal|"V: mark deleted"
argument_list|)
condition|)
block|{
name|markDeletedCount
operator|.
name|increment
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|line
operator|.
name|contains
argument_list|(
literal|"/Put/"
argument_list|)
condition|)
block|{
name|putCount
operator|.
name|increment
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|line
operator|.
name|contains
argument_list|(
literal|"/DeleteFamily/"
argument_list|)
condition|)
block|{
name|deleteCount
operator|.
name|increment
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|line
operator|.
name|startsWith
argument_list|(
literal|"Scanning -> "
argument_list|)
condition|)
block|{
name|fileScanned
operator|.
name|add
argument_list|(
name|line
operator|.
name|split
argument_list|(
literal|" -> "
argument_list|)
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fail
argument_list|(
literal|"Unrecognized output: "
operator|+
name|line
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|Exception
block|{
name|HFileProcedurePrettyPrinter
name|printer
init|=
operator|new
name|HFileProcedurePrettyPrinter
argument_list|()
decl_stmt|;
comment|// -a or -f is required so passing empty args will cause an error and return a non-zero value.
name|assertNotEquals
argument_list|(
literal|0
argument_list|,
name|ToolRunner
operator|.
name|run
argument_list|(
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|printer
argument_list|,
operator|new
name|String
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|RegionProcedureStoreTestProcedure
argument_list|>
name|procs
init|=
operator|new
name|ArrayList
argument_list|<>
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|RegionProcedureStoreTestProcedure
name|proc
init|=
operator|new
name|RegionProcedureStoreTestProcedure
argument_list|()
decl_stmt|;
name|store
operator|.
name|insert
argument_list|(
name|proc
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|procs
operator|.
name|add
argument_list|(
name|proc
argument_list|)
expr_stmt|;
block|}
name|store
operator|.
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|store
operator|.
name|delete
argument_list|(
name|procs
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getProcId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|store
operator|.
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|store
operator|.
name|cleanup
argument_list|()
expr_stmt|;
name|store
operator|.
name|region
operator|.
name|flush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Path
name|tableDir
init|=
name|CommonFSUtils
operator|.
name|getTableDir
argument_list|(
operator|new
name|Path
argument_list|(
name|htu
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
name|RegionProcedureStore
operator|.
name|MASTER_PROCEDURE_DIR
argument_list|)
argument_list|,
name|RegionProcedureStore
operator|.
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|tableDir
operator|.
name|getFileSystem
argument_list|(
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|regionDir
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|tableDir
argument_list|,
name|p
lambda|->
name|RegionInfo
operator|.
name|isEncodedRegionName
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|p
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
argument_list|)
index|[
literal|0
index|]
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|storefiles
init|=
name|HFile
operator|.
name|getStoreFiles
argument_list|(
name|fs
argument_list|,
name|regionDir
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|bos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|PrintStream
name|out
init|=
operator|new
name|PrintStream
argument_list|(
name|bos
argument_list|)
decl_stmt|;
name|MutableLong
name|putCount
init|=
operator|new
name|MutableLong
argument_list|()
decl_stmt|;
name|MutableLong
name|deleteCount
init|=
operator|new
name|MutableLong
argument_list|()
decl_stmt|;
name|MutableLong
name|markDeletedCount
init|=
operator|new
name|MutableLong
argument_list|()
decl_stmt|;
for|for
control|(
name|Path
name|file
range|:
name|storefiles
control|)
block|{
name|bos
operator|.
name|reset
argument_list|()
expr_stmt|;
name|printer
operator|=
operator|new
name|HFileProcedurePrettyPrinter
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ToolRunner
operator|.
name|run
argument_list|(
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|printer
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-f"
block|,
name|file
operator|.
name|toString
argument_list|()
block|}
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|bos
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
init|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|fileScanned
init|=
name|checkOutput
argument_list|(
name|reader
argument_list|,
name|putCount
argument_list|,
name|deleteCount
argument_list|,
name|markDeletedCount
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|fileScanned
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|file
operator|.
name|toString
argument_list|()
argument_list|,
name|fileScanned
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|putCount
operator|.
name|longValue
argument_list|()
operator|==
literal|10
condition|)
block|{
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|deleteCount
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|markDeletedCount
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|deleteCount
operator|.
name|longValue
argument_list|()
operator|==
literal|5
condition|)
block|{
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|putCount
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|markDeletedCount
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|markDeletedCount
operator|.
name|longValue
argument_list|()
operator|==
literal|5
condition|)
block|{
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|putCount
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|deleteCount
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fail
argument_list|(
literal|"Should have entered one of the above 3 branches"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|bos
operator|.
name|reset
argument_list|()
expr_stmt|;
name|printer
operator|=
operator|new
name|HFileProcedurePrettyPrinter
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ToolRunner
operator|.
name|run
argument_list|(
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|printer
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"-a"
block|}
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|bos
operator|.
name|toByteArray
argument_list|()
argument_list|)
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
init|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|fileScanned
init|=
name|checkOutput
argument_list|(
name|reader
argument_list|,
name|putCount
argument_list|,
name|deleteCount
argument_list|,
name|markDeletedCount
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|fileScanned
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|putCount
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|deleteCount
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|markDeletedCount
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

