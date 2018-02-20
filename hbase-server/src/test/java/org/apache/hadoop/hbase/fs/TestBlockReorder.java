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
name|fs
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|BindException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|ServerSocket
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
name|BlockLocation
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
name|testclassification
operator|.
name|MiscTests
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
name|hdfs
operator|.
name|protocol
operator|.
name|DatanodeInfo
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
name|protocol
operator|.
name|LocatedBlock
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
name|protocol
operator|.
name|LocatedBlocks
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
name|server
operator|.
name|datanode
operator|.
name|DataNode
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
name|Assert
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|Rule
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
name|junit
operator|.
name|rules
operator|.
name|TestName
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

begin_comment
comment|/**  * Tests for the hdfs fix from HBASE-6435.  *  * Please don't add new subtest which involves starting / stopping MiniDFSCluster in this class.  * When stopping MiniDFSCluster, shutdown hooks would be cleared in hadoop's ShutdownHookManager  *   in hadoop 3.  * This leads to 'Failed suppression of fs shutdown hook' error in region server.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestBlockReorder
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
name|TestBlockReorder
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
name|TestBlockReorder
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|MiniDFSCluster
name|cluster
decl_stmt|;
specifier|private
name|HBaseTestingUtility
name|htu
decl_stmt|;
specifier|private
name|DistributedFileSystem
name|dfs
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|host1
init|=
literal|"host1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|host2
init|=
literal|"host2"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|host3
init|=
literal|"host3"
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|htu
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|htu
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.blocksize"
argument_list|,
literal|1024
argument_list|)
expr_stmt|;
comment|// For the test with multiple blocks
name|htu
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.replication"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|htu
operator|.
name|startMiniDFSCluster
argument_list|(
literal|3
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"/r1"
block|,
literal|"/r2"
block|,
literal|"/r3"
block|}
argument_list|,
operator|new
name|String
index|[]
block|{
name|host1
block|,
name|host2
block|,
name|host3
block|}
argument_list|)
expr_stmt|;
name|conf
operator|=
name|htu
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|cluster
operator|=
name|htu
operator|.
name|getDFSCluster
argument_list|()
expr_stmt|;
name|dfs
operator|=
operator|(
name|DistributedFileSystem
operator|)
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|htu
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Test that we're can add a hook, and that this hook works when we try to read the file in HDFS.    */
annotation|@
name|Test
specifier|public
name|void
name|testBlockLocationReorder
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
literal|"hello"
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|(
name|short
operator|)
name|cluster
operator|.
name|getDataNodes
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|1
argument_list|)
expr_stmt|;
specifier|final
name|int
name|repCount
init|=
literal|2
decl_stmt|;
comment|// Let's write the file
name|FSDataOutputStream
name|fop
init|=
name|dfs
operator|.
name|create
argument_list|(
name|p
argument_list|,
operator|(
name|short
operator|)
name|repCount
argument_list|)
decl_stmt|;
specifier|final
name|double
name|toWrite
init|=
literal|875.5613
decl_stmt|;
name|fop
operator|.
name|writeDouble
argument_list|(
name|toWrite
argument_list|)
expr_stmt|;
name|fop
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// Let's check we can read it when everybody's there
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|FSDataInputStream
name|fin
init|=
name|dfs
operator|.
name|open
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|toWrite
operator|==
name|fin
operator|.
name|readDouble
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|end
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"readtime= "
operator|+
operator|(
name|end
operator|-
name|start
operator|)
argument_list|)
expr_stmt|;
name|fin
operator|.
name|close
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
operator|(
name|end
operator|-
name|start
operator|)
operator|<
literal|30
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// Let's kill the first location. But actually the fist location returned will change
comment|// The first thing to do is to get the location, then the port
name|FileStatus
name|f
init|=
name|dfs
operator|.
name|getFileStatus
argument_list|(
name|p
argument_list|)
decl_stmt|;
name|BlockLocation
index|[]
name|lbs
decl_stmt|;
do|do
block|{
name|lbs
operator|=
name|dfs
operator|.
name|getFileBlockLocations
argument_list|(
name|f
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|lbs
operator|.
name|length
operator|!=
literal|1
operator|&&
name|lbs
index|[
literal|0
index|]
operator|.
name|getLength
argument_list|()
operator|!=
name|repCount
condition|)
do|;
specifier|final
name|String
name|name
init|=
name|lbs
index|[
literal|0
index|]
operator|.
name|getNames
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|name
operator|.
name|indexOf
argument_list|(
literal|':'
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|String
name|portS
init|=
name|name
operator|.
name|substring
argument_list|(
name|name
operator|.
name|indexOf
argument_list|(
literal|':'
argument_list|)
operator|+
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|int
name|port
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|portS
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"port= "
operator|+
name|port
argument_list|)
expr_stmt|;
name|int
name|ipcPort
init|=
operator|-
literal|1
decl_stmt|;
comment|// Let's find the DN to kill. cluster.getDataNodes(int) is not on the same port, so we need
comment|// to iterate ourselves.
name|boolean
name|ok
init|=
literal|false
decl_stmt|;
specifier|final
name|String
name|lookup
init|=
name|lbs
index|[
literal|0
index|]
operator|.
name|getHosts
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|DataNode
name|dn
range|:
name|cluster
operator|.
name|getDataNodes
argument_list|()
control|)
block|{
specifier|final
name|String
name|dnName
init|=
name|getHostName
argument_list|(
name|dn
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|dnName
argument_list|)
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
if|if
condition|(
name|lookup
operator|.
name|equals
argument_list|(
name|dnName
argument_list|)
condition|)
block|{
name|ok
operator|=
literal|true
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"killing datanode "
operator|+
name|name
operator|+
literal|" / "
operator|+
name|lookup
argument_list|)
expr_stmt|;
name|ipcPort
operator|=
name|dn
operator|.
name|ipcServer
operator|.
name|getListenerAddress
argument_list|()
operator|.
name|getPort
argument_list|()
expr_stmt|;
name|dn
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"killed datanode "
operator|+
name|name
operator|+
literal|" / "
operator|+
name|lookup
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"didn't find the server to kill, was looking for "
operator|+
name|lookup
operator|+
literal|" found "
operator|+
name|sb
argument_list|,
name|ok
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"ipc port= "
operator|+
name|ipcPort
argument_list|)
expr_stmt|;
comment|// Add the hook, with an implementation checking that we don't use the port we've just killed.
name|Assert
operator|.
name|assertTrue
argument_list|(
name|HFileSystem
operator|.
name|addLocationsOrderInterceptor
argument_list|(
name|conf
argument_list|,
operator|new
name|HFileSystem
operator|.
name|ReorderBlocks
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|reorderBlocks
parameter_list|(
name|Configuration
name|c
parameter_list|,
name|LocatedBlocks
name|lbs
parameter_list|,
name|String
name|src
parameter_list|)
block|{
for|for
control|(
name|LocatedBlock
name|lb
range|:
name|lbs
operator|.
name|getLocatedBlocks
argument_list|()
control|)
block|{
if|if
condition|(
name|lb
operator|.
name|getLocations
argument_list|()
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|DatanodeInfo
index|[]
name|infos
init|=
name|lb
operator|.
name|getLocations
argument_list|()
decl_stmt|;
if|if
condition|(
name|infos
index|[
literal|0
index|]
operator|.
name|getHostName
argument_list|()
operator|.
name|equals
argument_list|(
name|lookup
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"HFileSystem bad host, inverting"
argument_list|)
expr_stmt|;
name|DatanodeInfo
name|tmp
init|=
name|infos
index|[
literal|0
index|]
decl_stmt|;
name|infos
index|[
literal|0
index|]
operator|=
name|infos
index|[
literal|1
index|]
expr_stmt|;
name|infos
index|[
literal|1
index|]
operator|=
name|tmp
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|int
name|retries
init|=
literal|10
decl_stmt|;
name|ServerSocket
name|ss
init|=
literal|null
decl_stmt|;
name|ServerSocket
name|ssI
decl_stmt|;
try|try
block|{
name|ss
operator|=
operator|new
name|ServerSocket
argument_list|(
name|port
argument_list|)
expr_stmt|;
comment|// We're taking the port to have a timeout issue later.
name|ssI
operator|=
operator|new
name|ServerSocket
argument_list|(
name|ipcPort
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BindException
name|be
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Got bind exception trying to set up socket on "
operator|+
name|port
operator|+
literal|" or "
operator|+
name|ipcPort
operator|+
literal|", this means that the datanode has not closed the socket or"
operator|+
literal|" someone else took it. It may happen, skipping this test for this time."
argument_list|,
name|be
argument_list|)
expr_stmt|;
if|if
condition|(
name|ss
operator|!=
literal|null
condition|)
block|{
name|ss
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return;
block|}
comment|// Now it will fail with a timeout, unfortunately it does not always connect to the same box,
comment|// so we try retries times;  with the reorder it will never last more than a few milli seconds
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|retries
condition|;
name|i
operator|++
control|)
block|{
name|start
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|fin
operator|=
name|dfs
operator|.
name|open
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|toWrite
operator|==
name|fin
operator|.
name|readDouble
argument_list|()
argument_list|)
expr_stmt|;
name|fin
operator|.
name|close
argument_list|()
expr_stmt|;
name|end
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HFileSystem readtime= "
operator|+
operator|(
name|end
operator|-
name|start
operator|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
literal|"We took too much time to read"
argument_list|,
operator|(
name|end
operator|-
name|start
operator|)
operator|>
literal|60000
argument_list|)
expr_stmt|;
block|}
name|ss
operator|.
name|close
argument_list|()
expr_stmt|;
name|ssI
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Allow to get the hostname, using getHostName (hadoop 1) or getDisplayName (hadoop 2)    */
specifier|private
name|String
name|getHostName
parameter_list|(
name|DataNode
name|dn
parameter_list|)
throws|throws
name|InvocationTargetException
throws|,
name|IllegalAccessException
block|{
name|Method
name|m
decl_stmt|;
try|try
block|{
name|m
operator|=
name|DataNode
operator|.
name|class
operator|.
name|getMethod
argument_list|(
literal|"getDisplayName"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
try|try
block|{
name|m
operator|=
name|DataNode
operator|.
name|class
operator|.
name|getMethod
argument_list|(
literal|"getHostName"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e1
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e1
argument_list|)
throw|;
block|}
block|}
name|String
name|res
init|=
operator|(
name|String
operator|)
name|m
operator|.
name|invoke
argument_list|(
name|dn
argument_list|)
decl_stmt|;
if|if
condition|(
name|res
operator|.
name|contains
argument_list|(
literal|":"
argument_list|)
condition|)
block|{
return|return
name|res
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
index|[
literal|0
index|]
return|;
block|}
else|else
block|{
return|return
name|res
return|;
block|}
block|}
block|}
end_class

end_unit

