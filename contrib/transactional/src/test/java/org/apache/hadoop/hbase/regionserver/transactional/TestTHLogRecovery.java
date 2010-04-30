begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|transactional
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
name|Get
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
name|client
operator|.
name|ResultScanner
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
name|Scan
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
name|transactional
operator|.
name|CommitUnsuccessfulException
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
name|transactional
operator|.
name|HBaseBackedTransactionLogger
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
name|transactional
operator|.
name|TransactionManager
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
name|transactional
operator|.
name|TransactionState
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
name|transactional
operator|.
name|TransactionalTable
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
name|ipc
operator|.
name|TransactionalRegionInterface
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
name|JVMClusterUtil
import|;
end_import

begin_class
specifier|public
class|class
name|TestTHLogRecovery
extends|extends
name|HBaseClusterTestCase
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
name|TestTHLogRecovery
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"table1"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|QUAL_A
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW1
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW2
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row2"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW3
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row3"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|TOTAL_VALUE
init|=
literal|10
decl_stmt|;
specifier|private
name|HBaseAdmin
name|admin
decl_stmt|;
specifier|private
name|TransactionManager
name|transactionManager
decl_stmt|;
specifier|private
name|TransactionalTable
name|table
decl_stmt|;
comment|/** constructor */
specifier|public
name|TestTHLogRecovery
parameter_list|()
block|{
name|super
argument_list|(
literal|2
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_CLASS
argument_list|,
name|TransactionalRegionInterface
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_IMPL
argument_list|,
name|TransactionalRegionServer
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Set flush params so we don't get any
comment|// FIXME (defaults are probably fine)
comment|// Copied from TestRegionServerExit
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
annotation|@
name|Override
specifier|protected
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|FileSystem
name|lfs
init|=
name|FileSystem
operator|.
name|getLocal
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Path
name|p
init|=
operator|new
name|Path
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
decl_stmt|;
if|if
condition|(
name|lfs
operator|.
name|exists
argument_list|(
name|p
argument_list|)
condition|)
name|lfs
operator|.
name|delete
argument_list|(
name|p
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
expr_stmt|;
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|table
operator|=
operator|new
name|TransactionalTable
argument_list|(
name|conf
argument_list|,
name|desc
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|HBaseBackedTransactionLogger
operator|.
name|createTable
argument_list|()
expr_stmt|;
name|transactionManager
operator|=
operator|new
name|TransactionManager
argument_list|(
operator|new
name|HBaseBackedTransactionLogger
argument_list|()
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|writeInitalRows
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|writeInitalRows
parameter_list|()
throws|throws
name|IOException
block|{
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|ROW1
argument_list|)
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|TOTAL_VALUE
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|ROW2
argument_list|)
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
operator|new
name|Put
argument_list|(
name|ROW3
argument_list|)
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testWithoutFlush
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommitUnsuccessfulException
block|{
name|writeInitalRows
argument_list|()
expr_stmt|;
name|TransactionState
name|state1
init|=
name|makeTransaction
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|transactionManager
operator|.
name|tryCommit
argument_list|(
name|state1
argument_list|)
expr_stmt|;
name|stopOrAbortRegionServer
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Thread
name|t
init|=
name|startVerificationThread
argument_list|(
literal|1
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
specifier|public
name|void
name|testWithFlushBeforeCommit
parameter_list|()
throws|throws
name|IOException
throws|,
name|CommitUnsuccessfulException
block|{
name|writeInitalRows
argument_list|()
expr_stmt|;
name|TransactionState
name|state1
init|=
name|makeTransaction
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|flushRegionServer
argument_list|()
expr_stmt|;
name|transactionManager
operator|.
name|tryCommit
argument_list|(
name|state1
argument_list|)
expr_stmt|;
name|stopOrAbortRegionServer
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Thread
name|t
init|=
name|startVerificationThread
argument_list|(
literal|1
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
comment|// FIXME, TODO
comment|// public void testWithFlushBetweenTransactionWrites() {
comment|// fail();
comment|// }
specifier|private
name|void
name|flushRegionServer
parameter_list|()
block|{
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
name|regionThreads
init|=
name|cluster
operator|.
name|getRegionServerThreads
argument_list|()
decl_stmt|;
name|HRegion
name|region
init|=
literal|null
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
name|Bytes
operator|.
name|toBytes
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|)
condition|)
block|{
name|server
operator|=
name|i
expr_stmt|;
name|region
operator|=
name|r
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
literal|"could not find region server serving table region"
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
operator|(
operator|(
name|TransactionalRegionServer
operator|)
name|regionThreads
operator|.
name|get
argument_list|(
name|server
argument_list|)
operator|.
name|getRegionServer
argument_list|()
operator|)
operator|.
name|getFlushRequester
argument_list|()
operator|.
name|request
argument_list|(
name|region
argument_list|)
expr_stmt|;
block|}
comment|/**    * Stop the region server serving TABLE_NAME.    *     * @param abort set to true if region server should be aborted, if false it is    * just shut down.    */
specifier|private
name|void
name|stopOrAbortRegionServer
parameter_list|(
specifier|final
name|boolean
name|abort
parameter_list|)
block|{
name|List
argument_list|<
name|JVMClusterUtil
operator|.
name|RegionServerThread
argument_list|>
name|regionThreads
init|=
name|cluster
operator|.
name|getRegionServerThreads
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
name|LOG
operator|.
name|info
argument_list|(
literal|"server: "
operator|+
name|regionThreads
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|HRegion
name|r
range|:
name|regions
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"region: "
operator|+
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
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
name|Bytes
operator|.
name|toBytes
argument_list|(
name|TABLE_NAME
argument_list|)
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
literal|"could not find region server serving table region"
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
argument_list|,
literal|false
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
specifier|private
name|void
name|verify
parameter_list|(
specifier|final
name|int
name|numRuns
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Reads
name|int
name|row1
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW1
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|)
argument_list|)
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|row2
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW2
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|)
argument_list|)
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|row3
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|table
operator|.
name|get
argument_list|(
operator|new
name|Get
argument_list|(
name|ROW3
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|)
argument_list|)
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|TOTAL_VALUE
operator|-
literal|2
operator|*
name|numRuns
argument_list|,
name|row1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|numRuns
argument_list|,
name|row2
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|numRuns
argument_list|,
name|row3
argument_list|)
expr_stmt|;
block|}
comment|// Move 2 out of ROW1 and 1 into ROW2 and 1 into ROW3
specifier|private
name|TransactionState
name|makeTransaction
parameter_list|(
specifier|final
name|boolean
name|flushMidWay
parameter_list|)
throws|throws
name|IOException
block|{
name|TransactionState
name|transactionState
init|=
name|transactionManager
operator|.
name|beginTransaction
argument_list|()
decl_stmt|;
comment|// Reads
name|int
name|row1
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|table
operator|.
name|get
argument_list|(
name|transactionState
argument_list|,
operator|new
name|Get
argument_list|(
name|ROW1
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|)
argument_list|)
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|row2
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|table
operator|.
name|get
argument_list|(
name|transactionState
argument_list|,
operator|new
name|Get
argument_list|(
name|ROW2
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|)
argument_list|)
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|row3
init|=
name|Bytes
operator|.
name|toInt
argument_list|(
name|table
operator|.
name|get
argument_list|(
name|transactionState
argument_list|,
operator|new
name|Get
argument_list|(
name|ROW3
argument_list|)
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|)
argument_list|)
operator|.
name|getValue
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|)
argument_list|)
decl_stmt|;
name|row1
operator|-=
literal|2
expr_stmt|;
name|row2
operator|+=
literal|1
expr_stmt|;
name|row3
operator|+=
literal|1
expr_stmt|;
if|if
condition|(
name|flushMidWay
condition|)
block|{
name|flushRegionServer
argument_list|()
expr_stmt|;
block|}
comment|// Writes
name|Put
name|write
init|=
operator|new
name|Put
argument_list|(
name|ROW1
argument_list|)
decl_stmt|;
name|write
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row1
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|transactionState
argument_list|,
name|write
argument_list|)
expr_stmt|;
name|write
operator|=
operator|new
name|Put
argument_list|(
name|ROW2
argument_list|)
expr_stmt|;
name|write
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row2
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|transactionState
argument_list|,
name|write
argument_list|)
expr_stmt|;
name|write
operator|=
operator|new
name|Put
argument_list|(
name|ROW3
argument_list|)
expr_stmt|;
name|write
operator|.
name|add
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row3
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|transactionState
argument_list|,
name|write
argument_list|)
expr_stmt|;
return|return
name|transactionState
return|;
block|}
comment|/*    * Run verification in a thread so I can concurrently run a thread-dumper    * while we're waiting (because in this test sometimes the meta scanner looks    * to be be stuck). @param tableName Name of table to find. @param row Row we    * expect to find. @return Verification thread. Caller needs to calls start on    * it.    */
specifier|private
name|Thread
name|startVerificationThread
parameter_list|(
specifier|final
name|int
name|numRuns
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
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|Scan
name|s
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|s
operator|.
name|addColumn
argument_list|(
name|FAMILY
argument_list|,
name|QUAL_A
argument_list|)
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|t
operator|.
name|getScanner
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|scanner
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
try|try
block|{
name|verify
argument_list|(
name|numRuns
argument_list|)
expr_stmt|;
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

