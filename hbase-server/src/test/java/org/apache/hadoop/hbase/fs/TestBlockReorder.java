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
name|Field
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
name|commons
operator|.
name|logging
operator|.
name|impl
operator|.
name|Log4JLogger
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
name|MiniHBaseCluster
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
name|regionserver
operator|.
name|HRegionServer
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
name|DFSClient
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
name|ClientProtocol
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
name|DirectoryListing
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
name|HdfsFileStatus
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
name|apache
operator|.
name|log4j
operator|.
name|Level
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
comment|/**  * Tests for the hdfs fix from HBASE-6435.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestBlockReorder
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
name|TestBlockReorder
operator|.
name|class
argument_list|)
decl_stmt|;
static|static
block|{
operator|(
operator|(
name|Log4JLogger
operator|)
name|DFSClient
operator|.
name|LOG
operator|)
operator|.
name|getLogger
argument_list|()
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|ALL
argument_list|)
expr_stmt|;
operator|(
operator|(
name|Log4JLogger
operator|)
name|HFileSystem
operator|.
name|LOG
operator|)
operator|.
name|getLogger
argument_list|()
operator|.
name|setLevel
argument_list|(
name|Level
operator|.
name|ALL
argument_list|)
expr_stmt|;
block|}
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
name|setBoolean
argument_list|(
literal|"dfs.support.append"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|lb
operator|.
name|getLocations
argument_list|()
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
name|lb
operator|.
name|getLocations
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|lb
operator|.
name|getLocations
argument_list|()
index|[
literal|0
index|]
operator|=
name|lb
operator|.
name|getLocations
argument_list|()
index|[
literal|1
index|]
expr_stmt|;
name|lb
operator|.
name|getLocations
argument_list|()
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
comment|/**    * Test that the hook works within HBase, including when there are multiple blocks.    */
annotation|@
name|Test
argument_list|()
specifier|public
name|void
name|testHBaseCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|sb
init|=
literal|"sb"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|htu
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|MiniHBaseCluster
name|hbm
init|=
name|htu
operator|.
name|startMiniHBaseCluster
argument_list|(
literal|1
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|hbm
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
name|HRegionServer
name|targetRs
init|=
name|hbm
operator|.
name|getMaster
argument_list|()
decl_stmt|;
comment|// We want to have a datanode with the same name as the region server, so
comment|//  we're going to get the regionservername, and start a new datanode with this name.
name|String
name|host4
init|=
name|targetRs
operator|.
name|getServerName
argument_list|()
operator|.
name|getHostname
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting a new datanode with the name="
operator|+
name|host4
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|startDataNodes
argument_list|(
name|conf
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"/r4"
block|}
argument_list|,
operator|new
name|String
index|[]
block|{
name|host4
block|}
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitClusterUp
argument_list|()
expr_stmt|;
specifier|final
name|int
name|repCount
init|=
literal|3
decl_stmt|;
comment|// We use the regionserver file system& conf as we expect it to have the hook.
name|conf
operator|=
name|targetRs
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
name|HFileSystem
name|rfs
init|=
operator|(
name|HFileSystem
operator|)
name|targetRs
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|Table
name|h
init|=
name|htu
operator|.
name|createTable
argument_list|(
literal|"table"
operator|.
name|getBytes
argument_list|()
argument_list|,
name|sb
argument_list|)
decl_stmt|;
comment|// Now, we have 4 datanodes and a replication count of 3. So we don't know if the datanode
comment|// with the same node will be used. We can't really stop an existing datanode, this would
comment|// make us fall in nasty hdfs bugs/issues. So we're going to try multiple times.
comment|// Now we need to find the log file, its locations, and look at it
name|String
name|rootDir
init|=
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
operator|+
literal|"/"
operator|+
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
operator|+
literal|"/"
operator|+
name|targetRs
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|toUri
argument_list|()
operator|.
name|getPath
argument_list|()
decl_stmt|;
name|DistributedFileSystem
name|mdfs
init|=
operator|(
name|DistributedFileSystem
operator|)
name|hbm
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|int
name|nbTest
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|nbTest
operator|<
literal|10
condition|)
block|{
name|htu
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|rollHLogWriter
argument_list|(
name|targetRs
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// We need a sleep as the namenode is informed asynchronously
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
comment|// insert one put to ensure a minimal size
name|Put
name|p
init|=
operator|new
name|Put
argument_list|(
name|sb
argument_list|)
decl_stmt|;
name|p
operator|.
name|add
argument_list|(
name|sb
argument_list|,
name|sb
argument_list|,
name|sb
argument_list|)
expr_stmt|;
name|h
operator|.
name|put
argument_list|(
name|p
argument_list|)
expr_stmt|;
name|DirectoryListing
name|dl
init|=
name|dfs
operator|.
name|getClient
argument_list|()
operator|.
name|listPaths
argument_list|(
name|rootDir
argument_list|,
name|HdfsFileStatus
operator|.
name|EMPTY_NAME
argument_list|)
decl_stmt|;
name|HdfsFileStatus
index|[]
name|hfs
init|=
name|dl
operator|.
name|getPartialListing
argument_list|()
decl_stmt|;
comment|// As we wrote a put, we should have at least one log file.
name|Assert
operator|.
name|assertTrue
argument_list|(
name|hfs
operator|.
name|length
operator|>=
literal|1
argument_list|)
expr_stmt|;
for|for
control|(
name|HdfsFileStatus
name|hf
range|:
name|hfs
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Log file found: "
operator|+
name|hf
operator|.
name|getLocalName
argument_list|()
operator|+
literal|" in "
operator|+
name|rootDir
argument_list|)
expr_stmt|;
name|String
name|logFile
init|=
name|rootDir
operator|+
literal|"/"
operator|+
name|hf
operator|.
name|getLocalName
argument_list|()
decl_stmt|;
name|FileStatus
name|fsLog
init|=
name|rfs
operator|.
name|getFileStatus
argument_list|(
operator|new
name|Path
argument_list|(
name|logFile
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Checking log file: "
operator|+
name|logFile
argument_list|)
expr_stmt|;
comment|// Now checking that the hook is up and running
comment|// We can't call directly getBlockLocations, it's not available in HFileSystem
comment|// We're trying multiple times to be sure, as the order is random
name|BlockLocation
index|[]
name|bls
init|=
name|rfs
operator|.
name|getFileBlockLocations
argument_list|(
name|fsLog
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|bls
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|BlockLocation
name|bl
init|=
name|bls
index|[
literal|0
index|]
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|bl
operator|.
name|getHosts
argument_list|()
operator|.
name|length
operator|+
literal|" replicas for block 0 in "
operator|+
name|logFile
operator|+
literal|" "
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
name|bl
operator|.
name|getHosts
argument_list|()
operator|.
name|length
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|bl
operator|.
name|getHosts
argument_list|()
index|[
name|i
index|]
operator|+
literal|"    "
operator|+
name|logFile
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotSame
argument_list|(
name|bl
operator|.
name|getHosts
argument_list|()
index|[
name|i
index|]
argument_list|,
name|host4
argument_list|)
expr_stmt|;
block|}
name|String
name|last
init|=
name|bl
operator|.
name|getHosts
argument_list|()
index|[
name|bl
operator|.
name|getHosts
argument_list|()
operator|.
name|length
operator|-
literal|1
index|]
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|last
operator|+
literal|"    "
operator|+
name|logFile
argument_list|)
expr_stmt|;
if|if
condition|(
name|host4
operator|.
name|equals
argument_list|(
name|last
argument_list|)
condition|)
block|{
name|nbTest
operator|++
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|logFile
operator|+
literal|" is on the new datanode and is ok"
argument_list|)
expr_stmt|;
if|if
condition|(
name|bl
operator|.
name|getHosts
argument_list|()
operator|.
name|length
operator|==
literal|3
condition|)
block|{
comment|// We can test this case from the file system as well
comment|// Checking the underlying file system. Multiple times as the order is random
name|testFromDFS
argument_list|(
name|dfs
argument_list|,
name|logFile
argument_list|,
name|repCount
argument_list|,
name|host4
argument_list|)
expr_stmt|;
comment|// now from the master
name|testFromDFS
argument_list|(
name|mdfs
argument_list|,
name|logFile
argument_list|,
name|repCount
argument_list|,
name|host4
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
specifier|private
name|void
name|testFromDFS
parameter_list|(
name|DistributedFileSystem
name|dfs
parameter_list|,
name|String
name|src
parameter_list|,
name|int
name|repCount
parameter_list|,
name|String
name|localhost
parameter_list|)
throws|throws
name|Exception
block|{
comment|// Multiple times as the order is random
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
name|LocatedBlocks
name|l
decl_stmt|;
comment|// The NN gets the block list asynchronously, so we may need multiple tries to get the list
specifier|final
name|long
name|max
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
literal|10000
decl_stmt|;
name|boolean
name|done
decl_stmt|;
do|do
block|{
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Can't get enouth replica."
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|max
argument_list|)
expr_stmt|;
name|l
operator|=
name|getNamenode
argument_list|(
name|dfs
operator|.
name|getClient
argument_list|()
argument_list|)
operator|.
name|getBlockLocations
argument_list|(
name|src
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"Can't get block locations for "
operator|+
name|src
argument_list|,
name|l
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|l
operator|.
name|getLocatedBlocks
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|l
operator|.
name|getLocatedBlocks
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|done
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|int
name|y
init|=
literal|0
init|;
name|y
operator|<
name|l
operator|.
name|getLocatedBlocks
argument_list|()
operator|.
name|size
argument_list|()
operator|&&
name|done
condition|;
name|y
operator|++
control|)
block|{
name|done
operator|=
operator|(
name|l
operator|.
name|get
argument_list|(
name|y
argument_list|)
operator|.
name|getLocations
argument_list|()
operator|.
name|length
operator|==
name|repCount
operator|)
expr_stmt|;
block|}
block|}
do|while
condition|(
operator|!
name|done
condition|)
do|;
for|for
control|(
name|int
name|y
init|=
literal|0
init|;
name|y
operator|<
name|l
operator|.
name|getLocatedBlocks
argument_list|()
operator|.
name|size
argument_list|()
operator|&&
name|done
condition|;
name|y
operator|++
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
name|localhost
argument_list|,
name|l
operator|.
name|get
argument_list|(
name|y
argument_list|)
operator|.
name|getLocations
argument_list|()
index|[
name|repCount
operator|-
literal|1
index|]
operator|.
name|getHostName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|ClientProtocol
name|getNamenode
parameter_list|(
name|DFSClient
name|dfsc
parameter_list|)
throws|throws
name|Exception
block|{
name|Field
name|nf
init|=
name|DFSClient
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"namenode"
argument_list|)
decl_stmt|;
name|nf
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
operator|(
name|ClientProtocol
operator|)
name|nf
operator|.
name|get
argument_list|(
name|dfsc
argument_list|)
return|;
block|}
comment|/**    * Test that the reorder algo works as we expect.    */
annotation|@
name|Test
specifier|public
name|void
name|testBlockLocation
parameter_list|()
throws|throws
name|Exception
block|{
comment|// We need to start HBase to get  HConstants.HBASE_DIR set in conf
name|htu
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
name|MiniHBaseCluster
name|hbm
init|=
name|htu
operator|.
name|startMiniHBaseCluster
argument_list|(
literal|1
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|conf
operator|=
name|hbm
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
comment|// The "/" is mandatory, without it we've got a null pointer exception on the namenode
specifier|final
name|String
name|fileName
init|=
literal|"/helloWorld"
decl_stmt|;
name|Path
name|p
init|=
operator|new
name|Path
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
specifier|final
name|int
name|repCount
init|=
literal|3
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
operator|>=
name|repCount
argument_list|)
expr_stmt|;
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
comment|// The interceptor is not set in this test, so we get the raw list at this point
name|LocatedBlocks
name|l
decl_stmt|;
specifier|final
name|long
name|max
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
literal|10000
decl_stmt|;
do|do
block|{
name|l
operator|=
name|getNamenode
argument_list|(
name|dfs
operator|.
name|getClient
argument_list|()
argument_list|)
operator|.
name|getBlockLocations
argument_list|(
name|fileName
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|l
operator|.
name|getLocatedBlocks
argument_list|()
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|l
operator|.
name|getLocatedBlocks
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
literal|"Expecting "
operator|+
name|repCount
operator|+
literal|" , got "
operator|+
name|l
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getLocations
argument_list|()
operator|.
name|length
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|<
name|max
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
name|l
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getLocations
argument_list|()
operator|.
name|length
operator|!=
name|repCount
condition|)
do|;
comment|// Should be filtered, the name is different => The order won't change
name|Object
name|originalList
index|[]
init|=
name|l
operator|.
name|getLocatedBlocks
argument_list|()
operator|.
name|toArray
argument_list|()
decl_stmt|;
name|HFileSystem
operator|.
name|ReorderWALBlocks
name|lrb
init|=
operator|new
name|HFileSystem
operator|.
name|ReorderWALBlocks
argument_list|()
decl_stmt|;
name|lrb
operator|.
name|reorderBlocks
argument_list|(
name|conf
argument_list|,
name|l
argument_list|,
name|fileName
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertArrayEquals
argument_list|(
name|originalList
argument_list|,
name|l
operator|.
name|getLocatedBlocks
argument_list|()
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
comment|// Should be reordered, as we pretend to be a file name with a compliant stuff
name|Assert
operator|.
name|assertNotNull
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertFalse
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|pseudoLogFile
init|=
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|)
operator|+
literal|"/"
operator|+
name|HConstants
operator|.
name|HREGION_LOGDIR_NAME
operator|+
literal|"/"
operator|+
name|host1
operator|+
literal|",6977,6576"
operator|+
literal|"/mylogfile"
decl_stmt|;
comment|// Check that it will be possible to extract a ServerName from our construction
name|Assert
operator|.
name|assertNotNull
argument_list|(
literal|"log= "
operator|+
name|pseudoLogFile
argument_list|,
name|HLogUtil
operator|.
name|getServerNameFromHLogDirectoryName
argument_list|(
name|dfs
operator|.
name|getConf
argument_list|()
argument_list|,
name|pseudoLogFile
argument_list|)
argument_list|)
expr_stmt|;
comment|// And check we're doing the right reorder.
name|lrb
operator|.
name|reorderBlocks
argument_list|(
name|conf
argument_list|,
name|l
argument_list|,
name|pseudoLogFile
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|host1
argument_list|,
name|l
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getLocations
argument_list|()
index|[
literal|2
index|]
operator|.
name|getHostName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check again, it should remain the same.
name|lrb
operator|.
name|reorderBlocks
argument_list|(
name|conf
argument_list|,
name|l
argument_list|,
name|pseudoLogFile
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|host1
argument_list|,
name|l
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getLocations
argument_list|()
index|[
literal|2
index|]
operator|.
name|getHostName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

