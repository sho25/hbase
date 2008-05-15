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
name|Collection
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
name|hbase
operator|.
name|HBaseClusterTestCase
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
name|LocalHBaseCluster
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
name|Scanner
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
name|RowResult
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

begin_comment
comment|/**  * Tests region server failover when a region server exits both cleanly and  * when it aborts.  */
end_comment

begin_class
specifier|public
class|class
name|TestRegionServerExit
extends|extends
name|HBaseClusterTestCase
block|{
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|HTable
name|table
decl_stmt|;
comment|/** constructor */
specifier|public
name|TestRegionServerExit
parameter_list|()
block|{
name|super
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"ipc.client.connect.max.retries"
argument_list|,
literal|5
argument_list|)
expr_stmt|;
comment|// reduce ipc retries
name|conf
operator|.
name|setInt
argument_list|(
literal|"ipc.client.timeout"
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
comment|// and ipc timeout
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|10000
argument_list|)
expr_stmt|;
comment|// increase client timeout
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.retries.number"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
comment|// increase HBase retries
block|}
comment|/**    * Test abort of region server.    * @throws IOException    */
specifier|public
name|void
name|testAbort
parameter_list|()
throws|throws
name|IOException
block|{
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
comment|// Create table and add a row.
specifier|final
name|String
name|tableName
init|=
name|getName
argument_list|()
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|createTableAndAddRow
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// Start up a new region server to take over serving of root and meta
comment|// after we shut down the current meta/root host.
name|this
operator|.
name|cluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
comment|// Now abort the meta region server and wait for it to go down and come back
name|stopOrAbortMetaRegionServer
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|// Verify that everything is back up.
name|Thread
name|t
init|=
name|startVerificationThread
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|threadDumpingJoin
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test abort of region server.    * @throws IOException    */
specifier|public
name|void
name|testCleanExit
parameter_list|()
throws|throws
name|IOException
block|{
comment|// When the META table can be opened, the region servers are running
operator|new
name|HTable
argument_list|(
name|this
operator|.
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
expr_stmt|;
comment|// Create table and add a row.
specifier|final
name|String
name|tableName
init|=
name|getName
argument_list|()
decl_stmt|;
name|byte
index|[]
name|row
init|=
name|createTableAndAddRow
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// Start up a new region server to take over serving of root and meta
comment|// after we shut down the current meta/root host.
name|this
operator|.
name|cluster
operator|.
name|startRegionServer
argument_list|()
expr_stmt|;
comment|// Now abort the meta region server and wait for it to go down and come back
name|stopOrAbortMetaRegionServer
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// Verify that everything is back up.
name|Thread
name|t
init|=
name|startVerificationThread
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
name|threadDumpingJoin
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
specifier|private
name|byte
index|[]
name|createTableAndAddRow
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
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
comment|// put some values in the table
name|this
operator|.
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
name|BatchUpdate
name|b
init|=
operator|new
name|BatchUpdate
argument_list|(
name|row
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
name|Bytes
operator|.
name|toBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|b
argument_list|)
expr_stmt|;
return|return
name|row
return|;
block|}
comment|/*    * Stop the region server serving the meta region and wait for the meta region    * to get reassigned. This is always the most problematic case.    *     * @param abort set to true if region server should be aborted, if false it    * is just shut down.    */
specifier|private
name|void
name|stopOrAbortMetaRegionServer
parameter_list|(
name|boolean
name|abort
parameter_list|)
block|{
name|List
argument_list|<
name|LocalHBaseCluster
operator|.
name|RegionServerThread
argument_list|>
name|regionThreads
init|=
name|cluster
operator|.
name|getRegionThreads
argument_list|()
decl_stmt|;
name|int
name|server
init|=
operator|-
literal|1
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
name|regionThreads
operator|.
name|size
argument_list|()
operator|&&
name|server
operator|==
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|HRegionServer
name|s
init|=
name|regionThreads
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getRegionServer
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|HRegion
argument_list|>
name|regions
init|=
name|s
operator|.
name|getOnlineRegions
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegion
name|r
range|:
name|regions
control|)
block|{
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|r
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
condition|)
block|{
name|server
operator|=
name|i
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|server
operator|==
operator|-
literal|1
condition|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"could not find region server serving meta region"
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|abort
condition|)
block|{
name|this
operator|.
name|cluster
operator|.
name|abortRegionServer
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|cluster
operator|.
name|stopRegionServer
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
name|this
operator|.
name|cluster
operator|.
name|waitOnRegionServer
argument_list|(
name|server
argument_list|)
operator|+
literal|" has been "
operator|+
operator|(
name|abort
condition|?
literal|"aborted"
else|:
literal|"shut down"
operator|)
argument_list|)
expr_stmt|;
block|}
comment|/*    * Run verification in a thread so I can concurrently run a thread-dumper    * while we're waiting (because in this test sometimes the meta scanner    * looks to be be stuck).    * @param tableName Name of table to find.    * @param row Row we expect to find.    * @return Verification thread.  Caller needs to calls start on it.    */
specifier|private
name|Thread
name|startVerificationThread
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|row
parameter_list|)
block|{
name|Runnable
name|runnable
init|=
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
comment|// Now try to open a scanner on the meta table. Should stall until
comment|// meta server comes back up.
name|HTable
name|t
init|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
name|HConstants
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|Scanner
name|s
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
name|s
operator|.
name|close
argument_list|()
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
name|fatal
argument_list|(
literal|"could not re-open meta table because"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
name|Scanner
name|scanner
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// Verify that the client can find the data after the region has moved
comment|// to a different server
name|scanner
operator|=
name|table
operator|.
name|getScanner
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Obtained scanner "
operator|+
name|scanner
argument_list|)
expr_stmt|;
for|for
control|(
name|RowResult
name|r
range|:
name|scanner
control|)
block|{
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|r
operator|.
name|getRow
argument_list|()
argument_list|,
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|r
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|bytes
init|=
name|r
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|COLUMN_FAMILY
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|tableName
operator|.
name|equals
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|bytes
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Success!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|scanner
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Closing scanner "
operator|+
name|scanner
argument_list|)
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
decl_stmt|;
return|return
operator|new
name|Thread
argument_list|(
name|runnable
argument_list|)
return|;
block|}
block|}
end_class

end_unit

