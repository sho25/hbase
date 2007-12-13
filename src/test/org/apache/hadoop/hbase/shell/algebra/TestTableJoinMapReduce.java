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
name|shell
operator|.
name|algebra
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
name|TreeMap
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
name|HScannerInterface
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
name|HStoreKey
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
name|MasterNotRunningException
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
name|MultiRegionTable
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
name|mapred
operator|.
name|TableReduce
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
name|mapred
operator|.
name|JobClient
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
name|mapred
operator|.
name|JobConf
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
name|mapred
operator|.
name|MiniMRCluster
import|;
end_import

begin_comment
comment|/**  * HBase shell join test  */
end_comment

begin_class
specifier|public
class|class
name|TestTableJoinMapReduce
extends|extends
name|MultiRegionTable
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"hiding"
argument_list|)
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
name|TestTableJoinMapReduce
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|static
specifier|final
name|String
name|FIRST_RELATION
init|=
literal|"r1"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SECOND_RELATION
init|=
literal|"r2"
decl_stmt|;
specifier|static
specifier|final
name|String
name|JOIN_EXPRESSION
init|=
literal|"r1.c: = r2.ROW BOOL "
decl_stmt|;
specifier|static
specifier|final
name|String
name|FIRST_COLUMNS
init|=
literal|"a: b: c:"
decl_stmt|;
specifier|static
specifier|final
name|String
name|SECOND_COLUMNS
init|=
literal|"d: e:"
decl_stmt|;
specifier|static
specifier|final
name|String
name|OUTPUT_TABLE
init|=
literal|"result_table"
decl_stmt|;
specifier|private
name|MiniDFSCluster
name|dfsCluster
init|=
literal|null
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Path
name|dir
decl_stmt|;
specifier|private
name|MiniHBaseCluster
name|hCluster
init|=
literal|null
decl_stmt|;
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|dfsCluster
operator|=
operator|new
name|MiniDFSCluster
argument_list|(
name|conf
argument_list|,
literal|1
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
try|try
block|{
name|fs
operator|=
name|dfsCluster
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
name|dir
operator|=
operator|new
name|Path
argument_list|(
literal|"/hbase"
argument_list|)
expr_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|dir
argument_list|)
expr_stmt|;
comment|// Start up HBase cluster
name|hCluster
operator|=
operator|new
name|MiniHBaseCluster
argument_list|(
name|conf
argument_list|,
literal|1
argument_list|,
name|dfsCluster
argument_list|)
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
name|dfsCluster
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
comment|/**    * {@inheritDoc}    */
annotation|@
name|Override
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
if|if
condition|(
name|hCluster
operator|!=
literal|null
condition|)
block|{
name|hCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
name|StaticTestEnvironment
operator|.
name|shutdownDfs
argument_list|(
name|dfsCluster
argument_list|)
expr_stmt|;
block|}
comment|/**    * @throws Exception    */
specifier|public
name|void
name|testTableJoinMapReduce
parameter_list|()
throws|throws
name|Exception
block|{
name|HTable
name|table
init|=
literal|null
decl_stmt|;
try|try
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|FIRST_RELATION
argument_list|)
decl_stmt|;
name|String
index|[]
name|columns
init|=
name|FIRST_COLUMNS
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
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
name|columns
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|columns
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|this
operator|.
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
comment|// insert random data into the input table
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
name|FIRST_RELATION
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|5
condition|;
name|j
operator|++
control|)
block|{
name|long
name|lockid
init|=
name|table
operator|.
name|startUpdate
argument_list|(
operator|new
name|Text
argument_list|(
literal|"rowKey"
operator|+
name|j
argument_list|)
argument_list|)
decl_stmt|;
name|table
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
operator|new
name|Text
argument_list|(
literal|"a:"
argument_list|)
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|j
argument_list|)
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
operator|new
name|Text
argument_list|(
literal|"b:"
argument_list|)
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|j
argument_list|)
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
operator|new
name|Text
argument_list|(
literal|"c:"
argument_list|)
argument_list|,
operator|(
literal|"joinKey-"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|j
argument_list|)
operator|)
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|lockid
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|MasterNotRunningException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
try|try
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|SECOND_RELATION
argument_list|)
decl_stmt|;
name|String
index|[]
name|columns
init|=
name|SECOND_COLUMNS
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
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
name|columns
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|columns
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|this
operator|.
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
comment|// insert random data into the input table
name|table
operator|=
operator|new
name|HTable
argument_list|(
name|conf
argument_list|,
operator|new
name|Text
argument_list|(
name|SECOND_RELATION
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|3
condition|;
name|j
operator|++
control|)
block|{
name|long
name|lockid
init|=
name|table
operator|.
name|startUpdate
argument_list|(
operator|new
name|Text
argument_list|(
literal|"joinKey-"
operator|+
name|j
argument_list|)
argument_list|)
decl_stmt|;
name|table
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
operator|new
name|Text
argument_list|(
literal|"d:"
argument_list|)
argument_list|,
operator|(
literal|"s-"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|j
argument_list|)
operator|)
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|lockid
argument_list|,
operator|new
name|Text
argument_list|(
literal|"e:"
argument_list|)
argument_list|,
operator|(
literal|"s-"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|j
argument_list|)
operator|)
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|commit
argument_list|(
name|lockid
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|MasterNotRunningException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|table
operator|!=
literal|null
condition|)
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
try|try
block|{
name|HTableDescriptor
name|output
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|OUTPUT_TABLE
argument_list|)
decl_stmt|;
name|String
index|[]
name|columns
init|=
operator|(
name|FIRST_COLUMNS
operator|+
literal|" "
operator|+
name|SECOND_COLUMNS
operator|)
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
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
name|columns
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|output
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|columns
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// create output table
name|HBaseAdmin
name|admin
init|=
operator|new
name|HBaseAdmin
argument_list|(
name|this
operator|.
name|conf
argument_list|)
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|output
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MasterNotRunningException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|MiniMRCluster
name|mrCluster
init|=
literal|null
decl_stmt|;
try|try
block|{
name|mrCluster
operator|=
operator|new
name|MiniMRCluster
argument_list|(
literal|2
argument_list|,
name|fs
operator|.
name|getUri
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|JobConf
name|jobConf
init|=
operator|new
name|JobConf
argument_list|(
name|conf
argument_list|,
name|TestTableJoinMapReduce
operator|.
name|class
argument_list|)
decl_stmt|;
name|jobConf
operator|.
name|setJobName
argument_list|(
literal|"process table join mapreduce"
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setNumMapTasks
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|jobConf
operator|.
name|setNumReduceTasks
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|IndexJoinMap
operator|.
name|initJob
argument_list|(
name|FIRST_RELATION
argument_list|,
name|SECOND_RELATION
argument_list|,
name|FIRST_COLUMNS
argument_list|,
name|SECOND_COLUMNS
argument_list|,
name|JOIN_EXPRESSION
argument_list|,
name|IndexJoinMap
operator|.
name|class
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
name|TableReduce
operator|.
name|initJob
argument_list|(
name|OUTPUT_TABLE
argument_list|,
name|IndexJoinReduce
operator|.
name|class
argument_list|,
name|jobConf
argument_list|)
expr_stmt|;
name|JobClient
operator|.
name|runJob
argument_list|(
name|jobConf
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
throw|throw
name|e
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|mrCluster
operator|!=
literal|null
condition|)
block|{
name|mrCluster
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
try|try
block|{
name|verify
argument_list|(
name|OUTPUT_TABLE
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
throw|throw
name|e
throw|;
block|}
block|}
comment|/**    * Check the result table lattice size.    *     * @param conf    * @param outputTable    * @throws IOException    */
specifier|private
name|void
name|verify
parameter_list|(
name|String
name|outputTable
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Sleep before we start the verify to ensure that when the scanner takes
comment|// its snapshot, all the updates have made it into the cache.
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|conf
operator|.
name|getLong
argument_list|(
literal|"hbase.regionserver.optionalcacheflushinterval"
argument_list|,
literal|60L
operator|*
literal|1000L
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
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
name|outputTable
argument_list|)
argument_list|)
decl_stmt|;
name|Text
index|[]
name|columns
init|=
block|{
operator|new
name|Text
argument_list|(
literal|"a:"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"b:"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"c:"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"d:"
argument_list|)
block|,
operator|new
name|Text
argument_list|(
literal|"e:"
argument_list|)
block|}
decl_stmt|;
name|HScannerInterface
name|scanner
init|=
name|table
operator|.
name|obtainScanner
argument_list|(
name|columns
argument_list|,
name|HConstants
operator|.
name|EMPTY_START_ROW
argument_list|)
decl_stmt|;
try|try
block|{
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|results
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"result_table.column.size: "
operator|+
name|results
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5
argument_list|,
name|results
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|i
operator|==
literal|3
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"result_table.row.count: "
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

