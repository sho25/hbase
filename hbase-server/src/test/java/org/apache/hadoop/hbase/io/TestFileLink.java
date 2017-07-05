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
name|io
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
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
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
name|FSDataInputStream
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
name|testclassification
operator|.
name|IOTests
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
name|FSUtils
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
name|DistributedFileSystem
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
name|apache
operator|.
name|hadoop
operator|.
name|ipc
operator|.
name|RemoteException
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
comment|/**  * Test that FileLink switches between alternate locations  * when the current location moves or gets deleted.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|IOTests
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
name|TestFileLink
block|{
annotation|@
name|Test
specifier|public
name|void
name|testEquals
parameter_list|()
block|{
name|Path
name|p1
init|=
operator|new
name|Path
argument_list|(
literal|"/p1"
argument_list|)
decl_stmt|;
name|Path
name|p2
init|=
operator|new
name|Path
argument_list|(
literal|"/p2"
argument_list|)
decl_stmt|;
name|Path
name|p3
init|=
operator|new
name|Path
argument_list|(
literal|"/p3"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|FileLink
argument_list|()
argument_list|,
operator|new
name|FileLink
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|)
argument_list|,
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|)
argument_list|,
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|,
name|p3
argument_list|)
argument_list|,
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|,
name|p3
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|)
argument_list|,
operator|new
name|FileLink
argument_list|(
name|p3
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|)
argument_list|,
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|)
argument_list|,
operator|new
name|FileLink
argument_list|(
name|p2
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|)
argument_list|,
operator|new
name|FileLink
argument_list|(
name|p2
argument_list|,
name|p1
argument_list|)
argument_list|)
expr_stmt|;
comment|// ordering important!
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHashCode
parameter_list|()
block|{
name|Path
name|p1
init|=
operator|new
name|Path
argument_list|(
literal|"/p1"
argument_list|)
decl_stmt|;
name|Path
name|p2
init|=
operator|new
name|Path
argument_list|(
literal|"/p2"
argument_list|)
decl_stmt|;
name|Path
name|p3
init|=
operator|new
name|Path
argument_list|(
literal|"/p3"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|FileLink
argument_list|()
operator|.
name|hashCode
argument_list|()
argument_list|,
operator|new
name|FileLink
argument_list|()
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|,
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|,
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|,
name|p3
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|,
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|,
name|p3
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|,
operator|new
name|FileLink
argument_list|(
name|p3
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|,
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|,
operator|new
name|FileLink
argument_list|(
name|p2
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
operator|new
name|FileLink
argument_list|(
name|p1
argument_list|,
name|p2
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|,
operator|new
name|FileLink
argument_list|(
name|p2
argument_list|,
name|p1
argument_list|)
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
comment|// ordering
block|}
comment|/**    * Test, on HDFS, that the FileLink is still readable    * even when the current file gets renamed.    */
annotation|@
name|Test
specifier|public
name|void
name|testHDFSLinkReadDuringRename
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseTestingUtility
name|testUtil
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|testUtil
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"dfs.blocksize"
argument_list|,
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"dfs.client.read.prefetch.size"
argument_list|,
literal|2
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|testUtil
operator|.
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|MiniDFSCluster
name|cluster
init|=
name|testUtil
operator|.
name|getDFSCluster
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|cluster
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"hdfs"
argument_list|,
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|testLinkReadDuringRename
argument_list|(
name|fs
argument_list|,
name|testUtil
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|testUtil
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
class|class
name|MyDistributedFileSystem
extends|extends
name|DistributedFileSystem
block|{
name|MyDistributedFileSystem
parameter_list|()
block|{     }
annotation|@
name|Override
specifier|public
name|FSDataInputStream
name|open
parameter_list|(
name|Path
name|f
parameter_list|,
specifier|final
name|int
name|bufferSize
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|RemoteException
argument_list|(
name|FileNotFoundException
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
literal|""
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConf
parameter_list|()
block|{
return|return
operator|new
name|Configuration
argument_list|()
return|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|FileNotFoundException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testLinkReadWithMissingFile
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseTestingUtility
name|testUtil
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
operator|new
name|MyDistributedFileSystem
argument_list|()
decl_stmt|;
name|Path
name|originalPath
init|=
operator|new
name|Path
argument_list|(
name|testUtil
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|,
literal|"test.file"
argument_list|)
decl_stmt|;
name|Path
name|archivedPath
init|=
operator|new
name|Path
argument_list|(
name|testUtil
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|,
literal|"archived.file"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<
name|Path
argument_list|>
argument_list|()
decl_stmt|;
name|files
operator|.
name|add
argument_list|(
name|originalPath
argument_list|)
expr_stmt|;
name|files
operator|.
name|add
argument_list|(
name|archivedPath
argument_list|)
expr_stmt|;
name|FileLink
name|link
init|=
operator|new
name|FileLink
argument_list|(
name|files
argument_list|)
decl_stmt|;
name|link
operator|.
name|open
argument_list|(
name|fs
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test, on a local filesystem, that the FileLink is still readable    * even when the current file gets renamed.    */
annotation|@
name|Test
specifier|public
name|void
name|testLocalLinkReadDuringRename
parameter_list|()
throws|throws
name|IOException
block|{
name|HBaseTestingUtility
name|testUtil
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|testUtil
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"file"
argument_list|,
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|)
expr_stmt|;
name|testLinkReadDuringRename
argument_list|(
name|fs
argument_list|,
name|testUtil
operator|.
name|getDataTestDir
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test that link is still readable even when the current file gets renamed.    */
specifier|private
name|void
name|testLinkReadDuringRename
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|rootDir
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|originalPath
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
literal|"test.file"
argument_list|)
decl_stmt|;
name|Path
name|archivedPath
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
literal|"archived.file"
argument_list|)
decl_stmt|;
name|writeSomeData
argument_list|(
name|fs
argument_list|,
name|originalPath
argument_list|,
literal|256
operator|<<
literal|20
argument_list|,
operator|(
name|byte
operator|)
literal|2
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|files
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|files
operator|.
name|add
argument_list|(
name|originalPath
argument_list|)
expr_stmt|;
name|files
operator|.
name|add
argument_list|(
name|archivedPath
argument_list|)
expr_stmt|;
name|FileLink
name|link
init|=
operator|new
name|FileLink
argument_list|(
name|files
argument_list|)
decl_stmt|;
name|FSDataInputStream
name|in
init|=
name|link
operator|.
name|open
argument_list|(
name|fs
argument_list|)
decl_stmt|;
try|try
block|{
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[
literal|8192
index|]
decl_stmt|;
name|long
name|size
init|=
literal|0
decl_stmt|;
comment|// Read from origin
name|int
name|n
init|=
name|in
operator|.
name|read
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|dataVerify
argument_list|(
name|data
argument_list|,
name|n
argument_list|,
operator|(
name|byte
operator|)
literal|2
argument_list|)
expr_stmt|;
name|size
operator|+=
name|n
expr_stmt|;
if|if
condition|(
name|FSUtils
operator|.
name|WINDOWS
condition|)
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// Move origin to archive
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|archivedPath
argument_list|)
argument_list|)
expr_stmt|;
name|fs
operator|.
name|rename
argument_list|(
name|originalPath
argument_list|,
name|archivedPath
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|originalPath
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|archivedPath
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|FSUtils
operator|.
name|WINDOWS
condition|)
block|{
name|in
operator|=
name|link
operator|.
name|open
argument_list|(
name|fs
argument_list|)
expr_stmt|;
comment|// re-read from beginning
name|in
operator|.
name|read
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
comment|// Try to read to the end
while|while
condition|(
operator|(
name|n
operator|=
name|in
operator|.
name|read
argument_list|(
name|data
argument_list|)
operator|)
operator|>
literal|0
condition|)
block|{
name|dataVerify
argument_list|(
name|data
argument_list|,
name|n
argument_list|,
operator|(
name|byte
operator|)
literal|2
argument_list|)
expr_stmt|;
name|size
operator|+=
name|n
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|256
operator|<<
literal|20
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|originalPath
argument_list|)
condition|)
name|fs
operator|.
name|delete
argument_list|(
name|originalPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|archivedPath
argument_list|)
condition|)
name|fs
operator|.
name|delete
argument_list|(
name|archivedPath
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test that link is still readable even when the current file gets deleted.    *    * NOTE: This test is valid only on HDFS.    * When a file is deleted from a local file-system, it is simply 'unlinked'.    * The inode, which contains the file's data, is not deleted until all    * processes have finished with it.    * In HDFS when the request exceed the cached block locations,    * a query to the namenode is performed, using the filename,    * and the deleted file doesn't exists anymore (FileNotFoundException).    */
annotation|@
name|Test
specifier|public
name|void
name|testHDFSLinkReadDuringDelete
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseTestingUtility
name|testUtil
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|testUtil
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"dfs.blocksize"
argument_list|,
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"dfs.client.read.prefetch.size"
argument_list|,
literal|2
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
expr_stmt|;
name|testUtil
operator|.
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|MiniDFSCluster
name|cluster
init|=
name|testUtil
operator|.
name|getDFSCluster
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|cluster
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"hdfs"
argument_list|,
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|getScheme
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|files
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
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"test-data-%d"
argument_list|,
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|writeSomeData
argument_list|(
name|fs
argument_list|,
name|path
argument_list|,
literal|1
operator|<<
literal|20
argument_list|,
operator|(
name|byte
operator|)
name|i
argument_list|)
expr_stmt|;
name|files
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
name|FileLink
name|link
init|=
operator|new
name|FileLink
argument_list|(
name|files
argument_list|)
decl_stmt|;
name|FSDataInputStream
name|in
init|=
name|link
operator|.
name|open
argument_list|(
name|fs
argument_list|)
decl_stmt|;
try|try
block|{
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[
literal|8192
index|]
decl_stmt|;
name|int
name|n
decl_stmt|;
comment|// Switch to file 1
name|n
operator|=
name|in
operator|.
name|read
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|dataVerify
argument_list|(
name|data
argument_list|,
name|n
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|files
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|skipBuffer
argument_list|(
name|in
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
comment|// Switch to file 2
name|n
operator|=
name|in
operator|.
name|read
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|dataVerify
argument_list|(
name|data
argument_list|,
name|n
argument_list|,
operator|(
name|byte
operator|)
literal|1
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|files
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|skipBuffer
argument_list|(
name|in
argument_list|,
operator|(
name|byte
operator|)
literal|1
argument_list|)
expr_stmt|;
comment|// Switch to file 3
name|n
operator|=
name|in
operator|.
name|read
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|dataVerify
argument_list|(
name|data
argument_list|,
name|n
argument_list|,
operator|(
name|byte
operator|)
literal|2
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|files
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|skipBuffer
argument_list|(
name|in
argument_list|,
operator|(
name|byte
operator|)
literal|2
argument_list|)
expr_stmt|;
comment|// No more files available
try|try
block|{
name|n
operator|=
name|in
operator|.
name|read
argument_list|(
name|data
argument_list|)
expr_stmt|;
assert|assert
operator|(
name|n
operator|<=
literal|0
operator|)
assert|;
block|}
catch|catch
parameter_list|(
name|FileNotFoundException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|testUtil
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Write up to 'size' bytes with value 'v' into a new file called 'path'.    */
specifier|private
name|void
name|writeSomeData
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|path
parameter_list|,
name|long
name|size
parameter_list|,
name|byte
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[
literal|4096
index|]
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
name|data
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|data
index|[
name|i
index|]
operator|=
name|v
expr_stmt|;
block|}
name|FSDataOutputStream
name|stream
init|=
name|fs
operator|.
name|create
argument_list|(
name|path
argument_list|)
decl_stmt|;
try|try
block|{
name|long
name|written
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|written
operator|<
name|size
condition|)
block|{
name|stream
operator|.
name|write
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|written
operator|+=
name|data
operator|.
name|length
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|stream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Verify that all bytes in 'data' have 'v' as value.    */
specifier|private
specifier|static
name|void
name|dataVerify
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|n
parameter_list|,
name|byte
name|v
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|n
condition|;
operator|++
name|i
control|)
block|{
name|assertEquals
argument_list|(
name|v
argument_list|,
name|data
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|skipBuffer
parameter_list|(
name|FSDataInputStream
name|in
parameter_list|,
name|byte
name|v
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[
literal|8192
index|]
decl_stmt|;
try|try
block|{
name|int
name|n
decl_stmt|;
while|while
condition|(
operator|(
name|n
operator|=
name|in
operator|.
name|read
argument_list|(
name|data
argument_list|)
operator|)
operator|==
name|data
operator|.
name|length
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|data
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|data
index|[
name|i
index|]
operator|!=
name|v
condition|)
throw|throw
operator|new
name|Exception
argument_list|(
literal|"File changed"
argument_list|)
throw|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{     }
block|}
block|}
end_class

end_unit

