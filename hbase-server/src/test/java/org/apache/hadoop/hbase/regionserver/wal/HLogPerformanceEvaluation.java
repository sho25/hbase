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
name|java
operator|.
name|util
operator|.
name|Map
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
name|java
operator|.
name|util
operator|.
name|UUID
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
name|classification
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
name|Cell
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
name|HBaseConfiguration
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
name|KeyValueUtil
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
name|wal
operator|.
name|HLog
operator|.
name|Entry
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
name|hbase
operator|.
name|HConstants
import|;
end_import

begin_comment
comment|/**  * This class runs performance benchmarks for {@link HLog}.  * See usage for this tool by running:  *<code>$ hbase org.apache.hadoop.hbase.regionserver.wal.HLogPerformanceEvaluation -h</code>  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|HLogPerformanceEvaluation
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
name|HLogPerformanceEvaluation
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
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
specifier|static
specifier|final
name|String
name|TABLE_NAME
init|=
literal|"HLogPerformanceEvaluation"
decl_stmt|;
specifier|static
specifier|final
name|String
name|QUALIFIER_PREFIX
init|=
literal|"q"
decl_stmt|;
specifier|static
specifier|final
name|String
name|FAMILY_PREFIX
init|=
literal|"cf"
decl_stmt|;
specifier|private
name|int
name|numQualifiers
init|=
literal|1
decl_stmt|;
specifier|private
name|int
name|valueSize
init|=
literal|512
decl_stmt|;
specifier|private
name|int
name|keySize
init|=
literal|16
decl_stmt|;
comment|/**    * Perform HLog.append() of Put object, for the number of iterations requested.    * Keys and Vaues are generated randomly, the number of column families,    * qualifiers and key/value size is tunable by the user.    */
class|class
name|HLogPutBenchmark
implements|implements
name|Runnable
block|{
specifier|private
specifier|final
name|long
name|numIterations
decl_stmt|;
specifier|private
specifier|final
name|int
name|numFamilies
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|noSync
decl_stmt|;
specifier|private
specifier|final
name|HRegion
name|region
decl_stmt|;
specifier|private
specifier|final
name|HTableDescriptor
name|htd
decl_stmt|;
name|HLogPutBenchmark
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|,
specifier|final
name|HTableDescriptor
name|htd
parameter_list|,
specifier|final
name|long
name|numIterations
parameter_list|,
specifier|final
name|boolean
name|noSync
parameter_list|)
block|{
name|this
operator|.
name|numIterations
operator|=
name|numIterations
expr_stmt|;
name|this
operator|.
name|noSync
operator|=
name|noSync
expr_stmt|;
name|this
operator|.
name|numFamilies
operator|=
name|htd
operator|.
name|getColumnFamilies
argument_list|()
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|region
expr_stmt|;
name|this
operator|.
name|htd
operator|=
name|htd
expr_stmt|;
block|}
specifier|public
name|void
name|run
parameter_list|()
block|{
name|byte
index|[]
name|key
init|=
operator|new
name|byte
index|[
name|keySize
index|]
decl_stmt|;
name|byte
index|[]
name|value
init|=
operator|new
name|byte
index|[
name|valueSize
index|]
decl_stmt|;
name|Random
name|rand
init|=
operator|new
name|Random
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getId
argument_list|()
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
try|try
block|{
name|long
name|startTime
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
name|numIterations
condition|;
operator|++
name|i
control|)
block|{
name|Put
name|put
init|=
name|setupPut
argument_list|(
name|rand
argument_list|,
name|key
argument_list|,
name|value
argument_list|,
name|numFamilies
argument_list|)
decl_stmt|;
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|WALEdit
name|walEdit
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|addFamilyMapToWALEdit
argument_list|(
name|put
operator|.
name|getFamilyCellMap
argument_list|()
argument_list|,
name|walEdit
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
if|if
condition|(
name|this
operator|.
name|noSync
condition|)
block|{
name|hlog
operator|.
name|appendNoSync
argument_list|(
name|hri
argument_list|,
name|hri
operator|.
name|getTable
argument_list|()
argument_list|,
name|walEdit
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|UUID
argument_list|>
argument_list|()
argument_list|,
name|now
argument_list|,
name|htd
argument_list|,
literal|null
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
name|hri
operator|.
name|getTable
argument_list|()
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
name|long
name|totalTime
init|=
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
decl_stmt|;
name|logBenchmarkResult
argument_list|(
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|numIterations
argument_list|,
name|totalTime
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
name|error
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" Thread failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|rootRegionDir
init|=
literal|null
decl_stmt|;
name|int
name|numThreads
init|=
literal|1
decl_stmt|;
name|long
name|numIterations
init|=
literal|10000
decl_stmt|;
name|int
name|numFamilies
init|=
literal|1
decl_stmt|;
name|boolean
name|noSync
init|=
literal|false
decl_stmt|;
name|boolean
name|verify
init|=
literal|false
decl_stmt|;
name|boolean
name|verbose
init|=
literal|false
decl_stmt|;
name|boolean
name|cleanup
init|=
literal|true
decl_stmt|;
name|boolean
name|noclosefs
init|=
literal|false
decl_stmt|;
name|long
name|roll
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
comment|// Process command line args
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|cmd
init|=
name|args
index|[
name|i
index|]
decl_stmt|;
try|try
block|{
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-threads"
argument_list|)
condition|)
block|{
name|numThreads
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-iterations"
argument_list|)
condition|)
block|{
name|numIterations
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-path"
argument_list|)
condition|)
block|{
name|rootRegionDir
operator|=
operator|new
name|Path
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-families"
argument_list|)
condition|)
block|{
name|numFamilies
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-qualifiers"
argument_list|)
condition|)
block|{
name|numQualifiers
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-keySize"
argument_list|)
condition|)
block|{
name|keySize
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-valueSize"
argument_list|)
condition|)
block|{
name|valueSize
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-nosync"
argument_list|)
condition|)
block|{
name|noSync
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-verify"
argument_list|)
condition|)
block|{
name|verify
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-verbose"
argument_list|)
condition|)
block|{
name|verbose
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-nocleanup"
argument_list|)
condition|)
block|{
name|cleanup
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-noclosefs"
argument_list|)
condition|)
block|{
name|noclosefs
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-roll"
argument_list|)
condition|)
block|{
name|roll
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|args
index|[
operator|++
name|i
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-h"
argument_list|)
condition|)
block|{
name|printUsageAndExit
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"--help"
argument_list|)
condition|)
block|{
name|printUsageAndExit
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"UNEXPECTED: "
operator|+
name|cmd
argument_list|)
expr_stmt|;
name|printUsageAndExit
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|printUsageAndExit
argument_list|()
expr_stmt|;
block|}
block|}
comment|// Run HLog Performance Evaluation
comment|// First set the fs from configs.  Do it for both configs in case we
comment|// are on hadoop1
name|getConf
argument_list|()
operator|.
name|set
argument_list|(
literal|"fs.default.name"
argument_list|,
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|)
argument_list|)
expr_stmt|;
name|getConf
argument_list|()
operator|.
name|set
argument_list|(
literal|"fs.defaultFS"
argument_list|,
name|getConf
argument_list|()
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|HBASE_DIR
argument_list|)
argument_list|)
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"FileSystem: "
operator|+
name|fs
argument_list|)
expr_stmt|;
try|try
block|{
if|if
condition|(
name|rootRegionDir
operator|==
literal|null
condition|)
block|{
name|rootRegionDir
operator|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"HLogPerformanceEvaluation"
argument_list|)
expr_stmt|;
block|}
name|rootRegionDir
operator|=
name|rootRegionDir
operator|.
name|makeQualified
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|cleanRegionRootDir
argument_list|(
name|fs
argument_list|,
name|rootRegionDir
argument_list|)
expr_stmt|;
comment|// Initialize Table Descriptor
name|HTableDescriptor
name|htd
init|=
name|createHTableDescriptor
argument_list|(
name|numFamilies
argument_list|)
decl_stmt|;
specifier|final
name|long
name|whenToRoll
init|=
name|roll
decl_stmt|;
name|HLog
name|hlog
init|=
operator|new
name|FSHLog
argument_list|(
name|fs
argument_list|,
name|rootRegionDir
argument_list|,
literal|"wals"
argument_list|,
name|getConf
argument_list|()
argument_list|)
block|{
name|int
name|appends
init|=
literal|0
decl_stmt|;
specifier|protected
name|void
name|doWrite
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|appends
operator|++
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|appends
operator|%
name|whenToRoll
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Rolling after "
operator|+
name|appends
operator|+
literal|" edits"
argument_list|)
expr_stmt|;
name|rollWriter
argument_list|()
expr_stmt|;
block|}
name|super
operator|.
name|doWrite
argument_list|(
name|info
argument_list|,
name|logKey
argument_list|,
name|logEdit
argument_list|,
name|htd
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
empty_stmt|;
block|}
decl_stmt|;
name|hlog
operator|.
name|rollWriter
argument_list|()
expr_stmt|;
name|HRegion
name|region
init|=
literal|null
decl_stmt|;
try|try
block|{
name|region
operator|=
name|openRegion
argument_list|(
name|fs
argument_list|,
name|rootRegionDir
argument_list|,
name|htd
argument_list|,
name|hlog
argument_list|)
expr_stmt|;
name|long
name|putTime
init|=
name|runBenchmark
argument_list|(
operator|new
name|HLogPutBenchmark
argument_list|(
name|region
argument_list|,
name|htd
argument_list|,
name|numIterations
argument_list|,
name|noSync
argument_list|)
argument_list|,
name|numThreads
argument_list|)
decl_stmt|;
name|logBenchmarkResult
argument_list|(
literal|"Summary: threads="
operator|+
name|numThreads
operator|+
literal|", iterations="
operator|+
name|numIterations
argument_list|,
name|numIterations
operator|*
name|numThreads
argument_list|,
name|putTime
argument_list|)
expr_stmt|;
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
name|closeRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
name|region
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|verify
condition|)
block|{
name|Path
name|dir
init|=
operator|(
operator|(
name|FSHLog
operator|)
name|hlog
operator|)
operator|.
name|getDir
argument_list|()
decl_stmt|;
name|long
name|editCount
init|=
literal|0
decl_stmt|;
name|FileStatus
index|[]
name|fsss
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|dir
argument_list|)
decl_stmt|;
if|if
condition|(
name|fsss
operator|.
name|length
operator|==
literal|0
condition|)
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"No WAL found"
argument_list|)
throw|;
for|for
control|(
name|FileStatus
name|fss
range|:
name|fsss
control|)
block|{
name|Path
name|p
init|=
name|fss
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|p
argument_list|)
condition|)
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|p
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
name|editCount
operator|+=
name|verify
argument_list|(
name|p
argument_list|,
name|verbose
argument_list|)
expr_stmt|;
block|}
name|long
name|expected
init|=
name|numIterations
operator|*
name|numThreads
decl_stmt|;
if|if
condition|(
name|editCount
operator|!=
name|expected
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Counted="
operator|+
name|editCount
operator|+
literal|", expected="
operator|+
name|expected
argument_list|)
throw|;
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
name|closeRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
comment|// Remove the root dir for this test region
if|if
condition|(
name|cleanup
condition|)
name|cleanRegionRootDir
argument_list|(
name|fs
argument_list|,
name|rootRegionDir
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
comment|// We may be called inside a test that wants to keep on using the fs.
if|if
condition|(
operator|!
name|noclosefs
condition|)
name|fs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
operator|(
literal|0
operator|)
return|;
block|}
specifier|private
specifier|static
name|HTableDescriptor
name|createHTableDescriptor
parameter_list|(
specifier|final
name|int
name|numFamilies
parameter_list|)
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|TABLE_NAME
argument_list|)
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
name|numFamilies
condition|;
operator|++
name|i
control|)
block|{
name|HColumnDescriptor
name|colDef
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILY_PREFIX
operator|+
name|i
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|colDef
argument_list|)
expr_stmt|;
block|}
return|return
name|htd
return|;
block|}
comment|/**    * Verify the content of the WAL file.    * Verify that sequenceids are ascending and that the file has expected number    * of edits.    * @param wal    * @return Count of edits.    * @throws IOException    */
specifier|private
name|long
name|verify
parameter_list|(
specifier|final
name|Path
name|wal
parameter_list|,
specifier|final
name|boolean
name|verbose
parameter_list|)
throws|throws
name|IOException
block|{
name|HLog
operator|.
name|Reader
name|reader
init|=
name|HLogFactory
operator|.
name|createReader
argument_list|(
name|wal
operator|.
name|getFileSystem
argument_list|(
name|getConf
argument_list|()
argument_list|)
argument_list|,
name|wal
argument_list|,
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|previousSeqid
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|count
init|=
literal|0
decl_stmt|;
try|try
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|Entry
name|e
init|=
name|reader
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|e
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Read count="
operator|+
name|count
operator|+
literal|" from "
operator|+
name|wal
argument_list|)
expr_stmt|;
break|break;
block|}
name|count
operator|++
expr_stmt|;
name|long
name|seqid
init|=
name|e
operator|.
name|getKey
argument_list|()
operator|.
name|getLogSeqNum
argument_list|()
decl_stmt|;
if|if
condition|(
name|verbose
condition|)
name|LOG
operator|.
name|info
argument_list|(
literal|"seqid="
operator|+
name|seqid
argument_list|)
expr_stmt|;
if|if
condition|(
name|previousSeqid
operator|>=
name|seqid
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"wal="
operator|+
name|wal
operator|.
name|getName
argument_list|()
operator|+
literal|", previousSeqid="
operator|+
name|previousSeqid
operator|+
literal|", seqid="
operator|+
name|seqid
argument_list|)
throw|;
block|}
name|previousSeqid
operator|=
name|seqid
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
specifier|private
specifier|static
name|void
name|logBenchmarkResult
parameter_list|(
name|String
name|testName
parameter_list|,
name|long
name|numTests
parameter_list|,
name|long
name|totalTime
parameter_list|)
block|{
name|float
name|tsec
init|=
name|totalTime
operator|/
literal|1000.0f
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
name|String
operator|.
name|format
argument_list|(
literal|"%s took %.3fs %.3fops/s"
argument_list|,
name|testName
argument_list|,
name|tsec
argument_list|,
name|numTests
operator|/
name|tsec
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|printUsageAndExit
parameter_list|()
block|{
name|System
operator|.
name|err
operator|.
name|printf
argument_list|(
literal|"Usage: bin/hbase %s [options]\n"
argument_list|,
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" where [options] are:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -h|-help         Show this help and exit."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -threads<N>     Number of threads writing on the WAL."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -iterations<N>  Number of iterations per thread."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -path<PATH>     Path where region's root directory is created."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -families<N>    Number of column families to write."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -qualifiers<N>  Number of qualifiers to write."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -keySize<N>     Row key size in byte."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -valueSize<N>   Row/Col value size in byte."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -nocleanup       Do NOT remove test data when done."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -noclosefs       Do NOT close the filesystem when done."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -nosync          Append without syncing"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -verify          Verify edits written in sequence"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -verbose         Output extra info; e.g. all edit seq ids when verifying"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"  -roll<N>        Roll the way every N appends"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Examples:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" To run 100 threads on hdfs with log rolling every 10k edits and verification afterward do:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" $ ./bin/hbase org.apache.hadoop.hbase.regionserver.wal.HLogPerformanceEvaluation \\"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"    -conf ./core-site.xml -path hdfs://example.org:7000/tmp -threads 100 -roll 10000 -verify"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|private
name|HRegion
name|openRegion
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|dir
parameter_list|,
specifier|final
name|HTableDescriptor
name|htd
parameter_list|,
specifier|final
name|HLog
name|hlog
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Initialize HRegion
name|HRegionInfo
name|regionInfo
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|regionInfo
argument_list|,
name|dir
argument_list|,
name|getConf
argument_list|()
argument_list|,
name|htd
argument_list|,
name|hlog
argument_list|)
return|;
block|}
specifier|private
name|void
name|closeRegion
parameter_list|(
specifier|final
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|region
operator|!=
literal|null
condition|)
block|{
name|region
operator|.
name|close
argument_list|()
expr_stmt|;
name|HLog
name|wal
init|=
name|region
operator|.
name|getLog
argument_list|()
decl_stmt|;
if|if
condition|(
name|wal
operator|!=
literal|null
condition|)
name|wal
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|cleanRegionRootDir
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|fs
operator|.
name|exists
argument_list|(
name|dir
argument_list|)
condition|)
block|{
name|fs
operator|.
name|delete
argument_list|(
name|dir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|Put
name|setupPut
parameter_list|(
name|Random
name|rand
parameter_list|,
name|byte
index|[]
name|key
parameter_list|,
name|byte
index|[]
name|value
parameter_list|,
specifier|final
name|int
name|numFamilies
parameter_list|)
block|{
name|rand
operator|.
name|nextBytes
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|key
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|cf
init|=
literal|0
init|;
name|cf
operator|<
name|numFamilies
condition|;
operator|++
name|cf
control|)
block|{
for|for
control|(
name|int
name|q
init|=
literal|0
init|;
name|q
operator|<
name|numQualifiers
condition|;
operator|++
name|q
control|)
block|{
name|rand
operator|.
name|nextBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|put
operator|.
name|add
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY_PREFIX
operator|+
name|cf
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|QUALIFIER_PREFIX
operator|+
name|q
argument_list|)
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|put
return|;
block|}
specifier|private
name|void
name|addFamilyMapToWALEdit
parameter_list|(
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|List
argument_list|<
name|Cell
argument_list|>
argument_list|>
name|familyMap
parameter_list|,
name|WALEdit
name|walEdit
parameter_list|)
block|{
for|for
control|(
name|List
argument_list|<
name|Cell
argument_list|>
name|edits
range|:
name|familyMap
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|Cell
name|cell
range|:
name|edits
control|)
block|{
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|cell
argument_list|)
decl_stmt|;
name|walEdit
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|private
name|long
name|runBenchmark
parameter_list|(
name|Runnable
name|runnable
parameter_list|,
specifier|final
name|int
name|numThreads
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
name|numThreads
index|]
decl_stmt|;
name|long
name|startTime
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
name|numThreads
condition|;
operator|++
name|i
control|)
block|{
name|threads
index|[
name|i
index|]
operator|=
operator|new
name|Thread
argument_list|(
name|runnable
argument_list|,
literal|"t"
operator|+
name|i
argument_list|)
expr_stmt|;
name|threads
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|Thread
name|t
range|:
name|threads
control|)
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
name|long
name|endTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
return|return
operator|(
name|endTime
operator|-
name|startTime
operator|)
return|;
block|}
comment|/**    * The guts of the {@link #main} method.    * Call this method to avoid the {@link #main(String[])} System.exit.    * @param args    * @return errCode    * @throws Exception    */
specifier|static
name|int
name|innerMain
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|,
specifier|final
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|ToolRunner
operator|.
name|run
argument_list|(
name|c
argument_list|,
operator|new
name|HLogPerformanceEvaluation
argument_list|()
argument_list|,
name|args
argument_list|)
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
name|System
operator|.
name|exit
argument_list|(
name|innerMain
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

