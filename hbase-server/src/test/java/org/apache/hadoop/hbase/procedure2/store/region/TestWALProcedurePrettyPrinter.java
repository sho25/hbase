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
name|TestWALProcedurePrettyPrinter
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
name|TestWALProcedurePrettyPrinter
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
name|TestWALProcedurePrettyPrinter
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|Exception
block|{
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
name|cleanup
argument_list|()
expr_stmt|;
name|Path
name|walParentDir
init|=
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
operator|+
literal|"/"
operator|+
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|walParentDir
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
name|walDir
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|walParentDir
argument_list|)
index|[
literal|0
index|]
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|Path
name|walFile
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|walDir
argument_list|)
index|[
literal|0
index|]
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|store
operator|.
name|walRoller
operator|.
name|requestRollAll
argument_list|()
expr_stmt|;
name|store
operator|.
name|walRoller
operator|.
name|waitUntilWalRollFinished
argument_list|()
expr_stmt|;
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
name|WALProcedurePrettyPrinter
name|printer
init|=
operator|new
name|WALProcedurePrettyPrinter
argument_list|(
name|out
argument_list|)
decl_stmt|;
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
name|fs
operator|.
name|makeQualified
argument_list|(
name|walFile
argument_list|)
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
name|long
name|inserted
init|=
literal|0
decl_stmt|;
name|long
name|markedDeleted
init|=
literal|0
decl_stmt|;
name|long
name|deleted
init|=
literal|0
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
operator|==
literal|null
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|line
operator|.
name|startsWith
argument_list|(
literal|"\t"
argument_list|)
condition|)
block|{
if|if
condition|(
name|line
operator|.
name|startsWith
argument_list|(
literal|"\tpid="
argument_list|)
condition|)
block|{
name|inserted
operator|++
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
literal|"\tmark deleted"
argument_list|,
name|line
argument_list|)
expr_stmt|;
name|markedDeleted
operator|++
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|line
operator|.
name|contains
argument_list|(
literal|"type=DeleteFamily"
argument_list|)
condition|)
block|{
name|deleted
operator|++
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|inserted
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|markedDeleted
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|deleted
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

