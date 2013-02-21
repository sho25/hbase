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
name|assertFalse
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
name|FSDataOutputStream
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
name|FileStatus
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
name|errorhandling
operator|.
name|ForeignException
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
name|errorhandling
operator|.
name|ForeignExceptionDispatcher
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
name|regionserver
operator|.
name|wal
operator|.
name|HLogUtil
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
name|snapshot
operator|.
name|CopyRecoveredEditsTask
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
name|snapshot
operator|.
name|SnapshotDescriptionUtils
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
name|FSUtils
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
name|Mockito
import|;
end_import

begin_comment
comment|/**  * Test that we correctly copy the recovered edits from a directory  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestCopyRecoveredEditsTask
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
annotation|@
name|Test
specifier|public
name|void
name|testCopyFiles
parameter_list|()
throws|throws
name|Exception
block|{
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
name|ForeignExceptionDispatcher
name|monitor
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ForeignExceptionDispatcher
operator|.
name|class
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Path
name|root
init|=
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
name|String
name|regionName
init|=
literal|"regionA"
decl_stmt|;
name|Path
name|regionDir
init|=
operator|new
name|Path
argument_list|(
name|root
argument_list|,
name|regionName
argument_list|)
decl_stmt|;
name|Path
name|workingDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getWorkingSnapshotDir
argument_list|(
name|snapshot
argument_list|,
name|root
argument_list|)
decl_stmt|;
try|try
block|{
comment|// doesn't really matter where the region's snapshot directory is, but this is pretty close
name|Path
name|snapshotRegionDir
init|=
operator|new
name|Path
argument_list|(
name|workingDir
argument_list|,
name|regionName
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|snapshotRegionDir
argument_list|)
expr_stmt|;
comment|// put some stuff in the recovered.edits directory
name|Path
name|edits
init|=
name|HLogUtil
operator|.
name|getRegionDirRecoveredEditsDir
argument_list|(
name|regionDir
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|edits
argument_list|)
expr_stmt|;
comment|// make a file with some data
name|Path
name|file1
init|=
operator|new
name|Path
argument_list|(
name|edits
argument_list|,
literal|"0000000000000002352"
argument_list|)
decl_stmt|;
name|FSDataOutputStream
name|out
init|=
name|fs
operator|.
name|create
argument_list|(
name|file1
argument_list|)
decl_stmt|;
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[]
block|{
literal|1
block|,
literal|2
block|,
literal|3
block|,
literal|4
block|}
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// make an empty file
name|Path
name|empty
init|=
operator|new
name|Path
argument_list|(
name|edits
argument_list|,
literal|"empty"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|empty
argument_list|)
expr_stmt|;
name|CopyRecoveredEditsTask
name|task
init|=
operator|new
name|CopyRecoveredEditsTask
argument_list|(
name|snapshot
argument_list|,
name|monitor
argument_list|,
name|fs
argument_list|,
name|regionDir
argument_list|,
name|snapshotRegionDir
argument_list|)
decl_stmt|;
name|CopyRecoveredEditsTask
name|taskSpy
init|=
name|Mockito
operator|.
name|spy
argument_list|(
name|task
argument_list|)
decl_stmt|;
name|taskSpy
operator|.
name|call
argument_list|()
expr_stmt|;
name|Path
name|snapshotEdits
init|=
name|HLogUtil
operator|.
name|getRegionDirRecoveredEditsDir
argument_list|(
name|snapshotRegionDir
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|snapshotEditFiles
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|snapshotEdits
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Got wrong number of files in the snapshot edits"
argument_list|,
literal|1
argument_list|,
name|snapshotEditFiles
operator|.
name|length
argument_list|)
expr_stmt|;
name|FileStatus
name|file
init|=
name|snapshotEditFiles
index|[
literal|0
index|]
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Didn't copy expected file"
argument_list|,
name|file1
operator|.
name|getName
argument_list|()
argument_list|,
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|monitor
argument_list|,
name|Mockito
operator|.
name|never
argument_list|()
argument_list|)
operator|.
name|receive
argument_list|(
name|Mockito
operator|.
name|any
argument_list|(
name|ForeignException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|taskSpy
argument_list|,
name|Mockito
operator|.
name|never
argument_list|()
argument_list|)
operator|.
name|snapshotFailure
argument_list|(
name|Mockito
operator|.
name|anyString
argument_list|()
argument_list|,
name|Mockito
operator|.
name|any
argument_list|(
name|Exception
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// cleanup the working directory
name|FSUtils
operator|.
name|delete
argument_list|(
name|fs
argument_list|,
name|regionDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|delete
argument_list|(
name|fs
argument_list|,
name|workingDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Check that we don't get an exception if there is no recovered edits directory to copy    * @throws Exception on failure    */
annotation|@
name|Test
specifier|public
name|void
name|testNoEditsDir
parameter_list|()
throws|throws
name|Exception
block|{
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
name|ForeignExceptionDispatcher
name|monitor
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ForeignExceptionDispatcher
operator|.
name|class
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Path
name|root
init|=
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
name|String
name|regionName
init|=
literal|"regionA"
decl_stmt|;
name|Path
name|regionDir
init|=
operator|new
name|Path
argument_list|(
name|root
argument_list|,
name|regionName
argument_list|)
decl_stmt|;
name|Path
name|workingDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getWorkingSnapshotDir
argument_list|(
name|snapshot
argument_list|,
name|root
argument_list|)
decl_stmt|;
try|try
block|{
comment|// doesn't really matter where the region's snapshot directory is, but this is pretty close
name|Path
name|snapshotRegionDir
init|=
operator|new
name|Path
argument_list|(
name|workingDir
argument_list|,
name|regionName
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|snapshotRegionDir
argument_list|)
expr_stmt|;
name|Path
name|regionEdits
init|=
name|HLogUtil
operator|.
name|getRegionDirRecoveredEditsDir
argument_list|(
name|regionDir
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"Edits dir exists already - it shouldn't"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|regionEdits
argument_list|)
argument_list|)
expr_stmt|;
name|CopyRecoveredEditsTask
name|task
init|=
operator|new
name|CopyRecoveredEditsTask
argument_list|(
name|snapshot
argument_list|,
name|monitor
argument_list|,
name|fs
argument_list|,
name|regionDir
argument_list|,
name|snapshotRegionDir
argument_list|)
decl_stmt|;
name|task
operator|.
name|call
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
comment|// cleanup the working directory
name|FSUtils
operator|.
name|delete
argument_list|(
name|fs
argument_list|,
name|regionDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|FSUtils
operator|.
name|delete
argument_list|(
name|fs
argument_list|,
name|workingDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

