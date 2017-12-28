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
name|concurrent
operator|.
name|TimeUnit
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
name|cli
operator|.
name|CommandLine
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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
name|client
operator|.
name|TableSnapshotScanner
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
name|metrics
operator|.
name|ScanMetrics
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
name|ImmutableBytesWritable
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
name|mapreduce
operator|.
name|TableMapReduceUtil
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
name|mapreduce
operator|.
name|TableMapper
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
name|AbstractHBaseTool
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
name|io
operator|.
name|NullWritable
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
name|mapreduce
operator|.
name|Counters
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
name|mapreduce
operator|.
name|Job
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
name|mapreduce
operator|.
name|lib
operator|.
name|output
operator|.
name|NullOutputFormat
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
name|util
operator|.
name|StringUtils
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
name|util
operator|.
name|ToolRunner
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Stopwatch
import|;
end_import

begin_comment
comment|/**  * A simple performance evaluation tool for single client and MR scans  * and snapshot scans.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|TOOLS
argument_list|)
specifier|public
class|class
name|ScanPerformanceEvaluation
extends|extends
name|AbstractHBaseTool
block|{
specifier|private
specifier|static
specifier|final
name|String
name|HBASE_COUNTER_GROUP_NAME
init|=
literal|"HBase Counters"
decl_stmt|;
specifier|private
name|String
name|type
decl_stmt|;
specifier|private
name|String
name|file
decl_stmt|;
specifier|private
name|String
name|tablename
decl_stmt|;
specifier|private
name|String
name|snapshotName
decl_stmt|;
specifier|private
name|String
name|restoreDir
decl_stmt|;
specifier|private
name|String
name|caching
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
operator|.
name|setConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Path
name|rootDir
decl_stmt|;
try|try
block|{
name|rootDir
operator|=
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|rootDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|addOptions
parameter_list|()
block|{
name|this
operator|.
name|addRequiredOptWithArg
argument_list|(
literal|"t"
argument_list|,
literal|"type"
argument_list|,
literal|"the type of the test. One of the following: streaming|scan|snapshotscan|scanmapreduce|snapshotscanmapreduce"
argument_list|)
expr_stmt|;
name|this
operator|.
name|addOptWithArg
argument_list|(
literal|"f"
argument_list|,
literal|"file"
argument_list|,
literal|"the filename to read from"
argument_list|)
expr_stmt|;
name|this
operator|.
name|addOptWithArg
argument_list|(
literal|"tn"
argument_list|,
literal|"table"
argument_list|,
literal|"the tablename to read from"
argument_list|)
expr_stmt|;
name|this
operator|.
name|addOptWithArg
argument_list|(
literal|"sn"
argument_list|,
literal|"snapshot"
argument_list|,
literal|"the snapshot name to read from"
argument_list|)
expr_stmt|;
name|this
operator|.
name|addOptWithArg
argument_list|(
literal|"rs"
argument_list|,
literal|"restoredir"
argument_list|,
literal|"the directory to restore the snapshot"
argument_list|)
expr_stmt|;
name|this
operator|.
name|addOptWithArg
argument_list|(
literal|"ch"
argument_list|,
literal|"caching"
argument_list|,
literal|"scanner caching value"
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
name|type
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"type"
argument_list|)
expr_stmt|;
name|file
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"file"
argument_list|)
expr_stmt|;
name|tablename
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"table"
argument_list|)
expr_stmt|;
name|snapshotName
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"snapshot"
argument_list|)
expr_stmt|;
name|restoreDir
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"restoredir"
argument_list|)
expr_stmt|;
name|caching
operator|=
name|cmd
operator|.
name|getOptionValue
argument_list|(
literal|"caching"
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|testHdfsStreaming
parameter_list|(
name|Path
name|filename
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
literal|1024
index|]
decl_stmt|;
name|FileSystem
name|fs
init|=
name|filename
operator|.
name|getFileSystem
argument_list|(
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
comment|// read the file from start to finish
name|Stopwatch
name|fileOpenTimer
init|=
name|Stopwatch
operator|.
name|createUnstarted
argument_list|()
decl_stmt|;
name|Stopwatch
name|streamTimer
init|=
name|Stopwatch
operator|.
name|createUnstarted
argument_list|()
decl_stmt|;
name|fileOpenTimer
operator|.
name|start
argument_list|()
expr_stmt|;
name|FSDataInputStream
name|in
init|=
name|fs
operator|.
name|open
argument_list|(
name|filename
argument_list|)
decl_stmt|;
name|fileOpenTimer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|long
name|totalBytes
init|=
literal|0
decl_stmt|;
name|streamTimer
operator|.
name|start
argument_list|()
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|read
init|=
name|in
operator|.
name|read
argument_list|(
name|buf
argument_list|)
decl_stmt|;
if|if
condition|(
name|read
operator|<
literal|0
condition|)
block|{
break|break;
block|}
name|totalBytes
operator|+=
name|read
expr_stmt|;
block|}
name|streamTimer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|double
name|throughput
init|=
operator|(
name|double
operator|)
name|totalBytes
operator|/
name|streamTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"HDFS streaming: "
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total time to open: "
operator|+
name|fileOpenTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total time to read: "
operator|+
name|streamTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total bytes: "
operator|+
name|totalBytes
operator|+
literal|" bytes ("
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|totalBytes
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"throghput  : "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|throughput
argument_list|)
operator|+
literal|"B/s"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|Scan
name|getScan
parameter_list|()
block|{
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
comment|// default scan settings
name|scan
operator|.
name|setCacheBlocks
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setMaxVersions
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|scan
operator|.
name|setScanMetricsEnabled
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|caching
operator|!=
literal|null
condition|)
block|{
name|scan
operator|.
name|setCaching
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|caching
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|scan
return|;
block|}
specifier|public
name|void
name|testScan
parameter_list|()
throws|throws
name|IOException
block|{
name|Stopwatch
name|tableOpenTimer
init|=
name|Stopwatch
operator|.
name|createUnstarted
argument_list|()
decl_stmt|;
name|Stopwatch
name|scanOpenTimer
init|=
name|Stopwatch
operator|.
name|createUnstarted
argument_list|()
decl_stmt|;
name|Stopwatch
name|scanTimer
init|=
name|Stopwatch
operator|.
name|createUnstarted
argument_list|()
decl_stmt|;
name|tableOpenTimer
operator|.
name|start
argument_list|()
expr_stmt|;
name|Connection
name|connection
init|=
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|Table
name|table
init|=
name|connection
operator|.
name|getTable
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tablename
argument_list|)
argument_list|)
decl_stmt|;
name|tableOpenTimer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|Scan
name|scan
init|=
name|getScan
argument_list|()
decl_stmt|;
name|scanOpenTimer
operator|.
name|start
argument_list|()
expr_stmt|;
name|ResultScanner
name|scanner
init|=
name|table
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|scanOpenTimer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|long
name|numRows
init|=
literal|0
decl_stmt|;
name|long
name|numCells
init|=
literal|0
decl_stmt|;
name|scanTimer
operator|.
name|start
argument_list|()
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|numRows
operator|++
expr_stmt|;
name|numCells
operator|+=
name|result
operator|.
name|rawCells
argument_list|()
operator|.
name|length
expr_stmt|;
block|}
name|scanTimer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|connection
operator|.
name|close
argument_list|()
expr_stmt|;
name|ScanMetrics
name|metrics
init|=
name|scan
operator|.
name|getScanMetrics
argument_list|()
decl_stmt|;
name|long
name|totalBytes
init|=
name|metrics
operator|.
name|countOfBytesInResults
operator|.
name|get
argument_list|()
decl_stmt|;
name|double
name|throughput
init|=
operator|(
name|double
operator|)
name|totalBytes
operator|/
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|double
name|throughputRows
init|=
operator|(
name|double
operator|)
name|numRows
operator|/
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|double
name|throughputCells
init|=
operator|(
name|double
operator|)
name|numCells
operator|/
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"HBase scan: "
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total time to open table: "
operator|+
name|tableOpenTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total time to open scanner: "
operator|+
name|scanOpenTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total time to scan: "
operator|+
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Scan metrics:\n"
operator|+
name|metrics
operator|.
name|getMetricsMap
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total bytes: "
operator|+
name|totalBytes
operator|+
literal|" bytes ("
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|totalBytes
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"throughput  : "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|throughput
argument_list|)
operator|+
literal|"B/s"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total rows  : "
operator|+
name|numRows
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"throughput  : "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|throughputRows
argument_list|)
operator|+
literal|" rows/s"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total cells : "
operator|+
name|numCells
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"throughput  : "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|throughputCells
argument_list|)
operator|+
literal|" cells/s"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testSnapshotScan
parameter_list|()
throws|throws
name|IOException
block|{
name|Stopwatch
name|snapshotRestoreTimer
init|=
name|Stopwatch
operator|.
name|createUnstarted
argument_list|()
decl_stmt|;
name|Stopwatch
name|scanOpenTimer
init|=
name|Stopwatch
operator|.
name|createUnstarted
argument_list|()
decl_stmt|;
name|Stopwatch
name|scanTimer
init|=
name|Stopwatch
operator|.
name|createUnstarted
argument_list|()
decl_stmt|;
name|Path
name|restoreDir
init|=
operator|new
name|Path
argument_list|(
name|this
operator|.
name|restoreDir
argument_list|)
decl_stmt|;
name|snapshotRestoreTimer
operator|.
name|start
argument_list|()
expr_stmt|;
name|restoreDir
operator|.
name|getFileSystem
argument_list|(
name|conf
argument_list|)
operator|.
name|delete
argument_list|(
name|restoreDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|snapshotRestoreTimer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|Scan
name|scan
init|=
name|getScan
argument_list|()
decl_stmt|;
name|scanOpenTimer
operator|.
name|start
argument_list|()
expr_stmt|;
name|TableSnapshotScanner
name|scanner
init|=
operator|new
name|TableSnapshotScanner
argument_list|(
name|conf
argument_list|,
name|restoreDir
argument_list|,
name|snapshotName
argument_list|,
name|scan
argument_list|)
decl_stmt|;
name|scanOpenTimer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|long
name|numRows
init|=
literal|0
decl_stmt|;
name|long
name|numCells
init|=
literal|0
decl_stmt|;
name|scanTimer
operator|.
name|start
argument_list|()
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|Result
name|result
init|=
name|scanner
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|numRows
operator|++
expr_stmt|;
name|numCells
operator|+=
name|result
operator|.
name|rawCells
argument_list|()
operator|.
name|length
expr_stmt|;
block|}
name|scanTimer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|ScanMetrics
name|metrics
init|=
name|scanner
operator|.
name|getScanMetrics
argument_list|()
decl_stmt|;
name|long
name|totalBytes
init|=
name|metrics
operator|.
name|countOfBytesInResults
operator|.
name|get
argument_list|()
decl_stmt|;
name|double
name|throughput
init|=
operator|(
name|double
operator|)
name|totalBytes
operator|/
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|double
name|throughputRows
init|=
operator|(
name|double
operator|)
name|numRows
operator|/
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|double
name|throughputCells
init|=
operator|(
name|double
operator|)
name|numCells
operator|/
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"HBase scan snapshot: "
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total time to restore snapshot: "
operator|+
name|snapshotRestoreTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total time to open scanner: "
operator|+
name|scanOpenTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total time to scan: "
operator|+
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Scan metrics:\n"
operator|+
name|metrics
operator|.
name|getMetricsMap
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total bytes: "
operator|+
name|totalBytes
operator|+
literal|" bytes ("
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|totalBytes
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"throughput  : "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|throughput
argument_list|)
operator|+
literal|"B/s"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total rows  : "
operator|+
name|numRows
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"throughput  : "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|throughputRows
argument_list|)
operator|+
literal|" rows/s"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total cells : "
operator|+
name|numCells
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"throughput  : "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|throughputCells
argument_list|)
operator|+
literal|" cells/s"
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
enum|enum
name|ScanCounter
block|{
name|NUM_ROWS
block|,
name|NUM_CELLS
block|,   }
specifier|public
specifier|static
class|class
name|MyMapper
parameter_list|<
name|KEYOUT
parameter_list|,
name|VALUEOUT
parameter_list|>
extends|extends
name|TableMapper
argument_list|<
name|KEYOUT
argument_list|,
name|VALUEOUT
argument_list|>
block|{
annotation|@
name|Override
specifier|protected
name|void
name|map
parameter_list|(
name|ImmutableBytesWritable
name|key
parameter_list|,
name|Result
name|value
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|context
operator|.
name|getCounter
argument_list|(
name|ScanCounter
operator|.
name|NUM_ROWS
argument_list|)
operator|.
name|increment
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|context
operator|.
name|getCounter
argument_list|(
name|ScanCounter
operator|.
name|NUM_CELLS
argument_list|)
operator|.
name|increment
argument_list|(
name|value
operator|.
name|rawCells
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|testScanMapReduce
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
block|{
name|Stopwatch
name|scanOpenTimer
init|=
name|Stopwatch
operator|.
name|createUnstarted
argument_list|()
decl_stmt|;
name|Stopwatch
name|scanTimer
init|=
name|Stopwatch
operator|.
name|createUnstarted
argument_list|()
decl_stmt|;
name|Scan
name|scan
init|=
name|getScan
argument_list|()
decl_stmt|;
name|String
name|jobName
init|=
literal|"testScanMapReduce"
decl_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJobName
argument_list|(
name|jobName
argument_list|)
expr_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|initTableMapperJob
argument_list|(
name|this
operator|.
name|tablename
argument_list|,
name|scan
argument_list|,
name|MyMapper
operator|.
name|class
argument_list|,
name|NullWritable
operator|.
name|class
argument_list|,
name|NullWritable
operator|.
name|class
argument_list|,
name|job
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputKeyClass
argument_list|(
name|NullWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputValueClass
argument_list|(
name|NullWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|NullOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|scanTimer
operator|.
name|start
argument_list|()
expr_stmt|;
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|scanTimer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|Counters
name|counters
init|=
name|job
operator|.
name|getCounters
argument_list|()
decl_stmt|;
name|long
name|numRows
init|=
name|counters
operator|.
name|findCounter
argument_list|(
name|ScanCounter
operator|.
name|NUM_ROWS
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|long
name|numCells
init|=
name|counters
operator|.
name|findCounter
argument_list|(
name|ScanCounter
operator|.
name|NUM_CELLS
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|long
name|totalBytes
init|=
name|counters
operator|.
name|findCounter
argument_list|(
name|HBASE_COUNTER_GROUP_NAME
argument_list|,
literal|"BYTES_IN_RESULTS"
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|double
name|throughput
init|=
operator|(
name|double
operator|)
name|totalBytes
operator|/
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|double
name|throughputRows
init|=
operator|(
name|double
operator|)
name|numRows
operator|/
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|double
name|throughputCells
init|=
operator|(
name|double
operator|)
name|numCells
operator|/
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"HBase scan mapreduce: "
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total time to open scanner: "
operator|+
name|scanOpenTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total time to scan: "
operator|+
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total bytes: "
operator|+
name|totalBytes
operator|+
literal|" bytes ("
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|totalBytes
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"throughput  : "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|throughput
argument_list|)
operator|+
literal|"B/s"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total rows  : "
operator|+
name|numRows
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"throughput  : "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|throughputRows
argument_list|)
operator|+
literal|" rows/s"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total cells : "
operator|+
name|numCells
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"throughput  : "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|throughputCells
argument_list|)
operator|+
literal|" cells/s"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|testSnapshotScanMapReduce
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ClassNotFoundException
block|{
name|Stopwatch
name|scanOpenTimer
init|=
name|Stopwatch
operator|.
name|createUnstarted
argument_list|()
decl_stmt|;
name|Stopwatch
name|scanTimer
init|=
name|Stopwatch
operator|.
name|createUnstarted
argument_list|()
decl_stmt|;
name|Scan
name|scan
init|=
name|getScan
argument_list|()
decl_stmt|;
name|String
name|jobName
init|=
literal|"testSnapshotScanMapReduce"
decl_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJobName
argument_list|(
name|jobName
argument_list|)
expr_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
name|TableMapReduceUtil
operator|.
name|initTableSnapshotMapperJob
argument_list|(
name|this
operator|.
name|snapshotName
argument_list|,
name|scan
argument_list|,
name|MyMapper
operator|.
name|class
argument_list|,
name|NullWritable
operator|.
name|class
argument_list|,
name|NullWritable
operator|.
name|class
argument_list|,
name|job
argument_list|,
literal|true
argument_list|,
operator|new
name|Path
argument_list|(
name|restoreDir
argument_list|)
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputKeyClass
argument_list|(
name|NullWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputValueClass
argument_list|(
name|NullWritable
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|NullOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|scanTimer
operator|.
name|start
argument_list|()
expr_stmt|;
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|scanTimer
operator|.
name|stop
argument_list|()
expr_stmt|;
name|Counters
name|counters
init|=
name|job
operator|.
name|getCounters
argument_list|()
decl_stmt|;
name|long
name|numRows
init|=
name|counters
operator|.
name|findCounter
argument_list|(
name|ScanCounter
operator|.
name|NUM_ROWS
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|long
name|numCells
init|=
name|counters
operator|.
name|findCounter
argument_list|(
name|ScanCounter
operator|.
name|NUM_CELLS
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|long
name|totalBytes
init|=
name|counters
operator|.
name|findCounter
argument_list|(
name|HBASE_COUNTER_GROUP_NAME
argument_list|,
literal|"BYTES_IN_RESULTS"
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|double
name|throughput
init|=
operator|(
name|double
operator|)
name|totalBytes
operator|/
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|double
name|throughputRows
init|=
operator|(
name|double
operator|)
name|numRows
operator|/
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|double
name|throughputCells
init|=
operator|(
name|double
operator|)
name|numCells
operator|/
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"HBase scan mapreduce: "
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total time to open scanner: "
operator|+
name|scanOpenTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total time to scan: "
operator|+
name|scanTimer
operator|.
name|elapsed
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total bytes: "
operator|+
name|totalBytes
operator|+
literal|" bytes ("
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|totalBytes
argument_list|)
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"throughput  : "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|throughput
argument_list|)
operator|+
literal|"B/s"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total rows  : "
operator|+
name|numRows
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"throughput  : "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|throughputRows
argument_list|)
operator|+
literal|" rows/s"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"total cells : "
operator|+
name|numCells
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"throughput  : "
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|long
operator|)
name|throughputCells
argument_list|)
operator|+
literal|" cells/s"
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
if|if
condition|(
name|type
operator|.
name|equals
argument_list|(
literal|"streaming"
argument_list|)
condition|)
block|{
name|testHdfsStreaming
argument_list|(
operator|new
name|Path
argument_list|(
name|file
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equals
argument_list|(
literal|"scan"
argument_list|)
condition|)
block|{
name|testScan
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equals
argument_list|(
literal|"snapshotscan"
argument_list|)
condition|)
block|{
name|testSnapshotScan
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equals
argument_list|(
literal|"scanmapreduce"
argument_list|)
condition|)
block|{
name|testScanMapReduce
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|.
name|equals
argument_list|(
literal|"snapshotscanmapreduce"
argument_list|)
condition|)
block|{
name|testSnapshotScanMapReduce
argument_list|()
expr_stmt|;
block|}
return|return
literal|0
return|;
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
throws|throws
name|Exception
block|{
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
operator|new
name|ScanPerformanceEvaluation
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

