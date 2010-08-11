begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
operator|.
name|wal
package|;
end_package

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
name|io
operator|.
name|OutputStream
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
name|HTableDescriptor
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
name|HRegion
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
name|HBaseAdmin
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
name|hadoop
operator|.
name|hdfs
operator|.
name|server
operator|.
name|namenode
operator|.
name|FSNamesystem
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
name|namenode
operator|.
name|LeaseManager
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
name|AfterClass
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
comment|/**  * Test log deletion as logs are rolled.  */
end_comment

begin_class
specifier|public
class|class
name|TestLogRolling
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
name|TestLogRolling
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|HRegionServer
name|server
decl_stmt|;
specifier|private
name|HLog
name|log
decl_stmt|;
specifier|private
name|String
name|tableName
decl_stmt|;
specifier|private
name|byte
index|[]
name|value
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|MiniDFSCluster
name|dfsCluster
decl_stmt|;
specifier|private
specifier|static
name|HBaseAdmin
name|admin
decl_stmt|;
specifier|private
specifier|static
name|MiniHBaseCluster
name|cluster
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
comment|// verbose logging on classes that are touched in these tests
block|{
operator|(
operator|(
name|Log4JLogger
operator|)
name|DataNode
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
name|LeaseManager
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
name|FSNamesystem
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
name|HRegionServer
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
name|HRegion
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
name|HLog
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
comment|/**    * constructor    * @throws Exception    */
specifier|public
name|TestLogRolling
parameter_list|()
block|{
comment|// start one regionserver and a minidfs.
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|server
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|log
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|value
operator|=
literal|null
expr_stmt|;
name|String
name|className
init|=
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|StringBuilder
name|v
init|=
operator|new
name|StringBuilder
argument_list|(
name|className
argument_list|)
decl_stmt|;
while|while
condition|(
name|v
operator|.
name|length
argument_list|()
operator|<
literal|1000
condition|)
block|{
name|v
operator|.
name|append
argument_list|(
name|className
argument_list|)
expr_stmt|;
block|}
name|value
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|v
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Need to override this setup so we can edit the config before it gets sent
comment|// to the HDFS& HBase cluster startup.
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
comment|/**** configuration for testLogRolling ****/
comment|// Force a region split after every 768KB
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
literal|"hbase.hregion.max.filesize"
argument_list|,
literal|768L
operator|*
literal|1024L
argument_list|)
expr_stmt|;
comment|// We roll the log after every 32 writes
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.maxlogentries"
argument_list|,
literal|32
argument_list|)
expr_stmt|;
comment|// For less frequently updated regions flush after every 2 flushes
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memstore.optionalflushcount"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// We flush the cache after every 8192 bytes
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memstore.flush.size"
argument_list|,
literal|8192
argument_list|)
expr_stmt|;
comment|// Increase the amount of time between client retries
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setLong
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|15
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// Reduce thread wake frequency so that other threads can get
comment|// a chance to run.
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|THREAD_WAKE_FREQUENCY
argument_list|,
literal|2
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|/**** configuration for testLogRollOnDatanodeDeath ****/
comment|// make sure log.hflush() calls syncFs() to open a pipeline
name|TEST_UTIL
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
comment|// lower the namenode& datanode heartbeat so the namenode
comment|// quickly detects datanode failures
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"heartbeat.recheck.interval"
argument_list|,
literal|5000
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.heartbeat.interval"
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// the namenode might still try to choose the recently-dead datanode
comment|// for a pipeline, so try to a new pipeline multiple times
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"dfs.client.block.write.retries"
argument_list|,
literal|30
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|cluster
operator|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
expr_stmt|;
name|dfsCluster
operator|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
expr_stmt|;
name|fs
operator|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|IOException
block|{
name|TEST_UTIL
operator|.
name|cleanupTestDir
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|startAndWriteData
parameter_list|()
throws|throws
name|IOException
block|{
comment|// When the META table can be opened, the region servers are running
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionServer
argument_list|()
expr_stmt|;
name|this
operator|.
name|log
operator|=
name|server
operator|.
name|getLog
argument_list|()
expr_stmt|;
comment|// Create the test table and open it
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|HTable
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
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
literal|256
condition|;
name|i
operator|++
control|)
block|{
comment|// 256 writes should cause 8 log rolls
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$04d"
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|null
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|%
literal|32
operator|==
literal|0
condition|)
block|{
comment|// After every 32 writes sleep to let the log roller run
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
block|}
block|}
comment|/**    * Tests that logs are deleted    * @throws IOException    * @throws FailedLogCloseException    */
annotation|@
name|Test
specifier|public
name|void
name|testLogRolling
parameter_list|()
throws|throws
name|FailedLogCloseException
throws|,
name|IOException
block|{
name|this
operator|.
name|tableName
operator|=
name|getName
argument_list|()
expr_stmt|;
name|startAndWriteData
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"after writing there are "
operator|+
name|log
operator|.
name|getNumLogFiles
argument_list|()
operator|+
literal|" log files"
argument_list|)
expr_stmt|;
comment|// flush all regions
name|List
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
operator|new
name|ArrayList
argument_list|<
name|HRegion
argument_list|>
argument_list|(
name|server
operator|.
name|getOnlineRegions
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|HRegion
name|r
range|:
name|regions
control|)
block|{
name|r
operator|.
name|flushcache
argument_list|()
expr_stmt|;
block|}
comment|// Now roll the log
name|log
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|int
name|count
init|=
name|log
operator|.
name|getNumLogFiles
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"after flushing all regions and rolling logs there are "
operator|+
name|log
operator|.
name|getNumLogFiles
argument_list|()
operator|+
literal|" log files"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
operator|(
literal|"actual count: "
operator|+
name|count
operator|)
argument_list|,
name|count
operator|<=
literal|2
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"TestLogRolling"
return|;
block|}
name|void
name|writeData
parameter_list|(
name|HTable
name|table
parameter_list|,
name|int
name|rownum
parameter_list|)
throws|throws
name|IOException
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|String
operator|.
name|format
argument_list|(
literal|"%1$04d"
argument_list|,
name|rownum
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|add
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|,
literal|null
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
comment|// sleep to let the log roller run (if it needs to)
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
block|}
comment|/**    * Give me the HDFS pipeline for this log file    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"null"
argument_list|)
name|DatanodeInfo
index|[]
name|getPipeline
parameter_list|(
name|HLog
name|log
parameter_list|)
throws|throws
name|IllegalArgumentException
throws|,
name|IllegalAccessException
throws|,
name|InvocationTargetException
block|{
comment|// kill a datanode in the pipeline to force a log roll on the next sync()
name|OutputStream
name|stm
init|=
name|log
operator|.
name|getOutputStream
argument_list|()
decl_stmt|;
name|Method
name|getPipeline
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Method
name|m
range|:
name|stm
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredMethods
argument_list|()
control|)
block|{
if|if
condition|(
name|m
operator|.
name|getName
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|"getPipeline"
argument_list|)
condition|)
block|{
name|getPipeline
operator|=
name|m
expr_stmt|;
name|getPipeline
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|assertTrue
argument_list|(
literal|"Need DFSOutputStream.getPipeline() for this test"
argument_list|,
literal|null
operator|!=
name|getPipeline
argument_list|)
expr_stmt|;
name|Object
name|repl
init|=
name|getPipeline
operator|.
name|invoke
argument_list|(
name|stm
argument_list|,
operator|new
name|Object
index|[]
block|{}
comment|/* NO_ARGS */
argument_list|)
decl_stmt|;
return|return
operator|(
name|DatanodeInfo
index|[]
operator|)
name|repl
return|;
block|}
comment|/**    * Tests that logs are rolled upon detecting datanode death    * Requires an HDFS jar with HDFS-826& syncFs() support (HDFS-200)    * @throws IOException    * @throws InterruptedException    * @throws InvocationTargetException     * @throws IllegalAccessException    * @throws IllegalArgumentException      */
annotation|@
name|Test
specifier|public
name|void
name|testLogRollOnDatanodeDeath
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|IllegalArgumentException
throws|,
name|IllegalAccessException
throws|,
name|InvocationTargetException
block|{
name|assertTrue
argument_list|(
literal|"This test requires HLog file replication."
argument_list|,
name|fs
operator|.
name|getDefaultReplication
argument_list|()
operator|>
literal|1
argument_list|)
expr_stmt|;
comment|// When the META table can be opened, the region servers are running
operator|new
name|HTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|cluster
operator|.
name|getRegionServer
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|log
operator|=
name|server
operator|.
name|getLog
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Need HDFS-826 for this test"
argument_list|,
name|log
operator|.
name|canGetCurReplicas
argument_list|()
argument_list|)
expr_stmt|;
comment|// don't run this test without append support (HDFS-200& HDFS-142)
name|assertTrue
argument_list|(
literal|"Need append support for this test"
argument_list|,
name|FSUtils
operator|.
name|isAppendSupported
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// add up the datanode count, to ensure proper replication when we kill 1
name|dfsCluster
operator|.
name|startDataNodes
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|dfsCluster
operator|.
name|waitActive
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|dfsCluster
operator|.
name|getDataNodes
argument_list|()
operator|.
name|size
argument_list|()
operator|>=
name|fs
operator|.
name|getDefaultReplication
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
comment|// Create the test table and open it
name|String
name|tableName
init|=
name|getName
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|HTable
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
name|tableName
argument_list|)
decl_stmt|;
name|writeData
argument_list|(
name|table
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|table
operator|.
name|setAutoFlush
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|long
name|curTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|oldFilenum
init|=
name|log
operator|.
name|getFilenum
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Log should have a timestamp older than now"
argument_list|,
name|curTime
operator|>
name|oldFilenum
operator|&&
name|oldFilenum
operator|!=
operator|-
literal|1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"The log shouldn't have rolled yet"
argument_list|,
name|oldFilenum
operator|==
name|log
operator|.
name|getFilenum
argument_list|()
argument_list|)
expr_stmt|;
name|DatanodeInfo
index|[]
name|pipeline
init|=
name|getPipeline
argument_list|(
name|log
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|pipeline
operator|.
name|length
operator|==
name|fs
operator|.
name|getDefaultReplication
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|dfsCluster
operator|.
name|stopDataNode
argument_list|(
name|pipeline
index|[
literal|0
index|]
operator|.
name|getName
argument_list|()
argument_list|)
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
comment|// this write should succeed, but trigger a log roll
name|writeData
argument_list|(
name|table
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|long
name|newFilenum
init|=
name|log
operator|.
name|getFilenum
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Missing datanode should've triggered a log roll"
argument_list|,
name|newFilenum
operator|>
name|oldFilenum
operator|&&
name|newFilenum
operator|>
name|curTime
argument_list|)
expr_stmt|;
comment|// write some more log data (this should use a new hdfs_out)
name|writeData
argument_list|(
name|table
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"The log should not roll again."
argument_list|,
name|log
operator|.
name|getFilenum
argument_list|()
operator|==
name|newFilenum
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"New log file should have the default replication"
argument_list|,
name|log
operator|.
name|getLogReplication
argument_list|()
operator|==
name|fs
operator|.
name|getDefaultReplication
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

