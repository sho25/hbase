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
name|assertTrue
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
name|ProtobufUtil
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
name|TestReferenceRegionHFilesTask
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
name|testRun
parameter_list|()
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
comment|// setup the region internals
name|Path
name|testdir
init|=
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
name|Path
name|regionDir
init|=
operator|new
name|Path
argument_list|(
name|testdir
argument_list|,
literal|"region"
argument_list|)
decl_stmt|;
name|Path
name|family1
init|=
operator|new
name|Path
argument_list|(
name|regionDir
argument_list|,
literal|"fam1"
argument_list|)
decl_stmt|;
comment|// make an empty family
name|Path
name|family2
init|=
operator|new
name|Path
argument_list|(
name|regionDir
argument_list|,
literal|"fam2"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|family2
argument_list|)
expr_stmt|;
comment|// add some files to family 1
name|Path
name|file1
init|=
operator|new
name|Path
argument_list|(
name|family1
argument_list|,
literal|"05f99689ae254693836613d1884c6b63"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|file1
argument_list|)
expr_stmt|;
name|Path
name|file2
init|=
operator|new
name|Path
argument_list|(
name|family1
argument_list|,
literal|"7ac9898bf41d445aa0003e3d699d5d26"
argument_list|)
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|file2
argument_list|)
expr_stmt|;
comment|// create the snapshot directory
name|Path
name|snapshotRegionDir
init|=
operator|new
name|Path
argument_list|(
name|testdir
argument_list|,
name|HConstants
operator|.
name|SNAPSHOT_DIR_NAME
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|snapshotRegionDir
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
literal|"name"
argument_list|)
operator|.
name|setTable
argument_list|(
literal|"table"
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
name|ReferenceRegionHFilesTask
name|task
init|=
operator|new
name|ReferenceRegionHFilesTask
argument_list|(
name|snapshot
argument_list|,
name|monitor
argument_list|,
name|regionDir
argument_list|,
name|fs
argument_list|,
name|snapshotRegionDir
argument_list|)
decl_stmt|;
name|ReferenceRegionHFilesTask
name|taskSpy
init|=
name|Mockito
operator|.
name|spy
argument_list|(
name|task
argument_list|)
decl_stmt|;
name|task
operator|.
name|call
argument_list|()
expr_stmt|;
comment|// make sure we never get an error
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
comment|// verify that all the hfiles get referenced
name|List
argument_list|<
name|String
argument_list|>
name|hfiles
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|regions
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|snapshotRegionDir
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|region
range|:
name|regions
control|)
block|{
name|FileStatus
index|[]
name|fams
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|region
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|fam
range|:
name|fams
control|)
block|{
name|FileStatus
index|[]
name|files
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|fam
operator|.
name|getPath
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|FileStatus
name|file
range|:
name|files
control|)
block|{
name|hfiles
operator|.
name|add
argument_list|(
name|file
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|assertTrue
argument_list|(
literal|"Didn't reference :"
operator|+
name|file1
argument_list|,
name|hfiles
operator|.
name|contains
argument_list|(
name|file1
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Didn't reference :"
operator|+
name|file1
argument_list|,
name|hfiles
operator|.
name|contains
argument_list|(
name|file2
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

