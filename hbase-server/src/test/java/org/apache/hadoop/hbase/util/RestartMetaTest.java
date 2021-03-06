begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|client
operator|.
name|Connection
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
name|ConnectionFactory
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
name|Result
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
name|io
operator|.
name|compress
operator|.
name|Compression
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
name|encoding
operator|.
name|DataBlockEncoding
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
name|test
operator|.
name|LoadTestDataGenerator
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
import|;
end_import

begin_comment
comment|/**  * A command-line tool that spins up a local process-based cluster, loads  * some data, restarts the regionserver holding hbase:meta, and verifies that the  * cluster recovers.  */
end_comment

begin_class
specifier|public
class|class
name|RestartMetaTest
extends|extends
name|AbstractHBaseTool
block|{
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
name|RestartMetaTest
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** The number of region servers used if not specified */
specifier|private
specifier|static
specifier|final
name|int
name|DEFAULT_NUM_RS
init|=
literal|2
decl_stmt|;
comment|/** Table name for the test */
specifier|private
specifier|static
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"load_test"
argument_list|)
decl_stmt|;
comment|/** The number of seconds to sleep after loading the data */
specifier|private
specifier|static
specifier|final
name|int
name|SLEEP_SEC_AFTER_DATA_LOAD
init|=
literal|5
decl_stmt|;
comment|/** The actual number of region servers */
specifier|private
name|int
name|numRegionServers
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|OPT_NUM_RS
init|=
literal|"num_rs"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|NUM_DATANODES
init|=
literal|3
decl_stmt|;
comment|/** Loads data into the table using the multi-threaded writer. */
specifier|private
name|void
name|loadData
parameter_list|()
throws|throws
name|IOException
block|{
name|long
name|startKey
init|=
literal|0
decl_stmt|;
name|long
name|endKey
init|=
literal|100000
decl_stmt|;
name|int
name|minColsPerKey
init|=
literal|5
decl_stmt|;
name|int
name|maxColsPerKey
init|=
literal|15
decl_stmt|;
name|int
name|minColDataSize
init|=
literal|256
decl_stmt|;
name|int
name|maxColDataSize
init|=
literal|256
operator|*
literal|3
decl_stmt|;
name|int
name|numThreads
init|=
literal|10
decl_stmt|;
comment|// print out the arguments
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"Key range %d .. %d\n"
argument_list|,
name|startKey
argument_list|,
name|endKey
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"Number of Columns/Key: %d..%d\n"
argument_list|,
name|minColsPerKey
argument_list|,
name|maxColsPerKey
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"Data Size/Column: %d..%d bytes\n"
argument_list|,
name|minColDataSize
argument_list|,
name|maxColDataSize
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"Client Threads: %d\n"
argument_list|,
name|numThreads
argument_list|)
expr_stmt|;
comment|// start the writers
name|LoadTestDataGenerator
name|dataGen
init|=
operator|new
name|MultiThreadedAction
operator|.
name|DefaultDataGenerator
argument_list|(
name|minColDataSize
argument_list|,
name|maxColDataSize
argument_list|,
name|minColsPerKey
argument_list|,
name|maxColsPerKey
argument_list|,
name|HFileTestUtil
operator|.
name|DEFAULT_COLUMN_FAMILY
argument_list|)
decl_stmt|;
name|MultiThreadedWriter
name|writer
init|=
operator|new
name|MultiThreadedWriter
argument_list|(
name|dataGen
argument_list|,
name|conf
argument_list|,
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|writer
operator|.
name|setMultiPut
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|writer
operator|.
name|start
argument_list|(
name|startKey
argument_list|,
name|endKey
argument_list|,
name|numThreads
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"Started loading data..."
argument_list|)
expr_stmt|;
name|writer
operator|.
name|waitForFinish
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
literal|"Finished loading data..."
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|doWork
parameter_list|()
throws|throws
name|Exception
block|{
name|ProcessBasedLocalHBaseCluster
name|hbaseCluster
init|=
operator|new
name|ProcessBasedLocalHBaseCluster
argument_list|(
name|conf
argument_list|,
name|NUM_DATANODES
argument_list|,
name|numRegionServers
argument_list|)
decl_stmt|;
name|hbaseCluster
operator|.
name|startMiniDFS
argument_list|()
expr_stmt|;
comment|// start the process based HBase cluster
name|hbaseCluster
operator|.
name|startHBase
argument_list|()
expr_stmt|;
comment|// create tables if needed
name|HBaseTestingUtility
operator|.
name|createPreSplitLoadTestTable
argument_list|(
name|conf
argument_list|,
name|TABLE_NAME
argument_list|,
name|HFileTestUtil
operator|.
name|DEFAULT_COLUMN_FAMILY
argument_list|,
name|Compression
operator|.
name|Algorithm
operator|.
name|NONE
argument_list|,
name|DataBlockEncoding
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Loading data....\n\n"
argument_list|)
expr_stmt|;
name|loadData
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Sleeping for "
operator|+
name|SLEEP_SEC_AFTER_DATA_LOAD
operator|+
literal|" seconds....\n\n"
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|5
operator|*
name|SLEEP_SEC_AFTER_DATA_LOAD
argument_list|)
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|int
name|metaRSPort
init|=
name|HBaseTestingUtility
operator|.
name|getMetaRSPort
argument_list|(
name|connection
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Killing hbase:meta region server running on port "
operator|+
name|metaRSPort
argument_list|)
expr_stmt|;
name|hbaseCluster
operator|.
name|killRegionServer
argument_list|(
name|metaRSPort
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Restarting region server running on port metaRSPort"
argument_list|)
expr_stmt|;
name|hbaseCluster
operator|.
name|startRegionServer
argument_list|(
name|metaRSPort
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
literal|2000
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Trying to scan meta"
argument_list|)
expr_stmt|;
name|Table
name|metaTable
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
decl_stmt|;
name|ResultScanner
name|scanner
init|=
name|metaTable
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
decl_stmt|;
name|Result
name|result
decl_stmt|;
while|while
condition|(
operator|(
name|result
operator|=
name|scanner
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Region assignment from META: "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|result
operator|.
name|getRow
argument_list|()
argument_list|)
operator|+
literal|" => "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|result
operator|.
name|getFamilyMap
argument_list|(
name|HConstants
operator|.
name|CATALOG_FAMILY
argument_list|)
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|SERVER_QUALIFIER
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|metaTable
operator|.
name|close
argument_list|()
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|addOptWithArg
argument_list|(
name|OPT_NUM_RS
argument_list|,
literal|"Number of Region Servers"
argument_list|)
expr_stmt|;
name|addOptWithArg
argument_list|(
name|HFileTestUtil
operator|.
name|OPT_DATA_BLOCK_ENCODING
argument_list|,
name|HFileTestUtil
operator|.
name|OPT_DATA_BLOCK_ENCODING_USAGE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|processOptions
parameter_list|(
name|CommandLine
name|cmd
parameter_list|)
block|{
name|numRegionServers
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|getOptionValue
argument_list|(
name|OPT_NUM_RS
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|DEFAULT_NUM_RS
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
operator|new
name|RestartMetaTest
argument_list|()
operator|.
name|doStaticMain
argument_list|(
name|args
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

