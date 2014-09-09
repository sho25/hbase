begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*   * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|util
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
name|io
operator|.
name|File
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
name|HColumnDescriptor
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
name|HTestConst
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
name|ResultScanner
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
name|client
operator|.
name|Table
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

begin_comment
comment|/**  * A basic unit test that spins up a local HBase cluster.  */
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
name|TestProcessBasedCluster
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
name|TestProcessBasedCluster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|COLS_PER_ROW
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|FLUSHES
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_REGIONS
init|=
literal|5
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ROWS_PER_FLUSH
init|=
literal|5
decl_stmt|;
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
comment|// DISABLED BECAUSE FLAKEY @Test(timeout=300 * 1000)
specifier|public
name|void
name|testProcessBasedCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|ProcessBasedLocalHBaseCluster
name|cluster
init|=
operator|new
name|ProcessBasedLocalHBaseCluster
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|cluster
operator|.
name|startMiniDFS
argument_list|()
expr_stmt|;
name|cluster
operator|.
name|startHBase
argument_list|()
expr_stmt|;
try|try
block|{
name|TEST_UTIL
operator|.
name|createRandomTable
argument_list|(
name|HTestConst
operator|.
name|DEFAULT_TABLE_STR
argument_list|,
name|HTestConst
operator|.
name|DEFAULT_CF_STR_SET
argument_list|,
name|HColumnDescriptor
operator|.
name|DEFAULT_VERSIONS
argument_list|,
name|COLS_PER_ROW
argument_list|,
name|FLUSHES
argument_list|,
name|NUM_REGIONS
argument_list|,
name|ROWS_PER_FLUSH
argument_list|)
expr_stmt|;
name|Table
name|table
init|=
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|HTestConst
operator|.
name|DEFAULT_TABLE_BYTES
argument_list|)
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|HTestConst
operator|.
name|DEFAULT_CF_BYTES
argument_list|)
decl_stmt|;
name|Result
name|result
decl_stmt|;
name|int
name|rows
init|=
literal|0
decl_stmt|;
name|int
name|cols
init|=
literal|0
decl_stmt|;
while|while
condition|(
operator|(
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
operator|++
name|rows
expr_stmt|;
name|cols
operator|+=
name|result
operator|.
name|getFamilyMap
argument_list|(
name|HTestConst
operator|.
name|DEFAULT_CF_BYTES
argument_list|)
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Read "
operator|+
name|rows
operator|+
literal|" rows, "
operator|+
name|cols
operator|+
literal|" columns"
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// These numbers are deterministic, seeded by table name.
name|assertEquals
argument_list|(
literal|19
argument_list|,
name|rows
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|35
argument_list|,
name|cols
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
name|ex
argument_list|)
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
finally|finally
block|{
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHomePath
parameter_list|()
block|{
name|File
name|pom
init|=
operator|new
name|File
argument_list|(
name|HBaseHomePath
operator|.
name|getHomePath
argument_list|()
argument_list|,
literal|"pom.xml"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|pom
operator|.
name|getPath
argument_list|()
operator|+
literal|" does not exist"
argument_list|,
name|pom
operator|.
name|exists
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

