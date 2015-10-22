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
name|hdfs
operator|.
name|MiniDFSCluster
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
name|java
operator|.
name|util
operator|.
name|UUID
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

begin_comment
comment|/**  * Test that an HBase cluster can run on top of an existing MiniDfsCluster  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestHBaseOnOtherDfsCluster
block|{
annotation|@
name|Test
specifier|public
name|void
name|testOveralyOnOtherCluster
parameter_list|()
throws|throws
name|Exception
block|{
comment|// just run HDFS
name|HBaseTestingUtility
name|util1
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|MiniDFSCluster
name|dfs
init|=
name|util1
operator|.
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
decl_stmt|;
comment|// run HBase on that HDFS
name|HBaseTestingUtility
name|util2
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|// set the dfs
name|util2
operator|.
name|setDFSCluster
argument_list|(
name|dfs
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|util2
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
comment|//ensure that they are pointed at the same place
name|FileSystem
name|fs
init|=
name|dfs
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|FileSystem
name|targetFs
init|=
name|util2
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|assertFsSameUri
argument_list|(
name|fs
argument_list|,
name|targetFs
argument_list|)
expr_stmt|;
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|util1
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|targetFs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|util2
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|assertFsSameUri
argument_list|(
name|fs
argument_list|,
name|targetFs
argument_list|)
expr_stmt|;
name|Path
name|randomFile
init|=
operator|new
name|Path
argument_list|(
literal|"/"
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|targetFs
operator|.
name|createNewFile
argument_list|(
name|randomFile
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|randomFile
argument_list|)
argument_list|)
expr_stmt|;
comment|// do a simple create/write to ensure the cluster works as expected
name|byte
index|[]
name|family
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testfamily"
argument_list|)
decl_stmt|;
name|TableName
name|tablename
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testtable"
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|util2
operator|.
name|createTable
argument_list|(
name|tablename
argument_list|,
name|family
argument_list|)
decl_stmt|;
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|}
argument_list|)
decl_stmt|;
name|p
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
literal|null
argument_list|,
operator|new
name|byte
index|[]
block|{
literal|1
block|}
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
comment|// shutdown and make sure cleanly shutting down
name|util2
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|util1
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|assertFsSameUri
parameter_list|(
name|FileSystem
name|sourceFs
parameter_list|,
name|FileSystem
name|targetFs
parameter_list|)
block|{
name|Path
name|source
init|=
operator|new
name|Path
argument_list|(
name|sourceFs
operator|.
name|getUri
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|target
init|=
operator|new
name|Path
argument_list|(
name|targetFs
operator|.
name|getUri
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|source
argument_list|,
name|target
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

