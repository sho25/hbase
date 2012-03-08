begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|util
operator|.
name|UUID
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
name|fs
operator|.
name|permission
operator|.
name|FsPermission
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
name|HBaseConfiguration
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
name|HDFSBlocksDistribution
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

begin_comment
comment|/**  * Test {@link FSUtils}.  */
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
name|TestFSUtils
block|{
annotation|@
name|Test
specifier|public
name|void
name|testIsHDFS
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseTestingUtility
name|htu
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|htu
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"dfs.support.append"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|FSUtils
operator|.
name|isHDFS
argument_list|(
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|htu
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
literal|"dfs.support.append"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|MiniDFSCluster
name|cluster
init|=
literal|null
decl_stmt|;
try|try
block|{
name|cluster
operator|=
name|htu
operator|.
name|startMiniDFSCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|FSUtils
operator|.
name|isHDFS
argument_list|(
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|FSUtils
operator|.
name|isAppendSupported
argument_list|(
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|cluster
operator|!=
literal|null
condition|)
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|WriteDataToHDFS
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|Path
name|file
parameter_list|,
name|int
name|dataSize
parameter_list|)
throws|throws
name|Exception
block|{
name|FSDataOutputStream
name|out
init|=
name|fs
operator|.
name|create
argument_list|(
name|file
argument_list|)
decl_stmt|;
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[
name|dataSize
index|]
decl_stmt|;
name|out
operator|.
name|write
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|dataSize
argument_list|)
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testcomputeHDFSBlocksDistribution
parameter_list|()
throws|throws
name|Exception
block|{
name|HBaseTestingUtility
name|htu
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|final
name|int
name|DEFAULT_BLOCK_SIZE
init|=
literal|1024
decl_stmt|;
name|htu
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
literal|"dfs.block.size"
argument_list|,
name|DEFAULT_BLOCK_SIZE
argument_list|)
expr_stmt|;
name|MiniDFSCluster
name|cluster
init|=
literal|null
decl_stmt|;
name|Path
name|testFile
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// set up a cluster with 3 nodes
name|String
name|hosts
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"host1"
block|,
literal|"host2"
block|,
literal|"host3"
block|}
decl_stmt|;
name|cluster
operator|=
name|htu
operator|.
name|startMiniDFSCluster
argument_list|(
name|hosts
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitActive
argument_list|()
expr_stmt|;
name|FileSystem
name|fs
init|=
name|cluster
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
comment|// create a file with two blocks
name|testFile
operator|=
operator|new
name|Path
argument_list|(
literal|"/test1.txt"
argument_list|)
expr_stmt|;
name|WriteDataToHDFS
argument_list|(
name|fs
argument_list|,
name|testFile
argument_list|,
literal|2
operator|*
name|DEFAULT_BLOCK_SIZE
argument_list|)
expr_stmt|;
comment|// given the default replication factor is 3, the same as the number of
comment|// datanodes; the locality index for each host should be 100%,
comment|// or getWeight for each host should be the same as getUniqueBlocksWeights
name|FileStatus
name|status
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|testFile
argument_list|)
decl_stmt|;
name|HDFSBlocksDistribution
name|blocksDistribution
init|=
name|FSUtils
operator|.
name|computeHDFSBlocksDistribution
argument_list|(
name|fs
argument_list|,
name|status
argument_list|,
literal|0
argument_list|,
name|status
operator|.
name|getLen
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|uniqueBlocksTotalWeight
init|=
name|blocksDistribution
operator|.
name|getUniqueBlocksTotalWeight
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|host
range|:
name|hosts
control|)
block|{
name|long
name|weight
init|=
name|blocksDistribution
operator|.
name|getWeight
argument_list|(
name|host
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|uniqueBlocksTotalWeight
operator|==
name|weight
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|htu
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
block|}
try|try
block|{
comment|// set up a cluster with 4 nodes
name|String
name|hosts
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"host1"
block|,
literal|"host2"
block|,
literal|"host3"
block|,
literal|"host4"
block|}
decl_stmt|;
name|cluster
operator|=
name|htu
operator|.
name|startMiniDFSCluster
argument_list|(
name|hosts
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitActive
argument_list|()
expr_stmt|;
name|FileSystem
name|fs
init|=
name|cluster
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
comment|// create a file with three blocks
name|testFile
operator|=
operator|new
name|Path
argument_list|(
literal|"/test2.txt"
argument_list|)
expr_stmt|;
name|WriteDataToHDFS
argument_list|(
name|fs
argument_list|,
name|testFile
argument_list|,
literal|3
operator|*
name|DEFAULT_BLOCK_SIZE
argument_list|)
expr_stmt|;
comment|// given the default replication factor is 3, we will have total of 9
comment|// replica of blocks; thus the host with the highest weight should have
comment|// weight == 3 * DEFAULT_BLOCK_SIZE
name|FileStatus
name|status
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|testFile
argument_list|)
decl_stmt|;
name|HDFSBlocksDistribution
name|blocksDistribution
init|=
name|FSUtils
operator|.
name|computeHDFSBlocksDistribution
argument_list|(
name|fs
argument_list|,
name|status
argument_list|,
literal|0
argument_list|,
name|status
operator|.
name|getLen
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|uniqueBlocksTotalWeight
init|=
name|blocksDistribution
operator|.
name|getUniqueBlocksTotalWeight
argument_list|()
decl_stmt|;
name|String
name|tophost
init|=
name|blocksDistribution
operator|.
name|getTopHosts
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|long
name|weight
init|=
name|blocksDistribution
operator|.
name|getWeight
argument_list|(
name|tophost
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|uniqueBlocksTotalWeight
operator|==
name|weight
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|htu
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
block|}
try|try
block|{
comment|// set up a cluster with 4 nodes
name|String
name|hosts
index|[]
init|=
operator|new
name|String
index|[]
block|{
literal|"host1"
block|,
literal|"host2"
block|,
literal|"host3"
block|,
literal|"host4"
block|}
decl_stmt|;
name|cluster
operator|=
name|htu
operator|.
name|startMiniDFSCluster
argument_list|(
name|hosts
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitActive
argument_list|()
expr_stmt|;
name|FileSystem
name|fs
init|=
name|cluster
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
comment|// create a file with one block
name|testFile
operator|=
operator|new
name|Path
argument_list|(
literal|"/test3.txt"
argument_list|)
expr_stmt|;
name|WriteDataToHDFS
argument_list|(
name|fs
argument_list|,
name|testFile
argument_list|,
name|DEFAULT_BLOCK_SIZE
argument_list|)
expr_stmt|;
comment|// given the default replication factor is 3, we will have total of 3
comment|// replica of blocks; thus there is one host without weight
name|FileStatus
name|status
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|testFile
argument_list|)
decl_stmt|;
name|HDFSBlocksDistribution
name|blocksDistribution
init|=
name|FSUtils
operator|.
name|computeHDFSBlocksDistribution
argument_list|(
name|fs
argument_list|,
name|status
argument_list|,
literal|0
argument_list|,
name|status
operator|.
name|getLen
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Wrong number of hosts distributing blocks."
argument_list|,
literal|3
argument_list|,
name|blocksDistribution
operator|.
name|getTopHosts
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|htu
operator|.
name|shutdownMiniDFSCluster
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPermMask
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|ENABLE_DATA_FILE_UMASK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
comment|// first check that we don't crash if we don't have perms set
name|FsPermission
name|defaultPerms
init|=
name|FSUtils
operator|.
name|getFilePermissions
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|HConstants
operator|.
name|DATA_FILE_UMASK_KEY
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|FsPermission
operator|.
name|getDefault
argument_list|()
argument_list|,
name|defaultPerms
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|HConstants
operator|.
name|DATA_FILE_UMASK_KEY
argument_list|,
literal|"077"
argument_list|)
expr_stmt|;
comment|// now check that we get the right perms
name|FsPermission
name|filePerm
init|=
name|FSUtils
operator|.
name|getFilePermissions
argument_list|(
name|fs
argument_list|,
name|conf
argument_list|,
name|HConstants
operator|.
name|DATA_FILE_UMASK_KEY
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|FsPermission
argument_list|(
literal|"700"
argument_list|)
argument_list|,
name|filePerm
argument_list|)
expr_stmt|;
comment|// then that the correct file is created
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
literal|"target"
operator|+
name|File
operator|.
name|separator
operator|+
name|UUID
operator|.
name|randomUUID
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|FSDataOutputStream
name|out
init|=
name|FSUtils
operator|.
name|create
argument_list|(
name|fs
argument_list|,
name|p
argument_list|,
name|filePerm
argument_list|)
decl_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
name|FileStatus
name|stat
init|=
name|fs
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|FsPermission
argument_list|(
literal|"700"
argument_list|)
argument_list|,
name|stat
operator|.
name|getPermission
argument_list|()
argument_list|)
expr_stmt|;
comment|// and then cleanup
block|}
finally|finally
block|{
name|fs
operator|.
name|delete
argument_list|(
name|p
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

