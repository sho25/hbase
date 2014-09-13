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
name|snapshot
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
name|assertFalse
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
name|IOException
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|SnapshotDescription
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
name|util
operator|.
name|EnvironmentEdgeManagerTestHelper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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

begin_comment
comment|/**  * Test that the {@link SnapshotDescription} helper is helping correctly.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
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
name|TestSnapshotDescriptionUtils
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|Path
name|root
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupFS
parameter_list|()
throws|throws
name|Exception
block|{
name|fs
operator|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
expr_stmt|;
name|root
operator|=
operator|new
name|Path
argument_list|(
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|,
literal|"hbase"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|cleanupFS
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|root
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|root
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to delete root test dir: "
operator|+
name|root
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|mkdirs
argument_list|(
name|root
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to create root test dir: "
operator|+
name|root
argument_list|)
throw|;
block|}
block|}
name|EnvironmentEdgeManagerTestHelper
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
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
name|TestSnapshotDescriptionUtils
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testValidateMissingTableName
parameter_list|()
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
decl_stmt|;
try|try
block|{
name|SnapshotDescriptionUtils
operator|.
name|validate
argument_list|(
name|SnapshotDescription
operator|.
name|newBuilder
argument_list|()
operator|.
name|setName
argument_list|(
literal|"fail"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Snapshot was considered valid without a table name"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Correctly failed when snapshot doesn't have a tablename"
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test that we throw an exception if there is no working snapshot directory when we attempt to    * 'complete' the snapshot    * @throws Exception on failure    */
annotation|@
name|Test
specifier|public
name|void
name|testCompleteSnapshotWithNoSnapshotDirectoryFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|snapshotDir
init|=
operator|new
name|Path
argument_list|(
name|root
argument_list|,
name|HConstants
operator|.
name|SNAPSHOT_DIR_NAME
argument_list|)
decl_stmt|;
name|Path
name|tmpDir
init|=
operator|new
name|Path
argument_list|(
name|snapshotDir
argument_list|,
literal|".tmp"
argument_list|)
decl_stmt|;
name|Path
name|workingDir
init|=
operator|new
name|Path
argument_list|(
name|tmpDir
argument_list|,
literal|"not_a_snapshot"
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"Already have working snapshot dir: "
operator|+
name|workingDir
operator|+
literal|" but shouldn't. Test file leak?"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|workingDir
argument_list|)
argument_list|)
expr_stmt|;
name|SnapshotDescription
name|snapshot
init|=
name|SnapshotDescription
operator|.
name|newBuilder
argument_list|()
operator|.
name|setName
argument_list|(
literal|"snapshot"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
block|{
name|SnapshotDescriptionUtils
operator|.
name|completeSnapshot
argument_list|(
name|snapshot
argument_list|,
name|root
argument_list|,
name|workingDir
argument_list|,
name|fs
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Shouldn't successfully complete move of a non-existent directory."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Correctly failed to move non-existant directory: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

