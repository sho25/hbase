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
name|util
operator|.
name|Random
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
name|log4j
operator|.
name|Level
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
name|util
operator|.
name|Tool
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
name|conf
operator|.
name|Configured
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
name|KeyValue
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
name|HRegionInfo
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
name|ipc
operator|.
name|HBaseRPC
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

begin_class
specifier|public
class|class
name|TestHLogBench
extends|extends
name|Configured
implements|implements
name|Tool
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestHLogBench
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Random
name|r
init|=
operator|new
name|Random
argument_list|()
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
literal|"hlogbenchFamily"
argument_list|)
decl_stmt|;
comment|// accumulate time here
specifier|private
specifier|static
name|int
name|totalTime
init|=
literal|0
decl_stmt|;
specifier|private
specifier|static
name|Object
name|lock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
comment|// the file system where to create the Hlog file
specifier|protected
name|FileSystem
name|fs
decl_stmt|;
comment|// the number of threads and the number of iterations per thread
specifier|private
name|int
name|numThreads
init|=
literal|300
decl_stmt|;
specifier|private
name|int
name|numIterationsPerThread
init|=
literal|10000
decl_stmt|;
specifier|private
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
name|Path
name|regionRootDir
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"TestHLogBench"
argument_list|)
decl_stmt|;
specifier|private
name|boolean
name|appendNoSync
init|=
literal|false
decl_stmt|;
specifier|public
name|TestHLogBench
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|private
name|TestHLogBench
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|fs
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Initialize file system object    */
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|IOException
block|{
name|getConf
argument_list|()
operator|.
name|setQuietMode
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|fs
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|fs
operator|=
name|FileSystem
operator|.
name|get
argument_list|(
name|getConf
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Close down file system    */
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|fs
operator|!=
literal|null
condition|)
block|{
name|fs
operator|.
name|close
argument_list|()
expr_stmt|;
name|fs
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**    * The main run method of TestHLogBench    */
specifier|public
name|int
name|run
parameter_list|(
name|String
name|argv
index|[]
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|exitCode
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
comment|// verify that we have enough command line parameters
if|if
condition|(
name|argv
operator|.
name|length
operator|<
literal|4
condition|)
block|{
name|printUsage
argument_list|(
literal|""
argument_list|)
expr_stmt|;
return|return
name|exitCode
return|;
block|}
comment|// initialize LogBench
try|try
block|{
name|init
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|HBaseRPC
operator|.
name|VersionMismatch
name|v
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Version Mismatch between client and server"
operator|+
literal|"... command aborted."
argument_list|)
expr_stmt|;
return|return
name|exitCode
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Bad connection to FS. command aborted."
argument_list|)
expr_stmt|;
return|return
name|exitCode
return|;
block|}
try|try
block|{
for|for
control|(
init|;
name|i
operator|<
name|argv
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
literal|"-numThreads"
operator|.
name|equals
argument_list|(
name|argv
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|i
operator|++
expr_stmt|;
name|this
operator|.
name|numThreads
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|argv
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"-numIterationsPerThread"
operator|.
name|equals
argument_list|(
name|argv
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|i
operator|++
expr_stmt|;
name|this
operator|.
name|numIterationsPerThread
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|argv
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"-path"
operator|.
name|equals
argument_list|(
name|argv
index|[
name|i
index|]
argument_list|)
condition|)
block|{
comment|// get an absolute path using the default file system
name|i
operator|++
expr_stmt|;
name|this
operator|.
name|regionRootDir
operator|=
operator|new
name|Path
argument_list|(
name|argv
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionRootDir
operator|=
name|regionRootDir
operator|.
name|makeQualified
argument_list|(
name|this
operator|.
name|fs
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"-nosync"
operator|.
name|equals
argument_list|(
name|argv
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|this
operator|.
name|appendNoSync
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|printUsage
argument_list|(
name|argv
index|[
name|i
index|]
argument_list|)
expr_stmt|;
return|return
name|exitCode
return|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|nfe
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Illegal numThreads or numIterationsPerThread, "
operator|+
literal|" a positive integer expected"
argument_list|)
expr_stmt|;
throw|throw
name|nfe
throw|;
block|}
name|go
argument_list|()
expr_stmt|;
return|return
literal|0
return|;
block|}
specifier|private
name|void
name|go
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|log
argument_list|(
literal|"Running TestHLogBench with "
operator|+
name|numThreads
operator|+
literal|" threads each doing "
operator|+
name|numIterationsPerThread
operator|+
literal|" HLog appends "
operator|+
operator|(
name|appendNoSync
condition|?
literal|"nosync"
else|:
literal|"sync"
operator|)
operator|+
literal|" at rootDir "
operator|+
name|regionRootDir
argument_list|)
expr_stmt|;
comment|// Mock an HRegion
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"table"
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|familyNames
init|=
operator|new
name|byte
index|[]
index|[]
block|{
name|FAMILY
block|}
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|()
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|HRegion
name|region
init|=
name|mockRegion
argument_list|(
name|tableName
argument_list|,
name|familyNames
argument_list|,
name|regionRootDir
argument_list|)
decl_stmt|;
name|HLog
name|hlog
init|=
name|region
operator|.
name|getLog
argument_list|()
decl_stmt|;
comment|// Spin up N threads to each perform M log operations
name|LogWriter
index|[]
name|incrementors
init|=
operator|new
name|LogWriter
index|[
name|numThreads
index|]
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
name|numThreads
condition|;
name|i
operator|++
control|)
block|{
name|incrementors
index|[
name|i
index|]
operator|=
operator|new
name|LogWriter
argument_list|(
name|region
argument_list|,
name|tableName
argument_list|,
name|hlog
argument_list|,
name|i
argument_list|,
name|numIterationsPerThread
argument_list|,
name|appendNoSync
argument_list|)
expr_stmt|;
name|incrementors
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
comment|// Wait for threads to finish
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numThreads
condition|;
name|i
operator|++
control|)
block|{
comment|//log("Waiting for #" + i + " to finish");
name|incrementors
index|[
name|i
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
comment|// Output statistics
name|long
name|totalOps
init|=
name|numThreads
operator|*
name|numIterationsPerThread
decl_stmt|;
name|log
argument_list|(
literal|"Operations per second "
operator|+
operator|(
operator|(
name|totalOps
operator|*
literal|1000L
operator|)
operator|/
name|totalTime
operator|)
argument_list|)
expr_stmt|;
name|log
argument_list|(
literal|"Average latency in ms "
operator|+
operator|(
operator|(
name|totalTime
operator|*
literal|1000L
operator|)
operator|/
name|totalOps
operator|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Displays format of commands.    */
specifier|private
specifier|static
name|void
name|printUsage
parameter_list|(
name|String
name|cmd
parameter_list|)
block|{
name|String
name|prefix
init|=
literal|"Usage: java "
operator|+
name|TestHLogBench
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
decl_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|prefix
operator|+
name|cmd
operator|+
literal|" [-numThreads<number>] "
operator|+
literal|" [-numIterationsPerThread<number>] "
operator|+
literal|" [-path<path where region's root directory is created>]"
operator|+
literal|" [-nosync]"
argument_list|)
expr_stmt|;
block|}
comment|/**    * A thread that writes data to an HLog    */
specifier|public
specifier|static
class|class
name|LogWriter
extends|extends
name|Thread
block|{
specifier|private
specifier|final
name|HRegion
name|region
decl_stmt|;
specifier|private
specifier|final
name|int
name|threadNumber
decl_stmt|;
specifier|private
specifier|final
name|int
name|numIncrements
decl_stmt|;
specifier|private
specifier|final
name|HLog
name|hlog
decl_stmt|;
specifier|private
name|boolean
name|appendNoSync
decl_stmt|;
specifier|private
name|byte
index|[]
name|tableName
decl_stmt|;
specifier|private
name|int
name|count
decl_stmt|;
specifier|public
name|LogWriter
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|,
name|HLog
name|log
parameter_list|,
name|int
name|threadNumber
parameter_list|,
name|int
name|numIncrements
parameter_list|,
name|boolean
name|appendNoSync
parameter_list|)
block|{
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
name|this
operator|.
name|threadNumber
operator|=
name|threadNumber
expr_stmt|;
name|this
operator|.
name|numIncrements
operator|=
name|numIncrements
expr_stmt|;
name|this
operator|.
name|hlog
operator|=
name|log
expr_stmt|;
name|this
operator|.
name|count
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|appendNoSync
operator|=
name|appendNoSync
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|setDaemon
argument_list|(
literal|true
argument_list|)
expr_stmt|;
comment|//log("LogWriter[" + threadNumber + "] instantiated");
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|byte
index|[]
name|key
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"thisisakey"
argument_list|)
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|key
argument_list|,
name|now
argument_list|)
decl_stmt|;
name|WALEdit
name|walEdit
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|walEdit
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|HRegionInfo
name|hri
init|=
name|region
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|()
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|boolean
name|isMetaRegion
init|=
literal|false
decl_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
name|numIncrements
condition|;
name|i
operator|++
control|)
block|{
try|try
block|{
if|if
condition|(
name|appendNoSync
condition|)
block|{
name|hlog
operator|.
name|appendNoSync
argument_list|(
name|hri
argument_list|,
name|tableName
argument_list|,
name|walEdit
argument_list|,
name|HConstants
operator|.
name|DEFAULT_CLUSTER_ID
argument_list|,
name|now
argument_list|,
name|htd
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hlog
operator|.
name|append
argument_list|(
name|hri
argument_list|,
name|tableName
argument_list|,
name|walEdit
argument_list|,
name|now
argument_list|,
name|htd
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|log
argument_list|(
literal|"Fatal exception: "
operator|+
name|e
argument_list|)
expr_stmt|;
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|count
operator|++
expr_stmt|;
block|}
name|long
name|tot
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
decl_stmt|;
synchronized|synchronized
init|(
name|lock
init|)
block|{
name|totalTime
operator|+=
name|tot
expr_stmt|;
comment|// update global statistics
block|}
block|}
block|}
specifier|private
specifier|static
name|void
name|log
parameter_list|(
name|String
name|string
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
name|string
argument_list|)
expr_stmt|;
block|}
specifier|private
name|byte
index|[]
index|[]
name|makeBytes
parameter_list|(
name|int
name|numArrays
parameter_list|,
name|int
name|arraySize
parameter_list|)
block|{
name|byte
index|[]
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|numArrays
index|]
index|[]
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
name|numArrays
condition|;
name|i
operator|++
control|)
block|{
name|bytes
index|[
name|i
index|]
operator|=
operator|new
name|byte
index|[
name|arraySize
index|]
expr_stmt|;
name|r
operator|.
name|nextBytes
argument_list|(
name|bytes
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|bytes
return|;
block|}
comment|/**    * Create a dummy region    */
specifier|private
name|HRegion
name|mockRegion
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|byte
index|[]
index|[]
name|familyNames
parameter_list|,
name|Path
name|rootDir
parameter_list|)
throws|throws
name|IOException
block|{
name|HBaseTestingUtility
name|htu
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|htu
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.rs.cacheblocksonwrite"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.hregion.use.incrementnew"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"dfs.support.append"
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
name|int
name|numQualifiers
init|=
literal|10
decl_stmt|;
name|byte
index|[]
index|[]
name|qualifiers
init|=
operator|new
name|byte
index|[
name|numQualifiers
index|]
index|[]
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
name|numQualifiers
condition|;
name|i
operator|++
control|)
name|qualifiers
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qf"
operator|+
name|i
argument_list|)
expr_stmt|;
name|int
name|numRows
init|=
literal|10
decl_stmt|;
name|byte
index|[]
index|[]
name|rows
init|=
operator|new
name|byte
index|[
name|numRows
index|]
index|[]
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
name|numRows
condition|;
name|i
operator|++
control|)
name|rows
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r"
operator|+
name|i
argument_list|)
expr_stmt|;
comment|// switch off debug message from Region server
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
name|WARN
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|familyNames
control|)
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|family
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|tableName
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0L
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|0xffffffffL
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|rootDir
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|fs
operator|.
name|delete
argument_list|(
name|rootDir
argument_list|,
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed delete of "
operator|+
name|rootDir
argument_list|)
throw|;
block|}
block|}
return|return
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|hri
argument_list|,
name|rootDir
argument_list|,
name|conf
argument_list|,
name|htd
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testLogPerformance
parameter_list|()
throws|throws
name|Exception
block|{
name|TestHLogBench
name|bench
init|=
operator|new
name|TestHLogBench
argument_list|()
decl_stmt|;
name|int
name|res
decl_stmt|;
name|String
index|[]
name|argv
init|=
operator|new
name|String
index|[
literal|7
index|]
decl_stmt|;
name|argv
index|[
literal|0
index|]
operator|=
literal|"-numThreads"
expr_stmt|;
name|argv
index|[
literal|1
index|]
operator|=
name|Integer
operator|.
name|toString
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|argv
index|[
literal|2
index|]
operator|=
literal|"-numIterationsPerThread"
expr_stmt|;
name|argv
index|[
literal|3
index|]
operator|=
name|Integer
operator|.
name|toString
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|argv
index|[
literal|4
index|]
operator|=
literal|"-path"
expr_stmt|;
name|argv
index|[
literal|5
index|]
operator|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|()
operator|+
literal|"/HlogPerformance"
expr_stmt|;
name|argv
index|[
literal|6
index|]
operator|=
literal|"-nosync"
expr_stmt|;
try|try
block|{
name|res
operator|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|bench
argument_list|,
name|argv
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|bench
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|argv
parameter_list|)
throws|throws
name|Exception
block|{
name|TestHLogBench
name|bench
init|=
operator|new
name|TestHLogBench
argument_list|()
decl_stmt|;
name|int
name|res
decl_stmt|;
try|try
block|{
name|res
operator|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|bench
argument_list|,
name|argv
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|bench
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|System
operator|.
name|exit
argument_list|(
name|res
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

