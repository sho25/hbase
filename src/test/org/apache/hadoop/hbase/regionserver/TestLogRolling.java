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
package|;
end_package

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
name|hadoop
operator|.
name|dfs
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
name|io
operator|.
name|Text
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
name|HBaseTestCase
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
name|StaticTestEnvironment
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
name|io
operator|.
name|BatchUpdate
import|;
end_import

begin_comment
comment|/**  * Test log deletion as logs are rolled.  */
end_comment

begin_class
specifier|public
class|class
name|TestLogRolling
extends|extends
name|HBaseTestCase
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
name|MiniDFSCluster
name|dfs
decl_stmt|;
specifier|private
name|MiniHBaseCluster
name|cluster
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
comment|/**    * constructor    * @throws Exception    */
specifier|public
name|TestLogRolling
parameter_list|()
throws|throws
name|Exception
block|{
name|super
argument_list|()
expr_stmt|;
try|try
block|{
name|this
operator|.
name|dfs
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|cluster
operator|=
literal|null
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
comment|// Force a region split after every 768KB
name|conf
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
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.maxlogentries"
argument_list|,
literal|32
argument_list|)
expr_stmt|;
comment|// For less frequently updated regions flush after every 2 flushes
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memcache.optionalflushcount"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
comment|// We flush the cache after every 8192 bytes
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memcache.flush.size"
argument_list|,
literal|8192
argument_list|)
expr_stmt|;
comment|// Make lease timeout longer, lease checks less frequent
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.lease.period"
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.master.lease.thread.wakefrequency"
argument_list|,
literal|5
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// Increase the amount of time between client retries
name|conf
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
name|conf
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
name|v
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"error in constructor"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|dfs
operator|=
operator|new
name|MiniDFSCluster
argument_list|(
name|conf
argument_list|,
literal|2
argument_list|,
literal|true
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
comment|// Set the hbase.rootdir to be the home directory in mini dfs.
name|this
operator|.
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|,
name|this
operator|.
name|dfs
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getHomeDirectory
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|StaticTestEnvironment
operator|.
name|shutdownDfs
argument_list|(
name|dfs
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|fatal
argument_list|(
literal|"error during setUp: "
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
if|if
condition|(
name|cluster
operator|!=
literal|null
condition|)
block|{
comment|// shutdown mini HBase cluster
name|cluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|StaticTestEnvironment
operator|.
name|shutdownDfs
argument_list|(
name|dfs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"error in tearDown"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
specifier|private
name|void
name|startAndWriteData
parameter_list|()
throws|throws
name|Exception
block|{
name|cluster
operator|=
operator|new
name|MiniHBaseCluster
argument_list|(
name|conf
argument_list|,
literal|1
argument_list|,
name|dfs
argument_list|,
literal|true
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// Wait for region server to start
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// continue
block|}
name|this
operator|.
name|server
operator|=
name|cluster
operator|.
name|getRegionThreads
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
comment|// When the META table can be opened, the region servers are running
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
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
name|COLUMN_FAMILY
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
decl_stmt|;
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
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
name|tableName
argument_list|)
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
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
operator|new
name|Text
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
name|b
operator|.
name|put
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|b
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
comment|/**    * Tests that logs are deleted    *     * @throws Exception    */
specifier|public
name|void
name|testLogRolling
parameter_list|()
throws|throws
name|Exception
block|{
name|tableName
operator|=
name|getName
argument_list|()
expr_stmt|;
try|try
block|{
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
operator|.
name|values
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
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"unexpected exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
end_class

end_unit

